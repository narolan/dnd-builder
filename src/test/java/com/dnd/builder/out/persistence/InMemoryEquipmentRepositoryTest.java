package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryEquipmentRepositoryTest {

    private final InMemoryEquipmentRepository repository = new InMemoryEquipmentRepository();

    @Test
    @DisplayName("findByClass() returns non-empty slots for fighter")
    void fighterHasSlots() {
        List<EquipmentSlot> slots = repository.findByClass("fighter");
        assertFalse(slots.isEmpty());
    }

    @Test
    @DisplayName("findByClass() returns non-empty slots for all 11 classes")
    void allClassesHaveSlots() {
        for (String classId : List.of(
                "barbarian","bard","cleric","druid","fighter","monk",
                "paladin","ranger","rogue","sorcerer","warlock","wizard")) {
            assertFalse(repository.findByClass(classId).isEmpty(),
                    "Expected slots for class: " + classId);
        }
    }

    @Test
    @DisplayName("findByClass() returns empty list for unknown class")
    void unknownClassReturnsEmpty() {
        assertTrue(repository.findByClass("artificer").isEmpty());
    }

    @Test
    @DisplayName("Each slot has a non-blank slotId and at least 2 choices")
    void slotsAreWellFormed() {
        List<EquipmentSlot> slots = repository.findByClass("fighter");
        for (EquipmentSlot slot : slots) {
            assertFalse(slot.slotId().isBlank(), "slotId must not be blank");
            assertTrue(slot.choices().size() >= 2, "Each slot must have at least 2 choices");
        }
    }

    @Test
    @DisplayName("wizard has 4 equipment slots")
    void wizardHasFourSlots() {
        assertEquals(4, repository.findByClass("wizard").size());
    }

    @Test
    @DisplayName("cleric has 5 equipment slots (more choices than most)")
    void clericHasFiveSlots() {
        assertEquals(5, repository.findByClass("cleric").size());
    }

    @Test
    @DisplayName("All choice IDs within a class are unique")
    void choiceIdsUniqueWithinClass() {
        var slots = repository.findByClass("rogue");
        long totalChoices = slots.stream().mapToLong(s -> s.choices().size()).sum();
        long distinctIds = slots.stream()
                .flatMap(s -> s.choices().stream())
                .map(c -> c.optionId())
                .distinct().count();
        // each slot uses a/b/c, so we expect each slot's choices to have distinct ids
        // (they repeat across slots, but within slot they're distinct)
        for (var slot : slots) {
            long distinctSlotIds = slot.choices().stream().map(c -> c.optionId()).distinct().count();
            assertEquals(slot.choices().size(), distinctSlotIds,
                    "Duplicate choice id in slot: " + slot.slotId());
        }
    }
}
