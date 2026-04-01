package com.dnd.builder.out.persistence;

import com.dnd.builder.core.                                                                                                                                                                                                                                                                        model.SpellDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemorySpellRepository verifying D&D 5e 2014 spell accuracy.
 */
class InMemorySpellRepositoryTest {

    private InMemorySpellRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySpellRepository();
    }

    @Nested
    @DisplayName("Cantrip Class Lists")
    class CantripClassLists {

        @Test
        @DisplayName("Eldritch Blast is Warlock-only")
        void eldritchBlast() {
            SpellDefinition eb = repository.findById("eldritch_blast");
            assertNotNull(eb);
            assertEquals(0, eb.getLevel());
            assertEquals(List.of("warlock"), eb.getClasses());
        }

        @Test
        @DisplayName("Sacred Flame is Cleric-only")
        void sacredFlame() {
            SpellDefinition sf = repository.findById("sacred_flame");
            assertNotNull(sf);
            assertEquals(List.of("cleric"), sf.getClasses());
        }

        @Test
        @DisplayName("Vicious Mockery is Bard-only")
        void viciousMockery() {
            SpellDefinition vm = repository.findById("vicious_mockery");
            assertNotNull(vm);
            assertEquals(List.of("bard"), vm.getClasses());
        }

        @Test
        @DisplayName("Druidcraft is Druid-only")
        void druidcraft() {
            SpellDefinition dc = repository.findById("druidcraft");
            assertNotNull(dc);
            assertEquals(List.of("druid"), dc.getClasses());
        }

        @Test
        @DisplayName("Guidance is for Cleric and Druid")
        void guidance() {
            SpellDefinition g = repository.findById("guidance");
            assertNotNull(g);
            assertTrue(g.getClasses().contains("cleric"));
            assertTrue(g.getClasses().contains("druid"));
            assertEquals(2, g.getClasses().size());
        }
    }

    @Nested
    @DisplayName("Spell Class List Corrections")
    class SpellClassListCorrections {

        @Test
        @DisplayName("Thunderwave should NOT include Cleric")
        void thunderwaveNotCleric() {
            SpellDefinition tw = repository.findById("thunderwave");
            assertNotNull(tw);
            assertFalse(tw.getClasses().contains("cleric"),
                "Thunderwave is not on Cleric spell list in PHB");
            assertTrue(tw.getClasses().contains("bard"));
            assertTrue(tw.getClasses().contains("druid"));
            assertTrue(tw.getClasses().contains("sorcerer"));
            assertTrue(tw.getClasses().contains("wizard"));
        }

        @Test
        @DisplayName("Charm Person should NOT include Druid")
        void charmPersonNotDruid() {
            SpellDefinition cp = repository.findById("charm_person");
            assertNotNull(cp);
            assertFalse(cp.getClasses().contains("druid"),
                "Charm Person is not on Druid spell list in PHB");
            assertTrue(cp.getClasses().contains("bard"));
            assertTrue(cp.getClasses().contains("sorcerer"));
            assertTrue(cp.getClasses().contains("warlock"));
            assertTrue(cp.getClasses().contains("wizard"));
        }

        @Test
        @DisplayName("Protection from Evil and Good should NOT include Druid")
        void protectionEvilGoodNotDruid() {
            SpellDefinition peg = repository.findById("protection_evil_good");
            assertNotNull(peg);
            assertFalse(peg.getClasses().contains("druid"),
                "Protection from Evil and Good is not on Druid spell list in PHB");
            assertTrue(peg.getClasses().contains("cleric"));
            assertTrue(peg.getClasses().contains("paladin"));
            assertTrue(peg.getClasses().contains("warlock"));
            assertTrue(peg.getClasses().contains("wizard"));
        }
    }

    @Nested
    @DisplayName("Spell Properties")
    class SpellProperties {

        @Test
        @DisplayName("Concentration spells are marked correctly")
        void concentrationSpells() {
            assertTrue(repository.findById("bless").isConcentration());
            assertTrue(repository.findById("hex").isConcentration());
            assertTrue(repository.findById("hunters_mark").isConcentration());
            assertTrue(repository.findById("hold_person").isConcentration());
        }

        @Test
        @DisplayName("Non-concentration spells are marked correctly")
        void nonConcentrationSpells() {
            assertFalse(repository.findById("magic_missile").isConcentration());
            assertFalse(repository.findById("cure_wounds").isConcentration());
            assertFalse(repository.findById("shield").isConcentration());
        }

        @Test
        @DisplayName("Ritual spells are marked correctly")
        void ritualSpells() {
            assertTrue(repository.findById("detect_magic").isRitual());
            assertTrue(repository.findById("find_familiar").isRitual());
            assertTrue(repository.findById("identify").isRitual());
            assertTrue(repository.findById("speak_with_animals").isRitual());
            assertTrue(repository.findById("alarm").isRitual());
        }

        @Test
        @DisplayName("Non-ritual spells are marked correctly")
        void nonRitualSpells() {
            assertFalse(repository.findById("fireball") != null && repository.findById("fireball").isRitual());
            assertFalse(repository.findById("magic_missile").isRitual());
            assertFalse(repository.findById("cure_wounds").isRitual());
        }
    }

    @Nested
    @DisplayName("Spell Schools")
    class SpellSchools {

        @Test
        @DisplayName("Healing spells are Evocation")
        void healingSchool() {
            assertEquals("Evocation", repository.findById("cure_wounds").getSchool());
            assertEquals("Evocation", repository.findById("healing_word").getSchool());
        }

        @Test
        @DisplayName("Shield spell is Abjuration")
        void shieldSchool() {
            assertEquals("Abjuration", repository.findById("shield").getSchool());
        }

        @Test
        @DisplayName("Charm Person is Enchantment")
        void charmSchool() {
            assertEquals("Enchantment", repository.findById("charm_person").getSchool());
        }
    }

    @Nested
    @DisplayName("Class-Specific Spells")
    class ClassSpecificSpells {

        @Test
        @DisplayName("Hunter's Mark is Ranger-only")
        void huntersMark() {
            SpellDefinition hm = repository.findById("hunters_mark");
            assertNotNull(hm);
            assertEquals(List.of("ranger"), hm.getClasses());
        }

        @Test
        @DisplayName("Hex is Warlock-only")
        void hex() {
            SpellDefinition h = repository.findById("hex");
            assertNotNull(h);
            assertEquals(List.of("warlock"), h.getClasses());
        }

        @Test
        @DisplayName("Dissonant Whispers is Bard-only")
        void dissonantWhispers() {
            SpellDefinition dw = repository.findById("dissonant_whispers");
            assertNotNull(dw);
            assertEquals(List.of("bard"), dw.getClasses());
        }

        @Test
        @DisplayName("Guiding Bolt is Cleric-only")
        void guidingBolt() {
            SpellDefinition gb = repository.findById("guiding_bolt");
            assertNotNull(gb);
            assertEquals(List.of("cleric"), gb.getClasses());
        }

        @Test
        @DisplayName("Inflict Wounds is Cleric-only")
        void inflictWounds() {
            SpellDefinition iw = repository.findById("inflict_wounds");
            assertNotNull(iw);
            assertEquals(List.of("cleric"), iw.getClasses());
        }
    }

    @Nested
    @DisplayName("Spell Filtering")
    class SpellFiltering {

        @Test
        @DisplayName("Can filter spells by class")
        void filterByClass() {
            List<SpellDefinition> wizardSpells = repository.findByClass("wizard", null);
            assertFalse(wizardSpells.isEmpty());
            assertTrue(wizardSpells.stream().allMatch(s -> s.getClasses().contains("wizard")));
        }

        @Test
        @DisplayName("Can filter cantrips for a class")
        void filterCantrips() {
            List<SpellDefinition> clericCantrips = repository.findCantripsForClass("cleric");
            assertFalse(clericCantrips.isEmpty());
            assertTrue(clericCantrips.stream().allMatch(s -> s.getLevel() == 0));
            assertTrue(clericCantrips.stream().anyMatch(s -> s.getId().equals("sacred_flame")));
        }

        @Test
        @DisplayName("Can filter level 1 spells for a class")
        void filterLevel1() {
            List<SpellDefinition> bardLevel1 = repository.findLevel1ForClass("bard");
            assertFalse(bardLevel1.isEmpty());
            assertTrue(bardLevel1.stream().allMatch(s -> s.getLevel() == 1));
        }
    }

    @Test
    @DisplayName("Spells are sorted by level then name")
    void spellsSorted() {
        List<SpellDefinition> spells = repository.findByClass("wizard", null);
        for (int i = 1; i < spells.size(); i++) {
            SpellDefinition prev = spells.get(i - 1);
            SpellDefinition curr = spells.get(i);
            assertTrue(prev.getLevel() < curr.getLevel() ||
                    (prev.getLevel() == curr.getLevel() &&
                     prev.getName().compareTo(curr.getName()) <= 0),
                "Spells should be sorted by level, then name");
        }
    }
}
