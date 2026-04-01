package com.dnd.builder.core.port.out;

import com.dnd.builder.core.model.Race;
import java.util.List;

public interface RaceRepository {
    List<Race> findAll();
    Race findById(String id);
    int getSpeed(String raceId);
}
