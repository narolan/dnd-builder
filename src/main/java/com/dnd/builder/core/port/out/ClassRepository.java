package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.ClassDefinition;
import java.util.List;

public interface ClassRepository {
    List<ClassDefinition> findAll();
    ClassDefinition findById(String id);

    // Static utility methods for spell slot calculations
    static int proficiencyBonus(int level) {
        return 2 + (level - 1) / 4;
    }

    static int[] fullCasterSlots(int level) {
        return switch (level) {
            case 1  -> new int[]{2,0,0,0,0,0,0,0,0};
            case 2  -> new int[]{3,0,0,0,0,0,0,0,0};
            case 3  -> new int[]{4,2,0,0,0,0,0,0,0};
            case 4  -> new int[]{4,3,0,0,0,0,0,0,0};
            case 5  -> new int[]{4,3,2,0,0,0,0,0,0};
            case 6  -> new int[]{4,3,3,0,0,0,0,0,0};
            case 7  -> new int[]{4,3,3,1,0,0,0,0,0};
            case 8  -> new int[]{4,3,3,2,0,0,0,0,0};
            case 9  -> new int[]{4,3,3,3,1,0,0,0,0};
            case 10 -> new int[]{4,3,3,3,2,0,0,0,0};
            case 11,12 -> new int[]{4,3,3,3,2,1,0,0,0};
            case 13,14 -> new int[]{4,3,3,3,2,1,1,0,0};
            case 15,16 -> new int[]{4,3,3,3,2,1,1,1,0};
            case 17    -> new int[]{4,3,3,3,2,1,1,1,1};
            case 18    -> new int[]{4,3,3,3,3,1,1,1,1};
            case 19    -> new int[]{4,3,3,3,3,2,1,1,1};
            case 20    -> new int[]{4,3,3,3,3,2,2,1,1};
            default    -> new int[]{0,0,0,0,0,0,0,0,0};
        };
    }

    static int[] warlockSlots(int level) {
        int numSlots = level < 2 ? 1 : level < 11 ? 2 : level < 17 ? 3 : 4;
        int slotLevel = level < 3 ? 1 : level < 5 ? 2 : level < 7 ? 3 : level < 9 ? 4 : 5;
        return new int[]{numSlots, slotLevel};
    }

    // ── Spell Scaling Methods ─────────────────────────────────────────────────

    /** Max spell level a character can cast based on class type and level */
    static int maxSpellLevel(String classId, int level) {
        if (classId == null) return 0;
        return switch (classId) {
            // Full casters: spell level = (character level + 1) / 2, max 9
            case "bard", "cleric", "druid", "sorcerer", "wizard" ->
                Math.min(9, (level + 1) / 2);
            // Warlock: pact magic, max 5th level spells
            case "warlock" -> level < 3 ? 1 : level < 5 ? 2 : level < 7 ? 3 : level < 9 ? 4 : 5;
            // Half casters: spell level = (character level - 1) / 2, max 5
            case "paladin", "ranger" -> level < 2 ? 0 : Math.min(5, level / 4 + 1);
            // Non-casters
            default -> 0;
        };
    }

    /** Cantrips known by level */
    static int cantripsKnown(String classId, int level) {
        if (classId == null) return 0;
        return switch (classId) {
            case "bard" -> level < 4 ? 2 : level < 10 ? 3 : 4;
            case "cleric" -> level < 4 ? 3 : level < 10 ? 4 : 5;
            case "druid" -> level < 4 ? 2 : level < 10 ? 3 : 4;
            case "sorcerer" -> level < 4 ? 4 : level < 10 ? 5 : 6;
            case "warlock" -> level < 4 ? 2 : level < 10 ? 3 : 4;
            case "wizard" -> level < 4 ? 3 : level < 10 ? 4 : 5;
            default -> 0;
        };
    }

    /** Spells known for "known" casters (not prepared casters) */
    static int spellsKnown(String classId, int level) {
        if (classId == null) return 0;
        return switch (classId) {
            case "bard" -> level + 3; // 4 at L1, +1 per level (with swaps)
            case "sorcerer" -> level + 1; // 2 at L1, +1 per level
            case "warlock" -> level + 1; // 2 at L1, +1 per level
            case "ranger" -> level < 2 ? 0 : level < 3 ? 2 : level < 5 ? 3 : level < 7 ? 4 :
                level < 9 ? 5 : level < 11 ? 6 : level < 13 ? 7 : level < 15 ? 8 :
                level < 17 ? 9 : level < 19 ? 10 : 11;
            default -> 0; // Prepared casters don't use spells known
        };
    }

    /** Max spells that can be prepared (for prepared casters) */
    static int maxPrepared(String classId, int level, int abilityMod) {
        if (classId == null) return 0;
        return switch (classId) {
            case "cleric", "druid" -> Math.max(1, level + abilityMod);
            case "paladin" -> Math.max(1, level / 2 + abilityMod);
            case "wizard" -> Math.max(1, level + abilityMod); // From spellbook
            default -> 0;
        };
    }

    /** Half caster spell slots (paladin, ranger) */
    static int[] halfCasterSlots(int level) {
        if (level < 2) return new int[]{0,0,0,0,0};
        return switch (level) {
            case 2 -> new int[]{2,0,0,0,0};
            case 3, 4 -> new int[]{3,0,0,0,0};
            case 5, 6 -> new int[]{4,2,0,0,0};
            case 7, 8 -> new int[]{4,3,0,0,0};
            case 9, 10 -> new int[]{4,3,2,0,0};
            case 11, 12 -> new int[]{4,3,3,0,0};
            case 13, 14 -> new int[]{4,3,3,1,0};
            case 15, 16 -> new int[]{4,3,3,2,0};
            case 17, 18 -> new int[]{4,3,3,3,1};
            case 19, 20 -> new int[]{4,3,3,3,2};
            default -> new int[]{0,0,0,0,0};
        };
    }
}
