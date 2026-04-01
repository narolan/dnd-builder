package com.dnd.builder.service;

import com.dnd.builder.model.ClassDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClassRegistry verifying D&D 5e 2014 class accuracy.
 */
class ClassRegistryTest {

    private ClassRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ClassRegistry();
    }

    @Nested
    @DisplayName("Proficiency Bonus")
    class ProficiencyBonus {

        @ParameterizedTest
        @CsvSource({
            "1, 2", "2, 2", "3, 2", "4, 2",
            "5, 3", "6, 3", "7, 3", "8, 3",
            "9, 4", "10, 4", "11, 4", "12, 4",
            "13, 5", "14, 5", "15, 5", "16, 5",
            "17, 6", "18, 6", "19, 6", "20, 6"
        })
        @DisplayName("Proficiency bonus by level")
        void proficiencyByLevel(int level, int expectedBonus) {
            assertEquals(expectedBonus, ClassRegistry.proficiencyBonus(level));
        }
    }

    @Nested
    @DisplayName("Full Caster Spell Slots")
    class FullCasterSlots {

        @Test
        @DisplayName("Level 1 full caster has 2 first-level slots")
        void level1Slots() {
            int[] slots = ClassRegistry.fullCasterSlots(1);
            assertEquals(2, slots[0]); // 1st level
            assertEquals(0, slots[1]); // 2nd level
        }

        @Test
        @DisplayName("Level 3 full caster has 4 first-level, 2 second-level slots")
        void level3Slots() {
            int[] slots = ClassRegistry.fullCasterSlots(3);
            assertEquals(4, slots[0]);
            assertEquals(2, slots[1]);
            assertEquals(0, slots[2]);
        }

        @Test
        @DisplayName("Level 9 full caster has 5th-level slots")
        void level9Slots() {
            int[] slots = ClassRegistry.fullCasterSlots(9);
            assertEquals(4, slots[0]); // 1st
            assertEquals(3, slots[1]); // 2nd
            assertEquals(3, slots[2]); // 3rd
            assertEquals(3, slots[3]); // 4th
            assertEquals(1, slots[4]); // 5th
        }

        @Test
        @DisplayName("Level 20 full caster has all slot levels")
        void level20Slots() {
            int[] slots = ClassRegistry.fullCasterSlots(20);
            assertEquals(4, slots[0]); // 1st
            assertEquals(3, slots[1]); // 2nd
            assertEquals(3, slots[2]); // 3rd
            assertEquals(3, slots[3]); // 4th
            assertEquals(3, slots[4]); // 5th
            assertEquals(2, slots[5]); // 6th
            assertEquals(2, slots[6]); // 7th
            assertEquals(1, slots[7]); // 8th
            assertEquals(1, slots[8]); // 9th
        }
    }

    @Nested
    @DisplayName("Warlock Pact Magic Slots")
    class WarlockSlots {

        @ParameterizedTest
        @CsvSource({
            "1, 1, 1",   // Level 1: 1 slot, 1st-level
            "2, 2, 1",   // Level 2: 2 slots, 1st-level
            "3, 2, 2",   // Level 3: 2 slots, 2nd-level
            "5, 2, 3",   // Level 5: 2 slots, 3rd-level
            "7, 2, 4",   // Level 7: 2 slots, 4th-level
            "9, 2, 5",   // Level 9: 2 slots, 5th-level
            "11, 3, 5",  // Level 11: 3 slots, 5th-level
            "17, 4, 5"   // Level 17: 4 slots, 5th-level
        })
        @DisplayName("Warlock pact magic progression")
        void warlockSlotProgression(int level, int numSlots, int slotLevel) {
            int[] slots = ClassRegistry.warlockSlots(level);
            assertEquals(numSlots, slots[0], "Number of slots at level " + level);
            assertEquals(slotLevel, slots[1], "Slot level at level " + level);
        }
    }

    @Nested
    @DisplayName("Class Hit Dice")
    class HitDice {

        @Test
        @DisplayName("Barbarian has d12 hit die")
        void barbarianHitDie() {
            assertEquals(12, registry.getById("barbarian").getHitDie());
        }

        @Test
        @DisplayName("Fighter and Paladin have d10 hit die")
        void d10HitDie() {
            assertEquals(10, registry.getById("fighter").getHitDie());
            assertEquals(10, registry.getById("paladin").getHitDie());
            assertEquals(10, registry.getById("ranger").getHitDie());
        }

        @Test
        @DisplayName("Most classes have d8 hit die")
        void d8HitDie() {
            assertEquals(8, registry.getById("bard").getHitDie());
            assertEquals(8, registry.getById("cleric").getHitDie());
            assertEquals(8, registry.getById("druid").getHitDie());
            assertEquals(8, registry.getById("monk").getHitDie());
            assertEquals(8, registry.getById("rogue").getHitDie());
            assertEquals(8, registry.getById("warlock").getHitDie());
        }

        @Test
        @DisplayName("Sorcerer and Wizard have d6 hit die")
        void d6HitDie() {
            assertEquals(6, registry.getById("sorcerer").getHitDie());
            assertEquals(6, registry.getById("wizard").getHitDie());
        }
    }

    @Nested
    @DisplayName("Saving Throw Proficiencies")
    class SavingThrows {

        @Test
        @DisplayName("Barbarian has STR and CON saves")
        void barbarianSaves() {
            assertEquals(List.of("STR", "CON"), registry.getById("barbarian").getSavingThrows());
        }

        @Test
        @DisplayName("Bard has DEX and CHA saves")
        void bardSaves() {
            assertEquals(List.of("DEX", "CHA"), registry.getById("bard").getSavingThrows());
        }

        @Test
        @DisplayName("Cleric has WIS and CHA saves")
        void clericSaves() {
            assertEquals(List.of("WIS", "CHA"), registry.getById("cleric").getSavingThrows());
        }

        @Test
        @DisplayName("Druid has INT and WIS saves")
        void druidSaves() {
            assertEquals(List.of("INT", "WIS"), registry.getById("druid").getSavingThrows());
        }

        @Test
        @DisplayName("Fighter has STR and CON saves")
        void fighterSaves() {
            assertEquals(List.of("STR", "CON"), registry.getById("fighter").getSavingThrows());
        }

        @Test
        @DisplayName("Monk has STR and DEX saves")
        void monkSaves() {
            assertEquals(List.of("STR", "DEX"), registry.getById("monk").getSavingThrows());
        }

        @Test
        @DisplayName("Paladin has WIS and CHA saves")
        void paladinSaves() {
            assertEquals(List.of("WIS", "CHA"), registry.getById("paladin").getSavingThrows());
        }

        @Test
        @DisplayName("Ranger has STR and DEX saves")
        void rangerSaves() {
            assertEquals(List.of("STR", "DEX"), registry.getById("ranger").getSavingThrows());
        }

        @Test
        @DisplayName("Rogue has DEX and INT saves")
        void rogueSaves() {
            assertEquals(List.of("DEX", "INT"), registry.getById("rogue").getSavingThrows());
        }

        @Test
        @DisplayName("Sorcerer has CON and CHA saves")
        void sorcererSaves() {
            assertEquals(List.of("CON", "CHA"), registry.getById("sorcerer").getSavingThrows());
        }

        @Test
        @DisplayName("Warlock has WIS and CHA saves")
        void warlockSaves() {
            assertEquals(List.of("WIS", "CHA"), registry.getById("warlock").getSavingThrows());
        }

        @Test
        @DisplayName("Wizard has INT and WIS saves")
        void wizardSaves() {
            assertEquals(List.of("INT", "WIS"), registry.getById("wizard").getSavingThrows());
        }
    }

    @Nested
    @DisplayName("Skill Choices")
    class SkillChoices {

        @Test
        @DisplayName("Barbarian chooses 2 skills from 6 options")
        void barbarianSkills() {
            ClassDefinition c = registry.getById("barbarian");
            assertEquals(2, c.getSkillChoiceCount());
            assertEquals(6, c.getSkillList().size());
            assertTrue(c.getSkillList().containsAll(List.of(
                "Animal Handling", "Athletics", "Intimidation", "Nature", "Perception", "Survival")));
        }

        @Test
        @DisplayName("Bard chooses 3 skills from any skill")
        void bardSkills() {
            ClassDefinition c = registry.getById("bard");
            assertEquals(3, c.getSkillChoiceCount());
            assertEquals(18, c.getSkillList().size()); // All skills
        }

        @Test
        @DisplayName("Ranger chooses 3 skills")
        void rangerSkills() {
            assertEquals(3, registry.getById("ranger").getSkillChoiceCount());
        }

        @Test
        @DisplayName("Rogue chooses 4 skills")
        void rogueSkills() {
            assertEquals(4, registry.getById("rogue").getSkillChoiceCount());
        }
    }

    @Nested
    @DisplayName("Spellcasting")
    class Spellcasting {

        @Test
        @DisplayName("Non-casters have no spellcasting")
        void nonCasters() {
            assertNull(registry.getById("barbarian").getSpellcasting());
            assertNull(registry.getById("fighter").getSpellcasting());
            assertNull(registry.getById("monk").getSpellcasting());
            assertNull(registry.getById("rogue").getSpellcasting());
        }

        @Test
        @DisplayName("Full casters use the correct ability")
        void fullCasterAbility() {
            assertEquals("CHA", registry.getById("bard").getSpellcasting().getAbility());
            assertEquals("WIS", registry.getById("cleric").getSpellcasting().getAbility());
            assertEquals("WIS", registry.getById("druid").getSpellcasting().getAbility());
            assertEquals("CHA", registry.getById("sorcerer").getSpellcasting().getAbility());
            assertEquals("CHA", registry.getById("warlock").getSpellcasting().getAbility());
            assertEquals("INT", registry.getById("wizard").getSpellcasting().getAbility());
        }

        @Test
        @DisplayName("Half casters use the correct ability")
        void halfCasterAbility() {
            assertEquals("CHA", registry.getById("paladin").getSpellcasting().getAbility());
            assertEquals("WIS", registry.getById("ranger").getSpellcasting().getAbility());
        }

        @Test
        @DisplayName("Prepared casters are marked correctly")
        void preparedCasters() {
            assertTrue(registry.getById("cleric").getSpellcasting().isPrepareSpells());
            assertTrue(registry.getById("druid").getSpellcasting().isPrepareSpells());
            assertTrue(registry.getById("paladin").getSpellcasting().isPrepareSpells());
            assertTrue(registry.getById("wizard").getSpellcasting().isPrepareSpells());
        }

        @Test
        @DisplayName("Known casters are marked correctly")
        void knownCasters() {
            assertFalse(registry.getById("bard").getSpellcasting().isPrepareSpells());
            assertFalse(registry.getById("sorcerer").getSpellcasting().isPrepareSpells());
            assertFalse(registry.getById("warlock").getSpellcasting().isPrepareSpells());
            assertFalse(registry.getById("ranger").getSpellcasting().isPrepareSpells());
        }

        @Test
        @DisplayName("Cantrips at level 1 are correct")
        void cantripsAtLevel1() {
            assertEquals(2, registry.getById("bard").getSpellcasting().getCantripsAtL1());
            assertEquals(3, registry.getById("cleric").getSpellcasting().getCantripsAtL1());
            assertEquals(2, registry.getById("druid").getSpellcasting().getCantripsAtL1());
            assertEquals(4, registry.getById("sorcerer").getSpellcasting().getCantripsAtL1());
            assertEquals(2, registry.getById("warlock").getSpellcasting().getCantripsAtL1());
            assertEquals(3, registry.getById("wizard").getSpellcasting().getCantripsAtL1());
        }
    }

    @Nested
    @DisplayName("Subclass Levels")
    class SubclassLevels {

        @Test
        @DisplayName("Cleric, Sorcerer, and Warlock choose subclass at level 1")
        void level1Subclass() {
            assertEquals(1, registry.getById("cleric").getSubclassLevel());
            assertEquals(1, registry.getById("sorcerer").getSubclassLevel());
            assertEquals(1, registry.getById("warlock").getSubclassLevel());
        }

        @Test
        @DisplayName("Druid and Wizard choose subclass at level 2")
        void level2Subclass() {
            assertEquals(2, registry.getById("druid").getSubclassLevel());
            assertEquals(2, registry.getById("wizard").getSubclassLevel());
        }

        @Test
        @DisplayName("Most martial classes choose subclass at level 3")
        void level3Subclass() {
            assertEquals(3, registry.getById("barbarian").getSubclassLevel());
            assertEquals(3, registry.getById("bard").getSubclassLevel());
            assertEquals(3, registry.getById("fighter").getSubclassLevel());
            assertEquals(3, registry.getById("monk").getSubclassLevel());
            assertEquals(3, registry.getById("paladin").getSubclassLevel());
            assertEquals(3, registry.getById("ranger").getSubclassLevel());
            assertEquals(3, registry.getById("rogue").getSubclassLevel());
        }
    }

    @Test
    @DisplayName("All 12 PHB classes are present")
    void allClassesPresent() {
        assertEquals(12, registry.getAllClasses().size());
        assertNotNull(registry.getById("barbarian"));
        assertNotNull(registry.getById("bard"));
        assertNotNull(registry.getById("cleric"));
        assertNotNull(registry.getById("druid"));
        assertNotNull(registry.getById("fighter"));
        assertNotNull(registry.getById("monk"));
        assertNotNull(registry.getById("paladin"));
        assertNotNull(registry.getById("ranger"));
        assertNotNull(registry.getById("rogue"));
        assertNotNull(registry.getById("sorcerer"));
        assertNotNull(registry.getById("warlock"));
        assertNotNull(registry.getById("wizard"));
    }
}
