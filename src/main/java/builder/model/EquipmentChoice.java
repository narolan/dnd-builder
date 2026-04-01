package com.dnd.builder.model;

/**
 * One option within an EquipmentSlot (e.g., option "a" = "Rapier").
 */
public class EquipmentChoice {
    private String optionId;   // "a", "b", "c"
    private String label;      // Full display string

    public EquipmentChoice() {}
    public EquipmentChoice(String optionId, String label) {
        this.optionId = optionId; this.label = label;
    }

    public String getOptionId() { return optionId; }
    public void setOptionId(String optionId) { this.optionId = optionId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
