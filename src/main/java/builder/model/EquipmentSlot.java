package com.dnd.builder.model;

import java.util.List;

/**
 * Represents one equipment decision slot for a class (e.g., "Choose a weapon").
 * Each slot has 2–3 choices (a, b, c).
 */
public class EquipmentSlot {
    private String slotId;       // unique within class, e.g. "fighter_slot1"
    private String prompt;       // "Choose a weapon:"
    private List<EquipmentChoice> choices;

    public EquipmentSlot() {}
    public EquipmentSlot(String slotId, String prompt, List<EquipmentChoice> choices) {
        this.slotId = slotId; this.prompt = prompt; this.choices = choices;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public List<EquipmentChoice> getChoices() { return choices; }
    public void setChoices(List<EquipmentChoice> choices) { this.choices = choices; }
}
