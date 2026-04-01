package com.dnd.builder.model;

import java.util.List;
import java.util.Map;

/**
 * Definition of a playable race with ability score bonuses.
 */
public record Race(
    String id,
    String name,
    String source,                           // PHB, MToF, VGtM, etc.
    Map<String, Integer> fixedBonuses,       // Fixed ASI bonuses (e.g., STR +2)
    List<FlexibleBonus> flexibleBonuses      // Player-chosen bonuses (e.g., Half-Elf)
) {}
