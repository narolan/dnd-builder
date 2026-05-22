package com.dnd.builder.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMagicItemRepositoryTest {

    private final InMemoryMagicItemRepository repository = new InMemoryMagicItemRepository();

    @Test
    @DisplayName("findAll() returns a non-empty list")
    void findAllNonEmpty() {
        assertFalse(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll() list is unmodifiable")
    void findAllIsUnmodifiable() {
        var items = repository.findAll();
        assertThrows(UnsupportedOperationException.class,
                () -> items.add(new InMemoryMagicItemRepository.MagicItemTemplate(
                        "test", "Test", "Test", "potion", "common",
                        false, "desc", 0, 0, 0, null, 0, 0)));
    }

    @Test
    @DisplayName("findById() returns Potion of Healing")
    void findByIdPotionOfHealing() {
        var item = repository.findById("potion_healing");

        assertNotNull(item);
        assertEquals("potion_healing", item.id());
        assertEquals("Potion of Healing", item.name());
        assertEquals("common", item.rarity());
    }

    @Test
    @DisplayName("findById() returns null for unknown id")
    void findByIdUnknown() {
        assertNull(repository.findById("nonexistent_item_xyz"));
    }

    @Test
    @DisplayName("search() with empty query returns all items")
    void searchEmptyReturnsAll() {
        var results = repository.search("");
        assertEquals(repository.findAll().size(), results.size());
    }

    @Test
    @DisplayName("search() with null query returns all items")
    void searchNullReturnsAll() {
        var results = repository.search(null);
        assertEquals(repository.findAll().size(), results.size());
    }

    @Test
    @DisplayName("search() matches by name (case-insensitive)")
    void searchByNameCaseInsensitive() {
        var results = repository.search("potion");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(i ->
                i.name().toLowerCase().contains("potion")
                || i.category().toLowerCase().contains("potion")
                || i.rarity().toLowerCase().contains("potion")));
    }

    @Test
    @DisplayName("search() matches by rarity")
    void searchByRarity() {
        var results = repository.search("legendary");
        assertFalse(results.isEmpty(), "Expected at least one legendary item");
        assertTrue(results.stream().anyMatch(i -> "legendary".equals(i.rarity())));
    }

    @Test
    @DisplayName("search() returns empty for non-matching query")
    void searchNoMatch() {
        var results = repository.search("xyzzy_no_match_ever_12345");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("All items have non-blank id, name, category, and rarity")
    void allItemsWellFormed() {
        for (var item : repository.findAll()) {
            assertFalse(item.id().isBlank(), "id must not be blank");
            assertFalse(item.name().isBlank(), "name must not be blank");
            assertFalse(item.category().isBlank(), "category must not be blank");
            assertFalse(item.rarity().isBlank(), "rarity must not be blank");
        }
    }

    @Test
    @DisplayName("All item ids are unique")
    void allItemIdsUnique() {
        var items = repository.findAll();
        long distinct = items.stream().map(InMemoryMagicItemRepository.MagicItemTemplate::id).distinct().count();
        assertEquals(items.size(), distinct, "Duplicate item ids detected");
    }

    @Test
    @DisplayName("findAll() contains at least 30 items")
    void minimumItemCount() {
        assertTrue(repository.findAll().size() >= 30, "Expected at least 30 magic items");
    }

    @Test
    @DisplayName("Bag of Holding requires attunement is false")
    void bagOfHoldingNoAttunement() {
        var item = repository.findById("bag_of_holding");
        assertNotNull(item, "bag_of_holding must exist");
        assertFalse(item.requiresAttunement());
    }

    @Test
    @DisplayName("Ring of Protection requires attunement")
    void ringOfProtectionRequiresAttunement() {
        var item = repository.findById("ring_of_protection");
        assertNotNull(item, "ring_of_protection must exist");
        assertTrue(item.requiresAttunement());
    }
}
