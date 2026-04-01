package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.FeatDefinition;
import java.util.List;

public interface FeatRepository {
    List<FeatDefinition> findAll();
    FeatDefinition findById(String id);
}
