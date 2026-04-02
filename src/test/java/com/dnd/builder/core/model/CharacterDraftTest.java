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

    @Nested
    @DisplayName("Conditions")
    class Conditions {

        @Test
        @DisplayName("Fresh draft has no conditions")
        void freshDraftNoConditions() {
            assertTrue(draft.getConditions().isEmpty());
        }

        @Test
        @DisplayName("addCondition adds to list")
        void addCondition() {
            var condition = new ActiveCondition("Poisoned");
            draft.addCondition(condition);

            assertEquals(1, draft.getConditions().size());
            assertEquals("Poisoned", draft.getConditions().get(0).getName());
        }

        @Test
        @DisplayName("removeCondition removes by ID")
        void removeCondition() {
            var cond1 = new ActiveCondition("Poisoned");
            var cond2 = new ActiveCondition("Stunned");
            draft.addCondition(cond1);
            draft.addCondition(cond2);

            draft.removeCondition(cond1.getId());

            assertEquals(1, draft.getConditions().size());
            assertEquals("Stunned", draft.getConditions().get(0).getName());
        }

        @Test
        @DisplayName("hasCondition checks by name")
        void hasCondition() {
            draft.addCondition(new ActiveCondition("Poisoned"));

            assertTrue(draft.hasCondition("Poisoned"));
            assertTrue(draft.hasCondition("poisoned")); // case insensitive
            assertFalse(draft.hasCondition("Stunned"));
        }
    }

    @Nested
    @DisplayName("Death Saves")
    class DeathSaves {

        @Test
        @DisplayName("Fresh draft has 0 death saves")
        void freshDraftNoDeathSaves() {
            assertEquals(0, draft.getDeathSaveSuccesses());
            assertEquals(0, draft.getDeathSaveFailures());
        }

        @Test
        @DisplayName("Death saves are clamped to 0-3")
        void deathSavesClamped() {
            draft.setDeathSaveSuccesses(5);
            assertEquals(3, draft.getDeathSaveSuccesses());

            draft.setDeathSaveFailures(-1);
            assertEquals(0, draft.getDeathSaveFailures());
        }

        @Test
        @DisplayName("isStable returns true at 3 successes")
        void isStable() {
            draft.setDeathSaveSuccesses(2);
            assertFalse(draft.isStable());

            draft.setDeathSaveSuccesses(3);
            assertTrue(draft.isStable());
        }

        @Test
        @DisplayName("isDead returns true at 3 failures")
        void isDead() {
            draft.setDeathSaveFailures(2);
            assertFalse(draft.isDead());

            draft.setDeathSaveFailures(3);
            assertTrue(draft.isDead());
        }

        @Test
        @DisplayName("resetDeathSaves resets both")
        void resetDeathSaves() {
            draft.setDeathSaveSuccesses(2);
            draft.setDeathSaveFailures(1);

            draft.resetDeathSaves();

            assertEquals(0, draft.getDeathSaveSuccesses());
            assertEquals(0, draft.getDeathSaveFailures());
        }
    }

    @Nested
    @DisplayName("Concentration")
    class Concentration {

        @Test
        @DisplayName("Fresh draft has no concentration")
        void freshDraftNoConcentration() {
            assertNull(draft.getConcentratingOn());
            assertFalse(draft.isConcentrating());
        }

        @Test
        @DisplayName("setConcentratingOn sets spell")
        void setConcentration() {
            draft.setConcentratingOn("Haste");

            assertEquals("Haste", draft.getConcentratingOn());
            assertTrue(draft.isConcentrating());
        }

        @Test
        @DisplayName("breakConcentration clears spell")
        void breakConcentration() {
            draft.setConcentratingOn("Haste");
            draft.breakConcentration();

            assertNull(draft.getConcentratingOn());
            assertFalse(draft.isConcentrating());
        }

        @Test
        @DisplayName("Empty string counts as not concentrating")
        void emptyStringNotConcentrating() {
            draft.setConcentratingOn("");
            assertFalse(draft.isConcentrating());
        }
    }
}
