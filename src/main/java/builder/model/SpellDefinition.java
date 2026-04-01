package com.dnd.builder.model;

import java.util.List;

/**
 * A single spell from the PHB.
 */
public class SpellDefinition {
    private String id;
    private String name;
    private int level;           // 0 = cantrip
    private String school;       // Evocation, Necromancy, etc.
    private String castingTime;  // "1 action", "1 bonus action", "1 reaction"
    private String range;
    private String duration;
    private boolean concentration;
    private boolean ritual;
    private String components;   // "V, S", "V, S, M (a piece of fleece)"
    private String description;  // short description
    /** Class IDs that have this spell on their list */
    private List<String> classes;

    public SpellDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getCastingTime() { return castingTime; }
    public void setCastingTime(String ct) { this.castingTime = ct; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public boolean isConcentration() { return concentration; }
    public void setConcentration(boolean c) { this.concentration = c; }
    public boolean isRitual() { return ritual; }
    public void setRitual(boolean r) { this.ritual = r; }
    public String getComponents() { return components; }
    public void setComponents(String c) { this.components = c; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public List<String> getClasses() { return classes; }
    public void setClasses(List<String> classes) { this.classes = classes; }
}
