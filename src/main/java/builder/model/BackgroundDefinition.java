package com.dnd.builder.model;

import java.util.List;

/**
 * PHB Background definition. Provides fixed skill proficiencies, tool profs,
 * languages, equipment, and a feature.
 */
public class BackgroundDefinition {
    private String id;
    private String name;
    /** Always exactly 2 skills */
    private List<String> skillProficiencies;
    /** Tool proficiencies, e.g. "Thieves' tools", "Disguise kit" */
    private List<String> toolProficiencies;
    /** Number of additional languages */
    private int bonusLanguages;
    /** Starting equipment description */
    private String equipment;
    /** Name of background feature */
    private String featureName;
    /** Short description of background feature */
    private String featureDesc;
    private String personality;
    private int gold;

    public BackgroundDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getSkillProficiencies() { return skillProficiencies; }
    public void setSkillProficiencies(List<String> s) { this.skillProficiencies = s; }
    public List<String> getToolProficiencies() { return toolProficiencies; }
    public void setToolProficiencies(List<String> t) { this.toolProficiencies = t; }
    public int getBonusLanguages() { return bonusLanguages; }
    public void setBonusLanguages(int b) { this.bonusLanguages = b; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String e) { this.equipment = e; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String f) { this.featureName = f; }
    public String getFeatureDesc() { return featureDesc; }
    public void setFeatureDesc(String f) { this.featureDesc = f; }
    public String getPersonality() { return personality; }
    public void setPersonality(String p) { this.personality = p; }
    public int getGold() { return gold; }
    public void setGold(int g) { this.gold = g; }
}
