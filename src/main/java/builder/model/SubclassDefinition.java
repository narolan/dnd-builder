package com.dnd.builder.model;

public class SubclassDefinition {
    private String id;
    private String name;
    private String description;
    /** Extra spell list granted by subclass (Cleric domains, etc.) */
    private java.util.List<String> bonusSpells;

    public SubclassDefinition() {}
    public SubclassDefinition(String id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public java.util.List<String> getBonusSpells() { return bonusSpells; }
    public void setBonusSpells(java.util.List<String> b) { this.bonusSpells = b; }
}
