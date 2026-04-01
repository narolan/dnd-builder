package com.dnd.builder.model;

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

    // ── Step 6: Feats (Variant Human only at L1, or L4+ ASI choice) ─────────
    /** Chosen feat ID, empty if took ASI instead */
    private String chosenFeatId = "";
    /** For feats/ASIs that grant ability score increases */
    private Map<String, Integer> featAsiChoices = new LinkedHashMap<>();

    // ── Step 7: Spells ───────────────────────────────────────────────────────
    private List<String> chosenCantrips = new ArrayList<>();
    private List<String> chosenSpells   = new ArrayList<>();
    /** Wizard: all spells in spellbook (knows 6 at L1, prepares a subset) */
    private List<String> spellbookSpells = new ArrayList<>();

    // ── Step 8: Equipment ────────────────────────────────────────────────────
    /** slotId → chosen option label */
    private Map<String, String> equipmentChoices = new LinkedHashMap<>();

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

    public String getChosenFeatId() { return chosenFeatId; }
    public void setChosenFeatId(String chosenFeatId) { this.chosenFeatId = chosenFeatId != null ? chosenFeatId : ""; }

    public Map<String, Integer> getFeatAsiChoices() { return featAsiChoices; }
    public void setFeatAsiChoices(Map<String, Integer> featAsiChoices) { this.featAsiChoices = featAsiChoices != null ? featAsiChoices : new java.util.LinkedHashMap<>(); }

    public List<String> getChosenCantrips() { return chosenCantrips; }
    public void setChosenCantrips(List<String> chosenCantrips) { this.chosenCantrips = chosenCantrips != null ? chosenCantrips : new java.util.ArrayList<>(); }

    public List<String> getChosenSpells() { return chosenSpells; }
    public void setChosenSpells(List<String> chosenSpells) { this.chosenSpells = chosenSpells != null ? chosenSpells : new java.util.ArrayList<>(); }

    public List<String> getSpellbookSpells() { return spellbookSpells; }
    public void setSpellbookSpells(List<String> spellbookSpells) { this.spellbookSpells = spellbookSpells != null ? spellbookSpells : new java.util.ArrayList<>(); }

    public Map<String, String> getEquipmentChoices() { return equipmentChoices; }
    public void setEquipmentChoices(Map<String, String> equipmentChoices) { this.equipmentChoices = equipmentChoices != null ? equipmentChoices : new java.util.LinkedHashMap<>(); }

    public int getHighestStepReached() { return highestStepReached; }
    public void setHighestStepReached(int highestStepReached) { this.highestStepReached = highestStepReached; }
}
