package com.dnd.builder.core.model;

/**
 * One option within an EquipmentSlot (e.g., option "a" = "Rapier").
 */
public record EquipmentChoice(
    String optionId,   // "a", "b", "c"
    String label       // Full display string
) {}
