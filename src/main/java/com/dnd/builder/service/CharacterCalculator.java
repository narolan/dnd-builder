package com.dnd.builder.service;

import com.dnd.builder.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Pure computation: given a CharacterDraft + registry data, produce DerivedStats.
 * No mutation of the draft. Called whenever we need to display computed values.
 */
@Service
public class CharacterCalculator {

    private final RaceRegistry    raceRegistry;
    private final ClassRegistry   classRegistry;
    private final BackgroundRegistry bgRegistry;
    private final EquipmentRegistry equipmentRegistry;

    // All skills and their governing ability
    public static final Map<String, String> SKILL_ABILITY = new LinkedHashMap<>();
    static {
        SKILL_ABILITY.put("Acrobatics",     "DEX");
        SKILL_ABILITY.put("Animal Handling","WIS");
        SKILL_ABILITY.put("Arcana",         "INT");
        SKILL_ABILITY.put("Athletics",      "STR");
        SKILL_ABILITY.put("Deception",      "CHA");
        SKILL_ABILITY.put("History",        "INT");
        SKILL_ABILITY.put("Insight",        "WIS");
        SKILL_ABILITY.put("Intimidation",   "CHA");
        SKILL_ABILITY.put("Investigation",  "INT");
        SKILL_ABILITY.put("Medicine",       "WIS");
        SKILL_ABILITY.put("Nature",         "INT");
        SKILL_ABILITY.put("Perception",     "WIS");
        SKILL_ABILITY.put("Performance",    "CHA");
        SKILL_ABILITY.put("Persuasion",     "CHA");
        SKILL_ABILITY.put("Religion",       "INT");
        SKILL_ABILITY.put("Sleight of Hand","DEX");
        SKILL_ABILITY.put("Stealth",        "DEX");
        SKILL_ABILITY.put("Survival",       "WIS");
    }

    // Race speed overrides (defaults to 30)
    private static final Map<String, Integer> RACE_SPEED = Map.ofEntries(
        Map.entry("dwarf_hill",25), Map.entry("dwarf_mountain",25), Map.entry("dwarf_duergar",25),
        Map.entry("halfling_lightfoot",25), Map.entry("halfling_stout",25), Map.entry("halfling_ghostwise",25),
        Map.entry("gnome_forest",25), Map.entry("gnome_rock",25), Map.entry("gnome_deep",25),
        Map.entry("goblin",25),
        Map.entry("elf_wood",35)
    );

    public CharacterCalculator(RaceRegistry r, ClassRegistry c, BackgroundRegistry b,
                                SpellRegistry sp, EquipmentRegistry eq) {
        this.raceRegistry      = r;
        this.classRegistry     = c;
        this.bgRegistry        = b;
        this.equipmentRegistry = eq;
    }

    public DerivedStats calculate(CharacterDraft draft) {
        var ds = new DerivedStats();

        // ── 1. Final scores ──────────────────────────────────────────────────
        var finalScores = new LinkedHashMap<String, Integer>();
        for (var entry : draft.getBaseScores().entrySet()) {
            finalScores.put(entry.getKey(), entry.getValue() + getRacialBonus(draft, entry.getKey()));
        }
        ds.setFinalScores(finalScores);

        // ── 2. Modifiers ─────────────────────────────────────────────────────
        var mods = new LinkedHashMap<String, Integer>();
        finalScores.forEach((k, v) -> mods.put(k, modifier(v)));
        ds.setModifiers(mods);

        // ── 3. Proficiency bonus ─────────────────────────────────────────────
        int pb = ClassRegistry.proficiencyBonus(draft.getLevel());
        ds.setProficiencyBonus(pb);

        // ── 4. Basic stats ───────────────────────────────────────────────────
        ds.setInitiative(mods.get("DEX"));
        ds.setSpeed(RACE_SPEED.getOrDefault(draft.getRaceId(), 30));

        // ── 5. HP ────────────────────────────────────────────────────────────
        var classDef = classRegistry.getById(draft.getCharacterClass());
        int conMod   = mods.get("CON");
        if (classDef != null) {
            int hitDie = classDef.getHitDie();
            // Level 1: max hit die + CON mod. Further levels: avg + CON mod.
            int hp = hitDie + conMod + (draft.getLevel() - 1) * ((hitDie / 2 + 1) + conMod);
            ds.setMaxHitPoints(Math.max(1, hp));
            ds.setHitDice(classDef.getHitDie());
        } else {
            ds.setMaxHitPoints(8 + conMod);
            ds.setHitDice(8);
        }

        // ── 6. Armor Class ───────────────────────────────────────────────────
        // Default unarmored: 10 + DEX. Equipment step may upgrade this.
        // Barbarian unarmored defense: 10 + DEX + CON
        int dexMod = mods.get("DEX");
        if ("barbarian".equals(draft.getCharacterClass())) {
            ds.setArmorClass(10 + dexMod + mods.get("CON"));
        } else if ("monk".equals(draft.getCharacterClass())) {
            ds.setArmorClass(10 + dexMod + mods.get("WIS"));
        } else {
            // Check if they chose any armor in equipment
            int ac = 10 + dexMod;
            ac = resolveArmorAC(draft, dexMod, ac);
            ds.setArmorClass(ac);
        }

        // ── 7. Saving throws ─────────────────────────────────────────────────
        var saveProfs = classDef != null ? classDef.getSavingThrows() : List.<String>of();
        ds.setSavingThrowProficiencies(saveProfs);
        var saves = new LinkedHashMap<String, Integer>();
        for (var stat : List.of("STR","DEX","CON","INT","WIS","CHA")) {
            int bonus = mods.get(stat) + (saveProfs.contains(stat) ? pb : 0);
            saves.put(stat, bonus);
        }
        ds.setSavingThrows(saves);

        // ── 8. Skills ────────────────────────────────────────────────────────
        Set<String> allProfs = new LinkedHashSet<>(draft.getSkillProficiencies());
        // Background fixed skills
        var bg = bgRegistry.getById(draft.getBackground());
        if (bg != null) {
            allProfs.addAll(bg.getSkillProficiencies());
        }
        ds.setAllSkillProficiencies(new ArrayList<>(allProfs));

        var skillBonuses = new LinkedHashMap<String, Integer>();
        SKILL_ABILITY.forEach((skill, ability) -> {
            int mod  = mods.get(ability);
            int bonus = mod + (allProfs.contains(skill) ? pb : 0);
            skillBonuses.put(skill, bonus);
        });
        ds.setSkillBonuses(skillBonuses);

        // ── 9. Passive Perception ────────────────────────────────────────────
        ds.setPassivePerception(10 + skillBonuses.get("Perception"));

        // ── 10. Spellcasting ─────────────────────────────────────────────────
        if (classDef != null && classDef.getSpellcasting() != null) {
            var sc     = classDef.getSpellcasting();
            boolean isHalf = "half".equals(sc.getType());
            ds.setSpellcaster(!isHalf || draft.getLevel() >= 2);
            ds.setSpellcastingAbility(sc.getAbility());

            int abilityMod = mods.get(sc.getAbility());
            ds.setSpellSaveDC(8 + pb + abilityMod);
            ds.setSpellAttackBonus(pb + abilityMod);

            // Spell slot summary
            if ("warlock".equals(draft.getCharacterClass())) {
                int[] ws = ClassRegistry.warlockSlots(draft.getLevel());
                ds.setSpellSlotSummary(ws[0] + " × " + ordinal(ws[1]) + "-level (short rest)");
            } else if (!isHalf) {
                int[] slots = ClassRegistry.fullCasterSlots(draft.getLevel());
                var parts = new ArrayList<String>();
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] > 0) parts.add(slots[i] + " × " + ordinal(i+1));
                }
                ds.setSpellSlotSummary(String.join(", ", parts));
            } else {
                ds.setSpellSlotSummary("Spell slots begin at level 2");
            }
        } else {
            ds.setSpellcaster(false);
        }

        // ── 11. Proficiency lists ─────────────────────────────────────────────
        if (classDef != null) {
            ds.setArmorProficiencies(classDef.getArmorProficiencies());
            ds.setWeaponProficiencies(classDef.getWeaponProficiencies());
            var tools = new ArrayList<>(classDef.getToolProficiencies());
            if (bg != null) tools.addAll(bg.getToolProficiencies());
            ds.setToolProficiencies(tools);
        }

        // Languages (Common + racial + background)
        var languages = new ArrayList<String>();
        languages.add("Common");
        addRaceLanguages(draft, languages);
        if (bg != null && bg.getBonusLanguages() > 0) {
            for (int i = 0; i < bg.getBonusLanguages(); i++) {
                languages.add("(choose language " + (i+1) + ")");
            }
        }
        ds.setLanguages(languages);

        // ── 12. Equipment summary ─────────────────────────────────────────────
        var eqSummary = new ArrayList<String>();
        var slots = classDef != null ? equipmentRegistry.forClass(classDef.getId()) : List.<EquipmentSlot>of();
        for (var slot : slots) {
            String chosen = draft.getEquipmentChoices().get(slot.getSlotId());
            if (chosen != null) {
                slot.getChoices().stream()
                    .filter(c -> c.getOptionId().equals(chosen))
                    .map(EquipmentChoice::getLabel)
                    .findFirst()
                    .ifPresent(eqSummary::add);
            }
        }
        ds.setEquipmentSummary(eqSummary);
        if (bg != null) ds.setBackgroundEquipment(bg.getEquipment());

        return ds;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public int getRacialBonus(CharacterDraft draft, String statKey) {
        var race = raceRegistry.getById(draft.getRaceId());
        if (race == null) return 0;
        int bonus = Optional.ofNullable(race.getFixedBonuses())
                            .map(m -> m.getOrDefault(statKey, 0)).orElse(0);
        if (race.getFlexibleBonuses() != null) {
            for (int fbIdx = 0; fbIdx < race.getFlexibleBonuses().size(); fbIdx++) {
                var fb = race.getFlexibleBonuses().get(fbIdx);
                for (int pi = 0; pi < fb.getCount(); pi++) {
                    String key = "flex_" + fbIdx + "_" + pi;
                    if (statKey.equals(draft.getFlexPicks().get(key))) {
                        bonus += fb.getAmount();
                    }
                }
            }
        }
        return bonus;
    }

    public static int modifier(int score) { return (score - 10) / 2; }

    private String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st"; case 2 -> "2nd"; case 3 -> "3rd";
            default -> n + "th";
        };
    }

    private void addRaceLanguages(CharacterDraft draft, List<String> out) {
        switch (draft.getRaceId()) {
            case "dwarf_hill","dwarf_mountain","dwarf_duergar" -> out.add("Dwarvish");
            case "elf_high","elf_wood","elf_drow","elf_eladrin","elf_sea","elf_shadarkai" -> out.add("Elvish");
            case "halfling_lightfoot","halfling_stout","halfling_ghostwise" -> out.add("Halfling");
            case "dragonborn"  -> out.add("Draconic");
            case "gnome_forest","gnome_rock","gnome_deep" -> out.add("Gnomish");
            case "half_elf"    -> out.add("Elvish");
            case "half_orc"    -> out.add("Orc");
            case "tiefling"    -> out.add("Infernal");
            case "githzerai","githyanki" -> out.add("Gith");
            default -> {}
        }
    }

    /**
     * Determine AC by resolving chosen equipment option IDs → labels via the registry,
     * then checking those labels for known armor keywords.
     * draft.getEquipmentChoices() stores slotId → optionId ("a"/"b"/"c"), NOT labels.
     */
    private int resolveArmorAC(CharacterDraft draft, int dexMod, int defaultAC) {
        var slots = equipmentRegistry.forClass(draft.getCharacterClass());
        for (var slot : slots) {
            String chosenOptionId = draft.getEquipmentChoices().get(slot.getSlotId());
            if (chosenOptionId == null) continue;
            String label = slot.getChoices().stream()
                    .filter(c -> c.getOptionId().equals(chosenOptionId))
                    .map(EquipmentChoice::getLabel)
                    .findFirst()
                    .orElse("");
            if (label.contains("Chain mail"))      return 16;
            if (label.contains("Scale mail"))      return 14 + Math.min(dexMod, 2);
            if (label.contains("Leather armor"))   return 11 + dexMod;
            if (label.contains("Studded leather")) return 12 + dexMod;
            if (label.contains("Half plate"))      return 15 + Math.min(dexMod, 2);
        }
        return defaultAC;
    }

    /** Collect skill proficiency list valid for current class + background */
    public Set<String> getAvailableClassSkills(CharacterDraft draft) {
        var cd = classRegistry.getById(draft.getCharacterClass());
        return cd != null ? new LinkedHashSet<>(cd.getSkillList()) : new LinkedHashSet<>();
    }

    public int getClassSkillChoiceCount(CharacterDraft draft) {
        var cd = classRegistry.getById(draft.getCharacterClass());
        return cd != null ? cd.getSkillChoiceCount() : 0;
    }

    /** Background's fixed skill profs (can't be changed) */
    public List<String> getBackgroundSkills(CharacterDraft draft) {
        var bg = bgRegistry.getById(draft.getBackground());
        return bg != null ? bg.getSkillProficiencies() : List.of();
    }
}
