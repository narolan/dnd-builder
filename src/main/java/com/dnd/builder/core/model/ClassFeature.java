package com.dnd.builder.core.model;

/**
 * Represents a class or subclass feature gained at a specific level.
 */
public record ClassFeature(
    int level,
    String name,
    String description
) {
    public static ClassFeature of(int level, String name, String description) {
        return new ClassFeature(level, name, description);
    }
}
