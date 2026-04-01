package com.dnd.builder.core.model;

import java.util.List;

/**
 * Definition of a class subclass (e.g., Champion Fighter, Life Cleric).
 */
public record SubclassDefinition(
    String id,
    String name,
    String description,
    List<String> bonusSpells   // Extra spells granted by subclass (Cleric domains, etc.)
) {
    /** Convenience constructor for subclasses without bonus spells. */
    public SubclassDefinition(String id, String name, String description) {
        this(id, name, description, null);
    }
}
