package com.dnd.builder.core.service;

import com.dnd.builder.core.model.*;
import com.dnd.builder.core.port.out.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Pure computation: given a CharacterDraft + repository data, produce DerivedStats.
 * No mutation of the draft. Called whenever we need to display computed values.
 */
@Service
public class CharacterCalculator {

    private final RaceRepository       raceRepository;
    private final ClassRepository      classRepository;
    private final BackgroundRepository backgroundRepository;
    private final EquipmentRepository  equipmentRepository;

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

    public CharacterCalculator(RaceRepository r, ClassRepository c, BackgroundRepository b,
                                SpellRepository sp, EquipmentRepository eq) {
        this.raceRepository       = r;
        this.classRepository      = c;
        this.backgroundRepository = b;
        this.equipmentRepository  = eq;
    }

    public DerivedStats calculate(CharacterDraft draft) {
        var ds = new DerivedStats();

        // ── 1. Final scores ──────────────────────────────────────────────────
        var finalScores = new LinkedHashMap<String, Integer>();
        for (var entry : draft.getBaseScores().entrySet()) {
            int base = entry.getValue();
            int racial = getRacialBonus(draft, entry.getKey());
            int asi = getAsiBonus(draft, entry.getKey());
            finalScores.put(entry.getKey(), Math.min(20, base + racial + asi)); // Cap at 20
        }
        ds.setFinalScores(finalScores);

        // ── 2. Modifiers ─────────────────────────────────────────────────────
        var mods = new LinkedHashMap<String, Integer>();
        finalScores.forEach((k, v) -> mods.put(k, modifier(v)));
        ds.setModifiers(mods);

        // ── 3. Proficiency bonus ─────────────────────────────────────────────
        int pb = ClassRepository.proficiencyBonus(draft.getLevel());
        ds.setProficiencyBonus(pb);

        // ── 4. Basic stats ───────────────────────────────────────────────────
        ds.setInitiative(mods.get("DEX"));
        ds.setSpeed(RACE_SPEED.getOrDefault(draft.getRaceId(), 30));

        // ── 5. HP ────────────────────────────────────────────────────────────
        var classDef = classRepository.findById(draft.getCharacterClass());
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
        var bg = backgroundRepository.findById(draft.getBackground());
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
                int[] ws = ClassRepository.warlockSlots(draft.getLevel());
                ds.setSpellSlotSummary(ws[0] + " × " + ordinal(ws[1]) + "-level (short rest)");
            } else if (!isHalf) {
                int[] slots = ClassRepository.fullCasterSlots(draft.getLevel());
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
        var slots = classDef != null ? equipmentRepository.findByClass(classDef.getId()) : List.<EquipmentSlot>of();
        for (var slot : slots) {
            String chosen = draft.getEquipmentChoices().get(slot.slotId());
            if (chosen != null) {
                slot.choices().stream()
                    .filter(c -> c.optionId().equals(chosen))
                    .map(EquipmentChoice::label)
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
        var race = raceRepository.findById(draft.getRaceId());
        if (race == null) return 0;
        int bonus = Optional.ofNullable(race.fixedBonuses())
                            .map(m -> m.getOrDefault(statKey, 0)).orElse(0);
        if (race.flexibleBonuses() != null) {
            for (int fbIdx = 0; fbIdx < race.flexibleBonuses().size(); fbIdx++) {
                var fb = race.flexibleBonuses().get(fbIdx);
                for (int pi = 0; pi < fb.count(); pi++) {
                    String key = "flex_" + fbIdx + "_" + pi;
                    if (statKey.equals(draft.getFlexPicks().get(key))) {
                        bonus += fb.amount();
                    }
                }
            }
        }
        return bonus;
    }

    public static int modifier(int score) { return Math.floorDiv(score - 10, 2); }

    /** Sum of all ASI bonuses from asiChoices for a given stat */
    public int getAsiBonus(CharacterDraft draft, String statKey) {
        int bonus = 0;
        if (draft.getAsiChoices() != null) {
            for (var choice : draft.getAsiChoices()) {
                if (choice.statIncreases() != null) {
                    bonus += choice.statIncreases().getOrDefault(statKey, 0);
                }
            }
        }
        return bonus;
    }

    /** Get ASI levels the character has reached based on class and current level */
    public List<Integer> getAvailableAsiLevels(CharacterDraft draft) {
        var classDef = classRepository.findById(draft.getCharacterClass());
        if (classDef == null || classDef.getAsiLevels() == null) {
            return List.of();
        }
        return classDef.getAsiLevels().stream()
            .filter(lvl -> lvl <= draft.getLevel())
            .toList();
    }

    /** Check if character has made ASI choice for a given level */
    public boolean hasAsiChoiceForLevel(CharacterDraft draft, int level) {
        if (draft.getAsiChoices() == null) return false;
        return draft.getAsiChoices().stream().anyMatch(c -> c.level() == level);
    }

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
     * Determine AC by resolving chosen equipment option IDs → labels via the repository,
     * then checking those labels for known armor keywords.
     * draft.getEquipmentChoices() stores slotId → optionId ("a"/"b"/"c"), NOT labels.
     */
    private int resolveArmorAC(CharacterDraft draft, int dexMod, int defaultAC) {
        var slots = equipmentRepository.findByClass(draft.getCharacterClass());
        int baseAc = defaultAC;
        boolean shieldFound = false;
        for (var slot : slots) {
            String chosenOptionId = draft.getEquipmentChoices().get(slot.slotId());
            if (chosenOptionId == null) continue;
            String label = slot.choices().stream()
                    .filter(c -> c.optionId().equals(chosenOptionId))
                    .map(EquipmentChoice::label)
                    .findFirst()
                    .orElse("");
            if (label.contains("Chain mail"))      baseAc = Math.max(baseAc, 16);
            else if (label.contains("Scale mail"))      baseAc = Math.max(baseAc, 14 + Math.min(dexMod, 2));
            else if (label.contains("Leather armor"))   baseAc = Math.max(baseAc, 11 + dexMod);
            else if (label.contains("Studded leather")) baseAc = Math.max(baseAc, 12 + dexMod);
            else if (label.contains("Half plate"))      baseAc = Math.max(baseAc, 15 + Math.min(dexMod, 2));
            if (label.toLowerCase().contains("shield")) shieldFound = true;
        }
        return baseAc + (shieldFound ? 2 : 0);
    }

    /** Collect skill proficiency list valid for current class + background */
    public Set<String> getAvailableClassSkills(CharacterDraft draft) {
        var cd = classRepository.findById(draft.getCharacterClass());
        return cd != null ? new LinkedHashSet<>(cd.getSkillList()) : new LinkedHashSet<>();
    }

    public int getClassSkillChoiceCount(CharacterDraft draft) {
        var cd = classRepository.findById(draft.getCharacterClass());
        return cd != null ? cd.getSkillChoiceCount() : 0;
    }

    /** Background's fixed skill profs (can't be changed) */
    public List<String> getBackgroundSkills(CharacterDraft draft) {
        var bg = backgroundRepository.findById(draft.getBackground());
        return bg != null ? bg.getSkillProficiencies() : List.of();
    }
}
