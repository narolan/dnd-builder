package com.dnd.builder.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

/**
 * Central mutable state object stored in HttpSession.
 * Every field must be Jackson-serializable for JSON export/import.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterDraft {

    // ── Step 1: Race ─────────────────────────────────────────────────────────
    private String raceId = "";
    /** flex_<bonusIndex>_<pickIndex> → stat key (e.g. "STR") */
    private Map<String, String> flexPicks = new LinkedHashMap<>();

    // ── Step 2: Class ─────────────────────────────────────────────────────────
    private String characterClass = "";
    private String subclassId = "";
    private int level = 1;

    // ── Step 3: Background ───────────────────────────────────────────────────
    private String background = "";
    private String alignment = "";
    private String characterName = "";

    // ── Step 4: Ability Scores ───────────────────────────────────────────────
    private Map<String, Integer> baseScores = new LinkedHashMap<>(Map.of(
            "STR", 8, "DEX", 8, "CON", 8, "INT", 8, "WIS", 8, "CHA", 8));

    // ── Step 5: Skills ───────────────────────────────────────────────────────
    /** All skill proficiencies: class choices + background fixed */
    private List<String> skillProficiencies = new ArrayList<>();

    // ── Step 6: ASI/Feats (Variant Human at L1, plus levels 4, 8, 12, 16, 19, etc.) ─────────
    /** List of ASI/Feat choices made at each ASI level */
    private List<AsiChoice> asiChoices = new ArrayList<>();
    /** Legacy: Chosen feat ID for Variant Human L1 feat (kept for backwards compatibility) */
    private String chosenFeatId = "";
    /** Legacy: For feats/ASIs that grant ability score increases */
    private Map<String, Integer> featAsiChoices = new LinkedHashMap<>();

    // ── Level-Up Choices ─────────────────────────────────────────────────────
    private List<String> expertiseSkills     = new ArrayList<>();
    private String       pactBoon            = "";
    private List<String> eldritchInvocations = new ArrayList<>();
    private List<String> metamagicOptions    = new ArrayList<>();
    private List<String> favoredEnemies      = new ArrayList<>();
    private List<String> favoredTerrains     = new ArrayList<>();

    // ── Step 7: Spells ───────────────────────────────────────────────────────
    private List<String> chosenCantrips = new ArrayList<>();
    private List<String> chosenSpells   = new ArrayList<>();
    /** Wizard: all spells in spellbook (knows 6 at L1, prepares a subset) */
    private List<String> spellbookSpells = new ArrayList<>();

    // ── Step 8: Equipment ────────────────────────────────────────────────────
    /** slotId → chosen option label */
    private Map<String, String> equipmentChoices = new LinkedHashMap<>();

    // ── Campaign Inventory ───────────────────────────────────────────────────
    /** Items acquired during campaign play */
    private List<InventoryItem> inventory = new ArrayList<>();
    /** Gold pieces */
    private int gold = 0;
    /** Silver pieces */
    private int silver = 0;
    /** Copper pieces */
    private int copper = 0;
    /** Platinum pieces */
    private int platinum = 0;
    /** Electrum pieces */
    private int electrum = 0;

    // ── Session Tracking (for combat/rest) ───────────────────────────────────
    private int currentHp = -1;  // -1 means use max HP
    private int tempHp = 0;
    private int usedHitDice = 0;
    /** Spell slots used: index = spell level - 1 */
    private int[] usedSpellSlots = new int[9];

    // ── Combat State ─────────────────────────────────────────────────────────
    /** Active conditions affecting the character */
    private List<ActiveCondition> conditions = new ArrayList<>();
    /** Death save successes (0-3) */
    private int deathSaveSuccesses = 0;
    /** Death save failures (0-3) */
    private int deathSaveFailures = 0;
    /** Spell currently being concentrated on (null if none) */
    private String concentratingOn = null;

    // ── Tracking ─────────────────────────────────────────────────────────────
    private int highestStepReached = 1;

    // ── Constructors / Factory ───────────────────────────────────────────────
    public CharacterDraft() {}

    public static CharacterDraft fresh() {
        return new CharacterDraft();
    }

    // ── Derived helpers (not stored, computed at runtime) ────────────────────
    public boolean isVariantHuman() {
        return "human_variant".equals(raceId);
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getRaceId() { return raceId; }
    public void setRaceId(String raceId) { this.raceId = raceId != null ? raceId : ""; }

    public Map<String, String> getFlexPicks() { return flexPicks; }
    public void setFlexPicks(Map<String, String> flexPicks) { this.flexPicks = flexPicks != null ? flexPicks : new java.util.LinkedHashMap<>(); }

    public String getCharacterClass() { return characterClass; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass != null ? characterClass : ""; }

    public String getSubclassId() { return subclassId; }
    public void setSubclassId(String subclassId) { this.subclassId = subclassId != null ? subclassId : ""; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background != null ? background : ""; }

    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment != null ? alignment : ""; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName != null ? characterName : ""; }

    public Map<String, Integer> getBaseScores() { return baseScores; }
    public void setBaseScores(Map<String, Integer> baseScores) { this.baseScores = baseScores != null ? baseScores : new java.util.LinkedHashMap<>(); }

    public List<String> getSkillProficiencies() { return skillProficiencies; }
    public void setSkillProficiencies(List<String> skillProficiencies) { this.skillProficiencies = skillProficiencies != null ? skillProficiencies : new java.util.ArrayList<>(); }

    public List<AsiChoice> getAsiChoices() { return asiChoices; }
    public void setAsiChoices(List<AsiChoice> asiChoices) { this.asiChoices = asiChoices != null ? asiChoices : new ArrayList<>(); }

    public String getChosenFeatId() { return chosenFeatId; }
    public void setChosenFeatId(String chosenFeatId) { this.chosenFeatId = chosenFeatId != null ? chosenFeatId : ""; }

    public Map<String, Integer> getFeatAsiChoices() { return featAsiChoices; }
    public void setFeatAsiChoices(Map<String, Integer> featAsiChoices) { this.featAsiChoices = featAsiChoices != null ? featAsiChoices : new java.util.LinkedHashMap<>(); }

    public List<String> getExpertiseSkills() { return expertiseSkills; }
    public void setExpertiseSkills(List<String> expertiseSkills) { this.expertiseSkills = expertiseSkills != null ? expertiseSkills : new java.util.ArrayList<>(); }

    public String getPactBoon() { return pactBoon; }
    public void setPactBoon(String pactBoon) { this.pactBoon = pactBoon != null ? pactBoon : ""; }

    public List<String> getEldritchInvocations() { return eldritchInvocations; }
    public void setEldritchInvocations(List<String> eldritchInvocations) { this.eldritchInvocations = eldritchInvocations != null ? eldritchInvocations : new java.util.ArrayList<>(); }

    public List<String> getMetamagicOptions() { return metamagicOptions; }
    public void setMetamagicOptions(List<String> metamagicOptions) { this.metamagicOptions = metamagicOptions != null ? metamagicOptions : new java.util.ArrayList<>(); }

    public List<String> getFavoredEnemies() { return favoredEnemies; }
    public void setFavoredEnemies(List<String> favoredEnemies) { this.favoredEnemies = favoredEnemies != null ? favoredEnemies : new java.util.ArrayList<>(); }

    public List<String> getFavoredTerrains() { return favoredTerrains; }
    public void setFavoredTerrains(List<String> favoredTerrains) { this.favoredTerrains = favoredTerrains != null ? favoredTerrains : new java.util.ArrayList<>(); }

    public List<String> getChosenCantrips() { return chosenCantrips; }
    public void setChosenCantrips(List<String> chosenCantrips) { this.chosenCantrips = chosenCantrips != null ? chosenCantrips : new java.util.ArrayList<>(); }

    public List<String> getChosenSpells() { return chosenSpells; }
    public void setChosenSpells(List<String> chosenSpells) { this.chosenSpells = chosenSpells != null ? chosenSpells : new java.util.ArrayList<>(); }

    public List<String> getSpellbookSpells() { return spellbookSpells; }
    public void setSpellbookSpells(List<String> spellbookSpells) { this.spellbookSpells = spellbookSpells != null ? spellbookSpells : new java.util.ArrayList<>(); }

    public Map<String, String> getEquipmentChoices() { return equipmentChoices; }
    public void setEquipmentChoices(Map<String, String> equipmentChoices) { this.equipmentChoices = equipmentChoices != null ? equipmentChoices : new java.util.LinkedHashMap<>(); }

    public List<InventoryItem> getInventory() { return inventory; }
    public void setInventory(List<InventoryItem> inventory) { this.inventory = inventory != null ? inventory : new ArrayList<>(); }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public int getSilver() { return silver; }
    public void setSilver(int silver) { this.silver = silver; }
    public int getCopper() { return copper; }
    public void setCopper(int copper) { this.copper = copper; }
    public int getPlatinum() { return platinum; }
    public void setPlatinum(int platinum) { this.platinum = platinum; }
    public int getElectrum() { return electrum; }
    public void setElectrum(int electrum) { this.electrum = electrum; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getTempHp() { return tempHp; }
    public void setTempHp(int tempHp) { this.tempHp = tempHp; }
    public int getUsedHitDice() { return usedHitDice; }
    public void setUsedHitDice(int usedHitDice) { this.usedHitDice = usedHitDice; }
    public int[] getUsedSpellSlots() { return usedSpellSlots; }
    public void setUsedSpellSlots(int[] usedSpellSlots) { this.usedSpellSlots = usedSpellSlots != null ? usedSpellSlots : new int[9]; }

    // ── Inventory Helpers ────────────────────────────────────────────────────
    public void addItem(InventoryItem item) { this.inventory.add(item); }
    public void removeItem(String itemId) { this.inventory.removeIf(i -> i.getId().equals(itemId)); }
    public int getAttunedCount() { return (int) inventory.stream().filter(InventoryItem::isAttuned).count(); }
    public boolean canAttune() { return getAttunedCount() < 3; }

    // ── Combat State Accessors ───────────────────────────────────────────────
    public List<ActiveCondition> getConditions() { return conditions; }
    public void setConditions(List<ActiveCondition> conditions) { this.conditions = conditions != null ? conditions : new ArrayList<>(); }
    public void addCondition(ActiveCondition condition) { this.conditions.add(condition); }
    public void removeCondition(String conditionId) { this.conditions.removeIf(c -> c.getId().equals(conditionId)); }
    public boolean hasCondition(String name) { return conditions.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name)); }

    public int getDeathSaveSuccesses() { return deathSaveSuccesses; }
    public void setDeathSaveSuccesses(int deathSaveSuccesses) { this.deathSaveSuccesses = Math.max(0, Math.min(3, deathSaveSuccesses)); }
    public int getDeathSaveFailures() { return deathSaveFailures; }
    public void setDeathSaveFailures(int deathSaveFailures) { this.deathSaveFailures = Math.max(0, Math.min(3, deathSaveFailures)); }
    public void resetDeathSaves() { this.deathSaveSuccesses = 0; this.deathSaveFailures = 0; }
    public boolean isStable() { return deathSaveSuccesses >= 3; }
    public boolean isDead() { return deathSaveFailures >= 3; }

    public String getConcentratingOn() { return concentratingOn; }
    public void setConcentratingOn(String spell) { this.concentratingOn = spell; }
    public boolean isConcentrating() { return concentratingOn != null && !concentratingOn.isEmpty(); }
    public void breakConcentration() { this.concentratingOn = null; }

    public int getHighestStepReached() { return highestStepReached; }
    public void setHighestStepReached(int highestStepReached) { this.highestStepReached = highestStepReached; }
}
