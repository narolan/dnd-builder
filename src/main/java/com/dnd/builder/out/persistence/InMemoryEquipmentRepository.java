package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.EquipmentChoice;
import com.dnd.builder.core.model.EquipmentSlot;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryEquipmentRepository implements com.dnd.builder.core.port.out.EquipmentRepository {

    private final Map<String, List<EquipmentSlot>> byClass;

    public InMemoryEquipmentRepository() { byClass = buildAll(); }

    public List<EquipmentSlot> findByClass(String classId) {
        return byClass.getOrDefault(classId, List.of());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private EquipmentSlot slot(String id, String prompt, EquipmentChoice... choices) {
        return new EquipmentSlot(id, prompt, List.of(choices));
    }
    private EquipmentChoice opt(String id, String label) { return new EquipmentChoice(id, label); }

    private Map<String, List<EquipmentSlot>> buildAll() {
        var m = new HashMap<String, List<EquipmentSlot>>();

        m.put("barbarian", List.of(
            slot("barb1","Primary weapon:",
                opt("a","Greataxe (2d6 slashing, heavy, two-handed)"),
                opt("b","Any martial melee weapon")),
            slot("barb2","Secondary weapon:",
                opt("a","Two handaxes (1d6 slashing, light, thrown 20/60)"),
                opt("b","Any simple weapon")),
            slot("barb3","Pack:",
                opt("a","Explorer's Pack (backpack, bedroll, mess kit, tinderbox, 10 torches, 10 days rations, waterskin, 50 ft hempen rope)"),
                opt("b","Keep as-is")),
            slot("barb4","Standard:",
                opt("a","4 javelins (1d6 piercing, thrown 30/120)"))
        ));

        m.put("bard", List.of(
            slot("bard1","Primary weapon:",
                opt("a","Rapier (1d8 piercing, finesse)"),
                opt("b","Longsword (1d8/1d10 slashing, versatile)"),
                opt("c","Any simple weapon")),
            slot("bard2","Pack:",
                opt("a","Diplomat's Pack (chest, 2 map cases, fine clothes, ink, quill, lamp, 2 flasks oil, 5 sheets paper, vial perfume, sealing wax, soap)"),
                opt("b","Entertainer's Pack (backpack, bedroll, 2 costumes, 5 candles, 5 days rations, waterskin, disguise kit)")),
            slot("bard3","Instrument:",
                opt("a","Lute"),
                opt("b","Any other musical instrument")),
            slot("bard4","Standard:",
                opt("a","Leather armor (AC 11 + DEX mod), dagger (1d4 piercing, finesse, light, thrown 20/60)"))
        ));

        m.put("cleric", List.of(
            slot("cler1","Primary weapon:",
                opt("a","Mace (1d6 bludgeoning)"),
                opt("b","Warhammer (1d8/1d10 bludgeoning, versatile) — if proficient")),
            slot("cler2","Armor:",
                opt("a","Scale mail (AC 14, disadvantage on Stealth)"),
                opt("b","Leather armor (AC 11 + DEX mod)"),
                opt("c","Chain mail (AC 16, STR 13 req, disadvantage on Stealth) — if proficient")),
            slot("cler3","Ranged weapon:",
                opt("a","Light crossbow + 20 bolts (1d8 piercing, two-handed, 80/320 ft)"),
                opt("b","Any simple weapon")),
            slot("cler4","Pack:",
                opt("a","Priest's Pack (backpack, blanket, 10 candles, tinderbox, alms box, 2 incense blocks, censer, vestments, 2 days rations, waterskin)"),
                opt("b","Explorer's Pack")),
            slot("cler5","Standard:",
                opt("a","Shield (+ 2 AC), holy symbol"))
        ));

        m.put("druid", List.of(
            slot("dru1","Primary weapon:",
                opt("a","Wooden shield (+ 2 AC) + scimitar (1d6 slashing, finesse)"),
                opt("b","Any simple weapon")),
            slot("dru2","Pack:",
                opt("a","Explorer's Pack"),
                opt("b","Scholar's Pack (backpack, book of lore, bottle ink, quill, 10 sheets parchment, small bag sand, small knife)")),
            slot("dru3","Standard:",
                opt("a","Leather armor (AC 11 + DEX mod), explorer's pack, druidic focus"))
        ));

        m.put("fighter", List.of(
            slot("figh1","Armor:",
                opt("a","Chain mail (AC 16, STR 13 req, disadvantage on Stealth)"),
                opt("b","Leather armor (AC 11 + DEX mod) + longbow + 20 arrows")),
            slot("figh2","Primary weapon set:",
                opt("a","Martial weapon + shield (choose any martial weapon + AC +2)"),
                opt("b","Two martial weapons (choose any two)")),
            slot("figh3","Ranged weapon:",
                opt("a","Light crossbow + 20 bolts (1d8 piercing, two-handed, 80/320 ft)"),
                opt("b","Two handaxes (1d6 slashing, light, thrown 20/60)")),
            slot("figh4","Pack:",
                opt("a","Dungeoneer's Pack (backpack, crowbar, hammer, 10 pitons, 10 torches, tinderbox, 10 days rations, waterskin, 50 ft hempen rope)"),
                opt("b","Explorer's Pack"))
        ));

        m.put("monk", List.of(
            slot("monk1","Primary weapon:",
                opt("a","Shortsword (1d6 piercing, finesse, light)"),
                opt("b","Any simple melee weapon")),
            slot("monk2","Pack:",
                opt("a","Dungeoneer's Pack"),
                opt("b","Explorer's Pack")),
            slot("monk3","Standard:",
                opt("a","10 darts (1d4 piercing, finesse, thrown 20/60)"))
        ));

        m.put("paladin", List.of(
            slot("pala1","Primary weapon set:",
                opt("a","Martial weapon + shield"),
                opt("b","Two martial weapons")),
            slot("pala2","Ranged weapon:",
                opt("a","Five javelins (1d6 piercing, thrown 30/120)"),
                opt("b","Any simple melee weapon")),
            slot("pala3","Pack:",
                opt("a","Priest's Pack"),
                opt("b","Explorer's Pack")),
            slot("pala4","Standard:",
                opt("a","Chain mail (AC 16, STR 13 req), holy symbol"))
        ));

        m.put("ranger", List.of(
            slot("rang1","Armor:",
                opt("a","Scale mail (AC 14, disadvantage on Stealth)"),
                opt("b","Leather armor (AC 11 + DEX mod)")),
            slot("rang2","Primary weapon:",
                opt("a","Two shortswords (1d6 piercing, finesse, light)"),
                opt("b","Two simple melee weapons")),
            slot("rang3","Pack:",
                opt("a","Dungeoneer's Pack"),
                opt("b","Explorer's Pack")),
            slot("rang4","Standard:",
                opt("a","Longbow + quiver of 20 arrows (1d8 piercing, two-handed, 150/600 ft)"))
        ));

        m.put("rogue", List.of(
            slot("rogu1","Primary weapon:",
                opt("a","Rapier (1d8 piercing, finesse)"),
                opt("b","Shortsword (1d6 piercing, finesse, light)")),
            slot("rogu2","Ranged weapon:",
                opt("a","Shortbow + quiver of 20 arrows (1d6 piercing, two-handed, 80/320 ft)"),
                opt("b","Shortsword (1d6 piercing, finesse, light)")),
            slot("rogu3","Pack:",
                opt("a","Burglar's Pack (backpack, ball bearings, 10 ft string, bell, 5 candles, crowbar, hammer, 10 pitons, hooded lantern, 2 flasks oil, 5 days rations, tinderbox, waterskin, 50 ft hempen rope)"),
                opt("b","Dungeoneer's Pack"),
                opt("c","Explorer's Pack")),
            slot("rogu4","Standard:",
                opt("a","Leather armor, two daggers, thieves' tools"))
        ));

        m.put("sorcerer", List.of(
            slot("sorc1","Primary weapon:",
                opt("a","Light crossbow + 20 bolts (1d8 piercing, two-handed, 80/320 ft)"),
                opt("b","Any simple weapon")),
            slot("sorc2","Focus or components:",
                opt("a","Component pouch"),
                opt("b","Arcane focus (staff, wand, crystal, orb, or rod)")),
            slot("sorc3","Pack:",
                opt("a","Dungeoneer's Pack"),
                opt("b","Explorer's Pack")),
            slot("sorc4","Standard:",
                opt("a","Two daggers (1d4 piercing, finesse, light, thrown 20/60)"))
        ));

        m.put("warlock", List.of(
            slot("warl1","Primary weapon:",
                opt("a","Light crossbow + 20 bolts (1d8 piercing, two-handed, 80/320 ft)"),
                opt("b","Any simple weapon")),
            slot("warl2","Focus or components:",
                opt("a","Component pouch"),
                opt("b","Arcane focus")),
            slot("warl3","Pack:",
                opt("a","Scholar's Pack"),
                opt("b","Dungeoneer's Pack")),
            slot("warl4","Standard:",
                opt("a","Leather armor, any simple weapon, two daggers"))
        ));

        m.put("wizard", List.of(
            slot("wiz1","Primary weapon:",
                opt("a","Quarterstaff (1d6/1d8 bludgeoning, versatile)"),
                opt("b","Dagger (1d4 piercing, finesse, light, thrown 20/60)")),
            slot("wiz2","Focus or components:",
                opt("a","Component pouch"),
                opt("b","Arcane focus")),
            slot("wiz3","Pack:",
                opt("a","Scholar's Pack"),
                opt("b","Explorer's Pack")),
            slot("wiz4","Standard:",
                opt("a","Spellbook"))
        ));

        return m;
    }
}
