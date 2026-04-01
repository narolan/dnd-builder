package com.dnd.builder.service;

import com.dnd.builder.model.Race;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RaceRegistry verifying D&D 5e 2014 + sourcebook accuracy.
 */
class RaceRegistryTest {

    private RaceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RaceRegistry();
    }

    @Nested
    @DisplayName("PHB Races")
    class PhbRaces {

        @Test
        @DisplayName("Human (Standard) should have +1 to all ability scores")
        void humanStandard() {
            Race human = registry.getById("human_standard");
            assertNotNull(human);
            assertEquals("Human", human.getName());
            assertEquals(1, human.getFixedBonuses().get("STR"));
            assertEquals(1, human.getFixedBonuses().get("DEX"));
            assertEquals(1, human.getFixedBonuses().get("CON"));
            assertEquals(1, human.getFixedBonuses().get("INT"));
            assertEquals(1, human.getFixedBonuses().get("WIS"));
            assertEquals(1, human.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Variant Human should not exist (disabled per user request)")
        void variantHumanDisabled() {
            assertNull(registry.getById("human_variant"));
        }

        @Test
        @DisplayName("Hill Dwarf should have CON +2, WIS +1")
        void hillDwarf() {
            Race dwarf = registry.getById("dwarf_hill");
            assertNotNull(dwarf);
            assertEquals(2, dwarf.getFixedBonuses().get("CON"));
            assertEquals(1, dwarf.getFixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Mountain Dwarf should have STR +2, CON +2")
        void mountainDwarf() {
            Race dwarf = registry.getById("dwarf_mountain");
            assertNotNull(dwarf);
            assertEquals(2, dwarf.getFixedBonuses().get("STR"));
            assertEquals(2, dwarf.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("High Elf should have DEX +2, INT +1")
        void highElf() {
            Race elf = registry.getById("elf_high");
            assertNotNull(elf);
            assertEquals(2, elf.getFixedBonuses().get("DEX"));
            assertEquals(1, elf.getFixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Wood Elf should have DEX +2, WIS +1")
        void woodElf() {
            Race elf = registry.getById("elf_wood");
            assertNotNull(elf);
            assertEquals(2, elf.getFixedBonuses().get("DEX"));
            assertEquals(1, elf.getFixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Drow should have DEX +2, CHA +1")
        void drow() {
            Race elf = registry.getById("elf_drow");
            assertNotNull(elf);
            assertEquals(2, elf.getFixedBonuses().get("DEX"));
            assertEquals(1, elf.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Lightfoot Halfling should have DEX +2, CHA +1")
        void lightfootHalfling() {
            Race halfling = registry.getById("halfling_lightfoot");
            assertNotNull(halfling);
            assertEquals(2, halfling.getFixedBonuses().get("DEX"));
            assertEquals(1, halfling.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Stout Halfling should have DEX +2, CON +1")
        void stoutHalfling() {
            Race halfling = registry.getById("halfling_stout");
            assertNotNull(halfling);
            assertEquals(2, halfling.getFixedBonuses().get("DEX"));
            assertEquals(1, halfling.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Dragonborn should have STR +2, CHA +1")
        void dragonborn() {
            Race db = registry.getById("dragonborn");
            assertNotNull(db);
            assertEquals(2, db.getFixedBonuses().get("STR"));
            assertEquals(1, db.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Forest Gnome should have INT +2, DEX +1")
        void forestGnome() {
            Race gnome = registry.getById("gnome_forest");
            assertNotNull(gnome);
            assertEquals(2, gnome.getFixedBonuses().get("INT"));
            assertEquals(1, gnome.getFixedBonuses().get("DEX"));
        }

        @Test
        @DisplayName("Rock Gnome should have INT +2, CON +1")
        void rockGnome() {
            Race gnome = registry.getById("gnome_rock");
            assertNotNull(gnome);
            assertEquals(2, gnome.getFixedBonuses().get("INT"));
            assertEquals(1, gnome.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Half-Elf should have CHA +2 and flexible +1 to two others")
        void halfElf() {
            Race he = registry.getById("half_elf");
            assertNotNull(he);
            assertEquals(2, he.getFixedBonuses().get("CHA"));
            assertNotNull(he.getFlexibleBonuses());
            assertEquals(1, he.getFlexibleBonuses().size());
            assertEquals(2, he.getFlexibleBonuses().get(0).getCount());
            assertEquals(1, he.getFlexibleBonuses().get(0).getAmount());
            assertTrue(he.getFlexibleBonuses().get(0).getExcludedStats().contains("CHA"));
        }

        @Test
        @DisplayName("Half-Orc should have STR +2, CON +1")
        void halfOrc() {
            Race ho = registry.getById("half_orc");
            assertNotNull(ho);
            assertEquals(2, ho.getFixedBonuses().get("STR"));
            assertEquals(1, ho.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Tiefling should have INT +1, CHA +2")
        void tiefling() {
            Race t = registry.getById("tiefling");
            assertNotNull(t);
            assertEquals(1, t.getFixedBonuses().get("INT"));
            assertEquals(2, t.getFixedBonuses().get("CHA"));
        }
    }

    @Nested
    @DisplayName("MToF Races")
    class MtofRaces {

        @Test
        @DisplayName("Githzerai should have INT +1, WIS +2")
        void githzerai() {
            Race g = registry.getById("githzerai");
            assertNotNull(g);
            assertEquals("MToF", g.getSource());
            assertEquals(1, g.getFixedBonuses().get("INT"));
            assertEquals(2, g.getFixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Githyanki should have STR +2, INT +1 (per MToF)")
        void githyanki() {
            Race g = registry.getById("githyanki");
            assertNotNull(g);
            assertEquals("MToF", g.getSource());
            assertEquals(2, g.getFixedBonuses().get("STR"));
            assertEquals(1, g.getFixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Eladrin should have DEX +2, CHA +1")
        void eladrin() {
            Race e = registry.getById("elf_eladrin");
            assertNotNull(e);
            assertEquals(2, e.getFixedBonuses().get("DEX"));
            assertEquals(1, e.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Sea Elf should have DEX +2, CON +1")
        void seaElf() {
            Race se = registry.getById("elf_sea");
            assertNotNull(se);
            assertEquals(2, se.getFixedBonuses().get("DEX"));
            assertEquals(1, se.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Shadar-kai should have DEX +2, CON +1")
        void shadarKai() {
            Race sk = registry.getById("elf_shadarkai");
            assertNotNull(sk);
            assertEquals(2, sk.getFixedBonuses().get("DEX"));
            assertEquals(1, sk.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Duergar should have CON +2, STR +1")
        void duergar() {
            Race d = registry.getById("dwarf_duergar");
            assertNotNull(d);
            assertEquals(2, d.getFixedBonuses().get("CON"));
            assertEquals(1, d.getFixedBonuses().get("STR"));
        }
    }

    @Nested
    @DisplayName("VGtM Races")
    class VgtmRaces {

        @Test
        @DisplayName("Aasimar should have CHA +2")
        void aasimar() {
            Race a = registry.getById("aasimar");
            assertNotNull(a);
            assertEquals("VGtM", a.getSource());
            assertEquals(2, a.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Firbolg should have WIS +2, STR +1")
        void firbolg() {
            Race f = registry.getById("firbolg");
            assertNotNull(f);
            assertEquals(2, f.getFixedBonuses().get("WIS"));
            assertEquals(1, f.getFixedBonuses().get("STR"));
        }

        @Test
        @DisplayName("Goliath should have STR +2, CON +1")
        void goliath() {
            Race g = registry.getById("goliath");
            assertNotNull(g);
            assertEquals(2, g.getFixedBonuses().get("STR"));
            assertEquals(1, g.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Kenku should have DEX +2, WIS +1")
        void kenku() {
            Race k = registry.getById("kenku");
            assertNotNull(k);
            assertEquals(2, k.getFixedBonuses().get("DEX"));
            assertEquals(1, k.getFixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Lizardfolk should have CON +2, WIS +1")
        void lizardfolk() {
            Race l = registry.getById("lizardfolk");
            assertNotNull(l);
            assertEquals(2, l.getFixedBonuses().get("CON"));
            assertEquals(1, l.getFixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Tabaxi should have DEX +2, CHA +1")
        void tabaxi() {
            Race t = registry.getById("tabaxi");
            assertNotNull(t);
            assertEquals(2, t.getFixedBonuses().get("DEX"));
            assertEquals(1, t.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Triton should have STR +1, CON +1, CHA +1")
        void triton() {
            Race t = registry.getById("triton");
            assertNotNull(t);
            assertEquals(1, t.getFixedBonuses().get("STR"));
            assertEquals(1, t.getFixedBonuses().get("CON"));
            assertEquals(1, t.getFixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Yuan-ti Pureblood should have CHA +2, INT +1")
        void yuanTi() {
            Race y = registry.getById("yuanti");
            assertNotNull(y);
            assertEquals(2, y.getFixedBonuses().get("CHA"));
            assertEquals(1, y.getFixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Bugbear should have STR +2, DEX +1")
        void bugbear() {
            Race b = registry.getById("bugbear");
            assertNotNull(b);
            assertEquals(2, b.getFixedBonuses().get("STR"));
            assertEquals(1, b.getFixedBonuses().get("DEX"));
        }

        @Test
        @DisplayName("Goblin should have DEX +2, CON +1")
        void goblin() {
            Race g = registry.getById("goblin");
            assertNotNull(g);
            assertEquals(2, g.getFixedBonuses().get("DEX"));
            assertEquals(1, g.getFixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Hobgoblin should have CON +2, INT +1")
        void hobgoblin() {
            Race h = registry.getById("hobgoblin");
            assertNotNull(h);
            assertEquals(2, h.getFixedBonuses().get("CON"));
            assertEquals(1, h.getFixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Kobold should have DEX +2, STR -2")
        void kobold() {
            Race k = registry.getById("kobold");
            assertNotNull(k);
            assertEquals(2, k.getFixedBonuses().get("DEX"));
            assertEquals(-2, k.getFixedBonuses().get("STR"));
        }

        @Test
        @DisplayName("Orc should have STR +2, CON +1, INT -2")
        void orc() {
            Race o = registry.getById("orc");
            assertNotNull(o);
            assertEquals(2, o.getFixedBonuses().get("STR"));
            assertEquals(1, o.getFixedBonuses().get("CON"));
            assertEquals(-2, o.getFixedBonuses().get("INT"));
        }
    }

    @Nested
    @DisplayName("Race Speeds")
    class RaceSpeeds {

        @Test
        @DisplayName("Dwarves should have 25 ft speed")
        void dwarfSpeed() {
            assertEquals(25, registry.getSpeed("dwarf_hill"));
            assertEquals(25, registry.getSpeed("dwarf_mountain"));
            assertEquals(25, registry.getSpeed("dwarf_duergar"));
        }

        @Test
        @DisplayName("Halflings should have 25 ft speed")
        void halflingSpeed() {
            assertEquals(25, registry.getSpeed("halfling_lightfoot"));
            assertEquals(25, registry.getSpeed("halfling_stout"));
            assertEquals(25, registry.getSpeed("halfling_ghostwise"));
        }

        @Test
        @DisplayName("Gnomes should have 25 ft speed")
        void gnomeSpeed() {
            assertEquals(25, registry.getSpeed("gnome_forest"));
            assertEquals(25, registry.getSpeed("gnome_rock"));
            assertEquals(25, registry.getSpeed("gnome_deep"));
        }

        @Test
        @DisplayName("Wood Elf should have 35 ft speed")
        void woodElfSpeed() {
            assertEquals(35, registry.getSpeed("elf_wood"));
        }

        @Test
        @DisplayName("Most races should have 30 ft speed")
        void standardSpeed() {
            assertEquals(30, registry.getSpeed("human_standard"));
            assertEquals(30, registry.getSpeed("elf_high"));
            assertEquals(30, registry.getSpeed("dragonborn"));
            assertEquals(30, registry.getSpeed("half_elf"));
            assertEquals(30, registry.getSpeed("tiefling"));
        }

        @Test
        @DisplayName("Goblin should have 25 ft speed")
        void goblinSpeed() {
            assertEquals(25, registry.getSpeed("goblin"));
        }
    }

    @Nested
    @DisplayName("Point Buy System")
    class PointBuy {

        @Test
        @DisplayName("Point costs should follow PHB rules")
        void pointCosts() {
            assertEquals(0, RaceRegistry.POINT_COSTS.get(8));
            assertEquals(1, RaceRegistry.POINT_COSTS.get(9));
            assertEquals(2, RaceRegistry.POINT_COSTS.get(10));
            assertEquals(3, RaceRegistry.POINT_COSTS.get(11));
            assertEquals(4, RaceRegistry.POINT_COSTS.get(12));
            assertEquals(5, RaceRegistry.POINT_COSTS.get(13));
            assertEquals(7, RaceRegistry.POINT_COSTS.get(14));
            assertEquals(9, RaceRegistry.POINT_COSTS.get(15));
        }

        @Test
        @DisplayName("Point budget should be 27")
        void pointBudget() {
            assertEquals(27, RaceRegistry.POINT_BUDGET);
        }

        @Test
        @DisplayName("Score range should be 8-15")
        void scoreRange() {
            assertEquals(8, RaceRegistry.SCORE_MIN);
            assertEquals(15, RaceRegistry.SCORE_MAX);
        }
    }
}
