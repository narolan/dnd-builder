package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.model.DerivedStats;
import com.dnd.builder.core.model.InventoryItem;
import com.dnd.builder.core.port.out.ClassRepository;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.out.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import com.dnd.builder.out.persistence.InMemoryFeatRepository;
import com.dnd.builder.out.persistence.InMemorySpellRepository;

import java.util.List;
import java.util.Map;

import static com.dnd.builder.in.web.CharacterBuilderController.DRAFT_KEY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayModeController.
 */
class PlayModeControllerTest {

    private PlayModeController controller;
    private MockHttpSession session;
    private CharacterDraft draft;
    private CharacterCalculator calculator;
    private ClassRepository classRepository;

    @BeforeEach
    void setUp() {
        classRepository = new InMemoryClassRepository();
        calculator = new CharacterCalculator(
                new InMemoryRaceRepository(),
                (InMemoryClassRepository) classRepository,
                new InMemoryBackgroundRepository(),
                new InMemorySpellRepository(),
                new InMemoryEquipmentRepository()
        );
        controller = new PlayModeController(
                calculator,
                classRepository,
                new InMemorySpellRepository(),
                new InMemoryFeatRepository()
        );

        session = new MockHttpSession();
        draft = CharacterDraft.fresh();
        draft.setRaceId("human_standard");
        draft.setCharacterClass("fighter");
        draft.setLevel(5);
        draft.setBaseScores(Map.of("STR", 16, "DEX", 14, "CON", 14, "INT", 10, "WIS", 10, "CHA", 10));
        session.setAttribute(DRAFT_KEY, draft);
    }

    @Nested
    @DisplayName("Dashboard Access")
    class DashboardAccess {

        @Test
        @DisplayName("Redirects to step 1 if no draft")
        void redirectsWithoutDraft() {
            String result = controller.playDashboard(new MockHttpSession(), new ExtendedModelMap());
            assertEquals("redirect:/step/1", result);
        }

        @Test
        @DisplayName("Shows dashboard with valid draft")
        void showsDashboard() {
            Model model = new ExtendedModelMap();
            String result = controller.playDashboard(session, model);

            assertEquals("play/dashboard", result);
            assertTrue(model.containsAttribute("draft"));
            assertTrue(model.containsAttribute("derived"));
            assertTrue(model.containsAttribute("currentHp"));
        }
    }

    @Nested
    @DisplayName("HP Tracking")
    class HpTracking {

        @Test
        @DisplayName("heal endpoint increases HP")
        void healIncreasesHp() {
            draft.setCurrentHp(10);

            Map<String, Object> result = controller.heal(5, session);

            assertEquals(15, result.get("currentHp"));
            assertEquals(5, result.get("healed"));
        }

        @Test
        @DisplayName("heal does not exceed max HP")
        void healCapsAtMax() {
            draft.setCurrentHp(35); // Fighter level 5 with +2 CON = 44 max HP

            Map<String, Object> result = controller.heal(100, session);

            assertEquals(44, result.get("currentHp"));
        }

        @Test
        @DisplayName("damage reduces HP")
        void damageReducesHp() {
            draft.setCurrentHp(20);

            Map<String, Object> result = controller.damage(8, session);

            assertEquals(12, result.get("currentHp"));
        }

        @Test
        @DisplayName("damage is absorbed by temp HP first")
        void damageAbsorbedByTempHp() {
            draft.setCurrentHp(20);
            draft.setTempHp(10);

            Map<String, Object> result = controller.damage(7, session);

            assertEquals(20, result.get("currentHp"));
            assertEquals(3, result.get("tempHp"));
        }

        @Test
        @DisplayName("damage overflows from temp HP to regular HP")
        void damageOverflowsTempHp() {
            draft.setCurrentHp(20);
            draft.setTempHp(5);

            Map<String, Object> result = controller.damage(12, session);

            assertEquals(13, result.get("currentHp"));
            assertEquals(0, result.get("tempHp"));
        }

        @Test
        @DisplayName("HP cannot go below 0")
        void hpFloorsAtZero() {
            draft.setCurrentHp(10);

            Map<String, Object> result = controller.damage(100, session);

            assertEquals(0, result.get("currentHp"));
        }

        @Test
        @DisplayName("setTempHp sets temporary HP")
        void setTempHp() {
            Map<String, Object> result = controller.setTempHp(15, session);

            assertEquals(15, result.get("tempHp"));
            assertEquals(15, draft.getTempHp());
        }

        @Test
        @DisplayName("setTempHp does not allow negative values")
        void tempHpNotNegative() {
            Map<String, Object> result = controller.setTempHp(-5, session);

            assertEquals(0, result.get("tempHp"));
        }
    }

    @Nested
    @DisplayName("Spell Slots")
    class SpellSlots {

        @BeforeEach
        void setCaster() {
            draft.setCharacterClass("wizard");
        }

        @Test
        @DisplayName("useSpellSlot increments used count")
        void useSlotIncrementsUsed() {
            Map<String, Object> result = controller.useSpellSlot(1, session);

            assertEquals(1, result.get("level"));
            assertEquals(1, result.get("used"));
            assertEquals(1, draft.getUsedSpellSlots()[0]);
        }

        @Test
        @DisplayName("restoreSpellSlot decrements used count")
        void restoreSlotDecrementsUsed() {
            draft.getUsedSpellSlots()[0] = 2;

            Map<String, Object> result = controller.restoreSpellSlot(1, session);

            assertEquals(1, result.get("level"));
            assertEquals(1, result.get("used"));
        }

        @Test
        @DisplayName("restoreSpellSlot does not go below 0")
        void restoreSlotFloorsAtZero() {
            Map<String, Object> result = controller.restoreSpellSlot(1, session);

            assertEquals(0, result.get("used"));
        }
    }

    @Nested
    @DisplayName("Rests")
    class Rests {

        @Test
        @DisplayName("shortRest restores warlock slots")
        void shortRestWarlockSlots() {
            draft.setCharacterClass("warlock");
            draft.getUsedSpellSlots()[0] = 2;

            Map<String, Object> result = controller.shortRest(session);

            assertEquals("Short rest complete. Roll hit dice to heal.", result.get("message"));
            assertArrayEquals(new int[9], draft.getUsedSpellSlots());
        }

        @Test
        @DisplayName("longRest restores HP, slots, and hit dice")
        void longRestRestoresAll() {
            draft.setCurrentHp(10);
            draft.setTempHp(5);
            draft.setUsedHitDice(3);
            draft.getUsedSpellSlots()[0] = 4;

            Map<String, Object> result = controller.longRest(session);

            assertEquals("Long rest complete!", result.get("message"));
            assertEquals(2, result.get("hitDiceRestored")); // Level 5 / 2 = 2 hit dice restored
            assertEquals(44, draft.getCurrentHp()); // Max HP restored
            assertEquals(0, draft.getTempHp());
            assertEquals(1, draft.getUsedHitDice()); // 3 - 2 = 1
            assertEquals(0, draft.getUsedSpellSlots()[0]);
        }

        @Test
        @DisplayName("useHitDie decrements available hit dice")
        void useHitDieDecrementsAvailable() {
            draft.setUsedHitDice(2);

            Map<String, Object> result = controller.useHitDie(session);

            assertEquals(true, result.get("success"));
            assertEquals(10, result.get("hitDie"));
            assertEquals(2, result.get("remaining")); // 5 - 3 = 2
            assertEquals(3, draft.getUsedHitDice());
        }

        @Test
        @DisplayName("useHitDie fails when none remaining")
        void useHitDieFailsWhenNoneRemaining() {
            draft.setUsedHitDice(5); // All used

            Map<String, Object> result = controller.useHitDie(session);

            assertEquals(false, result.get("success"));
            assertEquals("No hit dice remaining", result.get("message"));
        }
    }

    @Nested
    @DisplayName("Inventory Management")
    class InventoryManagement {

        @Test
        @DisplayName("inventory page shows inventory")
        void inventoryPage() {
            Model model = new ExtendedModelMap();
            String result = controller.inventory(session, model);

            assertEquals("play/inventory", result);
            assertTrue(model.containsAttribute("draft"));
            assertTrue(model.containsAttribute("attunedCount"));
        }

        @Test
        @DisplayName("addItem adds item to inventory")
        void addItem() {
            var item = new InventoryItem("Longsword", "weapon");

            Map<String, Object> result = controller.addItem(item, session);

            assertEquals(true, result.get("success"));
            assertFalse(draft.getInventory().isEmpty());
        }

        @Test
        @DisplayName("removeItem removes item from inventory")
        void removeItem() {
            var item = new InventoryItem("Longsword", "weapon");
            draft.addItem(item);

            Map<String, Object> result = controller.removeItem(item.getId(), session);

            assertEquals(true, result.get("success"));
            assertTrue(draft.getInventory().isEmpty());
        }

        @Test
        @DisplayName("equipItem toggles equipped status")
        void equipItem() {
            var item = new InventoryItem("Longsword", "weapon");
            draft.addItem(item);

            Map<String, Object> result = controller.equipItem(item.getId(), true, session);

            assertEquals(true, result.get("success"));
            assertEquals(true, result.get("equipped"));
            assertTrue(draft.getInventory().get(0).isEquipped());
        }

        @Test
        @DisplayName("attuneItem sets attunement")
        void attuneItem() {
            var item = new InventoryItem("Ring of Protection", "ring")
                    .withRequiresAttunement(true);
            draft.addItem(item);

            Map<String, Object> result = controller.attuneItem(item.getId(), true, session);

            assertEquals(true, result.get("success"));
            assertEquals(1, result.get("attunedCount"));
            assertTrue(draft.getInventory().get(0).isAttuned());
        }

        @Test
        @DisplayName("attuneItem fails when at attunement limit")
        void attuneItemFailsAtLimit() {
            // Add 3 attuned items
            for (int i = 0; i < 3; i++) {
                var attuned = new InventoryItem("Item " + i, "wondrous")
                        .withRequiresAttunement(true);
                attuned.setAttuned(true);
                draft.addItem(attuned);
            }

            // Try to attune a 4th
            var item = new InventoryItem("Ring of Protection", "ring")
                    .withRequiresAttunement(true);
            draft.addItem(item);

            Map<String, Object> result = controller.attuneItem(item.getId(), true, session);

            assertEquals(false, result.get("success"));
            assertEquals("Maximum 3 attuned items", result.get("error"));
        }
    }

    @Nested
    @DisplayName("Currency Management")
    class CurrencyManagement {

        @Test
        @DisplayName("updateCurrency sets all currency values")
        void updateCurrency() {
            Map<String, Object> result = controller.updateCurrency(5, 100, 10, 50, 25, session);

            assertEquals(true, result.get("success"));
            assertEquals(5, draft.getPlatinum());
            assertEquals(100, draft.getGold());
            assertEquals(10, draft.getElectrum());
            assertEquals(50, draft.getSilver());
            assertEquals(25, draft.getCopper());
        }

        @Test
        @DisplayName("updateCurrency does not allow negative values")
        void currencyNotNegative() {
            controller.updateCurrency(-5, -100, 0, 0, 0, session);

            assertEquals(0, draft.getPlatinum());
            assertEquals(0, draft.getGold());
        }
    }

    @Nested
    @DisplayName("Condition Tracking")
    class ConditionTracking {

        @Test
        @DisplayName("addCondition adds condition to draft")
        void addCondition() {
            Map<String, Object> result = controller.addCondition("Poisoned", 3, "Trap", session);

            assertEquals(true, result.get("success"));
            assertFalse(draft.getConditions().isEmpty());
            assertEquals("Poisoned", draft.getConditions().get(0).getName());
            assertEquals(3, draft.getConditions().get(0).getRemainingRounds());
        }

        @Test
        @DisplayName("removeCondition removes condition")
        void removeCondition() {
            controller.addCondition("Poisoned", -1, null, session);
            String condId = draft.getConditions().get(0).getId();

            Map<String, Object> result = controller.removeCondition(condId, session);

            assertEquals(true, result.get("success"));
            assertTrue(draft.getConditions().isEmpty());
        }

        @Test
        @DisplayName("tickConditions decrements durations")
        void tickConditions() {
            controller.addCondition("Poisoned", 2, null, session);

            Map<String, Object> result = controller.tickConditions(session);

            assertEquals(true, result.get("success"));
            assertEquals(1, draft.getConditions().get(0).getRemainingRounds());
        }

        @Test
        @DisplayName("tickConditions removes expired conditions")
        void tickRemovesExpired() {
            controller.addCondition("Poisoned", 1, null, session);

            Map<String, Object> result = controller.tickConditions(session);

            assertTrue(draft.getConditions().isEmpty());
            assertTrue(((java.util.List<?>)result.get("expired")).contains("Poisoned"));
        }
    }

    @Nested
    @DisplayName("Death Saves")
    class DeathSavesTracking {

        @Test
        @DisplayName("deathSaveSuccess increments successes")
        void deathSaveSuccess() {
            Map<String, Object> result = controller.deathSaveSuccess(session);

            assertEquals(1, result.get("successes"));
            assertEquals(0, result.get("failures"));
            assertEquals(false, result.get("stable"));
        }

        @Test
        @DisplayName("3 successes means stable")
        void threeSuccessesStable() {
            controller.deathSaveSuccess(session);
            controller.deathSaveSuccess(session);
            Map<String, Object> result = controller.deathSaveSuccess(session);

            assertEquals(3, result.get("successes"));
            assertEquals(true, result.get("stable"));
        }

        @Test
        @DisplayName("deathSaveFailure increments failures")
        void deathSaveFailure() {
            Map<String, Object> result = controller.deathSaveFailure(session);

            assertEquals(0, result.get("successes"));
            assertEquals(1, result.get("failures"));
            assertEquals(false, result.get("dead"));
        }

        @Test
        @DisplayName("3 failures means dead")
        void threeFailuresDead() {
            controller.deathSaveFailure(session);
            controller.deathSaveFailure(session);
            Map<String, Object> result = controller.deathSaveFailure(session);

            assertEquals(3, result.get("failures"));
            assertEquals(true, result.get("dead"));
        }

        @Test
        @DisplayName("resetDeathSaves clears both")
        void resetDeathSaves() {
            draft.setDeathSaveSuccesses(2);
            draft.setDeathSaveFailures(1);

            Map<String, Object> result = controller.resetDeathSaves(session);

            assertEquals(0, result.get("successes"));
            assertEquals(0, result.get("failures"));
        }
    }

    @Nested
    @DisplayName("Level Up")
    class LevelUp {

        @Test
        @DisplayName("returns options for next level — no choices needed (Barbarian L1→2)")
        void returnsOptionsNoChoicesNeeded() {
            draft.setCharacterClass("barbarian");
            draft.setLevel(1);

            var resp = new MockHttpServletResponse();
            Map<String, Object> result = controller.levelUpOptions(session, resp);

            assertEquals(200, resp.getStatus());
            assertEquals(1, result.get("currentLevel"));
            assertEquals(2, result.get("newLevel"));
            assertEquals(false, result.get("needsAsi"));
            assertEquals(false, result.get("needsSubclass"));
            assertEquals(0, result.get("newCantripsCount"));
            assertEquals(0, result.get("newSpellsCount"));
            assertEquals(0, result.get("wizardSpellbookGain"));
            assertTrue(result.get("hpGain") instanceof Integer);
            assertEquals(false, result.get("spellSlotsChanged"));
            assertEquals("", result.get("newSpellSlotSummary"));
        }

        @Test
        @DisplayName("needsAsi is true when levelling to an ASI level")
        void needsAsiAtLevel4() {
            draft.setCharacterClass("fighter");
            draft.setLevel(3);  // levelling to 4 (Fighter ASI)

            Map<String, Object> result = controller.levelUpOptions(session, new MockHttpServletResponse());

            assertEquals(true, result.get("needsAsi"));
            assertFalse(((List<?>) result.get("availableFeats")).isEmpty());
        }

        @Test
        @DisplayName("needsSubclass is true when reaching subclass level without one")
        void needsSubclassWhenMissing() {
            draft.setCharacterClass("fighter");
            draft.setSubclassId("");
            draft.setLevel(2);  // Fighter subclass at 3

            Map<String, Object> result = controller.levelUpOptions(session, new MockHttpServletResponse());

            assertEquals(true, result.get("needsSubclass"));
            assertFalse(((List<?>) result.get("availableSubclasses")).isEmpty());
        }

        @Test
        @DisplayName("needsSubclass is false when subclass already chosen")
        void noSubclassNeededWhenAlreadyChosen() {
            draft.setCharacterClass("fighter");
            draft.setSubclassId("champion");
            draft.setLevel(2);

            Map<String, Object> result = controller.levelUpOptions(session, new MockHttpServletResponse());

            assertEquals(false, result.get("needsSubclass"));
        }

        @Test
        @DisplayName("newSpellsCount > 0 for known caster (Warlock L1→2)")
        void warlockGainsSpell() {
            draft.setCharacterClass("warlock");
            draft.setLevel(1);

            Map<String, Object> result = controller.levelUpOptions(session, new MockHttpServletResponse());

            assertTrue((int) result.get("newSpellsCount") > 0);
            assertFalse(((List<?>) result.get("availableSpells")).isEmpty());
        }

        @Test
        @DisplayName("wizardSpellbookGain is 2 for wizard beyond level 1")
        void wizardGainsSpellbookSpells() {
            draft.setCharacterClass("wizard");
            draft.setLevel(1);

            Map<String, Object> result = controller.levelUpOptions(session, new MockHttpServletResponse());

            assertEquals(true, result.get("isWizard"));
            assertEquals(2, result.get("wizardSpellbookGain"));
        }

        @Test
        @DisplayName("returns 400 at level 20")
        void level20Cap() {
            draft.setLevel(20);
            var resp = new MockHttpServletResponse();

            Map<String, Object> result = controller.levelUpOptions(session, resp);

            assertEquals(400, resp.getStatus());
            assertTrue(result.containsKey("error"));
        }

        @Test
        @DisplayName("returns 400 when no character in session")
        void noCharacterInSession() {
            var resp = new MockHttpServletResponse();

            Map<String, Object> result = controller.levelUpOptions(new MockHttpSession(), resp);

            assertEquals(400, resp.getStatus());
            assertTrue(result.containsKey("error"));
        }
    }

    @Nested
    @DisplayName("Concentration")
    class ConcentrationTests {

        @BeforeEach
        void setCaster() {
            draft.setCharacterClass("wizard");
        }

        @Test
        @DisplayName("setConcentration sets spell")
        void setConcentration() {
            Map<String, Object> result = controller.setConcentration("Haste", session);

            assertEquals(true, result.get("success"));
            assertEquals("Haste", result.get("concentratingOn"));
            assertEquals("Haste", draft.getConcentratingOn());
        }

        @Test
        @DisplayName("breakConcentration clears spell")
        void breakConcentration() {
            draft.setConcentratingOn("Haste");

            Map<String, Object> result = controller.breakConcentration(session);

            assertEquals(true, result.get("success"));
            assertEquals("Haste", result.get("broken"));
            assertNull(draft.getConcentratingOn());
        }

        @Test
        @DisplayName("concentrationCheck returns DC when concentrating")
        void concentrationCheck() {
            draft.setConcentratingOn("Haste");

            Map<String, Object> result = controller.concentrationCheck(20, session);

            assertEquals(true, result.get("required"));
            assertEquals(10, result.get("dc")); // Max of 10 or 20/2=10
            assertEquals("Haste", result.get("spell"));
        }

        @Test
        @DisplayName("concentrationCheck DC is half damage when higher")
        void concentrationCheckHighDamage() {
            draft.setConcentratingOn("Haste");

            Map<String, Object> result = controller.concentrationCheck(40, session);

            assertEquals(20, result.get("dc")); // 40/2 = 20
        }

        @Test
        @DisplayName("concentrationCheck not required when not concentrating")
        void concentrationCheckNotRequired() {
            Map<String, Object> result = controller.concentrationCheck(20, session);

            assertEquals(false, result.get("required"));
        }
    }
}
