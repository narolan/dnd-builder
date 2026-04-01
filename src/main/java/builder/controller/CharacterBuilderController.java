package com.dnd.builder.controller;

import com.dnd.builder.model.*;
import com.dnd.builder.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CharacterBuilderController {

    static final String DRAFT_KEY = "characterDraft";
    static final int    TOTAL_STEPS = 9;

    private final RaceRegistry        raceRegistry;
    private final ClassRegistry       classRegistry;
    private final BackgroundRegistry  bgRegistry;
    private final SpellRegistry       spellRegistry;
    private final EquipmentRegistry   equipRegistry;
    private final FeatRegistry        featRegistry;
    private final CharacterCalculator calculator;
    private final ObjectMapper        objectMapper;

    public CharacterBuilderController(RaceRegistry r, ClassRegistry c, BackgroundRegistry b,
                             SpellRegistry sp, EquipmentRegistry eq, FeatRegistry f,
                             CharacterCalculator calc) {
        this.raceRegistry   = r;
        this.classRegistry  = c;
        this.bgRegistry     = b;
        this.spellRegistry  = sp;
        this.equipRegistry  = eq;
        this.featRegistry   = f;
        this.calculator     = calc;
        this.objectMapper  = new ObjectMapper();
    }

    // ── Home / start fresh ────────────────────────────────────────────────────
    @GetMapping("/")
    public String home() { return "redirect:/step/1"; }

    @PostMapping("/new")
    public String newCharacter(HttpSession session) {
        session.setAttribute(DRAFT_KEY, CharacterDraft.fresh());
        return "redirect:/step/1";
    }

    // ── Universal step GET ────────────────────────────────────────────────────
    @GetMapping("/step/{step}")
    public String showStep(@PathVariable int step, HttpSession session, Model model,
                           @RequestParam(required=false) String error) {
        if (step < 1 || step > TOTAL_STEPS) return "redirect:/step/1";
        CharacterDraft draft = getOrCreateDraft(session);

        // Don't allow skipping ahead past highest reached + 1
        if (step > draft.getHighestStepReached() + 1) {
            return "redirect:/step/" + Math.min(draft.getHighestStepReached() + 1, TOTAL_STEPS);
        }

        populateModel(model, draft, step, error);
        return "wizard";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 1 — Race
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/1")
    public String saveStep1(@RequestParam String raceId,
                             @RequestParam Map<String, String> allParams,
                             HttpSession session, RedirectAttributes ra) {
        CharacterDraft draft = getOrCreateDraft(session);
        draft.setRaceId(raceId);

        // Collect flex picks
        var flexPicks = new LinkedHashMap<String, String>();
        allParams.forEach((k, v) -> {
            if (k.startsWith("flex_") && !v.isBlank()) flexPicks.put(k, v);
        });
        draft.setFlexPicks(flexPicks);

        advance(draft, 1);
        return "redirect:/step/2";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 2 — Class & Subclass
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/2")
    public String saveStep2(@RequestParam String characterClass,
                             @RequestParam(defaultValue="") String subclassId,
                             @RequestParam(defaultValue="1") int level,
                             HttpSession session) {
        CharacterDraft draft = getOrCreateDraft(session);
        draft.setCharacterClass(characterClass);
        draft.setSubclassId(subclassId);
        draft.setLevel(Math.max(1, Math.min(20, level)));
        // Clear spell/equipment choices if class changed
        draft.setChosenCantrips(new ArrayList<>());
        draft.setChosenSpells(new ArrayList<>());
        draft.setSpellbookSpells(new ArrayList<>());
        draft.setEquipmentChoices(new LinkedHashMap<>());
        advance(draft, 2);
        return "redirect:/step/3";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 3 — Background & Identity
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/3")
    public String saveStep3(@RequestParam String background,
                             @RequestParam(defaultValue="") String alignment,
                             @RequestParam(defaultValue="") String characterName,
                             HttpSession session) {
        CharacterDraft draft = getOrCreateDraft(session);
        draft.setBackground(background);
        draft.setAlignment(alignment);
        draft.setCharacterName(characterName);
        advance(draft, 3);
        return "redirect:/step/4";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 4 — Ability Scores (Point Buy)
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/4")
    public String saveStep4(@RequestParam Map<String, String> allParams,
                             HttpSession session, RedirectAttributes ra) {
        CharacterDraft draft = getOrCreateDraft(session);
        var scores = new LinkedHashMap<String, Integer>();
        var stats  = List.of("STR","DEX","CON","INT","WIS","CHA");

        for (var stat : stats) {
            String raw = allParams.get("score_" + stat);
            int val = 8;
            try { val = Integer.parseInt(raw); } catch (Exception ignored) {}
            val = Math.max(8, Math.min(15, val));
            scores.put(stat, val);
        }

        // Validate point budget
        int spent = scores.values().stream().mapToInt(v -> RaceRegistry.POINT_COSTS.getOrDefault(v, 0)).sum();
        if (spent > RaceRegistry.POINT_BUDGET) {
            ra.addAttribute("error", "Point budget exceeded (" + spent + "/" + RaceRegistry.POINT_BUDGET + ")");
            return "redirect:/step/4";
        }

        draft.setBaseScores(scores);
        advance(draft, 4);
        return "redirect:/step/5";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 5 — Skills
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/5")
    public String saveStep5(@RequestParam(value="skills", required=false) List<String> skills,
                             HttpSession session, RedirectAttributes ra) {
        CharacterDraft draft = getOrCreateDraft(session);
        if (skills == null) skills = new ArrayList<>();

        // Validate count
        int allowed     = calculator.getClassSkillChoiceCount(draft);
        var bgSkills    = calculator.getBackgroundSkills(draft);
        var classSkills = calculator.getAvailableClassSkills(draft);

        // Filter to only valid class skills
        var chosen = skills.stream().filter(classSkills::contains).collect(Collectors.toList());
        if (chosen.size() > allowed) {
            ra.addAttribute("error", "Choose at most " + allowed + " skills");
            return "redirect:/step/5";
        }

        // Merge class choices with background fixed skills (deduplicated)
        var all = new ArrayList<>(bgSkills);
        chosen.forEach(s -> { if (!all.contains(s)) all.add(s); });
        draft.setSkillProficiencies(all);
        advance(draft, 5);

        // Skip feat step if not Variant Human (feat step only relevant at L1 for VH)
        if (!draft.isVariantHuman()) {
            advance(draft, 6);
            return "redirect:/step/7";
        }
        return "redirect:/step/6";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 6 — Feats (Variant Human only at L1)
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/6")
    public String saveStep6(@RequestParam(required=false) String chosenFeatId,
                             HttpSession session) {
        CharacterDraft draft = getOrCreateDraft(session);
        draft.setChosenFeatId(chosenFeatId != null ? chosenFeatId : "");
        advance(draft, 6);

        // Skip spells if not a spellcaster
        if (!classRegistry.isSpellcaster(draft.getCharacterClass())) {
            advance(draft, 7);
            return "redirect:/step/8";
        }
        return "redirect:/step/7";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 7 — Spells
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/7")
    public String saveStep7(@RequestParam(value="cantrips", required=false) List<String> cantrips,
                             @RequestParam(value="spells",   required=false) List<String> spells,
                             @RequestParam(value="spellbook",required=false) List<String> spellbook,
                             HttpSession session, RedirectAttributes ra) {
        CharacterDraft draft = getOrCreateDraft(session);
        if (cantrips  == null) cantrips  = new ArrayList<>();
        if (spells    == null) spells    = new ArrayList<>();
        if (spellbook == null) spellbook = new ArrayList<>();

        var classDef = classRegistry.getById(draft.getCharacterClass());
        if (classDef != null && classDef.getSpellcasting() != null) {
            var sc = classDef.getSpellcasting();

            // Validate cantrip count
            if (cantrips.size() > sc.getCantripsAtL1()) {
                ra.addAttribute("error","Choose at most " + sc.getCantripsAtL1() + " cantrips");
                return "redirect:/step/7";
            }

            // Validate spell count (known casters)
            if (!sc.isPrepareSpells() && spells.size() > sc.getSpellsKnownAtL1()) {
                ra.addAttribute("error","Choose at most " + sc.getSpellsKnownAtL1() + " spells");
                return "redirect:/step/7";
            }

            // Validate spellbook (wizard)
            if (sc.getSpellbookSizeAtL1() > 0 && spellbook.size() > sc.getSpellbookSizeAtL1()) {
                ra.addAttribute("error","Spellbook holds at most " + sc.getSpellbookSizeAtL1() + " spells at level 1");
                return "redirect:/step/7";
            }
        }

        draft.setChosenCantrips(cantrips);
        draft.setChosenSpells(spells);
        draft.setSpellbookSpells(spellbook);
        advance(draft, 7);
        return "redirect:/step/8";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 8 — Equipment
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/8")
    public String saveStep8(@RequestParam Map<String, String> allParams,
                             HttpSession session) {
        CharacterDraft draft = getOrCreateDraft(session);
        var choices = new LinkedHashMap<String, String>();
        // Each equipment slot param starts with slotId
        var slots = equipRegistry.forClass(draft.getCharacterClass());
        for (var slot : slots) {
            String chosen = allParams.get(slot.getSlotId());
            if (chosen != null && !chosen.isBlank()) {
                // Store the label for display, find it from slot choices
                final String chosenFinal = chosen;
                slot.getChoices().stream()
                    .filter(c -> c.getOptionId().equals(chosenFinal))
                    .findFirst()
                    .ifPresent(c -> choices.put(slot.getSlotId(), c.getOptionId()));
            }
        }
        draft.setEquipmentChoices(choices);
        advance(draft, 8);
        return "redirect:/step/9";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 9 — Review (GET only, no POST needed — export via separate endpoint)
    // ══════════════════════════════════════════════════════════════════════════

    // ── Model builder ─────────────────────────────────────────────────────────
    private void populateModel(Model model, CharacterDraft draft, int step, String error) {
        model.addAttribute("draft",       draft);
        model.addAttribute("step",        step);
        model.addAttribute("totalSteps",  TOTAL_STEPS);
        model.addAttribute("error",       error);
        model.addAttribute("derived",     calculator.calculate(draft));

        // Step-specific data
        switch (step) {
            case 1 -> {
                model.addAttribute("races",       raceRegistry.getAllRaces());
                model.addAttribute("racesJson",   safeJson(raceRegistry.getAllRaces()));
                model.addAttribute("pointCosts",  RaceRegistry.POINT_COSTS);
            }
            case 2 -> {
                model.addAttribute("classes",     classRegistry.getAllClasses());
                model.addAttribute("classesJson", safeJson(classRegistry.getAllClasses()));
                var cd = classRegistry.getById(draft.getCharacterClass());
                model.addAttribute("selectedClass", cd);
            }
            case 3 -> {
                model.addAttribute("backgrounds",     bgRegistry.getAllBackgrounds());
                model.addAttribute("backgroundsJson", safeJson(bgRegistry.getAllBackgrounds()));
            }
            case 4 -> {
                model.addAttribute("races",      raceRegistry.getAllRaces());
                model.addAttribute("racesJson",  safeJson(raceRegistry.getAllRaces()));
                model.addAttribute("pointCosts", RaceRegistry.POINT_COSTS);
                model.addAttribute("pointBudget",RaceRegistry.POINT_BUDGET);
                model.addAttribute("scoreMin",   RaceRegistry.SCORE_MIN);
                model.addAttribute("scoreMax",   RaceRegistry.SCORE_MAX);
            }
            case 5 -> {
                var availSkills = calculator.getAvailableClassSkills(draft);
                var bgSkills    = calculator.getBackgroundSkills(draft);
                var classChoiceCount = calculator.getClassSkillChoiceCount(draft);
                // Current class-only choices (exclude bg-granted)
                var currentChoices = draft.getSkillProficiencies().stream()
                        .filter(s -> !bgSkills.contains(s) && availSkills.contains(s))
                        .collect(Collectors.toList());
                model.addAttribute("availableSkills",    availSkills);
                model.addAttribute("bgSkills",           bgSkills);
                model.addAttribute("classChoiceCount",   classChoiceCount);
                model.addAttribute("currentSkillChoices",currentChoices);
                model.addAttribute("skillAbilityMap",    CharacterCalculator.SKILL_ABILITY);
            }
            case 6 -> {
                model.addAttribute("feats",       featRegistry.getAllFeats());
                model.addAttribute("isVariantHuman", draft.isVariantHuman());
            }
            case 7 -> {
                var cd = classRegistry.getById(draft.getCharacterClass());
                if (cd != null && cd.getSpellcasting() != null) {
                    var sc = cd.getSpellcasting();
                    model.addAttribute("spellcasting",      sc);
                    model.addAttribute("cantrips",          spellRegistry.cantripsFor(draft.getCharacterClass()));
                    model.addAttribute("level1Spells",      spellRegistry.level1For(draft.getCharacterClass()));
                    model.addAttribute("level2Spells",      spellRegistry.level2For(draft.getCharacterClass()));
                    model.addAttribute("isWizard",          "wizard".equals(draft.getCharacterClass()));
                    model.addAttribute("isPreparedCaster",  sc.isPrepareSpells() && !"wizard".equals(draft.getCharacterClass()));
                }
                model.addAttribute("isSpellcaster", classRegistry.isSpellcaster(draft.getCharacterClass()));
            }
            case 8 -> {
                var slots = equipRegistry.forClass(draft.getCharacterClass());
                model.addAttribute("equipmentSlots", slots);
            }
            case 9 -> {
                model.addAttribute("allSkills",   CharacterCalculator.SKILL_ABILITY);
                var cd  = classRegistry.getById(draft.getCharacterClass());
                var bg  = bgRegistry.getById(draft.getBackground());
                var race = raceRegistry.getById(draft.getRaceId());
                model.addAttribute("classDef",   cd);
                model.addAttribute("bgDef",      bg);
                model.addAttribute("raceDef",    race);
                // Chosen spells display
                var spellNames = new ArrayList<String>();
                for (var id : draft.getChosenCantrips()) {
                    var sp = spellRegistry.getById(id);
                    if (sp != null) spellNames.add(sp.getName() + " (cantrip)");
                }
                for (var id : draft.getChosenSpells()) {
                    var sp = spellRegistry.getById(id);
                    if (sp != null) spellNames.add(sp.getName() + " (1st)");
                }
                for (var id : draft.getSpellbookSpells()) {
                    var sp = spellRegistry.getById(id);
                    if (sp != null) spellNames.add(sp.getName() + " (spellbook)");
                }
                model.addAttribute("spellNames", spellNames);

                // Chosen feat name
                if (!draft.getChosenFeatId().isBlank()) {
                    var feat = featRegistry.getById(draft.getChosenFeatId());
                    model.addAttribute("featName", feat != null ? feat.getName() : draft.getChosenFeatId());
                }
            }
        }
    }

    // ── Session helpers ───────────────────────────────────────────────────────
    private CharacterDraft getOrCreateDraft(HttpSession session) {
        CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
        if (draft == null) {
            draft = CharacterDraft.fresh();
            session.setAttribute(DRAFT_KEY, draft);
        }
        return draft;
    }

    private void advance(CharacterDraft draft, int stepJustCompleted) {
        if (stepJustCompleted >= draft.getHighestStepReached()) {
            draft.setHighestStepReached(stepJustCompleted + 1);
        }
    }

    private String safeJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return "[]"; }
    }
}
