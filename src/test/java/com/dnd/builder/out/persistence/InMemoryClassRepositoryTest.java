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

    @Nested
    @DisplayName("Half Caster Spell Slots")
    class HalfCasterSlots {

        @Test
        @DisplayName("Level 1 half caster has no slots")
        void level1NoSlots() {
            int[] slots = ClassRepository.halfCasterSlots(1);
            assertEquals(0, slots[0]);
        }

        @Test
        @DisplayName("Level 2 half caster has 2 first-level slots")
        void level2Slots() {
            int[] slots = ClassRepository.halfCasterSlots(2);
            assertEquals(2, slots[0]);
            assertEquals(0, slots[1]);
        }

        @Test
        @DisplayName("Level 5 half caster has 4 first-level, 2 second-level slots")
        void level5Slots() {
            int[] slots = ClassRepository.halfCasterSlots(5);
            assertEquals(4, slots[0]);
            assertEquals(2, slots[1]);
            assertEquals(0, slots[2]);
        }

        @Test
        @DisplayName("Level 20 half caster has 5th-level slots")
        void level20Slots() {
            int[] slots = ClassRepository.halfCasterSlots(20);
            assertEquals(4, slots[0]); // 1st
            assertEquals(3, slots[1]); // 2nd
            assertEquals(3, slots[2]); // 3rd
            assertEquals(3, slots[3]); // 4th
            assertEquals(2, slots[4]); // 5th
        }
    }

    @Nested
    @DisplayName("Max Spell Level")
    class MaxSpellLevel {

        @ParameterizedTest
        @CsvSource({
            "wizard, 1, 1",
            "wizard, 3, 2",
            "wizard, 5, 3",
            "wizard, 9, 5",
            "wizard, 17, 9",
            "wizard, 20, 9"
        })
        @DisplayName("Full caster max spell level")
        void fullCasterMaxLevel(String classId, int level, int expectedMaxLevel) {
            assertEquals(expectedMaxLevel, ClassRepository.maxSpellLevel(classId, level));
        }

        @ParameterizedTest
        @CsvSource({
            "warlock, 1, 1",
            "warlock, 3, 2",
            "warlock, 5, 3",
            "warlock, 7, 4",
            "warlock, 9, 5",
            "warlock, 20, 5"
        })
        @DisplayName("Warlock max spell level (pact magic caps at 5)")
        void warlockMaxLevel(String classId, int level, int expectedMaxLevel) {
            assertEquals(expectedMaxLevel, ClassRepository.maxSpellLevel(classId, level));
        }

        @ParameterizedTest
        @CsvSource({
            "paladin, 1, 0",
            "paladin, 2, 1",
            "paladin, 5, 2",
            "paladin, 9, 3",
            "paladin, 13, 4",
            "paladin, 17, 5",
            "ranger, 2, 1",
            "ranger, 20, 5"
        })
        @DisplayName("Half caster max spell level")
        void halfCasterMaxLevel(String classId, int level, int expectedMaxLevel) {
            assertEquals(expectedMaxLevel, ClassRepository.maxSpellLevel(classId, level));
        }

        @Test
        @DisplayName("Non-casters have max spell level 0")
        void nonCasterMaxLevel() {
            assertEquals(0, ClassRepository.maxSpellLevel("fighter", 20));
            assertEquals(0, ClassRepository.maxSpellLevel("barbarian", 20));
            assertEquals(0, ClassRepository.maxSpellLevel("rogue", 20));
            assertEquals(0, ClassRepository.maxSpellLevel("monk", 20));
        }

        @Test
        @DisplayName("Null class returns 0")
        void nullClassMaxLevel() {
            assertEquals(0, ClassRepository.maxSpellLevel(null, 10));
        }
    }

    @Nested
    @DisplayName("Cantrips Known")
    class CantripsKnown {

        @ParameterizedTest
        @CsvSource({
            "bard, 1, 2", "bard, 4, 3", "bard, 10, 4",
            "cleric, 1, 3", "cleric, 4, 4", "cleric, 10, 5",
            "druid, 1, 2", "druid, 4, 3", "druid, 10, 4",
            "sorcerer, 1, 4", "sorcerer, 4, 5", "sorcerer, 10, 6",
            "warlock, 1, 2", "warlock, 4, 3", "warlock, 10, 4",
            "wizard, 1, 3", "wizard, 4, 4", "wizard, 10, 5"
        })
        @DisplayName("Cantrips known scales by level")
        void cantripsScaling(String classId, int level, int expectedCantrips) {
            assertEquals(expectedCantrips, ClassRepository.cantripsKnown(classId, level));
        }

        @Test
        @DisplayName("Non-casters have 0 cantrips")
        void nonCasterCantrips() {
            assertEquals(0, ClassRepository.cantripsKnown("fighter", 10));
            assertEquals(0, ClassRepository.cantripsKnown("barbarian", 10));
        }

        @Test
        @DisplayName("Null class returns 0 cantrips")
        void nullClassCantrips() {
            assertEquals(0, ClassRepository.cantripsKnown(null, 10));
        }
    }

    @Nested
    @DisplayName("Spells Known")
    class SpellsKnown {

        @ParameterizedTest
        @CsvSource({
            "bard, 1, 4",
            "bard, 5, 8",
            "bard, 10, 13",
            "sorcerer, 1, 2",
            "sorcerer, 5, 6",
            "warlock, 1, 2",
            "warlock, 10, 11"
        })
        @DisplayName("Known casters: spells known scales by level")
        void knownCasterSpells(String classId, int level, int expectedSpells) {
            assertEquals(expectedSpells, ClassRepository.spellsKnown(classId, level));
        }

        @ParameterizedTest
        @CsvSource({
            "ranger, 1, 0",
            "ranger, 2, 2",
            "ranger, 3, 3",
            "ranger, 5, 4",
            "ranger, 11, 7",
            "ranger, 19, 11"
        })
        @DisplayName("Ranger spells known follows unique progression")
        void rangerSpellsKnown(String classId, int level, int expectedSpells) {
            assertEquals(expectedSpells, ClassRepository.spellsKnown(classId, level));
        }

        @Test
        @DisplayName("Prepared casters return 0 spells known")
        void preparedCastersNoSpellsKnown() {
            assertEquals(0, ClassRepository.spellsKnown("cleric", 10));
            assertEquals(0, ClassRepository.spellsKnown("wizard", 10));
            assertEquals(0, ClassRepository.spellsKnown("druid", 10));
            assertEquals(0, ClassRepository.spellsKnown("paladin", 10));
        }
    }

    @Nested
    @DisplayName("Max Prepared Spells")
    class MaxPrepared {

        @ParameterizedTest
        @CsvSource({
            "cleric, 5, 3, 8",   // level 5, +3 WIS = 5 + 3 = 8
            "cleric, 10, 4, 14", // level 10, +4 WIS = 10 + 4 = 14
            "druid, 5, 3, 8",
            "wizard, 5, 4, 9"    // level 5, +4 INT = 5 + 4 = 9
        })
        @DisplayName("Full casters: level + ability mod")
        void fullCasterPrepared(String classId, int level, int abilityMod, int expectedMax) {
            assertEquals(expectedMax, ClassRepository.maxPrepared(classId, level, abilityMod));
        }

        @ParameterizedTest
        @CsvSource({
            "paladin, 5, 3, 5",   // level 5 / 2 + 3 = 5
            "paladin, 10, 4, 9"  // level 10 / 2 + 4 = 9
        })
        @DisplayName("Paladin: half level + ability mod")
        void paladinPrepared(String classId, int level, int abilityMod, int expectedMax) {
            assertEquals(expectedMax, ClassRepository.maxPrepared(classId, level, abilityMod));
        }

        @Test
        @DisplayName("Minimum 1 prepared spell")
        void minimumOnePrepared() {
            assertEquals(1, ClassRepository.maxPrepared("cleric", 1, -2));
            assertEquals(1, ClassRepository.maxPrepared("wizard", 1, -5));
        }

        @Test
        @DisplayName("Known casters return 0 max prepared")
        void knownCastersNoPrepared() {
            assertEquals(0, ClassRepository.maxPrepared("bard", 10, 5));
            assertEquals(0, ClassRepository.maxPrepared("sorcerer", 10, 5));
            assertEquals(0, ClassRepository.maxPrepared("warlock", 10, 5));
        }
    }

    @Nested
    @DisplayName("Class Features")
    class ClassFeatures {

        @Test
        @DisplayName("All classes have features")
        void allClassesHaveFeatures() {
            for (var cd : repository.findAll()) {
                assertNotNull(cd.getFeatures(), cd.getId() + " should have features");
                assertFalse(cd.getFeatures().isEmpty(), cd.getId() + " should have at least one feature");
            }
        }

        @Test
        @DisplayName("Fighter has Second Wind at level 1")
        void fighterSecondWind() {
            var fighter = repository.findById("fighter");
            var level1Features = fighter.getFeatures().stream()
                    .filter(f -> f.level() == 1)
                    .toList();
            assertTrue(level1Features.stream().anyMatch(f -> f.name().equals("Second Wind")));
        }

        @Test
        @DisplayName("Wizard has Arcane Recovery at level 1")
        void wizardArcaneRecovery() {
            var wizard = repository.findById("wizard");
            var level1Features = wizard.getFeatures().stream()
                    .filter(f -> f.level() == 1)
                    .toList();
            assertTrue(level1Features.stream().anyMatch(f -> f.name().equals("Arcane Recovery")));
        }

        @Test
        @DisplayName("Barbarian has Rage at level 1")
        void barbarianRage() {
            var barbarian = repository.findById("barbarian");
            var level1Features = barbarian.getFeatures().stream()
                    .filter(f -> f.level() == 1)
                    .toList();
            assertTrue(level1Features.stream().anyMatch(f -> f.name().equals("Rage")));
        }
    }

    @Nested
    @DisplayName("ASI Levels")
    class AsiLevels {

        @Test
        @DisplayName("Standard ASI levels for most classes")
        void standardAsiLevels() {
            var expected = List.of(4, 8, 12, 16, 19);
            assertEquals(expected, repository.findById("wizard").getAsiLevels());
            assertEquals(expected, repository.findById("barbarian").getAsiLevels());
            assertEquals(expected, repository.findById("cleric").getAsiLevels());
        }

        @Test
        @DisplayName("Fighter has extra ASI at levels 6 and 14")
        void fighterExtraAsi() {
            var fighter = repository.findById("fighter");
            var asiLevels = fighter.getAsiLevels();
            assertTrue(asiLevels.contains(6), "Fighter should have ASI at level 6");
            assertTrue(asiLevels.contains(14), "Fighter should have ASI at level 14");
            assertEquals(7, asiLevels.size(), "Fighter should have 7 ASI levels");
        }

        @Test
        @DisplayName("Rogue has extra ASI at level 10")
        void rogueExtraAsi() {
            var rogue = repository.findById("rogue");
            var asiLevels = rogue.getAsiLevels();
            assertTrue(asiLevels.contains(10), "Rogue should have ASI at level 10");
            assertEquals(6, asiLevels.size(), "Rogue should have 6 ASI levels");
        }
    }
}
