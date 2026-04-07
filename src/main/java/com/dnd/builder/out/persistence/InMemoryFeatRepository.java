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

        // ── XGtE Feats ──────────────────────────────────────────────────────────────
        list.add(f("bountiful_luck",    "Bountiful Luck",       "Halfling",
            "When an ally within 30 ft rolls a 1 on an attack, ability check, or saving throw, you can use your reaction to let them reroll, keeping the new result.",
            null, 0, null));
        list.add(f("dragon_fear",       "Dragon Fear",          "Dragonborn",
            "+1 STR, CON, or CHA. When frightened, you can replace the breath weapon with a frightening roar: DC 8 + prof + CHA mod, creatures within 30 ft make WIS save or are frightened until end of your next turn.",
            List.of("STR","CON","CHA"), 1, null));
        list.add(f("dragon_hide",       "Dragon Hide",          "Dragonborn",
            "+1 STR, CON, or CHA. Grow retractable claws (unarmed strike 1d4 slashing). Natural armor: AC = 13 + DEX mod (while unarmored).",
            List.of("STR","CON","CHA"), 1, null));
        list.add(f("drow_high_magic",   "Drow High Magic",      "Elf (drow)",
            "Learn Detect Magic (at will), Levitate (once/long rest), and Dispel Magic (once/long rest). CHA is spellcasting ability.",
            null, 0, null));
        list.add(f("dwarven_fortitude", "Dwarven Fortitude",    "Dwarf",
            "+1 CON (fixed). When taking the Dodge action, spend one hit die to heal (roll die + CON mod).",
            null, 0, null));
        list.add(f("elven_accuracy",    "Elven Accuracy",       "Elf or half-elf",
            "+1 DEX, INT, WIS, or CHA. When you have advantage on an attack roll using DEX, INT, WIS, or CHA, you can reroll one of the dice once.",
            List.of("DEX","INT","WIS","CHA"), 1, null));
        list.add(f("fade_away",         "Fade Away",            "Gnome",
            "+1 DEX or INT. When you take damage, use your reaction to turn invisible until the end of your next turn or until you attack/cast a spell (once/short or long rest).",
            List.of("DEX","INT"), 1, null));
        list.add(f("fey_teleportation", "Fey Teleportation",    "High elf",
            "+1 INT or CHA. Learn Misty Step (cast once/short or long rest without a spell slot). Learn Sylvan.",
            List.of("INT","CHA"), 1, null));
        list.add(f("flames_of_phlegethos","Flames of Phlegethos","Tiefling",
            "+1 INT or CHA. Reroll 1s on fire spell damage (must keep reroll). When you cast a fire spell, flames wreathe you until end of next turn (1d4 fire damage to touchers).",
            List.of("INT","CHA"), 1, null));
        list.add(f("infernal_constitution","Infernal Constitution","Tiefling",
            "+1 CON (fixed). Resistance to cold and poison damage. Advantage on saving throws against being poisoned.",
            null, 0, null));
        list.add(f("orcish_fury",       "Orcish Fury",          "Half-orc",
            "+1 STR or CON. Once per short/long rest, add one extra weapon damage die on a hit. When you use Relentless Endurance, you can immediately make one weapon attack as a reaction.",
            List.of("STR","CON"), 1, null));
        list.add(f("prodigy",           "Prodigy",              "Half-elf, half-orc, or human",
            "Gain one skill proficiency, one tool proficiency, and one language. One skill you are proficient in gains expertise (double proficiency).",
            null, 0, null));
        list.add(f("second_chance",     "Second Chance",        "Halfling",
            "+1 DEX, CON, or CHA. When a creature hits you, use your reaction to force it to reroll. Must use new roll. Recharges on initiative roll.",
            List.of("DEX","CON","CHA"), 1, null));
        list.add(f("squat_nimbleness", "Squat Nimbleness",      "Dwarf or small race",
            "+1 STR or DEX. Speed increases by 5 ft. Proficiency in Acrobatics or Athletics. No longer subject to disadvantage from moving through larger creatures' spaces.",
            List.of("STR","DEX"), 1, null));
        list.add(f("wood_elf_magic",    "Wood Elf Magic",        "Wood elf",
            "Learn Druidcraft cantrip. Learn one 1st-level druid spell, cast once/long rest without a spell slot. Learn one 2nd-level druid spell, cast once/long rest without a slot. WIS is spellcasting ability.",
            null, 0, null));

        // ── TCoE Feats ──────────────────────────────────────────────────────────────
        list.add(f("artificer_initiate","Artificer Initiate",   "",
            "Learn one cantrip and one 1st-level artificer spell (cast once/long rest). Gain proficiency with one type of artisan's tools. INT is spellcasting ability.",
            null, 0, null));
        list.add(f("chef",              "Chef",                  "",
            "+1 CON or WIS. During a short rest, cook food that lets you or up to 5 allies regain extra hit points equal to your proficiency bonus. Prepare treats during a long rest (prof bonus times) that grant 1d8 THP.",
            List.of("CON","WIS"), 1, null));
        list.add(f("crusher",           "Crusher",               "",
            "+1 STR or CON. When you deal bludgeoning damage, move target 5 ft to unoccupied space. Once per turn on a critical hit, grant advantage to all attacks vs target until start of your next turn.",
            List.of("STR","CON"), 1, null));
        list.add(f("eldritch_adept",    "Eldritch Adept",        "Spellcasting or Pact Magic",
            "Learn one Eldritch Invocation of your choice (must meet prerequisites). If it requires a Pact Boon, you must have that boon. Can replace it when you gain a level.",
            null, 0, null));
        list.add(f("fey_touched",       "Fey Touched",           "",
            "+1 INT, WIS, or CHA. Learn Misty Step and one 1st-level divination or enchantment spell. Cast each once/long rest for free. Can also cast using spell slots. Spellcasting ability is the stat boosted.",
            List.of("INT","WIS","CHA"), 1, null));
        list.add(f("fighting_initiate", "Fighting Initiate",     "Proficiency with a martial weapon",
            "Learn one Fighting Style of your choice. When you gain a level, you can replace it with another Fighting Style.",
            null, 0, null));
        list.add(f("gunner",            "Gunner",                "",
            "+1 DEX (fixed). Proficiency with firearms. Ignore the loading property of firearms. Being within 5 ft of a hostile creature doesn't impose disadvantage on ranged attack rolls with firearms.",
            null, 0, null));
        list.add(f("metamagic_adept",   "Metamagic Adept",       "Spellcasting or Pact Magic",
            "Learn two Metamagic options. Gain 2 sorcery points (regain on long rest). Can spend sorcery points only on Metamagic.",
            null, 0, null));
        list.add(f("piercer",           "Piercer",               "",
            "+1 STR or DEX. Once per turn when you deal piercing damage, reroll one damage die and use the higher result. On a critical hit, roll one additional piercing damage die.",
            List.of("STR","DEX"), 1, null));
        list.add(f("poisoner",          "Poisoner",              "",
            "Ignore resistance to poison damage. Coat a weapon with poison as a bonus action using your poisoner's kit. Creatures hit must make DC 14 CON save or be poisoned until end of your next turn. Gain proficiency with the poisoner's kit.",
            null, 0, null));
        list.add(f("shadow_touched",    "Shadow Touched",        "",
            "+1 INT, WIS, or CHA. Learn Invisibility and one 1st-level illusion or necromancy spell. Cast each once/long rest for free. Can also cast using spell slots. Spellcasting ability is the stat boosted.",
            List.of("INT","WIS","CHA"), 1, null));
        list.add(f("shield_training",   "Shield Training",       "",
            "+1 STR, DEX, or CON. Gain proficiency with shields. In a turn where you put on or take off a shield, you can still make an attack.",
            List.of("STR","DEX","CON"), 1, null));
        list.add(f("slasher",           "Slasher",               "",
            "+1 STR or DEX. Once per turn when you deal slashing damage, reduce target's speed by 10 ft until start of your next turn. On a critical hit, the target has disadvantage on attack rolls until start of your next turn.",
            List.of("STR","DEX"), 1, null));
        list.add(f("telekinetic",       "Telekinetic",           "",
            "+1 INT, WIS, or CHA. Learn Mage Hand (or extend its range by 30 ft). As a bonus action, telekinetically push/pull a creature within 30 ft 5 ft toward or away from you (STR save vs 8 + prof + stat mod).",
            List.of("INT","WIS","CHA"), 1, null));
        list.add(f("telepathic",        "Telepathic",            "",
            "+1 INT, WIS, or CHA. Speak telepathically to any creature you can see within 60 ft. Cast Detect Thoughts once/long rest without a spell slot. Spellcasting ability is the stat boosted.",
            List.of("INT","WIS","CHA"), 1, null));

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
