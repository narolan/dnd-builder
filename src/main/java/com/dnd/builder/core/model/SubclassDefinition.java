package com.dnd.builder.core.model;

import java.util.List;

/**
 * Definition of a class subclass (e.g., Champion Fighter, Life Cleric).
 */
public record SubclassDefinition(
    String id,
    String name,
    String description,
    List<String> bonusSpells,       // Extra spells granted by subclass (Cleric domains, etc.)
    List<ClassFeature> features     // Subclass features by level
) {
    /** Convenience constructor for subclasses without features or bonus spells. */
    public SubclassDefinition(String id, String name, String description) {
        this(id, name, description, null, null);
    }

    /** Convenience constructor for subclasses with bonus spells but no feature list. */
    public SubclassDefinition(String id, String name, String description, List<String> bonusSpells) {
        this(id, name, description, bonusSpells, null);
    }
}
