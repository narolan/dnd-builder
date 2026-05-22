package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.FeatDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatRepositoryTest {

    private final InMemoryFeatRepository repository = new InMemoryFeatRepository();

    @Test
    @DisplayName("findAll() returns a non-empty list")
    void findAllNonEmpty() {
        assertFalse(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll() list is unmodifiable")
    void findAllIsUnmodifiable() {
        var feats = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> feats.add(new FeatDefinition()));
    }

    @Test
    @DisplayName("findById() returns the correct feat for 'alert'")
    void findByIdAlert() {
        FeatDefinition feat = repository.findById("alert");

        assertNotNull(feat);
        assertEquals("alert", feat.getId());
        assertEquals("Alert", feat.getName());
    }

    @Test
    @DisplayName("findById() returns the correct feat for 'lucky'")
    void findByIdLucky() {
        FeatDefinition feat = repository.findById("lucky");

        assertNotNull(feat);
        assertEquals("lucky", feat.getId());
    }

    @Test
    @DisplayName("findById() returns null for unknown id")
    void findByIdUnknown() {
        assertNull(repository.findById("nonexistent_feat"));
    }

    @Test
    @DisplayName("All feats have non-blank id and name")
    void allFeatsHaveIdAndName() {
        for (FeatDefinition feat : repository.findAll()) {
            assertFalse(feat.getId().isBlank(), "Feat id must not be blank");
            assertFalse(feat.getName().isBlank(), "Feat name must not be blank: id=" + feat.getId());
        }
    }

    @Test
    @DisplayName("All feat ids are unique")
    void allFeatIdsUnique() {
        List<FeatDefinition> feats = repository.findAll();
        long distinctCount = feats.stream().map(FeatDefinition::getId).distinct().count();
        assertEquals(feats.size(), distinctCount, "Duplicate feat ids detected");
    }

    @Test
    @DisplayName("Feats with ASI bonus list have non-empty bonus list")
    void featsWithAsiBonusHaveList() {
        // 'athlete' gives +1 STR or DEX
        FeatDefinition athlete = repository.findById("athlete");
        assertNotNull(athlete);
        assertNotNull(athlete.getAsiBonus());
        assertFalse(athlete.getAsiBonus().isEmpty());
        assertTrue(athlete.getAsiBonus().contains("STR"));
    }

    @Test
    @DisplayName("Feat with min score requirement has reqScores populated")
    void featWithReqScore() {
        // inspiring_leader requires CHA 13
        FeatDefinition feat = repository.findById("inspiring_leader");
        assertNotNull(feat);
        assertNotNull(feat.getRequiredScore());
        assertEquals(13, feat.getRequiredScore().get("CHA"));
    }

    @Test
    @DisplayName("findAll() contains at least 20 feats")
    void minimumFeatCount() {
        assertTrue(repository.findAll().size() >= 20, "Expected at least 20 feats");
    }
}
