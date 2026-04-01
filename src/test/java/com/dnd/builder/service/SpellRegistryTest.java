package com.dnd.builder.service;

import com.dnd.builder.model.SpellDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpellRegistry verifying D&D 5e 2014 spell accuracy.
 */
class SpellRegistryTest {

    private SpellRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SpellRegistry();
    }

    @Nested
    @DisplayName("Cantrip Class Lists")
    class CantripClassLists {

        @Test
        @DisplayName("Eldritch Blast is Warlock-only")
        void eldritchBlast() {
            SpellDefinition eb = registry.getById("eldritch_blast");
            assertNotNull(eb);
            assertEquals(0, eb.getLevel());
            assertEquals(List.of("warlock"), eb.getClasses());
        }

        @Test
        @DisplayName("Sacred Flame is Cleric-only")
        void sacredFlame() {
            SpellDefinition sf = registry.getById("sacred_flame");
            assertNotNull(sf);
            assertEquals(List.of("cleric"), sf.getClasses());
        }

        @Test
        @DisplayName("Vicious Mockery is Bard-only")
        void viciousMockery() {
            SpellDefinition vm = registry.getById("vicious_mockery");
            assertNotNull(vm);
            assertEquals(List.of("bard"), vm.getClasses());
        }

        @Test
        @DisplayName("Druidcraft is Druid-only")
        void druidcraft() {
            SpellDefinition dc = registry.getById("druidcraft");
            assertNotNull(dc);
            assertEquals(List.of("druid"), dc.getClasses());
        }

        @Test
        @DisplayName("Guidance is for Cleric and Druid")
        void guidance() {
            SpellDefinition g = registry.getById("guidance");
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
            SpellDefinition tw = registry.getById("thunderwave");
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
            SpellDefinition cp = registry.getById("charm_person");
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
            SpellDefinition peg = registry.getById("protection_evil_good");
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
            assertTrue(registry.getById("bless").isConcentration());
            assertTrue(registry.getById("hex").isConcentration());
            assertTrue(registry.getById("hunters_mark").isConcentration());
            assertTrue(registry.getById("hold_person").isConcentration());
        }

        @Test
        @DisplayName("Non-concentration spells are marked correctly")
        void nonConcentrationSpells() {
            assertFalse(registry.getById("magic_missile").isConcentration());
            assertFalse(registry.getById("cure_wounds").isConcentration());
            assertFalse(registry.getById("shield").isConcentration());
        }

        @Test
        @DisplayName("Ritual spells are marked correctly")
        void ritualSpells() {
            assertTrue(registry.getById("detect_magic").isRitual());
            assertTrue(registry.getById("find_familiar").isRitual());
            assertTrue(registry.getById("identify").isRitual());
            assertTrue(registry.getById("speak_with_animals").isRitual());
            assertTrue(registry.getById("alarm").isRitual());
        }

        @Test
        @DisplayName("Non-ritual spells are marked correctly")
        void nonRitualSpells() {
            assertFalse(registry.getById("fireball") != null && registry.getById("fireball").isRitual());
            assertFalse(registry.getById("magic_missile").isRitual());
            assertFalse(registry.getById("cure_wounds").isRitual());
        }
    }

    @Nested
    @DisplayName("Spell Schools")
    class SpellSchools {

        @Test
        @DisplayName("Healing spells are Evocation")
        void healingSchool() {
            assertEquals("Evocation", registry.getById("cure_wounds").getSchool());
            assertEquals("Evocation", registry.getById("healing_word").getSchool());
        }

        @Test
        @DisplayName("Shield spell is Abjuration")
        void shieldSchool() {
            assertEquals("Abjuration", registry.getById("shield").getSchool());
        }

        @Test
        @DisplayName("Charm Person is Enchantment")
        void charmSchool() {
            assertEquals("Enchantment", registry.getById("charm_person").getSchool());
        }
    }

    @Nested
    @DisplayName("Class-Specific Spells")
    class ClassSpecificSpells {

        @Test
        @DisplayName("Hunter's Mark is Ranger-only")
        void huntersMark() {
            SpellDefinition hm = registry.getById("hunters_mark");
            assertNotNull(hm);
            assertEquals(List.of("ranger"), hm.getClasses());
        }

        @Test
        @DisplayName("Hex is Warlock-only")
        void hex() {
            SpellDefinition h = registry.getById("hex");
            assertNotNull(h);
            assertEquals(List.of("warlock"), h.getClasses());
        }

        @Test
        @DisplayName("Dissonant Whispers is Bard-only")
        void dissonantWhispers() {
            SpellDefinition dw = registry.getById("dissonant_whispers");
            assertNotNull(dw);
            assertEquals(List.of("bard"), dw.getClasses());
        }

        @Test
        @DisplayName("Guiding Bolt is Cleric-only")
        void guidingBolt() {
            SpellDefinition gb = registry.getById("guiding_bolt");
            assertNotNull(gb);
            assertEquals(List.of("cleric"), gb.getClasses());
        }

        @Test
        @DisplayName("Inflict Wounds is Cleric-only")
        void inflictWounds() {
            SpellDefinition iw = registry.getById("inflict_wounds");
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
            List<SpellDefinition> wizardSpells = registry.forClass("wizard", null);
            assertFalse(wizardSpells.isEmpty());
            assertTrue(wizardSpells.stream().allMatch(s -> s.getClasses().contains("wizard")));
        }

        @Test
        @DisplayName("Can filter cantrips for a class")
        void filterCantrips() {
            List<SpellDefinition> clericCantrips = registry.cantripsFor("cleric");
            assertFalse(clericCantrips.isEmpty());
            assertTrue(clericCantrips.stream().allMatch(s -> s.getLevel() == 0));
            assertTrue(clericCantrips.stream().anyMatch(s -> s.getId().equals("sacred_flame")));
        }

        @Test
        @DisplayName("Can filter level 1 spells for a class")
        void filterLevel1() {
            List<SpellDefinition> bardLevel1 = registry.level1For("bard");
            assertFalse(bardLevel1.isEmpty());
            assertTrue(bardLevel1.stream().allMatch(s -> s.getLevel() == 1));
        }
    }

    @Test
    @DisplayName("Spells are sorted by level then name")
    void spellsSorted() {
        List<SpellDefinition> spells = registry.forClass("wizard", null);
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
