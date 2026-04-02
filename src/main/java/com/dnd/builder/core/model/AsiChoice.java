package com.dnd.builder.core.model;

import java.util.Map;

/**
 * Represents a single ASI (Ability Score Improvement) or Feat choice at a given level.
 */
public record AsiChoice(
    int level,                      // Level at which this choice was made (4, 8, 12, etc.)
    String type,                    // "asi" or "feat"
    String featId,                  // If type="feat", the feat ID; otherwise empty
    Map<String, Integer> statIncreases  // If type="asi", e.g., {"STR": 2} or {"DEX": 1, "CON": 1}
) {
    /** Constructor for ASI choice (+2 to one stat or +1/+1 to two) */
    public static AsiChoice asi(int level, Map<String, Integer> stats) {
        return new AsiChoice(level, "asi", "", stats);
    }

    /** Constructor for feat choice */
    public static AsiChoice feat(int level, String featId, Map<String, Integer> bonusStats) {
        return new AsiChoice(level, "feat", featId, bonusStats != null ? bonusStats : Map.of());
    }
}
