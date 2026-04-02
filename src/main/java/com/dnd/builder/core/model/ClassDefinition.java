package com.dnd.builder.core.model;

import java.util.List;

/**
 * Full definition of a D&D 5e (2014) class.
 */
public class ClassDefinition {

    private String id;
    private String name;
    private int hitDie;

    /** Two saving throw proficiencies (ability score keys) */
    private List<String> savingThrows;

    /** E.g. "Light armor", "Medium armor", "Shields", "All armor" */
    private List<String> armorProficiencies;
    /** E.g. "Simple weapons", "Martial weapons", "Handcrossbows", "Longswords" */
    private List<String> weaponProficiencies;
    /** Tool proficiencies granted by class */
    private List<String> toolProficiencies;

    /** Number of skills the player picks from skillList */
    private int skillChoiceCount;
    /** Available skills to choose from */
    private List<String> skillList;

    /** null → non-caster; otherwise contains casting details */
    private SpellcastingInfo spellcasting;

    /** Primary ability for this class (used for various recommendations) */
    private String primaryAbility;

    /** All available subclasses */
    private List<SubclassDefinition> subclasses;

    /** Level at which subclass is chosen (1 or 3 for most) */
    private int subclassLevel;
    /** Human-readable note, e.g. "Choose Divine Domain at level 1" */
    private String subclassNote;

    // Starting wealth (gold pieces) as alternative to equipment
    private int startingGold;

    /** Levels at which ASI/Feat choice is granted (e.g., [4,8,12,16,19] for most classes) */
    private List<Integer> asiLevels;

    /** Class features by level */
    private List<ClassFeature> features;

    public ClassDefinition() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHitDie() { return hitDie; }
    public void setHitDie(int hitDie) { this.hitDie = hitDie; }
    public List<String> getSavingThrows() { return savingThrows; }
    public void setSavingThrows(List<String> s) { this.savingThrows = s; }
    public List<String> getArmorProficiencies() { return armorProficiencies; }
    public void setArmorProficiencies(List<String> a) { this.armorProficiencies = a; }
    public List<String> getWeaponProficiencies() { return weaponProficiencies; }
    public void setWeaponProficiencies(List<String> w) { this.weaponProficiencies = w; }
    public List<String> getToolProficiencies() { return toolProficiencies; }
    public void setToolProficiencies(List<String> t) { this.toolProficiencies = t; }
    public int getSkillChoiceCount() { return skillChoiceCount; }
    public void setSkillChoiceCount(int s) { this.skillChoiceCount = s; }
    public List<String> getSkillList() { return skillList; }
    public void setSkillList(List<String> s) { this.skillList = s; }
    public SpellcastingInfo getSpellcasting() { return spellcasting; }
    public void setSpellcasting(SpellcastingInfo s) { this.spellcasting = s; }
    public String getPrimaryAbility() { return primaryAbility; }
    public void setPrimaryAbility(String p) { this.primaryAbility = p; }
    public List<SubclassDefinition> getSubclasses() { return subclasses; }
    public void setSubclasses(List<SubclassDefinition> s) { this.subclasses = s; }
    public int getSubclassLevel() { return subclassLevel; }
    public void setSubclassLevel(int s) { this.subclassLevel = s; }
    public String getSubclassNote() { return subclassNote; }
    public void setSubclassNote(String s) { this.subclassNote = s; }
    public int getStartingGold() { return startingGold; }
    public void setStartingGold(int g) { this.startingGold = g; }
    public List<Integer> getAsiLevels() { return asiLevels; }
    public void setAsiLevels(List<Integer> a) { this.asiLevels = a; }
    public List<ClassFeature> getFeatures() { return features; }
    public void setFeatures(List<ClassFeature> f) { this.features = f; }

    // ── Inner: SpellcastingInfo ───────────────────────────────────────────────
    public static class SpellcastingInfo {
        /** "full", "half", "warlock", "third" */
        private String type;
        /** "INT", "WIS", or "CHA" */
        private String ability;
        /** true = Cleric/Druid/Wizard/Paladin/Ranger style prepared spells */
        private boolean prepareSpells;
        /** How many cantrips known at level 1 */
        private int cantripsAtL1;
        /** Spell slots at level 1: list of [slot level, count] pairs */
        private List<int[]> slotsAtL1;
        /** Known spells at level 1 (for known-casters like Bard, Sorcerer, Warlock) */
        private int spellsKnownAtL1;
        /**
         * For prepared casters: "ability" means prepared = abilityMod + level.
         * For Wizard: starts with 6 spells in spellbook.
         */
        private int spellbookSizeAtL1;

        public SpellcastingInfo() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAbility() { return ability; }
        public void setAbility(String ability) { this.ability = ability; }
        public boolean isPrepareSpells() { return prepareSpells; }
        public void setPrepareSpells(boolean p) { this.prepareSpells = p; }
        public int getCantripsAtL1() { return cantripsAtL1; }
        public void setCantripsAtL1(int c) { this.cantripsAtL1 = c; }
        public List<int[]> getSlotsAtL1() { return slotsAtL1; }
        public void setSlotsAtL1(List<int[]> s) { this.slotsAtL1 = s; }
        public int getSpellsKnownAtL1() { return spellsKnownAtL1; }
        public void setSpellsKnownAtL1(int s) { this.spellsKnownAtL1 = s; }
        public int getSpellbookSizeAtL1() { return spellbookSizeAtL1; }
        public void setSpellbookSizeAtL1(int s) { this.spellbookSizeAtL1 = s; }
    }
}
