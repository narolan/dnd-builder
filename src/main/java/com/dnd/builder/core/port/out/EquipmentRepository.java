package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.EquipmentSlot;
import java.util.List;

public interface EquipmentRepository {
    List<EquipmentSlot> findByClass(String classId);
}
