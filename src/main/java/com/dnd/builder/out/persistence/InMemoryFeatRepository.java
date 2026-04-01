package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.FeatDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryFeatRepository implements com.dnd.builder.core.port.out.FeatRepository {

    private final List<FeatDefinition> feats;
    private final Map<String, FeatDefinition> byId;

    public InMemoryFeatRepository() {
        feats = buildFeats();
        byId  = new HashMap<>();
        feats.forEach(f -> byId.put(f.getId(), f));
    }

    public List<FeatDefinition> findAll() { return Collections.unmodifiableList(feats); }
    public FeatDefinition findById(String id)  { return byId.get(id); }

    private List<FeatDefinition> buildFeats() {
        var list = new ArrayList<FeatDefinition>();

        // No prerequisite feats
        list.add(f("alert",         "Alert",            "",
            "+5 to initiative. Can't be surprised while conscious. Hidden creatures get no advantage vs. you.",
            null, 0, null));
        list.add(f("athlete",       "Athlete",          "",
            "+1 STR or DEX. Standing from prone costs 5 ft. Climbing/crawling at full speed. Running long jump needs 5-ft run-up.",
            List.of("STR","DEX"), 1, null));
        list.add(f("actor",         "Actor",            "",
            "+1 CHA. Mimic voices/sounds (Insight vs. Deception). Advantage on Deception/Performance when posing as someone.",
            List.of("CHA"), 0, null));
        list.add(f("charger",       "Charger",          "",
            "When you Dash and then make an attack, +5 damage or shove 10 ft.",
            null, 0, null));
        list.add(f("dungeon_delver","Dungeon Delver",   "",
            "Advantage on Perception/Investigation to detect secret doors. Advantage on saves vs traps. Resistance to trap damage. Search at normal pace.",
            null, 0, null));
        list.add(f("durable",       "Durable",          "",
            "+1 CON. When you roll hit dice, minimum equals your CON modifier (min 1).",
            List.of("CON"), 0, null));
        list.add(f("healer",        "Healer",           "",
            "Stabilize with healer's kit restores 1 HP. Using a healer's kit, spend one use to restore 1d6+4 HP.",
            null, 0, null));
        list.add(f("inspiring_leader","Inspiring Leader","",
            "After a short rest, inspire up to 6 creatures to gain THP = your level + CHA mod. Prereq: CHA 13.",
            null, 0, Map.of("CHA",13)));
        list.add(f("keen_mind",     "Keen Mind",        "",
            "+1 INT. Always know which way is north. Know how many hours since sunrise/sunset. Recall anything from last month.",
            List.of("INT"), 0, null));
        list.add(f("linguist",      "Linguist",         "",
            "+1 INT. Learn 3 languages. Create written ciphers only you can decode (INT save to crack).",
            List.of("INT"), 0, null));
        list.add(f("lucky",         "Lucky",            "",
            "3 luck points per long rest. When making an attack, ability check, or save, spend 1 luck point to roll an extra d20 and choose which result to use.",
            null, 0, null));
        list.add(f("magic_initiate","Magic Initiate",   "",
            "Choose a class: learn 2 cantrips + 1 1st-level spell (cast once per long rest). Use that class's spellcasting ability.",
            null, 0, null));
        list.add(f("mobile",        "Mobile",           "",
            "Speed +10 ft. Dash ignores difficult terrain. Attacking a creature (hit or miss) prevents opportunity attacks from it.",
            null, 0, null));
        list.add(f("mounted_combatant","Mounted Combatant","",
            "Advantage on melee attacks vs unmounted creatures smaller than mount. Force attacks on mount to target you instead. Mount is never damaged on successful DEX save dex spell.",
            null, 0, null));
        list.add(f("observant",     "Observant",        "",
            "+1 INT or WIS. Read lips. +5 to passive Perception and Investigation.",
            List.of("INT","WIS"), 1, null));
        list.add(f("resilient",     "Resilient",        "",
            "+1 to chosen ability score. Gain proficiency in saving throws with that ability.",
            null, 1, null)); // special: 1 chosen ASI
        list.add(f("ritual_caster","Ritual Caster",     "",
            "Acquire a ritual book with two rituals. Add rituals of your level or lower. Use INT, WIS, or CHA as spellcasting ability. Prereq: INT or WIS 13.",
            null, 0, Map.of("INT",13)));
        list.add(f("savage_attacker","Savage Attacker", "",
            "Once per turn when rolling melee damage dice, roll them twice and use the higher result.",
            null, 0, null));
        list.add(f("skilled",       "Skilled",          "",
            "Gain proficiency in any combination of three skills or tools.",
            null, 0, null));
        list.add(f("tavern_brawler","Tavern Brawler",   "",
            "+1 STR or CON. Proficient with improvised weapons. Unarmed strike uses d4. Grapple as a bonus action after unarmed strike.",
            List.of("STR","CON"), 1, null));
        list.add(f("tough",         "Tough",            "",
            "Max HP increases by 2 × your level. Further increases by 2 per level.",
            null, 0, null));
        list.add(f("war_caster",    "War Caster",       "",
            "Advantage on CON saves for concentration when taking damage. Can perform somatic components while wielding weapons/shield. Opportunity attack can be a spell targeting one creature. Prereq: can cast at least one spell.",
            null, 0, null));
        list.add(f("weapon_master","Weapon Master",     "",
            "+1 STR or DEX. Proficiency with 4 weapons of your choice.",
            List.of("STR","DEX"), 1, null));

        // Feats with prerequisites
        list.add(f("crossbow_expert","Crossbow Expert",  "None (removes loading penalty)",
            "Ignore loading property. Ranged attack with crossbow while in melee without disadvantage. When attacking with one-handed weapon, bonus action attack with hand crossbow.",
            null, 0, null));
        list.add(f("defensive_duelist","Defensive Duelist","Requires DEX 13",
            "When holding a finesse weapon and attacked, add proficiency bonus to AC as reaction.",
            null, 0, Map.of("DEX",13)));
        list.add(f("dual_wielder",  "Dual Wielder",     "None",
            "+1 AC while wielding two weapons. Draw/stow two weapons at once. Two-weapon fighting with non-light weapons.",
            null, 0, null));
        list.add(f("elemental_adept","Elemental Adept", "Requires ability to cast at least one spell",
            "Choose a damage type (acid/cold/fire/lightning/thunder). Spells of that type treat 1s as 2s on damage dice. Ignore resistance to that type.",
            null, 0, null));
        list.add(f("grappler",      "Grappler",         "Requires STR 13",
            "Advantage on attacks against a creature you're grappling. Pin a grappled creature (both restrained).",
            null, 0, Map.of("STR",13)));
        list.add(f("great_weapon_master","Great Weapon Master","None",
            "On a critical hit or killing blow, bonus action attack. Before attacking with a heavy weapon, take -5 to hit for +10 damage.",
            null, 0, null));
        list.add(f("heavily_armored","Heavily Armored", "Proficiency in medium armor",
            "+1 STR. Gain proficiency with heavy armor.",
            List.of("STR"), 0, null));
        list.add(f("heavy_armor_master","Heavy Armor Master","Proficiency in heavy armor",
            "+1 STR. While in heavy armor, bludgeoning/piercing/slashing from non-magical weapons deals 3 less damage.",
            List.of("STR"), 0, null));
        list.add(f("lightly_armored","Lightly Armored", "None",
            "+1 STR or DEX. Gain proficiency with light armor.",
            List.of("STR","DEX"), 1, null));
        list.add(f("mage_slayer",   "Mage Slayer",      "None",
            "Opportunity attack when caster within 5 ft casts a spell. Target has disadvantage on concentration save. Advantage on saves against spells by creatures within 5 ft.",
            null, 0, null));
        list.add(f("martial_adept", "Martial Adept",    "None",
            "Learn two maneuvers (as Battle Master). Gain 1 Superiority Die (d6) per short/long rest.",
            null, 0, null));
        list.add(f("medium_armor_master","Medium Armor Master","Proficiency in medium armor",
            "+1 DEX or STR. No disadvantage on Stealth in medium armor. Max DEX bonus to AC becomes 3.",
            List.of("DEX","STR"), 1, null));
        list.add(f("moderately_armored","Moderately Armored","Proficiency in light armor",
            "+1 STR or DEX. Gain proficiency with medium armor and shields.",
            List.of("STR","DEX"), 1, null));
        list.add(f("polearm_master","Polearm Master",   "None",
            "Bonus action attack with opposite end (1d4 bludgeoning). Opportunity attack when creature enters your reach.",
            null, 0, null));
        list.add(f("sentinel",      "Sentinel",         "None",
            "Reduce target speed to 0 on opportunity attack hit. Attack creatures who Disengage. When creature attacks ally adj to you, opportunity attack against them.",
            null, 0, null));
        list.add(f("sharpshooter",  "Sharpshooter",     "None",
            "Ignore cover (except full cover). Long range without disadvantage. Before attacking, take -5 to hit for +10 damage.",
            null, 0, null));
        list.add(f("shield_master", "Shield Master",    "None",
            "Bonus action shove while taking Attack action. Add shield AC to DEX saves against targeted spells. On DEX save for half, use reaction to take no damage on success.",
            null, 0, null));
        list.add(f("skulker",       "Skulker",          "Requires DEX 13",
            "Hide when lightly obscured. Missing a ranged attack doesn't reveal your position. Dim light doesn't impose disadvantage on Perception.",
            null, 0, Map.of("DEX",13)));
        list.add(f("spell_sniper",  "Spell Sniper",     "Requires ability to cast at least one spell",
            "Double range of attack-roll spells. Ignore half/three-quarters cover. Learn one ranged attack cantrip.",
            null, 0, null));

        return list;
    }

    private FeatDefinition f(String id, String name, String prereq, String desc,
                              List<String> asiBonus, int asiCount,
                              Map<String, Integer> reqScore) {
        var fd = new FeatDefinition();
        fd.setId(id); fd.setName(name); fd.setPrerequisite(prereq);
        fd.setDescription(desc); fd.setAsiBonus(asiBonus);
        fd.setAsiChoiceCount(asiCount); fd.setRequiredScore(reqScore);
        return fd;
    }
}
