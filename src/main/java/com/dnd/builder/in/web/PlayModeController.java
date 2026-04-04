package com.dnd.builder.in.web;

import com.dnd.builder.core.model.*;
import com.dnd.builder.core.port.out.ClassRepository;
import com.dnd.builder.core.port.out.FeatRepository;
import com.dnd.builder.core.port.out.SpellRepository;
import com.dnd.builder.core.service.CharacterCalculator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dnd.builder.in.web.CharacterBuilderController.DRAFT_KEY;

/**
 * Handles play mode features: combat dashboard, inventory, rests, etc.
 */
@Controller
@RequestMapping("/play")
public class PlayModeController {

    private final CharacterCalculator calculator;
    private final ClassRepository classRepository;
    private final SpellRepository spellRepository;
    private final FeatRepository  featRepository;

    public PlayModeController(CharacterCalculator calculator, ClassRepository classRepository,
                              SpellRepository spellRepository,
                              FeatRepository featRepository) {
        this.calculator      = calculator;
        this.classRepository = classRepository;
        this.spellRepository = spellRepository;
        this.featRepository  = featRepository;
    }

    // ── Static data constants ─────────────────────────────────────────────────

    private static final List<Map<String, String>> ELDRITCH_INVOCATIONS = List.of(
        inv("agonizing_blast",          "Agonizing Blast",             "Add CHA mod to Eldritch Blast damage.",                                                                                     "0",  ""),
        inv("armor_of_shadows",         "Armor of Shadows",            "Cast Mage Armor on yourself at will without expending a spell slot.",                                                        "0",  ""),
        inv("beast_speech",             "Beast Speech",                "Cast Speak with Animals at will without expending a spell slot.",                                                            "0",  ""),
        inv("beguiling_influence",      "Beguiling Influence",         "Gain proficiency in Deception and Persuasion.",                                                                             "0",  ""),
        inv("book_of_ancient_secrets",  "Book of Ancient Secrets",     "Inscribe rituals into Pact of the Tome book and cast them.",                                                                "0",  "tome"),
        inv("devils_sight",             "Devil's Sight",               "See normally in magical and nonmagical darkness to 120 ft.",                                                                "0",  ""),
        inv("eldritch_sight",           "Eldritch Sight",              "Cast Detect Magic at will without expending a spell slot.",                                                                 "0",  ""),
        inv("eldritch_spear",           "Eldritch Spear",              "Eldritch Blast range increases to 300 ft.",                                                                                 "0",  ""),
        inv("eyes_of_rune_keeper",      "Eyes of the Rune Keeper",     "Read all writing.",                                                                                                        "0",  ""),
        inv("fiendish_vigor",           "Fiendish Vigor",              "Cast False Life on yourself at will as a 1st-level spell.",                                                                 "0",  ""),
        inv("gaze_of_two_minds",        "Gaze of Two Minds",           "Touch a willing creature to perceive through its senses.",                                                                  "0",  ""),
        inv("mask_of_many_faces",       "Mask of Many Faces",          "Cast Disguise Self at will without expending a spell slot.",                                                                 "0",  ""),
        inv("misty_visions",            "Misty Visions",               "Cast Silent Image at will without expending a spell slot.",                                                                  "0",  ""),
        inv("repelling_blast",          "Repelling Blast",             "Eldritch Blast can push targets 10 ft away.",                                                                               "0",  ""),
        inv("thief_of_five_fates",      "Thief of Five Fates",         "Cast Bane once using a spell slot.",                                                                                       "0",  ""),
        inv("voice_of_chain_master",    "Voice of the Chain Master",   "Communicate telepathically with your Pact of the Chain familiar.",                                                          "0",  "chain"),
        inv("mire_the_mind",            "Mire the Mind",               "Cast Slow once using a spell slot. Requires Warlock 5.",                                                                    "5",  ""),
        inv("one_with_shadows",         "One with Shadows",            "In dim light or darkness, become invisible as an action.",                                                                   "5",  ""),
        inv("sign_of_ill_omen",         "Sign of Ill Omen",            "Cast Bestow Curse once using a spell slot. Requires Warlock 5.",                                                            "5",  ""),
        inv("thirsting_blade",          "Thirsting Blade",             "Attack twice with your pact weapon. Requires Warlock 5.",                                                                    "5",  "blade"),
        inv("bewitching_whispers",      "Bewitching Whispers",         "Cast Compulsion once using a spell slot. Requires Warlock 7.",                                                              "7",  ""),
        inv("dreadful_word",            "Dreadful Word",               "Cast Confusion once using a spell slot. Requires Warlock 7.",                                                               "7",  ""),
        inv("sculptor_of_flesh",        "Sculptor of Flesh",           "Cast Polymorph once using a spell slot. Requires Warlock 7.",                                                               "7",  ""),
        inv("ascendant_step",           "Ascendant Step",              "Cast Levitate on yourself at will. Requires Warlock 9.",                                                                     "9",  ""),
        inv("minions_of_chaos",         "Minions of Chaos",            "Cast Conjure Elemental once using a spell slot. Requires Warlock 9.",                                                       "9",  ""),
        inv("otherworldly_leap",        "Otherworldly Leap",           "Cast Jump on yourself at will. Requires Warlock 9.",                                                                        "9",  ""),
        inv("whispers_of_the_grave",    "Whispers of the Grave",       "Cast Speak with Dead at will. Requires Warlock 9.",                                                                         "9",  ""),
        inv("lifedrinker",              "Lifedrinker",                 "Add CHA mod as necrotic damage to pact weapon attacks. Requires Warlock 12.",                                               "12", "blade"),
        inv("chains_of_carceri",        "Chains of Carceri",           "Cast Hold Monster at will on celestials, fiends, or elementals. Requires Warlock 15.",                                     "15", "chain"),
        inv("master_of_myriad_forms",   "Master of Myriad Forms",      "Cast Alter Self at will. Requires Warlock 15.",                                                                             "15", ""),
        inv("visions_of_distant_realms","Visions of Distant Realms",   "Cast Arcane Eye at will. Requires Warlock 15.",                                                                             "15", ""),
        inv("witch_sight",              "Witch Sight",                 "See true form of any shapechanger or creature magically concealed within 30 ft. Requires Warlock 15.",                     "15", "")
    );

    private static final List<Map<String, String>> METAMAGIC_OPTIONS = List.of(
        meta("careful",     "Careful Spell",     "Spend 1 SP: chosen creatures auto-succeed on saves against your spell."),
        meta("distant",     "Distant Spell",     "Spend 1 SP: double a spell's range (or change touch to 30 ft)."),
        meta("empowered",   "Empowered Spell",   "Spend 1 SP: reroll up to CHA mod damage dice (keep either result)."),
        meta("extended",    "Extended Spell",    "Spend 1 SP: double a spell's duration (max 24 hours)."),
        meta("heightened",  "Heightened Spell",  "Spend 3 SP: one target of a spell has disadvantage on its first save."),
        meta("quickened",   "Quickened Spell",   "Spend 2 SP: change casting time from 1 action to 1 bonus action."),
        meta("subtle",      "Subtle Spell",      "Spend 1 SP: cast a spell without verbal or somatic components."),
        meta("twinned",     "Twinned Spell",     "Spend SP equal to spell level: target a second creature with a single-target spell."),
        meta("seeking",     "Seeking Spell",     "Spend 2 SP: reroll a missed spell attack roll (TCoE)."),
        meta("transmuted",  "Transmuted Spell",  "Spend 1 SP: change a spell's damage type among acid/cold/fire/lightning/poison/thunder (TCoE).")
    );

    private static final List<String> FAVORED_ENEMY_TYPES = List.of(
        "Aberrations", "Beasts", "Celestials", "Constructs", "Dragons",
        "Elementals", "Fey", "Fiends", "Giants", "Monstrosities",
        "Oozes", "Plants", "Undead", "Two types of humanoids"
    );

    private static final List<String> NATURAL_EXPLORER_TERRAINS = List.of(
        "Arctic", "Coast", "Desert", "Forest", "Grassland",
        "Mountain", "Swamp", "Underdark"
    );

    private static Map<String, String> inv(String id, String name, String desc,
                                            String minLevel, String requiresPact) {
        return Map.of("id", id, "name", name, "desc", desc,
                      "minLevel", minLevel, "requiresPact", requiresPact);
    }

    private static Map<String, String> meta(String id, String name, String desc) {
        return Map.of("id", id, "name", name, "desc", desc);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMBAT DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("")
    public String playDashboard(HttpSession session, Model model) {
        CharacterDraft draft = getDraft(session);
        if (draft == null || draft.getCharacterClass().isEmpty()) {
            return "redirect:/step/1";
        }

        var derived = calculator.calculate(draft);
        model.addAttribute("draft", draft);
        model.addAttribute("derived", derived);
        model.addAttribute("classDef", classRepository.findById(draft.getCharacterClass()));

        // Current HP (initialize to max if not set)
        int currentHp = draft.getCurrentHp();
        if (currentHp < 0) currentHp = derived.getMaxHitPoints();
        model.addAttribute("currentHp", currentHp);

        // Spell slots
        var cd = classRepository.findById(draft.getCharacterClass());
        if (cd != null && cd.getSpellcasting() != null) {
            int[] slots;
            if ("warlock".equals(draft.getCharacterClass())) {
                int[] ws = ClassRepository.warlockSlots(draft.getLevel());
                slots = new int[]{ws[0], 0, 0, 0, 0, 0, 0, 0, 0};
            } else if ("half".equals(cd.getSpellcasting().getType())) {
                slots = ClassRepository.halfCasterSlots(draft.getLevel());
            } else {
                slots = ClassRepository.fullCasterSlots(draft.getLevel());
            }
            model.addAttribute("maxSlots", slots);
            model.addAttribute("usedSlots", draft.getUsedSpellSlots());
        }

        return "play/dashboard";
    }

    // ── HP Tracking ───────────────────────────────────────────────────────────

    @PostMapping("/hp/update")
    @ResponseBody
    public Map<String, Object> updateHp(@RequestParam int hp, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        // Clamp HP between 0 and max + temp
        hp = Math.max(0, Math.min(hp, maxHp + draft.getTempHp()));
        draft.setCurrentHp(hp);

        return Map.of("currentHp", hp, "maxHp", maxHp);
    }

    @PostMapping("/hp/temp")
    @ResponseBody
    public Map<String, Object> setTempHp(@RequestParam int temp, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setTempHp(Math.max(0, temp));
        return Map.of("tempHp", draft.getTempHp());
    }

    @PostMapping("/hp/heal")
    @ResponseBody
    public Map<String, Object> heal(@RequestParam int amount, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        int current = draft.getCurrentHp() < 0 ? maxHp : draft.getCurrentHp();
        int newHp = Math.min(maxHp, current + amount);
        draft.setCurrentHp(newHp);

        return Map.of("currentHp", newHp, "maxHp", maxHp, "healed", newHp - current);
    }

    @PostMapping("/hp/damage")
    @ResponseBody
    public Map<String, Object> damage(@RequestParam int amount, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        int current = draft.getCurrentHp() < 0 ? maxHp : draft.getCurrentHp();
        int temp = draft.getTempHp();

        // Temp HP absorbs damage first
        int remaining = amount;
        if (temp > 0) {
            if (temp >= remaining) {
                draft.setTempHp(temp - remaining);
                remaining = 0;
            } else {
                remaining -= temp;
                draft.setTempHp(0);
            }
        }

        int newHp = Math.max(0, current - remaining);
        draft.setCurrentHp(newHp);

        return Map.of("currentHp", newHp, "tempHp", draft.getTempHp(), "maxHp", maxHp);
    }

    // ── Spell Slots ───────────────────────────────────────────────────────────

    @PostMapping("/slots/use")
    @ResponseBody
    public Map<String, Object> useSpellSlot(@RequestParam int level, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int[] used = draft.getUsedSpellSlots();
        if (level >= 1 && level <= 9) {
            used[level - 1]++;
            draft.setUsedSpellSlots(used);
        }
        return Map.of("level", level, "used", used[level - 1]);
    }

    @PostMapping("/slots/restore")
    @ResponseBody
    public Map<String, Object> restoreSpellSlot(@RequestParam int level, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int[] used = draft.getUsedSpellSlots();
        if (level >= 1 && level <= 9 && used[level - 1] > 0) {
            used[level - 1]--;
            draft.setUsedSpellSlots(used);
        }
        return Map.of("level", level, "used", used[level - 1]);
    }

    // ── Rests ─────────────────────────────────────────────────────────────────

    @PostMapping("/rest/short")
    @ResponseBody
    public Map<String, Object> shortRest(HttpSession session) {
        CharacterDraft draft = getDraft(session);

        // Warlock: restore pact magic slots
        if ("warlock".equals(draft.getCharacterClass())) {
            draft.setUsedSpellSlots(new int[9]);
        }

        return Map.of("message", "Short rest complete. Roll hit dice to heal.");
    }

    @PostMapping("/rest/long")
    @ResponseBody
    public Map<String, Object> longRest(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);

        // Restore HP to max
        draft.setCurrentHp(derived.getMaxHitPoints());
        draft.setTempHp(0);

        // Restore all spell slots
        draft.setUsedSpellSlots(new int[9]);

        // Restore half hit dice (minimum 1)
        int hitDiceToRestore = Math.max(1, draft.getLevel() / 2);
        draft.setUsedHitDice(Math.max(0, draft.getUsedHitDice() - hitDiceToRestore));

        return Map.of(
            "message", "Long rest complete!",
            "currentHp", derived.getMaxHitPoints(),
            "hitDiceRestored", hitDiceToRestore
        );
    }

    @PostMapping("/hitdice/use")
    @ResponseBody
    public Map<String, Object> useHitDie(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int available = draft.getLevel() - draft.getUsedHitDice();
        if (available > 0) {
            draft.setUsedHitDice(draft.getUsedHitDice() + 1);
            var derived = calculator.calculate(draft);
            return Map.of(
                "success", true,
                "hitDie", derived.getHitDice(),
                "conMod", derived.getModifiers().get("CON"),
                "remaining", available - 1
            );
        }
        return Map.of("success", false, "message", "No hit dice remaining");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INVENTORY MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        CharacterDraft draft = getDraft(session);
        if (draft == null) return "redirect:/step/1";

        model.addAttribute("draft", draft);
        model.addAttribute("derived", calculator.calculate(draft));
        model.addAttribute("attunedCount", draft.getAttunedCount());

        return "play/inventory";
    }

    @PostMapping("/inventory/add")
    @ResponseBody
    public Map<String, Object> addItem(@RequestBody InventoryItem item, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.addItem(item);
        return Map.of("success", true, "itemId", item.getId());
    }

    @PostMapping("/inventory/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam String itemId, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.removeItem(itemId);
        return Map.of("success", true);
    }

    @PostMapping("/inventory/equip")
    @ResponseBody
    public Map<String, Object> equipItem(@RequestParam String itemId, @RequestParam boolean equipped, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        for (var item : draft.getInventory()) {
            if (item.getId().equals(itemId)) {
                item.setEquipped(equipped);
                return Map.of("success", true, "equipped", equipped);
            }
        }
        return Map.of("success", false);
    }

    @PostMapping("/inventory/attune")
    @ResponseBody
    public Map<String, Object> attuneItem(@RequestParam String itemId, @RequestParam boolean attuned, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        for (var item : draft.getInventory()) {
            if (item.getId().equals(itemId)) {
                if (attuned && !draft.canAttune()) {
                    return Map.of("success", false, "error", "Maximum 3 attuned items");
                }
                item.setAttuned(attuned);
                return Map.of("success", true, "attuned", attuned, "attunedCount", draft.getAttunedCount());
            }
        }
        return Map.of("success", false);
    }

    // ── Currency ──────────────────────────────────────────────────────────────

    @PostMapping("/currency/update")
    @ResponseBody
    public Map<String, Object> updateCurrency(
            @RequestParam int platinum,
            @RequestParam int gold,
            @RequestParam int electrum,
            @RequestParam int silver,
            @RequestParam int copper,
            HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setPlatinum(Math.max(0, platinum));
        draft.setGold(Math.max(0, gold));
        draft.setElectrum(Math.max(0, electrum));
        draft.setSilver(Math.max(0, silver));
        draft.setCopper(Math.max(0, copper));
        return Map.of("success", true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONDITION TRACKING
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/conditions/add")
    @ResponseBody
    public Map<String, Object> addCondition(@RequestParam String name,
                                            @RequestParam(defaultValue = "-1") int rounds,
                                            @RequestParam(required = false) String source,
                                            HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var condition = new ActiveCondition(name, rounds);
        if (source != null && !source.isEmpty()) {
            condition.withSource(source);
        }
        draft.addCondition(condition);
        return Map.of("success", true, "conditionId", condition.getId(), "conditions", draft.getConditions());
    }

    @PostMapping("/conditions/remove")
    @ResponseBody
    public Map<String, Object> removeCondition(@RequestParam String conditionId, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.removeCondition(conditionId);
        return Map.of("success", true, "conditions", draft.getConditions());
    }

    @PostMapping("/conditions/tick")
    @ResponseBody
    public Map<String, Object> tickConditions(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var expired = new ArrayList<String>();
        draft.getConditions().removeIf(c -> {
            if (c.tick()) {
                expired.add(c.getName());
                return true;
            }
            return false;
        });
        return Map.of("success", true, "expired", expired, "conditions", draft.getConditions());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DEATH SAVES
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/deathsaves/success")
    @ResponseBody
    public Map<String, Object> deathSaveSuccess(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setDeathSaveSuccesses(draft.getDeathSaveSuccesses() + 1);
        boolean stable = draft.isStable();
        if (stable) {
            // Stabilized - regain 1 HP after 1d4 hours, but for gameplay we just mark as stable
        }
        return Map.of(
            "successes", draft.getDeathSaveSuccesses(),
            "failures", draft.getDeathSaveFailures(),
            "stable", stable,
            "dead", false
        );
    }

    @PostMapping("/deathsaves/failure")
    @ResponseBody
    public Map<String, Object> deathSaveFailure(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setDeathSaveFailures(draft.getDeathSaveFailures() + 1);
        boolean dead = draft.isDead();
        return Map.of(
            "successes", draft.getDeathSaveSuccesses(),
            "failures", draft.getDeathSaveFailures(),
            "stable", false,
            "dead", dead
        );
    }

    @PostMapping("/deathsaves/reset")
    @ResponseBody
    public Map<String, Object> resetDeathSaves(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.resetDeathSaves();
        return Map.of("successes", 0, "failures", 0, "stable", false, "dead", false);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONCENTRATION
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/concentration/set")
    @ResponseBody
    public Map<String, Object> setConcentration(@RequestParam String spell, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setConcentratingOn(spell);
        return Map.of("success", true, "concentratingOn", spell);
    }

    @PostMapping("/concentration/break")
    @ResponseBody
    public Map<String, Object> breakConcentration(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        String was = draft.getConcentratingOn();
        draft.breakConcentration();
        return Map.of("success", true, "broken", was != null ? was : "");
    }

    @GetMapping("/concentration/check")
    @ResponseBody
    public Map<String, Object> concentrationCheck(@RequestParam int damage, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        if (!draft.isConcentrating()) {
            return Map.of("required", false);
        }
        // DC is 10 or half damage, whichever is higher
        int dc = Math.max(10, damage / 2);
        var derived = calculator.calculate(draft);
        int conMod = derived.getModifiers().get("CON");
        int profBonus = derived.getProficiencyBonus();
        // Check if proficient in CON saves
        boolean proficient = derived.getSavingThrowProficiencies().contains("CON");
        int bonus = conMod + (proficient ? profBonus : 0);

        return Map.of(
            "required", true,
            "dc", dc,
            "bonus", bonus,
            "spell", draft.getConcentratingOn()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @GetMapping("/levelup/options")
    @ResponseBody
    public Map<String, Object> levelUpOptions(HttpSession session,
                                              jakarta.servlet.http.HttpServletResponse response) {
        CharacterDraft draft = getDraft(session);
        if (draft == null || draft.getCharacterClass().isEmpty()) {
            response.setStatus(400);
            return Map.of("error", "No character in session");
        }
        int currentLevel = draft.getLevel();
        if (currentLevel >= 20) {
            response.setStatus(400);
            return Map.of("error", "Already at maximum level");
        }
        int newLevel = currentLevel + 1;
        String classId = draft.getCharacterClass();
        var classDef = classRepository.findById(classId);
        if (classDef == null) {
            response.setStatus(400);
            return Map.of("error", "Unknown class");
        }

        // New class features at the new level
        var newFeatures = classDef.getFeatures() != null
            ? classDef.getFeatures().stream().filter(f -> f.level() == newLevel).toList()
            : List.of();

        // HP gain for next level: average roll + CON mod
        var derived = calculator.calculate(draft);
        int conMod  = derived.getModifiers().get("CON");
        int hpGain  = Math.max(1, (classDef.getHitDie() / 2 + 1) + conMod);

        // Proficiency bonus
        boolean profBonusChanged =
            ClassRepository.proficiencyBonus(newLevel) > ClassRepository.proficiencyBonus(currentLevel);

        // Spell slot changes
        boolean spellSlotsChanged = false;
        String  newSpellSlotSummary = "";
        var sc = classDef.getSpellcasting();
        if (sc != null) {
            if ("warlock".equals(classId)) {
                int[] o = ClassRepository.warlockSlots(currentLevel);
                int[] n = ClassRepository.warlockSlots(newLevel);
                spellSlotsChanged = o[0] != n[0] || o[1] != n[1];
                if (spellSlotsChanged) newSpellSlotSummary = n[0] + " × " + ordinal(n[1]) + "-level (short rest)";
            } else if ("half".equals(sc.getType())) {
                int[] o = ClassRepository.halfCasterSlots(currentLevel);
                int[] n = ClassRepository.halfCasterSlots(newLevel);
                spellSlotsChanged = !Arrays.equals(o, n);
                if (spellSlotsChanged) newSpellSlotSummary = buildSlotSummary(n);
            } else {
                int[] o = ClassRepository.fullCasterSlots(currentLevel);
                int[] n = ClassRepository.fullCasterSlots(newLevel);
                spellSlotsChanged = !Arrays.equals(o, n);
                if (spellSlotsChanged) newSpellSlotSummary = buildSlotSummary(n);
            }
        }

        // ASI needed?
        boolean needsAsi = classDef.getAsiLevels() != null && classDef.getAsiLevels().contains(newLevel);

        // Subclass needed?
        boolean needsSubclass = newLevel == classDef.getSubclassLevel()
            && (draft.getSubclassId() == null || draft.getSubclassId().isEmpty());

        // Cantrip gain
        int newCantripsCount = Math.max(0,
            ClassRepository.cantripsKnown(classId, newLevel) - ClassRepository.cantripsKnown(classId, currentLevel));

        // Spell gain: deficit between target count and current known spells
        int newSpellsCount = 0;
        boolean isWizard = "wizard".equals(classId);
        int wizardSpellbookGain = isWizard && newLevel > 1 ? 2 : 0;
        int maxNewSpellLevel = ClassRepository.maxSpellLevel(classId, newLevel);

        // Full prepared casters (Cleric, Druid): prepared=true, full caster, not wizard
        boolean isFullPreparedCaster = !isWizard && sc != null
            && sc.isPrepareSpells() && !"half".equals(sc.getType());

        if (sc != null && !sc.isPrepareSpells() && maxNewSpellLevel > 0) {
            // Known casters: fill up to target spells known at new level
            int target = ClassRepository.spellsKnown(classId, newLevel);
            newSpellsCount = Math.max(0, target - draft.getChosenSpells().size());
        } else if (isFullPreparedCaster) {
            // Prepared casters (Cleric, Druid): fill up to new preparation count
            int abilityMod = derived.getModifiers().getOrDefault(sc.getAbility(), 0);
            int target = ClassRepository.maxPrepared(classId, newLevel, abilityMod);
            newSpellsCount = Math.max(0, target - draft.getChosenSpells().size());
        }

        // Available cantrips (not already chosen)
        var availableCantrips = newCantripsCount > 0
            ? spellRepository.findCantripsForClass(classId).stream()
                .filter(s -> !draft.getChosenCantrips().contains(s.getId()))
                .toList()
            : List.of();

        // Available spells — all spells up to max accessible level (not just the newly unlocked tier)
        int maxLvl = maxNewSpellLevel;
        var availableSpells = (newSpellsCount > 0 || wizardSpellbookGain > 0)
            ? spellRepository.findByClass(classId, null).stream()
                .filter(s -> s.getLevel() > 0 && s.getLevel() <= maxLvl)
                .filter(s -> !draft.getChosenSpells().contains(s.getId())
                          && !draft.getSpellbookSpells().contains(s.getId()))
                .sorted(Comparator.comparingInt(com.dnd.builder.core.model.SpellDefinition::getLevel)
                                  .thenComparing(com.dnd.builder.core.model.SpellDefinition::getName))
                .toList()
            : List.of();

        var availableFeats      = needsAsi       ? featRepository.findAll()         : List.of();
        var availableSubclasses = needsSubclass && classDef.getSubclasses() != null
                                    ? classDef.getSubclasses() : List.of();

        var result = new LinkedHashMap<String, Object>();
        result.put("currentLevel",        currentLevel);
        result.put("newLevel",            newLevel);
        result.put("newFeatures",         newFeatures);
        result.put("hpGain",              hpGain);
        result.put("profBonusChanged",    profBonusChanged);
        result.put("spellSlotsChanged",   spellSlotsChanged);
        result.put("newSpellSlotSummary", newSpellSlotSummary);
        result.put("needsAsi",            needsAsi);
        result.put("needsSubclass",       needsSubclass);
        result.put("newCantripsCount",    newCantripsCount);
        result.put("newSpellsCount",      newSpellsCount);
        result.put("availableCantrips",   availableCantrips);
        result.put("availableSpells",     availableSpells);
        result.put("availableFeats",      availableFeats);
        result.put("availableSubclasses", availableSubclasses);
        result.put("maxNewSpellLevel",    maxNewSpellLevel);
        result.put("isWizard",               isWizard);
        result.put("isFullPreparedCaster",   isFullPreparedCaster);
        result.put("wizardSpellbookGain",    wizardSpellbookGain);

        // ── Expertise (Bard L3/10, Rogue L6) ─────────────────────────────────
        boolean needsExpertise = ("bard".equals(classId) && (newLevel == 3 || newLevel == 10))
                              || ("rogue".equals(classId) && newLevel == 6);
        int expertiseCount = needsExpertise ? 2 : 0;
        var eligibleExpertise = needsExpertise
            ? derived.getAllSkillProficiencies().stream()
                .filter(s -> !draft.getExpertiseSkills().contains(s))
                .sorted()
                .toList()
            : List.<String>of();

        // ── Magical Secrets (Bard L10/14/18) ─────────────────────────────────
        boolean needsMagicalSecrets = "bard".equals(classId)
            && (newLevel == 10 || newLevel == 14 || newLevel == 18);
        var availableMagicalSecrets = needsMagicalSecrets
            ? spellRepository.getAllSpells().stream()
                .filter(sp -> sp.getLevel() > 0 && sp.getLevel() <= maxNewSpellLevel)
                .filter(sp -> !draft.getChosenSpells().contains(sp.getId())
                           && !draft.getChosenCantrips().contains(sp.getId()))
                .sorted(Comparator.comparingInt(com.dnd.builder.core.model.SpellDefinition::getLevel)
                                  .thenComparing(com.dnd.builder.core.model.SpellDefinition::getName))
                .toList()
            : List.of();

        // ── Pact Boon (Warlock L3) ────────────────────────────────────────────
        boolean needsPactBoon = "warlock".equals(classId) && newLevel == 3
            && (draft.getPactBoon() == null || draft.getPactBoon().isBlank());

        // ── Eldritch Invocations (Warlock) ────────────────────────────────────
        int newInvocationsCount = 0;
        if ("warlock".equals(classId)) {
            if (newLevel == 2) newInvocationsCount = 2;
            else if (List.of(5, 7, 9, 12, 15, 18).contains(newLevel)) newInvocationsCount = 1;
        }
        boolean needsInvocations = newInvocationsCount > 0;
        var availableInvocations = needsInvocations
            ? ELDRITCH_INVOCATIONS.stream()
                .filter(inv -> Integer.parseInt(inv.get("minLevel")) <= newLevel)
                .filter(inv -> !draft.getEldritchInvocations().contains(inv.get("id")))
                .filter(inv -> {
                    String req = inv.get("requiresPact");
                    return req.isBlank() || req.equals(draft.getPactBoon());
                })
                .toList()
            : List.<Map<String, String>>of();

        // ── Metamagic (Sorcerer L3/10/17) ────────────────────────────────────
        int newMetamagicCount = 0;
        if ("sorcerer".equals(classId)) {
            if (newLevel == 3) newMetamagicCount = 2;
            else if (newLevel == 10 || newLevel == 17) newMetamagicCount = 1;
        }
        boolean needsMetamagic = newMetamagicCount > 0;
        var availableMetamagic = needsMetamagic
            ? METAMAGIC_OPTIONS.stream()
                .filter(m -> !draft.getMetamagicOptions().contains(m.get("id")))
                .toList()
            : List.<Map<String, String>>of();

        // ── Ranger: Favored Enemy / Natural Explorer ──────────────────────────
        boolean needsFavoredEnemy    = "ranger".equals(classId) && (newLevel == 6 || newLevel == 14);
        boolean needsNaturalExplorer = "ranger".equals(classId) && (newLevel == 6 || newLevel == 10 || newLevel == 14);

        // ── Prepared Caster: spell prep gain + new spell level unlock ─────────
        boolean isPreparedCaster = sc != null && sc.isPrepareSpells();
        int preparedSpellsGain = (isPreparedCaster && !"half".equals(sc.getType())) ? 1 : 0;
        int currentMaxSlotLevel = 0, newMaxSlotLevel = 0;
        if (isPreparedCaster && !"half".equals(sc.getType())) {
            int[] curSlots = ClassRepository.fullCasterSlots(currentLevel);
            int[] newSlots = ClassRepository.fullCasterSlots(newLevel);
            for (int i = 8; i >= 0; i--) {
                if (curSlots[i] > 0 && currentMaxSlotLevel == 0) currentMaxSlotLevel = i + 1;
                if (newSlots[i] > 0 && newMaxSlotLevel == 0) newMaxSlotLevel = i + 1;
            }
        }
        boolean unlocksNewSpellLevel = newMaxSlotLevel > currentMaxSlotLevel;
        int newUnlockedSpellLevel = unlocksNewSpellLevel ? newMaxSlotLevel : 0;

        // ── Warlock Mystic Arcanum ────────────────────────────────────────────
        int mysticArcanumLevel = 0;
        if ("warlock".equals(classId)) {
            mysticArcanumLevel = switch (newLevel) {
                case 11 -> 6; case 13 -> 7; case 15 -> 8; case 17 -> 9;
                default -> 0;
            };
        }
        boolean needsMysticArcanum = mysticArcanumLevel > 0;
        int finalMysticLevel = mysticArcanumLevel;
        var availableMysticArcanum = needsMysticArcanum
            ? spellRepository.findByClass("warlock", finalMysticLevel).stream()
                .filter(sp -> !draft.getChosenSpells().contains(sp.getId()))
                .toList()
            : List.of();

        result.put("needsExpertise",           needsExpertise);
        result.put("expertiseCount",           expertiseCount);
        result.put("eligibleExpertiseSkills",  eligibleExpertise);
        result.put("needsMagicalSecrets",      needsMagicalSecrets);
        result.put("availableMagicalSecrets",  availableMagicalSecrets);
        result.put("needsPactBoon",            needsPactBoon);
        result.put("needsInvocations",         needsInvocations);
        result.put("newInvocationsCount",      newInvocationsCount);
        result.put("availableInvocations",     availableInvocations);
        result.put("needsMetamagic",           needsMetamagic);
        result.put("newMetamagicCount",        newMetamagicCount);
        result.put("availableMetamagic",       availableMetamagic);
        result.put("needsFavoredEnemy",        needsFavoredEnemy);
        result.put("needsNaturalExplorer",     needsNaturalExplorer);
        result.put("favoredEnemyTypes",        needsFavoredEnemy   ? FAVORED_ENEMY_TYPES        : List.of());
        result.put("naturalExplorerTerrains",  needsNaturalExplorer ? NATURAL_EXPLORER_TERRAINS : List.of());
        result.put("preparedSpellsGain",       preparedSpellsGain);
        result.put("unlocksNewSpellLevel",     unlocksNewSpellLevel);
        result.put("newUnlockedSpellLevel",    newUnlockedSpellLevel);
        result.put("needsMysticArcanum",       needsMysticArcanum);
        result.put("mysticArcanumLevel",       mysticArcanumLevel);
        result.put("availableMysticArcanum",   availableMysticArcanum);
        return result;
    }

    @PostMapping("/levelup")
    @ResponseBody
    public Map<String, Object> levelUp(@RequestBody Map<String, Object> body,
                                       HttpSession session,
                                       jakarta.servlet.http.HttpServletResponse response) {
        CharacterDraft draft = getDraft(session);
        if (draft == null || draft.getCharacterClass().isEmpty()) {
            response.setStatus(400);
            return Map.of("success", false, "error", "No character in session");
        }
        if (draft.getLevel() >= 20) {
            response.setStatus(400);
            return Map.of("success", false, "error", "Already at maximum level");
        }

        int newLevel = draft.getLevel() + 1;
        draft.setLevel(newLevel);

        // ASI / Feat
        String asiType = (String) body.get("asiType");
        if (asiType != null && !asiType.isEmpty()) {
            if ("feat".equals(asiType)) {
                String featId = (String) body.get("featId");
                if (featId != null && !featId.isEmpty()) {
                    draft.getAsiChoices().add(AsiChoice.feat(newLevel, featId, Map.of()));
                }
            } else {
                var statIncreases = new LinkedHashMap<String, Integer>();
                String mode  = (String) body.get("asiMode");
                String stat1 = (String) body.get("asiStat1");
                String stat2 = (String) body.get("asiStat2");
                if ("single".equals(mode) && stat1 != null && !stat1.isEmpty()) {
                    statIncreases.put(stat1, 2);
                } else if ("split".equals(mode)) {
                    if (stat1 != null && !stat1.isEmpty()) statIncreases.merge(stat1, 1, Integer::sum);
                    if (stat2 != null && !stat2.isEmpty()) statIncreases.merge(stat2, 1, Integer::sum);
                }
                if (!statIncreases.isEmpty()) {
                    draft.getAsiChoices().add(AsiChoice.asi(newLevel, statIncreases));
                }
            }
        }

        // Subclass
        String subclassId = (String) body.get("subclassId");
        if (subclassId != null && !subclassId.isEmpty()) {
            draft.setSubclassId(subclassId);
        }

        // Safe casts: Spring's Jackson deserializes JSON arrays as List<String>
        @SuppressWarnings("unchecked") List<String> newCantrips =
            (List<String>) body.getOrDefault("newCantrips", List.of());
        draft.getChosenCantrips().addAll(newCantrips);

        @SuppressWarnings("unchecked") List<String> newSpells =
            (List<String>) body.getOrDefault("newSpells", List.of());
        draft.getChosenSpells().addAll(newSpells);

        @SuppressWarnings("unchecked") List<String> spellbookAdditions =
            (List<String>) body.getOrDefault("spellbookAdditions", List.of());
        draft.getSpellbookSpells().addAll(spellbookAdditions);

        // ── Expertise ─────────────────────────────────────────────────────────
        @SuppressWarnings("unchecked") List<String> expertiseChoices =
            (List<String>) body.getOrDefault("expertiseSkills", List.of());
        draft.getExpertiseSkills().addAll(expertiseChoices);

        // ── Pact Boon ──────────────────────────────────────────────────────────
        String pactBoon = (String) body.getOrDefault("pactBoon", "");
        if (pactBoon != null && !pactBoon.isBlank()) draft.setPactBoon(pactBoon);

        // ── Eldritch Invocations ───────────────────────────────────────────────
        @SuppressWarnings("unchecked") List<String> newInvocations =
            (List<String>) body.getOrDefault("newInvocations", List.of());
        draft.getEldritchInvocations().addAll(newInvocations);

        // ── Metamagic ─────────────────────────────────────────────────────────
        @SuppressWarnings("unchecked") List<String> newMetamagic =
            (List<String>) body.getOrDefault("newMetamagic", List.of());
        draft.getMetamagicOptions().addAll(newMetamagic);

        // ── Favored Enemy / Natural Explorer ──────────────────────────────────
        String newFavoredEnemy    = (String) body.getOrDefault("favoredEnemy", "");
        String newNaturalExplorer = (String) body.getOrDefault("naturalExplorer", "");
        if (newFavoredEnemy    != null && !newFavoredEnemy.isBlank())    draft.getFavoredEnemies().add(newFavoredEnemy);
        if (newNaturalExplorer != null && !newNaturalExplorer.isBlank()) draft.getFavoredTerrains().add(newNaturalExplorer);

        // ── Mystic Arcanum ────────────────────────────────────────────────────
        String mysticArcanumSpell = (String) body.getOrDefault("mysticArcanumSpell", "");
        if (mysticArcanumSpell != null && !mysticArcanumSpell.isBlank()) {
            draft.getChosenSpells().add(mysticArcanumSpell);
        }

        var derived = calculator.calculate(draft);
        var result  = new LinkedHashMap<String, Object>();
        result.put("success",  true);
        result.put("newLevel", newLevel);
        result.put("draft",    draft);
        result.put("derived",  derived);
        return result;
    }

    private String buildSlotSummary(int[] slots) {
        var parts = new ArrayList<String>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] > 0) parts.add(slots[i] + "×" + ordinal(i + 1));
        }
        return String.join(", ", parts);
    }

    private String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }

    private CharacterDraft getDraft(HttpSession session) {
        return (CharacterDraft) session.getAttribute(DRAFT_KEY);
    }
}
