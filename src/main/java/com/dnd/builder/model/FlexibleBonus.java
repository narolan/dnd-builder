package com.dnd.builder.model;

import java.util.List;

/**
 * Represents a flexible ability score bonus that the player can assign
 * (e.g., Half-Elf's +1 to two abilities other than CHA).
 */
public record FlexibleBonus(
    int count,                    // How many stats to pick
    int amount,                   // Bonus amount per pick
    List<String> excludedStats,   // Stats that cannot be chosen
    String description            // Display text
) {}
