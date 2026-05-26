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

    private final RaceRepository raceRepository;
    private final ClassRepository classRepository;
    private final BackgroundRepository backgroundRepository;
    private final EquipmentRepository equipmentRepository;

    public CharacterCalculator(RaceRepository r, ClassRepository c, BackgroundRepository b, EquipmentRepository eq) {
        this.raceRepository = r;
        this.classRepository = c;
        this.backgroundRepository = b;
        this.equipmentRepository = eq;
    }

    public DerivedStats calculate(CharacterDraft draft) {
        var ds = new DerivedStats();

        // Pre-collect equipped inventory items once; used across multiple steps below
        var equippedItems = draft.getInventory().stream()
            .filter(InventoryItem::isEquipped).toList();

        // ── 1. Final scores ──────────────────────────────────────────────────
        var finalScores = calculateFinalScores(draft, equippedItems);
        ds.setFinalScores(finalScores);

        // ── 2. Modifiers ─────────────────────────────────────────────────────
        var mods = calculateModifiers(finalScores);
        ds.setModifiers(mods);

        // ── 3. Proficiency bonus ─────────────────────────────────────────────
        int pb = ClassRepository.proficiencyBonus(draft.getLevel());
        ds.setProficiencyBonus(pb);

        // ── 4. Basic stats ───────────────────────────────────────────────────
        calculateBasicStats(ds, mods, draft, equippedItems);

        // ── 5. HP ────────────────────────────────────────────────────────
        calculateHP(ds, draft, mods);

        // ── 6. Armor Class ───────────────────────────────────────────────────
        calculateArmorClass(ds, draft, mods, equippedItems);

        // ── 7. Saving throws ─────────────────────────────────────────────────
        calculateSavingThrows(ds, draft, mods, pb);

        // ── 8. Skills ────────────────────────────────────────────────────────
        calculateSkills(ds, draft, mods, pb);

        // ── 9. Passive Perception ────────────────────────────────────────────
        ds.setPassivePerception(10 + ds.getSkillBonuses().get("Perception"));

        // ── 10. Spellcasting ─────────────────────────────────────────────────
        calculateSpellcasting(ds, draft, mods, pb, equippedItems);

        // ── 11. Proficiency lists ─────────────────────────────────────────────
        calculateProficiencyLists(ds, draft);

        // ── 12. Equipment summary ─────────────────────────────────────────────
        calculateEquipmentSummary(ds, draft);

        return ds;
    }

    // ── Helper Methods ───────────────────────────────────────────────────────────

    private Map<String, Integer> calculateFinalScores(CharacterDraft draft, List<InventoryItem> equippedItems) {
        var finalScores = new LinkedHashMap<String, Integer>();
        for (var entry : draft.getBaseScores().entrySet()) {
            int base = entry.getValue();
            int racial = getRacialBonus(draft, entry.getKey());
            int asi = getAsiBonus(draft, entry.getKey());
            finalScores.put(entry.getKey(), Math.min(20, base + racial + asi)); // Cap at 20
        }
        // Apply equipped item ability bonuses (e.g. Gauntlets of Ogre Power delta, Belt of Giant Strength)
        for (var item : equippedItems) {
            if (item.getAbilityBonuses() != null) {
                item.getAbilityBonuses().forEach((stat, bonus) ->
                    finalScores.computeIfPresent(stat, (k, v) -> v + bonus));
            }
        }
        return finalScores;
    }

    private Map<String, Integer> calculateModifiers(Map<String, Integer> finalScores) {
        var mods = new LinkedHashMap<String, Integer>();
        finalScores.forEach((k, v) -> mods.put(k, modifier(v)));
        return mods;
    }

    private void calculateBasicStats(DerivedStats ds, Map<String, Integer> mods, CharacterDraft draft, List<InventoryItem> equippedItems) {
        ds.setInitiative(mods.get("DEX"));
        int raceSpeed = RACE_SPEED.getOrDefault(draft.getRaceId(), 30);
        int itemSpeedBonus = equippedItems.stream().mapToInt(InventoryItem::getSpeedBonus).sum();
        ds.setSpeed(raceSpeed + itemSpeedBonus);
    }

    private void calculateHP(DerivedStats ds, CharacterDraft draft, Map<String, Integer> mods) {
        var classDef = classRepository.findById(draft.getCharacterClass());
        int conMod = mods.get("CON");
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
    }

    private void calculateArmorClass(DerivedStats ds, CharacterDraft draft, Map<String, Integer> mods, List<InventoryItem> equippedItems) {
        int dexMod = mods.get("DEX");
        // PHB: Unarmored Defense only applies while NOT wearing armor.
        boolean wearingArmor = equippedItems.stream()
            .anyMatch(i -> "armor".equalsIgnoreCase(i.getCategory()) && i.getBaseAc() > 0);
        if (!wearingArmor && "barbarian".equals(draft.getCharacterClass())) {
            ds.setArmorClass(10 + dexMod + mods.get("CON"));
        } else if (!wearingArmor && "monk".equals(draft.getCharacterClass())) {
            ds.setArmorClass(10 + dexMod + mods.get("WIS"));
        } else {
            int ac = 10 + dexMod;
            ac = resolveArmorAC(draft, dexMod, ac);
            ds.setArmorClass(ac);
        }
        // Apply AC bonuses from equipped items (rings, cloaks, etc.)
        int itemAcBonus = equippedItems.stream().mapToInt(InventoryItem::getAcBonus).sum();
        if (itemAcBonus != 0) ds.setArmorClass(ds.getArmorClass() + itemAcBonus);
    }

    private void calculateSavingThrows(DerivedStats ds, CharacterDraft draft, Map<String, Integer> mods, int pb) {
        var classDef = classRepository.findById(draft.getCharacterClass());
        var saveProfs = classDef != null ? classDef.getSavingThrows() : List.<String>of();
        ds.setSavingThrowProficiencies(saveProfs);
        var saves = new LinkedHashMap<String, Integer>();
        for (var stat : List.of("STR","DEX","CON","INT","WIS","CHA")) {
            int bonus = mods.get(stat) + (saveProfs.contains(stat) ? pb : 0);
            saves.put(stat, bonus);
        }
        ds.setSavingThrows(saves);
    }

    private void calculateSkills(DerivedStats ds, CharacterDraft draft, Map<String, Integer> mods, int pb) {
        Set<String> allProfs = new LinkedHashSet<>(draft.getSkillProficiencies());
        // Background fixed skills
        var bg = backgroundRepository.findById(draft.getBackground());
        if (bg != null) {
            allProfs.addAll(bg.getSkillProficiencies());
        }
        ds.setAllSkillProficiencies(new ArrayList<>(allProfs));

        Set<String> expertise = new java.util.HashSet<>(draft.getExpertiseSkills());
        // PHB: Bard level 2+ adds half proficiency bonus to non-proficient ability checks (Jack of All Trades)
        boolean jackOfAllTrades = "bard".equals(draft.getCharacterClass()) && draft.getLevel() >= 2;
        int halfPb = Math.floorDiv(pb, 2);

        var skillBonuses = new LinkedHashMap<String, Integer>();
        SKILL_ABILITY.forEach((skill, ability) -> {
            int mod = mods.get(ability);
            int bonus;
            if (allProfs.contains(skill)) {
                // Expertise: double proficiency bonus (PHB p. 96/117)
                bonus = mod + (expertise.contains(skill) ? pb * 2 : pb);
            } else {
                // Jack of All Trades: half proficiency on non-proficient checks (PHB p. 54)
                bonus = mod + (jackOfAllTrades ? halfPb : 0);
            }
            skillBonuses.put(skill, bonus);
        });
        ds.setSkillBonuses(skillBonuses);
    }

    private void calculateSpellcasting(DerivedStats ds, CharacterDraft draft, Map<String, Integer> mods, int pb, List<InventoryItem> equippedItems) {
        var classDef = classRepository.findById(draft.getCharacterClass());
        if (classDef != null && classDef.getSpellcasting() != null) {
            var sc = classDef.getSpellcasting();
            boolean isHalf = "half".equals(sc.getType());
            boolean isSpellcaster = !isHalf || draft.getLevel() >= 2;
            ds.setSpellcaster(isSpellcaster);
            ds.setSpellcastingAbility(sc.getAbility());

            // Only compute DC/attack when the character actually has spell slots (PHB half-casters get none at level 1)
            if (isSpellcaster) {
                int abilityMod = mods.get(sc.getAbility());
                int itemSaveDcBonus = equippedItems.stream().mapToInt(InventoryItem::getSaveDcBonus).sum();
                ds.setSpellSaveDC(8 + pb + abilityMod + itemSaveDcBonus);
                ds.setSpellAttackBonus(pb + abilityMod);
            }

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
    }

    private void calculateProficiencyLists(DerivedStats ds, CharacterDraft draft) {
        var classDef = classRepository.findById(draft.getCharacterClass());
        if (classDef != null) {
            ds.setArmorProficiencies(classDef.getArmorProficiencies());
            ds.setWeaponProficiencies(classDef.getWeaponProficiencies());
            var tools = new ArrayList<>(classDef.getToolProficiencies());
            var bg = backgroundRepository.findById(draft.getBackground());
            if (bg != null) tools.addAll(bg.getToolProficiencies());
            ds.setToolProficiencies(tools);
        }

        // Languages (Common + racial + background)
        var languages = new ArrayList<String>();
        languages.add("Common");
        addRaceLanguages(draft, languages);
        var bg = backgroundRepository.findById(draft.getBackground());
        if (bg != null && bg.getBonusLanguages() > 0) {
            for (int i = 0; i < bg.getBonusLanguages(); i++) {
                languages.add("(choose language " + (i+1) + ")");
            }
        }
        ds.setLanguages(languages);
    }

    private void calculateEquipmentSummary(DerivedStats ds, CharacterDraft draft) {
        var eqSummary = new ArrayList<String>();
        var classDef = classRepository.findById(draft.getCharacterClass());
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
        var bg = backgroundRepository.findById(draft.getBackground());
        if (bg != null) ds.setBackgroundEquipment(bg.getEquipment());
    }

    // ── Constants ───────────────────────────────────────────────────────────

    // All skills and their governing ability
    public static final Map<String, String> SKILL_ABILITY;
    static {
        var m = new LinkedHashMap<String, String>();
        m.put("Acrobatics", "DEX");
        m.put("Animal Handling", "WIS");
        m.put("Arcana", "INT");
        m.put("Athletics", "STR");
        m.put("Deception", "CHA");
        m.put("History", "INT");
        m.put("Insight", "WIS");
        m.put("Intimidation", "CHA");
        m.put("Investigation", "INT");
        m.put("Medicine", "WIS");
        m.put("Nature", "INT");
        m.put("Perception", "WIS");
        m.put("Performance", "CHA");
        m.put("Persuasion", "CHA");
        m.put("Religion", "INT");
        m.put("Sleight of Hand", "DEX");
        m.put("Stealth", "DEX");
        m.put("Survival", "WIS");
        SKILL_ABILITY = Collections.unmodifiableMap(m);
    }

    // Race speed overrides (defaults to 30)
    private static final Map<String, Integer> RACE_SPEED = Map.ofEntries(
        Map.entry("dwarf_hill",25), Map.entry("dwarf_mountain",25), Map.entry("dwarf_duergar",25),
        Map.entry("halfling_lightfoot",25), Map.entry("halfling_stout",25), Map.entry("halfling_ghostwise",25),
        Map.entry("gnome_forest",25), Map.entry("gnome_rock",25), Map.entry("gnome_deep",25),
        Map.entry("goblin",25),
        Map.entry("elf_wood",35)
    );

    // ── Helper Methods ───────────────────────────────────────────────────────────

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