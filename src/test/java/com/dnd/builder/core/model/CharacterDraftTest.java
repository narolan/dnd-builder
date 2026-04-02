package com.dnd.builder.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CharacterDraft, focusing on inventory management and session tracking.
 */
class CharacterDraftTest {

    private CharacterDraft draft;

    @BeforeEach
    void setUp() {
        draft = CharacterDraft.fresh();
    }

    @Nested
    @DisplayName("Inventory Management")
    class InventoryManagement {

        @Test
        @DisplayName("Fresh draft has empty inventory")
        void freshDraftEmptyInventory() {
            assertTrue(draft.getInventory().isEmpty());
        }

        @Test
        @DisplayName("addItem adds item to inventory")
        void addItemAddsToInventory() {
            var item = new InventoryItem("Longsword", "weapon");
            draft.addItem(item);

            assertEquals(1, draft.getInventory().size());
            assertEquals("Longsword", draft.getInventory().get(0).getName());
        }

        @Test
        @DisplayName("removeItem removes item by ID")
        void removeItemById() {
            var item1 = new InventoryItem("Longsword", "weapon");
            var item2 = new InventoryItem("Shield", "armor");
            draft.addItem(item1);
            draft.addItem(item2);

            draft.removeItem(item1.getId());

            assertEquals(1, draft.getInventory().size());
            assertEquals("Shield", draft.getInventory().get(0).getName());
        }

        @Test
        @DisplayName("removeItem with invalid ID does nothing")
        void removeItemInvalidId() {
            var item = new InventoryItem("Longsword", "weapon");
            draft.addItem(item);

            draft.removeItem("invalid-id");

            assertEquals(1, draft.getInventory().size());
        }
    }

    @Nested
    @DisplayName("Attunement")
    class Attunement {

        @Test
        @DisplayName("getAttunedCount returns 0 for empty inventory")
        void emptyInventoryAttunedCount() {
            assertEquals(0, draft.getAttunedCount());
        }

        @Test
        @DisplayName("getAttunedCount counts only attuned items")
        void attunedCountIgnoresNonAttuned() {
            var item1 = new InventoryItem("Ring of Protection", "ring")
                    .withRequiresAttunement(true);
            item1.setAttuned(true);

            var item2 = new InventoryItem("Longsword", "weapon");
            item2.setAttuned(false);

            var item3 = new InventoryItem("Cloak of Elvenkind", "wondrous")
                    .withRequiresAttunement(true);
            item3.setAttuned(true);

            draft.addItem(item1);
            draft.addItem(item2);
            draft.addItem(item3);

            assertEquals(2, draft.getAttunedCount());
        }

        @Test
        @DisplayName("canAttune returns true when under 3 attuned items")
        void canAttuneUnderLimit() {
            var item = new InventoryItem("Ring of Protection", "ring")
                    .withRequiresAttunement(true);
            item.setAttuned(true);
            draft.addItem(item);

            assertTrue(draft.canAttune());
        }

        @Test
        @DisplayName("canAttune returns false when at 3 attuned items")
        void cannotAttuneAtLimit() {
            for (int i = 0; i < 3; i++) {
                var item = new InventoryItem("Magic Item " + i, "wondrous")
                        .withRequiresAttunement(true);
                item.setAttuned(true);
                draft.addItem(item);
            }

            assertFalse(draft.canAttune());
        }
    }

    @Nested
    @DisplayName("Currency")
    class Currency {

        @Test
        @DisplayName("Fresh draft has 0 of all currencies")
        void freshDraftNoCurrency() {
            assertEquals(0, draft.getGold());
            assertEquals(0, draft.getSilver());
            assertEquals(0, draft.getCopper());
            assertEquals(0, draft.getPlatinum());
            assertEquals(0, draft.getElectrum());
        }

        @Test
        @DisplayName("Currency values can be set")
        void setCurrencies() {
            draft.setGold(100);
            draft.setSilver(50);
            draft.setCopper(25);
            draft.setPlatinum(5);
            draft.setElectrum(10);

            assertEquals(100, draft.getGold());
            assertEquals(50, draft.getSilver());
            assertEquals(25, draft.getCopper());
            assertEquals(5, draft.getPlatinum());
            assertEquals(10, draft.getElectrum());
        }
    }

    @Nested
    @DisplayName("Session Tracking")
    class SessionTracking {

        @Test
        @DisplayName("Fresh draft has currentHp of -1 (uses max HP)")
        void freshDraftUnsetHp() {
            assertEquals(-1, draft.getCurrentHp());
        }

        @Test
        @DisplayName("Fresh draft has 0 temp HP")
        void freshDraftNoTempHp() {
            assertEquals(0, draft.getTempHp());
        }

        @Test
        @DisplayName("Fresh draft has 0 used hit dice")
        void freshDraftNoUsedHitDice() {
            assertEquals(0, draft.getUsedHitDice());
        }

        @Test
        @DisplayName("Fresh draft has empty used spell slots")
        void freshDraftNoUsedSlots() {
            int[] slots = draft.getUsedSpellSlots();
            assertEquals(9, slots.length);
            for (int slot : slots) {
                assertEquals(0, slot);
            }
        }

        @Test
        @DisplayName("setUsedSpellSlots updates slots")
        void setUsedSpellSlots() {
            int[] used = {1, 2, 0, 0, 0, 0, 0, 0, 0};
            draft.setUsedSpellSlots(used);

            assertEquals(1, draft.getUsedSpellSlots()[0]);
            assertEquals(2, draft.getUsedSpellSlots()[1]);
        }

        @Test
        @DisplayName("setUsedSpellSlots with null resets to empty array")
        void setUsedSpellSlotsNull() {
            draft.setUsedSpellSlots(new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1});
            draft.setUsedSpellSlots(null);

            int[] slots = draft.getUsedSpellSlots();
            assertEquals(9, slots.length);
            for (int slot : slots) {
                assertEquals(0, slot);
            }
        }
    }

    @Nested
    @DisplayName("Variant Human")
    class VariantHuman {

        @Test
        @DisplayName("isVariantHuman returns true for human_variant race")
        void isVariantHumanTrue() {
            draft.setRaceId("human_variant");
            assertTrue(draft.isVariantHuman());
        }

        @Test
        @DisplayName("isVariantHuman returns false for other races")
        void isVariantHumanFalse() {
            draft.setRaceId("human_standard");
            assertFalse(draft.isVariantHuman());

            draft.setRaceId("elf_high");
            assertFalse(draft.isVariantHuman());
        }
    }

    @Nested
    @DisplayName("Null Safety")
    class NullSafety {

        @Test
        @DisplayName("Setters handle null values safely")
        void settersHandleNull() {
            draft.setRaceId(null);
            assertEquals("", draft.getRaceId());

            draft.setCharacterClass(null);
            assertEquals("", draft.getCharacterClass());

            draft.setCharacterName(null);
            assertEquals("", draft.getCharacterName());

            draft.setSkillProficiencies(null);
            assertNotNull(draft.getSkillProficiencies());

            draft.setInventory(null);
            assertNotNull(draft.getInventory());
        }
    }
}
