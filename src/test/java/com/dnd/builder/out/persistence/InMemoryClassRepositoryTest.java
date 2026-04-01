package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.ClassDefinition;
import com.dnd.builder.core.port.out.ClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemoryClassRepository verifying D&D 5e 2014 class accuracy.
 */
class InMemoryClassRepositoryTest {

    private InMemoryClassRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryClassRepository();
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
            assertEquals(expectedBonus, ClassRepository.proficiencyBonus(level));
        }
    }

    @Nested
    @DisplayName("Full Caster Spell Slots")
    class FullCasterSlots {

        @Test
        @DisplayName("Level 1 full caster has 2 first-level slots")
        void level1Slots() {
            int[] slots = ClassRepository.fullCasterSlots(1);
            assertEquals(2, slots[0]); // 1st level
            assertEquals(0, slots[1]); // 2nd level
        }

        @Test
        @DisplayName("Level 3 full caster has 4 first-level, 2 second-level slots")
        void level3Slots() {
            int[] slots = ClassRepository.fullCasterSlots(3);
            assertEquals(4, slots[0]);
            assertEquals(2, slots[1]);
            assertEquals(0, slots[2]);
        }

        @Test
        @DisplayName("Level 9 full caster has 5th-level slots")
        void level9Slots() {
            int[] slots = ClassRepository.fullCasterSlots(9);
            assertEquals(4, slots[0]); // 1st
            assertEquals(3, slots[1]); // 2nd
            assertEquals(3, slots[2]); // 3rd
            assertEquals(3, slots[3]); // 4th
            assertEquals(1, slots[4]); // 5th
        }

        @Test
        @DisplayName("Level 20 full caster has all slot levels")
        void level20Slots() {
            int[] slots = ClassRepository.fullCasterSlots(20);
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
            int[] slots = ClassRepository.warlockSlots(level);
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
            assertEquals(12, repository.findById("barbarian").getHitDie());
        }

        @Test
        @DisplayName("Fighter and Paladin have d10 hit die")
        void d10HitDie() {
            assertEquals(10, repository.findById("fighter").getHitDie());
            assertEquals(10, repository.findById("paladin").getHitDie());
            assertEquals(10, repository.findById("ranger").getHitDie());
        }

        @Test
        @DisplayName("Most classes have d8 hit die")
        void d8HitDie() {
            assertEquals(8, repository.findById("bard").getHitDie());
            assertEquals(8, repository.findById("cleric").getHitDie());
            assertEquals(8, repository.findById("druid").getHitDie());
            assertEquals(8, repository.findById("monk").getHitDie());
            assertEquals(8, repository.findById("rogue").getHitDie());
            assertEquals(8, repository.findById("warlock").getHitDie());
        }

        @Test
        @DisplayName("Sorcerer and Wizard have d6 hit die")
        void d6HitDie() {
            assertEquals(6, repository.findById("sorcerer").getHitDie());
            assertEquals(6, repository.findById("wizard").getHitDie());
        }
    }

    @Nested
    @DisplayName("Saving Throw Proficiencies")
    class SavingThrows {

        @Test
        @DisplayName("Barbarian has STR and CON saves")
        void barbarianSaves() {
            assertEquals(List.of("STR", "CON"), repository.findById("barbarian").getSavingThrows());
        }

        @Test
        @DisplayName("Bard has DEX and CHA saves")
        void bardSaves() {
            assertEquals(List.of("DEX", "CHA"), repository.findById("bard").getSavingThrows());
        }

        @Test
        @DisplayName("Cleric has WIS and CHA saves")
        void clericSaves() {
            assertEquals(List.of("WIS", "CHA"), repository.findById("cleric").getSavingThrows());
        }

        @Test
        @DisplayName("Druid has INT and WIS saves")
        void druidSaves() {
            assertEquals(List.of("INT", "WIS"), repository.findById("druid").getSavingThrows());
        }

        @Test
        @DisplayName("Fighter has STR and CON saves")
        void fighterSaves() {
            assertEquals(List.of("STR", "CON"), repository.findById("fighter").getSavingThrows());
        }

        @Test
        @DisplayName("Monk has STR and DEX saves")
        void monkSaves() {
            assertEquals(List.of("STR", "DEX"), repository.findById("monk").getSavingThrows());
        }

        @Test
        @DisplayName("Paladin has WIS and CHA saves")
        void paladinSaves() {
            assertEquals(List.of("WIS", "CHA"), repository.findById("paladin").getSavingThrows());
        }

        @Test
        @DisplayName("Ranger has STR and DEX saves")
        void rangerSaves() {
            assertEquals(List.of("STR", "DEX"), repository.findById("ranger").getSavingThrows());
        }

        @Test
        @DisplayName("Rogue has DEX and INT saves")
        void rogueSaves() {
            assertEquals(List.of("DEX", "INT"), repository.findById("rogue").getSavingThrows());
        }

        @Test
        @DisplayName("Sorcerer has CON and CHA saves")
        void sorcererSaves() {
            assertEquals(List.of("CON", "CHA"), repository.findById("sorcerer").getSavingThrows());
        }

        @Test
        @DisplayName("Warlock has WIS and CHA saves")
        void warlockSaves() {
            assertEquals(List.of("WIS", "CHA"), repository.findById("warlock").getSavingThrows());
        }

        @Test
        @DisplayName("Wizard has INT and WIS saves")
        void wizardSaves() {
            assertEquals(List.of("INT", "WIS"), repository.findById("wizard").getSavingThrows());
        }
    }

    @Nested
    @DisplayName("Skill Choices")
    class SkillChoices {

        @Test
        @DisplayName("Barbarian chooses 2 skills from 6 options")
        void barbarianSkills() {
            ClassDefinition c = repository.findById("barbarian");
            assertEquals(2, c.getSkillChoiceCount());
            assertEquals(6, c.getSkillList().size());
            assertTrue(c.getSkillList().containsAll(List.of(
                "Animal Handling", "Athletics", "Intimidation", "Nature", "Perception", "Survival")));
        }

        @Test
        @DisplayName("Bard chooses 3 skills from any skill")
        void bardSkills() {
            ClassDefinition c = repository.findById("bard");
            assertEquals(3, c.getSkillChoiceCount());
            assertEquals(18, c.getSkillList().size()); // All skills
        }

        @Test
        @DisplayName("Ranger chooses 3 skills")
        void rangerSkills() {
            assertEquals(3, repository.findById("ranger").getSkillChoiceCount());
        }

        @Test
        @DisplayName("Rogue chooses 4 skills")
        void rogueSkills() {
            assertEquals(4, repository.findById("rogue").getSkillChoiceCount());
        }
    }

    @Nested
    @DisplayName("Spellcasting")
    class Spellcasting {

        @Test
        @DisplayName("Non-casters have no spellcasting")
        void nonCasters() {
            assertNull(repository.findById("barbarian").getSpellcasting());
            assertNull(repository.findById("fighter").getSpellcasting());
            assertNull(repository.findById("monk").getSpellcasting());
            assertNull(repository.findById("rogue").getSpellcasting());
        }

        @Test
        @DisplayName("Full casters use the correct ability")
        void fullCasterAbility() {
            assertEquals("CHA", repository.findById("bard").getSpellcasting().getAbility());
            assertEquals("WIS", repository.findById("cleric").getSpellcasting().getAbility());
            assertEquals("WIS", repository.findById("druid").getSpellcasting().getAbility());
            assertEquals("CHA", repository.findById("sorcerer").getSpellcasting().getAbility());
            assertEquals("CHA", repository.findById("warlock").getSpellcasting().getAbility());
            assertEquals("INT", repository.findById("wizard").getSpellcasting().getAbility());
        }

        @Test
        @DisplayName("Half casters use the correct ability")
        void halfCasterAbility() {
            assertEquals("CHA", repository.findById("paladin").getSpellcasting().getAbility());
            assertEquals("WIS", repository.findById("ranger").getSpellcasting().getAbility());
        }

        @Test
        @DisplayName("Prepared casters are marked correctly")
        void preparedCasters() {
            assertTrue(repository.findById("cleric").getSpellcasting().isPrepareSpells());
            assertTrue(repository.findById("druid").getSpellcasting().isPrepareSpells());
            assertTrue(repository.findById("paladin").getSpellcasting().isPrepareSpells());
            assertTrue(repository.findById("wizard").getSpellcasting().isPrepareSpells());
        }

        @Test
        @DisplayName("Known casters are marked correctly")
        void knownCasters() {
            assertFalse(repository.findById("bard").getSpellcasting().isPrepareSpells());
            assertFalse(repository.findById("sorcerer").getSpellcasting().isPrepareSpells());
            assertFalse(repository.findById("warlock").getSpellcasting().isPrepareSpells());
            assertFalse(repository.findById("ranger").getSpellcasting().isPrepareSpells());
        }

        @Test
        @DisplayName("Cantrips at level 1 are correct")
        void cantripsAtLevel1() {
            assertEquals(2, repository.findById("bard").getSpellcasting().getCantripsAtL1());
            assertEquals(3, repository.findById("cleric").getSpellcasting().getCantripsAtL1());
            assertEquals(2, repository.findById("druid").getSpellcasting().getCantripsAtL1());
            assertEquals(4, repository.findById("sorcerer").getSpellcasting().getCantripsAtL1());
            assertEquals(2, repository.findById("warlock").getSpellcasting().getCantripsAtL1());
            assertEquals(3, repository.findById("wizard").getSpellcasting().getCantripsAtL1());
        }
    }

    @Nested
    @DisplayName("Subclass Levels")
    class SubclassLevels {

        @Test
        @DisplayName("Cleric, Sorcerer, and Warlock choose subclass at level 1")
        void level1Subclass() {
            assertEquals(1, repository.findById("cleric").getSubclassLevel());
            assertEquals(1, repository.findById("sorcerer").getSubclassLevel());
            assertEquals(1, repository.findById("warlock").getSubclassLevel());
        }

        @Test
        @DisplayName("Druid and Wizard choose subclass at level 2")
        void level2Subclass() {
            assertEquals(2, repository.findById("druid").getSubclassLevel());
            assertEquals(2, repository.findById("wizard").getSubclassLevel());
        }

        @Test
        @DisplayName("Most martial classes choose subclass at level 3")
        void level3Subclass() {
            assertEquals(3, repository.findById("barbarian").getSubclassLevel());
            assertEquals(3, repository.findById("bard").getSubclassLevel());
            assertEquals(3, repository.findById("fighter").getSubclassLevel());
            assertEquals(3, repository.findById("monk").getSubclassLevel());
            assertEquals(3, repository.findById("paladin").getSubclassLevel());
            assertEquals(3, repository.findById("ranger").getSubclassLevel());
            assertEquals(3, repository.findById("rogue").getSubclassLevel());
        }
    }

    @Test
    @DisplayName("All 12 PHB classes are present")
    void allClassesPresent() {
        assertEquals(12, repository.findAll().size());
        assertNotNull(repository.findById("barbarian"));
        assertNotNull(repository.findById("bard"));
        assertNotNull(repository.findById("cleric"));
        assertNotNull(repository.findById("druid"));
        assertNotNull(repository.findById("fighter"));
        assertNotNull(repository.findById("monk"));
        assertNotNull(repository.findById("paladin"));
        assertNotNull(repository.findById("ranger"));
        assertNotNull(repository.findById("rogue"));
        assertNotNull(repository.findById("sorcerer"));
        assertNotNull(repository.findById("warlock"));
        assertNotNull(repository.findById("wizard"));
    }
}
