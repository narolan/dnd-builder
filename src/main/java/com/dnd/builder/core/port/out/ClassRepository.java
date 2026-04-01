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
}
