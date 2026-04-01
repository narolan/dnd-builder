package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.SpellDefinition;
import java.util.List;

public interface SpellRepository {
    SpellDefinition findById(String id);
    List<SpellDefinition> findByClass(String classId, Integer maxLevel);
    List<SpellDefinition> findCantripsForClass(String classId);
    List<SpellDefinition> findLevel1ForClass(String classId);
}
