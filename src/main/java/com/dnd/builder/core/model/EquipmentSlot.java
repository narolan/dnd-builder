package com.dnd.builder.core.model;

import java.util.List;

/**
 * Represents one equipment decision slot for a class (e.g., "Choose a weapon").
 * Each slot has 2-3 choices (a, b, c).
 */
public record EquipmentSlot(
    String slotId,                  // unique within class, e.g. "fighter_slot1"
    String prompt,                  // "Choose a weapon:"
    List<EquipmentChoice> choices
) {}
