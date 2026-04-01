package com.dnd.builder.model;

import java.util.List;
import java.util.Map;

/**
 * All values derived from CharacterDraft — never stored in session,
 * always recomputed fresh by CharacterCalculator.
 */
public class DerivedStats {

    // ── Scores & modifiers ───────────────────────────────────────────────────
    /** Final ability scores after base + racial bonuses */
    private Map<String, Integer> finalScores;
    /** Modifiers keyed by stat */
    private Map<String, Integer> modifiers;

    // ── Core stats ───────────────────────────────────────────────────────────
    private int proficiencyBonus;
    private int initiative;           // DEX modifier
    private int passivePerception;    // 10 + WIS mod (+5 if proficient)
    private int speed;
    private int armorClass;
    private int maxHitPoints;
    private int hitDice;              // number = level, die size from class

    // ── Saving throws ────────────────────────────────────────────────────────
    /** Map of stat → total saving throw bonus */
    private Map<String, Integer> savingThrows;
    /** Stats with proficiency */
    private List<String> savingThrowProficiencies;

    // ── Skills ───────────────────────────────────────────────────────────────
    /** Skill name → total bonus */
    private Map<String, Integer> skillBonuses;
    /** Skill names with proficiency */
    private List<String> allSkillProficiencies;

    // ── Spellcasting ─────────────────────────────────────────────────────────
    private int spellSaveDC;
    private int spellAttackBonus;
    private String spellcastingAbility;
    private boolean isSpellcaster;
    private String spellSlotSummary;  // e.g. "2 × 1st-level"

    // ── All proficiencies summary ─────────────────────────────────────────────
    private List<String> armorProficiencies;
    private List<String> weaponProficiencies;
    private List<String> toolProficiencies;
    private List<String> languages;

    // ── Equipment summary ────────────────────────────────────────────────────
    private List<String> equipmentSummary;
    private String backgroundEquipment;

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Map<String, Integer> getFinalScores() { return finalScores; }
    public void setFinalScores(Map<String, Integer> f) { this.finalScores = f; }
    public Map<String, Integer> getModifiers() { return modifiers; }
    public void setModifiers(Map<String, Integer> m) { this.modifiers = m; }
    public int getProficiencyBonus() { return proficiencyBonus; }
    public void setProficiencyBonus(int p) { this.proficiencyBonus = p; }
    public int getInitiative() { return initiative; }
    public void setInitiative(int i) { this.initiative = i; }
    public int getPassivePerception() { return passivePerception; }
    public void setPassivePerception(int p) { this.passivePerception = p; }
    public int getSpeed() { return speed; }
    public void setSpeed(int s) { this.speed = s; }
    public int getArmorClass() { return armorClass; }
    public void setArmorClass(int a) { this.armorClass = a; }
    public int getMaxHitPoints() { return maxHitPoints; }
    public void setMaxHitPoints(int m) { this.maxHitPoints = m; }
    public int getHitDice() { return hitDice; }
    public void setHitDice(int h) { this.hitDice = h; }
    public Map<String, Integer> getSavingThrows() { return savingThrows; }
    public void setSavingThrows(Map<String, Integer> s) { this.savingThrows = s; }
    public List<String> getSavingThrowProficiencies() { return savingThrowProficiencies; }
    public void setSavingThrowProficiencies(List<String> s) { this.savingThrowProficiencies = s; }
    public Map<String, Integer> getSkillBonuses() { return skillBonuses; }
    public void setSkillBonuses(Map<String, Integer> s) { this.skillBonuses = s; }
    public List<String> getAllSkillProficiencies() { return allSkillProficiencies; }
    public void setAllSkillProficiencies(List<String> a) { this.allSkillProficiencies = a; }
    public int getSpellSaveDC() { return spellSaveDC; }
    public void setSpellSaveDC(int s) { this.spellSaveDC = s; }
    public int getSpellAttackBonus() { return spellAttackBonus; }
    public void setSpellAttackBonus(int s) { this.spellAttackBonus = s; }
    public String getSpellcastingAbility() { return spellcastingAbility; }
    public void setSpellcastingAbility(String s) { this.spellcastingAbility = s; }
    public boolean isSpellcaster() { return isSpellcaster; }
    public void setSpellcaster(boolean s) { this.isSpellcaster = s; }
    public String getSpellSlotSummary() { return spellSlotSummary; }
    public void setSpellSlotSummary(String s) { this.spellSlotSummary = s; }
    public List<String> getArmorProficiencies() { return armorProficiencies; }
    public void setArmorProficiencies(List<String> a) { this.armorProficiencies = a; }
    public List<String> getWeaponProficiencies() { return weaponProficiencies; }
    public void setWeaponProficiencies(List<String> w) { this.weaponProficiencies = w; }
    public List<String> getToolProficiencies() { return toolProficiencies; }
    public void setToolProficiencies(List<String> t) { this.toolProficiencies = t; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> l) { this.languages = l; }
    public List<String> getEquipmentSummary() { return equipmentSummary; }
    public void setEquipmentSummary(List<String> e) { this.equipmentSummary = e; }
    public String getBackgroundEquipment() { return backgroundEquipment; }
    public void setBackgroundEquipment(String b) { this.backgroundEquipment = b; }
}
