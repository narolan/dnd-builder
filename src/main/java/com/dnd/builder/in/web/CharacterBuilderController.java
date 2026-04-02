package com.dnd.builder.in.web;

import com.dnd.builder.core.model.*;
import com.dnd.builder.core.port.out.*;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.out.persistence.InMemoryRaceRepository;
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

    public static final String DRAFT_KEY = "characterDraft";
    static final int    TOTAL_STEPS = 9;

    private final RaceRepository       raceRepository;
    private final ClassRepository      classRepository;
    private final BackgroundRepository backgroundRepository;
    private final SpellRepository      spellRepository;
    private final EquipmentRepository  equipmentRepository;
    private final FeatRepository       featRepository;
    private final CharacterCalculator  calculator;
    private final ObjectMapper         objectMapper;

    public CharacterBuilderController(RaceRepository r, ClassRepository c, BackgroundRepository b,
                             SpellRepository sp, EquipmentRepository eq, FeatRepository f,
                             CharacterCalculator calc) {
        this.raceRepository       = r;
        this.classRepository      = c;
        this.backgroundRepository = b;
        this.spellRepository      = sp;
        this.equipmentRepository  = eq;
        this.featRepository       = f;
        this.calculator           = calc;
        this.objectMapper         = new ObjectMapper();
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
        int spent = scores.values().stream().mapToInt(v -> InMemoryRaceRepository.POINT_COSTS.getOrDefault(v, 0)).sum();
        if (spent > InMemoryRaceRepository.POINT_BUDGET) {
            ra.addAttribute("error", "Point budget exceeded (" + spent + "/" + InMemoryRaceRepository.POINT_BUDGET + ")");
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

        // Check if step 6 is needed: Variant Human OR character has reached an ASI level
        var asiLevels = calculator.getAvailableAsiLevels(draft);
        boolean needsStep6 = draft.isVariantHuman() || !asiLevels.isEmpty();

        if (!needsStep6) {
            advance(draft, 6);
            // Skip spells if not a spellcaster
            var classDef = classRepository.findById(draft.getCharacterClass());
            if (classDef == null || classDef.getSpellcasting() == null) {
                advance(draft, 7);
                return "redirect:/step/8";
            }
            return "redirect:/step/7";
        }
        return "redirect:/step/6";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STEP 6 — ASI/Feats
    // ══════════════════════════════════════════════════════════════════════════
    @PostMapping("/step/6")
    public String saveStep6(@RequestParam Map<String, String> allParams,
                             HttpSession session) {
        CharacterDraft draft = getOrCreateDraft(session);

        // Collect ASI choices from form params
        var asiLevels = calculator.getAvailableAsiLevels(draft);
        var choices = new ArrayList<com.dnd.builder.core.model.AsiChoice>();

        for (int lvl : asiLevels) {
            String choiceType = allParams.get("asi_type_" + lvl); // "asi" or "feat"
            if ("feat".equals(choiceType)) {
                String featId = allParams.get("feat_" + lvl);
                // Feats may also grant stat bonuses (handled separately if needed)
                choices.add(com.dnd.builder.core.model.AsiChoice.feat(lvl, featId, Map.of()));
            } else {
                // ASI: could be +2 to one stat or +1/+1 to two
                var statIncreases = new LinkedHashMap<String, Integer>();
                String mode = allParams.get("asi_mode_" + lvl); // "single" (+2) or "split" (+1/+1)
                if ("single".equals(mode)) {
                    String stat = allParams.get("asi_single_" + lvl);
                    if (stat != null && !stat.isEmpty()) {
                        statIncreases.put(stat, 2);
                    }
                } else {
                    String stat1 = allParams.get("asi_split1_" + lvl);
                    String stat2 = allParams.get("asi_split2_" + lvl);
                    if (stat1 != null && !stat1.isEmpty()) {
                        statIncreases.merge(stat1, 1, Integer::sum);
                    }
                    if (stat2 != null && !stat2.isEmpty()) {
                        statIncreases.merge(stat2, 1, Integer::sum);
                    }
                }
                choices.add(com.dnd.builder.core.model.AsiChoice.asi(lvl, statIncreases));
            }
        }

        // Also handle Variant Human L1 feat (legacy support)
        String vhFeatId = allParams.get("vh_feat");
        if (draft.isVariantHuman() && vhFeatId != null && !vhFeatId.isEmpty()) {
            draft.setChosenFeatId(vhFeatId);
        }

        draft.setAsiChoices(choices);
        advance(draft, 6);

        // Skip spells if not a spellcaster
        var classDef = classRepository.findById(draft.getCharacterClass());
        if (classDef == null || classDef.getSpellcasting() == null) {
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

        var classDef = classRepository.findById(draft.getCharacterClass());
        if (classDef != null && classDef.getSpellcasting() != null) {
            var sc = classDef.getSpellcasting();
            String classId = draft.getCharacterClass();
            int level = draft.getLevel();

            // Validate cantrip count using level-scaled limit
            int cantripsLimit = ClassRepository.cantripsKnown(classId, level);
            if (cantrips.size() > cantripsLimit) {
                ra.addAttribute("error", "Choose at most " + cantripsLimit + " cantrips");
                return "redirect:/step/7";
            }

            // Validate spell count for known casters
            if (!sc.isPrepareSpells() && !"wizard".equals(classId)) {
                int spellsKnown = ClassRepository.spellsKnown(classId, level);
                if (spells.size() > spellsKnown) {
                    ra.addAttribute("error", "Choose at most " + spellsKnown + " spells");
                    return "redirect:/step/7";
                }
            }

            // Validate prepared spell count
            if (sc.isPrepareSpells() && !"wizard".equals(classId)) {
                var derived = calculator.calculate(draft);
                int abilityMod = derived.getModifiers().get(sc.getAbility());
                int maxPrepared = ClassRepository.maxPrepared(classId, level, abilityMod);
                if (spells.size() > maxPrepared) {
                    ra.addAttribute("error", "You can prepare at most " + maxPrepared + " spells");
                    return "redirect:/step/7";
                }
            }

            // Validate spellbook (wizard)
            if ("wizard".equals(classId)) {
                int spellbookSize = 6 + (level - 1) * 2;
                if (spellbook.size() > spellbookSize) {
                    ra.addAttribute("error", "Spellbook holds at most " + spellbookSize + " spells at level " + level);
                    return "redirect:/step/7";
                }
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
        var slots = equipmentRepository.findByClass(draft.getCharacterClass());
        for (var slot : slots) {
            String chosen = allParams.get(slot.slotId());
            if (chosen != null && !chosen.isBlank()) {
                // Store the label for display, find it from slot choices
                final String chosenFinal = chosen;
                slot.choices().stream()
                    .filter(c -> c.optionId().equals(chosenFinal))
                    .findFirst()
                    .ifPresent(c -> choices.put(slot.slotId(), c.optionId()));
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
                model.addAttribute("races",       raceRepository.findAll());
                model.addAttribute("racesJson",   safeJson(raceRepository.findAll()));
                model.addAttribute("pointCosts",  InMemoryRaceRepository.POINT_COSTS);
            }
            case 2 -> {
                model.addAttribute("classes",     classRepository.findAll());
                model.addAttribute("classesJson", safeJson(classRepository.findAll()));
                var cd = classRepository.findById(draft.getCharacterClass());
                model.addAttribute("selectedClass", cd);
            }
            case 3 -> {
                model.addAttribute("backgrounds",     backgroundRepository.findAll());
                model.addAttribute("backgroundsJson", safeJson(backgroundRepository.findAll()));
            }
            case 4 -> {
                model.addAttribute("races",      raceRepository.findAll());
                model.addAttribute("racesJson",  safeJson(raceRepository.findAll()));
                model.addAttribute("pointCosts", InMemoryRaceRepository.POINT_COSTS);
                model.addAttribute("pointBudget",InMemoryRaceRepository.POINT_BUDGET);
                model.addAttribute("scoreMin",   InMemoryRaceRepository.SCORE_MIN);
                model.addAttribute("scoreMax",   InMemoryRaceRepository.SCORE_MAX);
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
                model.addAttribute("feats", featRepository.findAll());
                model.addAttribute("isVariantHuman", draft.isVariantHuman());

                // Get ASI levels the character has reached
                var asiLevels = calculator.getAvailableAsiLevels(draft);
                model.addAttribute("asiLevels", asiLevels);

                // For each ASI level, check if choice was already made
                var existingChoices = new java.util.HashMap<Integer, com.dnd.builder.core.model.AsiChoice>();
                if (draft.getAsiChoices() != null) {
                    for (var choice : draft.getAsiChoices()) {
                        existingChoices.put(choice.level(), choice);
                    }
                }
                model.addAttribute("existingChoices", existingChoices);

                // Add variant human L1 feat as a special case
                boolean showVariantHumanFeat = draft.isVariantHuman();
                model.addAttribute("showVariantHumanFeat", showVariantHumanFeat);

                // Need current stats for feat prerequisites
                var derived = calculator.calculate(draft);
                model.addAttribute("currentStats", derived.getFinalScores());
            }
            case 7 -> {
                var cd = classRepository.findById(draft.getCharacterClass());
                if (cd != null && cd.getSpellcasting() != null) {
                    var sc = cd.getSpellcasting();
                    String classId = draft.getCharacterClass();
                    int level = draft.getLevel();
                    int maxSpellLvl = ClassRepository.maxSpellLevel(classId, level);

                    model.addAttribute("spellcasting", sc);
                    model.addAttribute("cantrips", spellRepository.findCantripsForClass(classId));
                    model.addAttribute("maxSpellLevel", maxSpellLvl);

                    // Fetch spells grouped by level
                    var allSpells = spellRepository.findByClass(classId, maxSpellLvl);
                    var spellsByLevel = new LinkedHashMap<Integer, List<SpellDefinition>>();
                    for (int lvl = 1; lvl <= maxSpellLvl; lvl++) {
                        final int spellLvl = lvl;
                        var spellsAtLevel = allSpells.stream()
                                .filter(s -> s.getLevel() == spellLvl)
                                .collect(Collectors.toList());
                        if (!spellsAtLevel.isEmpty()) {
                            spellsByLevel.put(lvl, spellsAtLevel);
                        }
                    }
                    model.addAttribute("spellsByLevel", spellsByLevel);

                    // Scaling limits
                    int cantripsLimit = ClassRepository.cantripsKnown(classId, level);
                    model.addAttribute("cantripsLimit", cantripsLimit);

                    boolean isKnownCaster = !sc.isPrepareSpells();
                    boolean isWizard = "wizard".equals(classId);
                    model.addAttribute("isWizard", isWizard);
                    model.addAttribute("isPreparedCaster", sc.isPrepareSpells() && !isWizard);
                    model.addAttribute("isKnownCaster", isKnownCaster && !isWizard);

                    if (isKnownCaster && !isWizard) {
                        int spellsKnown = ClassRepository.spellsKnown(classId, level);
                        model.addAttribute("spellsKnownLimit", spellsKnown);
                    }

                    if (sc.isPrepareSpells() && !isWizard) {
                        var derived = calculator.calculate(draft);
                        int abilityMod = derived.getModifiers().get(sc.getAbility());
                        int maxPrepared = ClassRepository.maxPrepared(classId, level, abilityMod);
                        model.addAttribute("maxPrepared", maxPrepared);
                    }

                    if (isWizard) {
                        // Wizard spellbook: 6 at L1, +2 per wizard level
                        int spellbookSize = 6 + (level - 1) * 2;
                        model.addAttribute("spellbookSize", spellbookSize);
                    }
                }
                var classDef = classRepository.findById(draft.getCharacterClass());
                model.addAttribute("isSpellcaster", classDef != null && classDef.getSpellcasting() != null);
            }
            case 8 -> {
                var slots = equipmentRepository.findByClass(draft.getCharacterClass());
                model.addAttribute("equipmentSlots", slots);
            }
            case 9 -> {
                model.addAttribute("allSkills",   CharacterCalculator.SKILL_ABILITY);
                var cd  = classRepository.findById(draft.getCharacterClass());
                var bg  = backgroundRepository.findById(draft.getBackground());
                var race = raceRepository.findById(draft.getRaceId());
                model.addAttribute("classDef",   cd);
                model.addAttribute("bgDef",      bg);
                model.addAttribute("raceDef",    race);

                // Class features up to current level
                if (cd != null && cd.getFeatures() != null) {
                    var features = cd.getFeatures().stream()
                        .filter(f -> f.level() <= draft.getLevel())
                        .toList();
                    model.addAttribute("classFeatures", features);
                }

                // Subclass features if applicable
                if (cd != null && cd.getSubclasses() != null && !draft.getSubclassId().isEmpty()) {
                    var subclass = cd.getSubclasses().stream()
                        .filter(s -> s.id().equals(draft.getSubclassId()))
                        .findFirst().orElse(null);
                    model.addAttribute("subclassDef", subclass);
                    if (subclass != null && subclass.features() != null) {
                        var subFeatures = subclass.features().stream()
                            .filter(f -> f.level() <= draft.getLevel())
                            .toList();
                        model.addAttribute("subclassFeatures", subFeatures);
                    }
                }
                // Chosen spells display - with full spell objects for tooltips
                var chosenSpells = new ArrayList<SpellDefinition>();
                for (var id : draft.getChosenCantrips()) {
                    var sp = spellRepository.findById(id);
                    if (sp != null) chosenSpells.add(sp);
                }
                for (var id : draft.getChosenSpells()) {
                    var sp = spellRepository.findById(id);
                    if (sp != null) chosenSpells.add(sp);
                }
                for (var id : draft.getSpellbookSpells()) {
                    var sp = spellRepository.findById(id);
                    if (sp != null) chosenSpells.add(sp);
                }
                model.addAttribute("chosenSpells", chosenSpells);

                // Legacy spellNames for backwards compat
                var spellNames = new ArrayList<String>();
                for (var sp : chosenSpells) {
                    String lvl = sp.getLevel() == 0 ? "cantrip" : ordinal(sp.getLevel());
                    spellNames.add(sp.getName() + " (" + lvl + ")");
                }
                model.addAttribute("spellNames", spellNames);

                // Chosen feat name (Variant Human)
                if (!draft.getChosenFeatId().isBlank()) {
                    var feat = featRepository.findById(draft.getChosenFeatId());
                    model.addAttribute("featName", feat != null ? feat.getName() : draft.getChosenFeatId());
                }

                // ASI summary for review
                var asiSummary = new ArrayList<String>();
                if (draft.getAsiChoices() != null) {
                    for (var choice : draft.getAsiChoices()) {
                        if ("feat".equals(choice.type())) {
                            var feat = featRepository.findById(choice.featId());
                            String featName = feat != null ? feat.getName() : choice.featId();
                            asiSummary.add("Level " + choice.level() + ": " + featName + " (Feat)");
                        } else if (choice.statIncreases() != null && !choice.statIncreases().isEmpty()) {
                            var parts = new ArrayList<String>();
                            choice.statIncreases().forEach((stat, bonus) ->
                                parts.add(stat + " +" + bonus));
                            asiSummary.add("Level " + choice.level() + ": " + String.join(", ", parts));
                        }
                    }
                }
                model.addAttribute("asiSummary", asiSummary);
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

    private String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st"; case 2 -> "2nd"; case 3 -> "3rd";
            default -> n + "th";
        };
    }
}
