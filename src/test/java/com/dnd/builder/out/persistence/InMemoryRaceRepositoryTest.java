package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.Race;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InMemoryRaceRepository verifying D&D 5e 2014 + sourcebook accuracy.
 */
class InMemoryRaceRepositoryTest {

    private InMemoryRaceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRaceRepository();
    }

    @Nested
    @DisplayName("PHB Races")
    class PhbRaces {

        @Test
        @DisplayName("Human (Standard) should have +1 to all ability scores")
        void humanStandard() {
            Race human = repository.findById("human_standard");
            assertNotNull(human);
            assertEquals("Human", human.name());
            assertEquals(1, human.fixedBonuses().get("STR"));
            assertEquals(1, human.fixedBonuses().get("DEX"));
            assertEquals(1, human.fixedBonuses().get("CON"));
            assertEquals(1, human.fixedBonuses().get("INT"));
            assertEquals(1, human.fixedBonuses().get("WIS"));
            assertEquals(1, human.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Variant Human should not exist (disabled per user request)")
        void variantHumanDisabled() {
            assertNull(repository.findById("human_variant"));
        }

        @Test
        @DisplayName("Hill Dwarf should have CON +2, WIS +1")
        void hillDwarf() {
            Race dwarf = repository.findById("dwarf_hill");
            assertNotNull(dwarf);
            assertEquals(2, dwarf.fixedBonuses().get("CON"));
            assertEquals(1, dwarf.fixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Mountain Dwarf should have STR +2, CON +2")
        void mountainDwarf() {
            Race dwarf = repository.findById("dwarf_mountain");
            assertNotNull(dwarf);
            assertEquals(2, dwarf.fixedBonuses().get("STR"));
            assertEquals(2, dwarf.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("High Elf should have DEX +2, INT +1")
        void highElf() {
            Race elf = repository.findById("elf_high");
            assertNotNull(elf);
            assertEquals(2, elf.fixedBonuses().get("DEX"));
            assertEquals(1, elf.fixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Wood Elf should have DEX +2, WIS +1")
        void woodElf() {
            Race elf = repository.findById("elf_wood");
            assertNotNull(elf);
            assertEquals(2, elf.fixedBonuses().get("DEX"));
            assertEquals(1, elf.fixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Drow should have DEX +2, CHA +1")
        void drow() {
            Race elf = repository.findById("elf_drow");
            assertNotNull(elf);
            assertEquals(2, elf.fixedBonuses().get("DEX"));
            assertEquals(1, elf.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Lightfoot Halfling should have DEX +2, CHA +1")
        void lightfootHalfling() {
            Race halfling = repository.findById("halfling_lightfoot");
            assertNotNull(halfling);
            assertEquals(2, halfling.fixedBonuses().get("DEX"));
            assertEquals(1, halfling.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Stout Halfling should have DEX +2, CON +1")
        void stoutHalfling() {
            Race halfling = repository.findById("halfling_stout");
            assertNotNull(halfling);
            assertEquals(2, halfling.fixedBonuses().get("DEX"));
            assertEquals(1, halfling.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Dragonborn should have STR +2, CHA +1")
        void dragonborn() {
            Race db = repository.findById("dragonborn");
            assertNotNull(db);
            assertEquals(2, db.fixedBonuses().get("STR"));
            assertEquals(1, db.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Forest Gnome should have INT +2, DEX +1")
        void forestGnome() {
            Race gnome = repository.findById("gnome_forest");
            assertNotNull(gnome);
            assertEquals(2, gnome.fixedBonuses().get("INT"));
            assertEquals(1, gnome.fixedBonuses().get("DEX"));
        }

        @Test
        @DisplayName("Rock Gnome should have INT +2, CON +1")
        void rockGnome() {
            Race gnome = repository.findById("gnome_rock");
            assertNotNull(gnome);
            assertEquals(2, gnome.fixedBonuses().get("INT"));
            assertEquals(1, gnome.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Half-Elf should have CHA +2 and flexible +1 to two others")
        void halfElf() {
            Race he = repository.findById("half_elf");
            assertNotNull(he);
            assertEquals(2, he.fixedBonuses().get("CHA"));
            assertNotNull(he.flexibleBonuses());
            assertEquals(1, he.flexibleBonuses().size());
            assertEquals(2, he.flexibleBonuses().get(0).count());
            assertEquals(1, he.flexibleBonuses().get(0).amount());
            assertTrue(he.flexibleBonuses().get(0).excludedStats().contains("CHA"));
        }

        @Test
        @DisplayName("Half-Orc should have STR +2, CON +1")
        void halfOrc() {
            Race ho = repository.findById("half_orc");
            assertNotNull(ho);
            assertEquals(2, ho.fixedBonuses().get("STR"));
            assertEquals(1, ho.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Tiefling should have INT +1, CHA +2")
        void tiefling() {
            Race t = repository.findById("tiefling");
            assertNotNull(t);
            assertEquals(1, t.fixedBonuses().get("INT"));
            assertEquals(2, t.fixedBonuses().get("CHA"));
        }
    }

    @Nested
    @DisplayName("MToF Races")
    class MtofRaces {

        @Test
        @DisplayName("Githzerai should have INT +1, WIS +2")
        void githzerai() {
            Race g = repository.findById("githzerai");
            assertNotNull(g);
            assertEquals("MToF", g.source());
            assertEquals(1, g.fixedBonuses().get("INT"));
            assertEquals(2, g.fixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Githyanki should have STR +2, INT +1 (per MToF)")
        void githyanki() {
            Race g = repository.findById("githyanki");
            assertNotNull(g);
            assertEquals("MToF", g.source());
            assertEquals(2, g.fixedBonuses().get("STR"));
            assertEquals(1, g.fixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Eladrin should have DEX +2, CHA +1")
        void eladrin() {
            Race e = repository.findById("elf_eladrin");
            assertNotNull(e);
            assertEquals(2, e.fixedBonuses().get("DEX"));
            assertEquals(1, e.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Sea Elf should have DEX +2, CON +1")
        void seaElf() {
            Race se = repository.findById("elf_sea");
            assertNotNull(se);
            assertEquals(2, se.fixedBonuses().get("DEX"));
            assertEquals(1, se.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Shadar-kai should have DEX +2, CON +1")
        void shadarKai() {
            Race sk = repository.findById("elf_shadarkai");
            assertNotNull(sk);
            assertEquals(2, sk.fixedBonuses().get("DEX"));
            assertEquals(1, sk.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Duergar should have CON +2, STR +1")
        void duergar() {
            Race d = repository.findById("dwarf_duergar");
            assertNotNull(d);
            assertEquals(2, d.fixedBonuses().get("CON"));
            assertEquals(1, d.fixedBonuses().get("STR"));
        }
    }

    @Nested
    @DisplayName("VGtM Races")
    class VgtmRaces {

        @Test
        @DisplayName("Aasimar should have CHA +2")
        void aasimar() {
            Race a = repository.findById("aasimar");
            assertNotNull(a);
            assertEquals("VGtM", a.source());
            assertEquals(2, a.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Firbolg should have WIS +2, STR +1")
        void firbolg() {
            Race f = repository.findById("firbolg");
            assertNotNull(f);
            assertEquals(2, f.fixedBonuses().get("WIS"));
            assertEquals(1, f.fixedBonuses().get("STR"));
        }

        @Test
        @DisplayName("Goliath should have STR +2, CON +1")
        void goliath() {
            Race g = repository.findById("goliath");
            assertNotNull(g);
            assertEquals(2, g.fixedBonuses().get("STR"));
            assertEquals(1, g.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Kenku should have DEX +2, WIS +1")
        void kenku() {
            Race k = repository.findById("kenku");
            assertNotNull(k);
            assertEquals(2, k.fixedBonuses().get("DEX"));
            assertEquals(1, k.fixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Lizardfolk should have CON +2, WIS +1")
        void lizardfolk() {
            Race l = repository.findById("lizardfolk");
            assertNotNull(l);
            assertEquals(2, l.fixedBonuses().get("CON"));
            assertEquals(1, l.fixedBonuses().get("WIS"));
        }

        @Test
        @DisplayName("Tabaxi should have DEX +2, CHA +1")
        void tabaxi() {
            Race t = repository.findById("tabaxi");
            assertNotNull(t);
            assertEquals(2, t.fixedBonuses().get("DEX"));
            assertEquals(1, t.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Triton should have STR +1, CON +1, CHA +1")
        void triton() {
            Race t = repository.findById("triton");
            assertNotNull(t);
            assertEquals(1, t.fixedBonuses().get("STR"));
            assertEquals(1, t.fixedBonuses().get("CON"));
            assertEquals(1, t.fixedBonuses().get("CHA"));
        }

        @Test
        @DisplayName("Yuan-ti Pureblood should have CHA +2, INT +1")
        void yuanTi() {
            Race y = repository.findById("yuanti");
            assertNotNull(y);
            assertEquals(2, y.fixedBonuses().get("CHA"));
            assertEquals(1, y.fixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Bugbear should have STR +2, DEX +1")
        void bugbear() {
            Race b = repository.findById("bugbear");
            assertNotNull(b);
            assertEquals(2, b.fixedBonuses().get("STR"));
            assertEquals(1, b.fixedBonuses().get("DEX"));
        }

        @Test
        @DisplayName("Goblin should have DEX +2, CON +1")
        void goblin() {
            Race g = repository.findById("goblin");
            assertNotNull(g);
            assertEquals(2, g.fixedBonuses().get("DEX"));
            assertEquals(1, g.fixedBonuses().get("CON"));
        }

        @Test
        @DisplayName("Hobgoblin should have CON +2, INT +1")
        void hobgoblin() {
            Race h = repository.findById("hobgoblin");
            assertNotNull(h);
            assertEquals(2, h.fixedBonuses().get("CON"));
            assertEquals(1, h.fixedBonuses().get("INT"));
        }

        @Test
        @DisplayName("Kobold should have DEX +2, STR -2")
        void kobold() {
            Race k = repository.findById("kobold");
            assertNotNull(k);
            assertEquals(2, k.fixedBonuses().get("DEX"));
            assertEquals(-2, k.fixedBonuses().get("STR"));
        }

        @Test
        @DisplayName("Orc should have STR +2, CON +1, INT -2")
        void orc() {
            Race o = repository.findById("orc");
            assertNotNull(o);
            assertEquals(2, o.fixedBonuses().get("STR"));
            assertEquals(1, o.fixedBonuses().get("CON"));
            assertEquals(-2, o.fixedBonuses().get("INT"));
        }
    }

    @Nested
    @DisplayName("Race Speeds")
    class RaceSpeeds {

        @Test
        @DisplayName("Dwarves should have 25 ft speed")
        void dwarfSpeed() {
            assertEquals(25, repository.getSpeed("dwarf_hill"));
            assertEquals(25, repository.getSpeed("dwarf_mountain"));
            assertEquals(25, repository.getSpeed("dwarf_duergar"));
        }

        @Test
        @DisplayName("Halflings should have 25 ft speed")
        void halflingSpeed() {
            assertEquals(25, repository.getSpeed("halfling_lightfoot"));
            assertEquals(25, repository.getSpeed("halfling_stout"));
            assertEquals(25, repository.getSpeed("halfling_ghostwise"));
        }

        @Test
        @DisplayName("Gnomes should have 25 ft speed")
        void gnomeSpeed() {
            assertEquals(25, repository.getSpeed("gnome_forest"));
            assertEquals(25, repository.getSpeed("gnome_rock"));
            assertEquals(25, repository.getSpeed("gnome_deep"));
        }

        @Test
        @DisplayName("Wood Elf should have 35 ft speed")
        void woodElfSpeed() {
            assertEquals(35, repository.getSpeed("elf_wood"));
        }

        @Test
        @DisplayName("Most races should have 30 ft speed")
        void standardSpeed() {
            assertEquals(30, repository.getSpeed("human_standard"));
            assertEquals(30, repository.getSpeed("elf_high"));
            assertEquals(30, repository.getSpeed("dragonborn"));
            assertEquals(30, repository.getSpeed("half_elf"));
            assertEquals(30, repository.getSpeed("tiefling"));
        }

        @Test
        @DisplayName("Goblin should have 25 ft speed")
        void goblinSpeed() {
            assertEquals(25, repository.getSpeed("goblin"));
        }
    }

    @Nested
    @DisplayName("Point Buy System")
    class PointBuy {

        @Test
        @DisplayName("Point costs should follow PHB rules")
        void pointCosts() {
            assertEquals(0, InMemoryRaceRepository.POINT_COSTS.get(8));
            assertEquals(1, InMemoryRaceRepository.POINT_COSTS.get(9));
            assertEquals(2, InMemoryRaceRepository.POINT_COSTS.get(10));
            assertEquals(3, InMemoryRaceRepository.POINT_COSTS.get(11));
            assertEquals(4, InMemoryRaceRepository.POINT_COSTS.get(12));
            assertEquals(5, InMemoryRaceRepository.POINT_COSTS.get(13));
            assertEquals(7, InMemoryRaceRepository.POINT_COSTS.get(14));
            assertEquals(9, InMemoryRaceRepository.POINT_COSTS.get(15));
        }

        @Test
        @DisplayName("Point budget should be 27")
        void pointBudget() {
            assertEquals(27, InMemoryRaceRepository.POINT_BUDGET);
        }

        @Test
        @DisplayName("Score range should be 8-15")
        void scoreRange() {
            assertEquals(8, InMemoryRaceRepository.SCORE_MIN);
            assertEquals(15, InMemoryRaceRepository.SCORE_MAX);
        }
    }
}
