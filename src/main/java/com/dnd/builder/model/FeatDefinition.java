package com.dnd.builder.model;

import java.util.List;

/**
 * A PHB feat.
 */
public class FeatDefinition {
    private String id;
    private String name;
    private String description;     // Full effect summary
    /** Prerequisite description (empty string = no prereq) */
    private String prerequisite;
    /** Stats this feat increases by +1 (if any) */
    private List<String> asiBonus;
    /** How many of those ASI increases the player assigns (for feats like Resilient) */
    private int asiChoiceCount;
    /** Ability score required to take this feat (key → minimum value) */
    private java.util.Map<String, Integer> requiredScore;

    public FeatDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getPrerequisite() { return prerequisite; }
    public void setPrerequisite(String p) { this.prerequisite = p; }
    public List<String> getAsiBonus() { return asiBonus; }
    public void setAsiBonus(List<String> a) { this.asiBonus = a; }
    public int getAsiChoiceCount() { return asiChoiceCount; }
    public void setAsiChoiceCount(int a) { this.asiChoiceCount = a; }
    public java.util.Map<String, Integer> getRequiredScore() { return requiredScore; }
    public void setRequiredScore(java.util.Map<String, Integer> r) { this.requiredScore = r; }
}
