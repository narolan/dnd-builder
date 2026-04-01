package com.dnd.builder.service;

import com.dnd.builder.model.FlexibleBonus;
import com.dnd.builder.model.Race;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RaceRegistry {

    private final List<Race> races;
    private final Map<String, Race> byId;

    public static final Map<Integer, Integer> POINT_COSTS;
    static {
        POINT_COSTS = new LinkedHashMap<>();
        POINT_COSTS.put(8,0); POINT_COSTS.put(9,1); POINT_COSTS.put(10,2);
        POINT_COSTS.put(11,3); POINT_COSTS.put(12,4); POINT_COSTS.put(13,5);
        POINT_COSTS.put(14,7); POINT_COSTS.put(15,9);
    }
    public static final int POINT_BUDGET = 27;
    public static final int SCORE_MIN = 8;
    public static final int SCORE_MAX = 15;

    /** Base speed by race ID */
    private static final Map<String, Integer> SPEED = Map.ofEntries(
        Map.entry("human_standard",30), Map.entry("human_variant",30),
        Map.entry("dwarf_hill",25), Map.entry("dwarf_mountain",25), Map.entry("dwarf_duergar",25),
        Map.entry("elf_high",30), Map.entry("elf_wood",35), Map.entry("elf_drow",30),
        Map.entry("elf_eladrin",30), Map.entry("elf_sea",30), Map.entry("elf_shadarkai",30),
        Map.entry("halfling_lightfoot",25), Map.entry("halfling_stout",25), Map.entry("halfling_ghostwise",25),
        Map.entry("dragonborn",30), Map.entry("gnome_forest",25), Map.entry("gnome_rock",25), Map.entry("gnome_deep",25),
        Map.entry("half_elf",30), Map.entry("half_orc",30), Map.entry("tiefling",30),
        Map.entry("githzerai",30), Map.entry("githyanki",30),
        Map.entry("aasimar",30), Map.entry("firbolg",30), Map.entry("goliath",30),
        Map.entry("kenku",30), Map.entry("lizardfolk",30), Map.entry("tabaxi",30),
        Map.entry("triton",30), Map.entry("yuanti",30), Map.entry("bugbear",30),
        Map.entry("goblin",25), Map.entry("hobgoblin",30), Map.entry("kobold",30),
        Map.entry("orc",30)
    );

    public RaceRegistry() {
        races = buildRaces();
        byId  = new HashMap<>();
        races.forEach(r -> byId.put(r.getId(), r));
    }

    public List<Race> getAllRaces()         { return Collections.unmodifiableList(races); }
    public Race getById(String id)          { return byId.get(id); }
    public int getSpeed(String raceId)      { return SPEED.getOrDefault(raceId, 30); }

    private List<Race> buildRaces() {
        var list = new ArrayList<Race>();

        // ── PHB ──────────────────────────────────────────────────────────────
        list.add(r("human_standard","Human (Standard)","PHB", m("STR",1,"DEX",1,"CON",1,"INT",1,"WIS",1,"CHA",1)));
        list.add(rf("human_variant","Human (Variant)","PHB", Map.of(),
            List.of(new FlexibleBonus(2,1,List.of(),"+1 to two different ability scores of your choice"))));
        list.add(r("dwarf_hill",    "Dwarf (Hill)",    "PHB", m("CON",2,"WIS",1)));
        list.add(r("dwarf_mountain","Dwarf (Mountain)","PHB", m("STR",2,"CON",2)));
        list.add(r("elf_high",      "Elf (High)",      "PHB", m("DEX",2,"INT",1)));
        list.add(r("elf_wood",      "Elf (Wood)",      "PHB", m("DEX",2,"WIS",1)));
        list.add(r("elf_drow",      "Elf (Drow)",      "PHB", m("DEX",2,"CHA",1)));
        list.add(r("halfling_lightfoot","Halfling (Lightfoot)","PHB", m("DEX",2,"CHA",1)));
        list.add(r("halfling_stout",    "Halfling (Stout)",    "PHB", m("DEX",2,"CON",1)));
        list.add(r("dragonborn",    "Dragonborn",      "PHB", m("STR",2,"CHA",1)));
        list.add(r("gnome_forest",  "Gnome (Forest)",  "PHB", m("INT",2,"DEX",1)));
        list.add(r("gnome_rock",    "Gnome (Rock)",    "PHB", m("INT",2,"CON",1)));
        list.add(rf("half_elf","Half-Elf","PHB", m("CHA",2),
            List.of(new FlexibleBonus(2,1,List.of("CHA"),"+1 to two different ability scores other than CHA"))));
        list.add(r("half_orc",  "Half-Orc", "PHB", m("STR",2,"CON",1)));
        list.add(r("tiefling",  "Tiefling", "PHB", m("INT",1,"CHA",2)));

        // ── MToF ─────────────────────────────────────────────────────────────
        list.add(r("githzerai",    "Githzerai",        "MToF", m("INT",1,"WIS",2)));
        list.add(r("githyanki",    "Githyanki",        "MToF", m("INT",2,"STR",1)));
        list.add(r("elf_eladrin",  "Elf (Eladrin)",    "MToF", m("DEX",2,"CHA",1)));
        list.add(r("elf_sea",      "Elf (Sea)",        "MToF", m("DEX",2,"CON",1)));
        list.add(r("elf_shadarkai","Elf (Shadar-kai)", "MToF", m("DEX",2,"CON",1)));
        list.add(r("dwarf_duergar","Dwarf (Duergar)",  "MToF", m("CON",2,"STR",1)));

        // ── VGtM ─────────────────────────────────────────────────────────────
        list.add(r("aasimar",   "Aasimar",            "VGtM", m("CHA",2)));
        list.add(r("firbolg",   "Firbolg",            "VGtM", m("WIS",2,"STR",1)));
        list.add(r("goliath",   "Goliath",            "VGtM", m("STR",2,"CON",1)));
        list.add(r("kenku",     "Kenku",              "VGtM", m("DEX",2,"WIS",1)));
        list.add(r("lizardfolk","Lizardfolk",         "VGtM", m("CON",2,"WIS",1)));
        list.add(r("tabaxi",    "Tabaxi",             "VGtM", m("DEX",2,"CHA",1)));
        list.add(r("triton",    "Triton",             "VGtM", m("STR",1,"CON",1,"CHA",1)));
        list.add(r("yuanti",    "Yuan-ti Pureblood",  "VGtM", m("CHA",2,"INT",1)));
        list.add(r("bugbear",   "Bugbear",            "VGtM", m("STR",2,"DEX",1)));
        list.add(r("goblin",    "Goblin",             "VGtM", m("DEX",2,"CON",1)));
        list.add(r("hobgoblin", "Hobgoblin",          "VGtM", m("CON",2,"INT",1)));
        list.add(r("kobold",    "Kobold",             "VGtM", m("DEX",2,"STR",-2)));
        list.add(r("orc",       "Orc",                "VGtM", m("STR",2,"CON",1,"INT",-2)));

        // ── SCAG ─────────────────────────────────────────────────────────────
        list.add(r("gnome_deep",        "Gnome (Deep / Svirfneblin)", "SCAG", m("INT",2,"DEX",1)));
        list.add(r("halfling_ghostwise", "Halfling (Ghostwise)",      "SCAG", m("DEX",2,"WIS",1)));

        return list;
    }

    // ── Builder helpers ───────────────────────────────────────────────────────
    private Race r(String id, String name, String src, Map<String, Integer> fixed) {
        return rf(id, name, src, fixed, List.of());
    }
    private Race rf(String id, String name, String src,
                    Map<String, Integer> fixed, List<FlexibleBonus> flex) {
        return new Race(id, name, src, fixed, flex);
    }
    @SuppressWarnings("unchecked")
    private static <K,V> Map<K,V> m(Object... kv) {
        var map = new LinkedHashMap<K,V>();
        for (int i = 0; i < kv.length; i += 2) {
            //noinspection unchecked
            map.put((K) kv[i], (V) kv[i+1]);
        }
        return map;
    }
}
