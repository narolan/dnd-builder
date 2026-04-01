package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.BackgroundDefinition;
import java.util.List;

public interface BackgroundRepository {
    List<BackgroundDefinition> findAll();
    BackgroundDefinition findById(String id);
}
