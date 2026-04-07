package com.dnd.builder.out.persistence;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryMagicItemRepository {

    private final List<MagicItemTemplate> items;
    private final Map<String, MagicItemTemplate> byId;

    public InMemoryMagicItemRepository() {
        items = buildAll();
        byId = new HashMap<>();
        items.forEach(item -> byId.put(item.id(), item));
    }

    public List<MagicItemTemplate> findAll() { return Collections.unmodifiableList(items); }

    public MagicItemTemplate findById(String id) { return byId.get(id); }

    public List<MagicItemTemplate> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String q = query.toLowerCase();
        return items.stream()
            .filter(i -> i.name().toLowerCase().contains(q)
                      || i.category().toLowerCase().contains(q)
                      || i.rarity().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    private List<MagicItemTemplate> buildAll() {
        var list = new ArrayList<MagicItemTemplate>();

        // ── PHB — Potions ─────────────────────────────────────────────────────
        list.add(i("potion_healing", "Potion of Healing", "PHB", "potion", "common",
            false, "Regain 2d4+2 hit points.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_greater_healing", "Potion of Greater Healing", "PHB", "potion", "uncommon",
            false, "Regain 4d4+4 hit points.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_superior_healing", "Potion of Superior Healing", "PHB", "potion", "rare",
            false, "Regain 8d4+8 hit points.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_supreme_healing", "Potion of Supreme Healing", "PHB", "potion", "very-rare",
            false, "Regain 10d4+20 hit points.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_climbing", "Potion of Climbing", "PHB", "potion", "common",
            false, "Gain a climbing speed equal to your walking speed for 1 hour, and advantage on Athletics checks to climb.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_animal_friendship", "Potion of Animal Friendship", "PHB", "potion", "uncommon",
            false, "Cast Animal Friendship (save DC 13) for 1 hour after drinking.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_fire_breath", "Potion of Fire Breath", "PHB", "potion", "uncommon",
            false, "After drinking, use a bonus action up to 3 times (within 1 hour) to exhale fire at a target within 30 ft (4d6 fire, DC 13 DEX save for half).",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_flying", "Potion of Flying", "PHB", "potion", "very-rare",
            false, "Gain a flying speed of 60 ft for 1 hour.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_heroism", "Potion of Heroism", "PHB", "potion", "rare",
            false, "Gain 10 temporary hit points and the Bless condition for 1 hour (no concentration required).",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_invisibility", "Potion of Invisibility", "PHB", "potion", "very-rare",
            false, "Become invisible for 1 hour. Ends early if you attack or cast a spell.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_speed", "Potion of Speed", "PHB", "potion", "very-rare",
            false, "Gain the Haste effect for 1 minute (no concentration required).",
            0, 0, 0, null, 0, 30));
        list.add(i("potion_water_breathing", "Potion of Water Breathing", "PHB", "potion", "uncommon",
            false, "Breathe underwater for 1 hour.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_mind_reading", "Potion of Mind Reading", "PHB", "potion", "rare",
            false, "Cast Detect Thoughts (save DC 13) for 1 minute.",
            0, 0, 0, null, 0, 0));
        list.add(i("potion_vitality", "Potion of Vitality", "PHB", "potion", "very-rare",
            false, "Cure all diseases and poisons, and regain maximum hit points from all Hit Dice spent in the next 24 hours.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Scrolls ─────────────────────────────────────────────────────
        list.add(i("scroll_protection", "Scroll of Protection", "PHB", "scroll", "rare",
            false, "Use an action to read the scroll and create a 5-foot-radius invisible barrier around you that blocks specific creature types (chosen when scroll is made). Lasts 5 minutes or until you break it.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Armor ───────────────────────────────────────────────────────
        list.add(i("adamantine_armor", "Adamantine Armor", "PHB", "armor", "uncommon",
            false, "While wearing this armor, any critical hit against you becomes a normal hit.",
            0, 0, 0, null, 0, 0));
        list.add(i("mithral_armor", "Mithral Armor", "PHB", "armor", "uncommon",
            false, "This armor is non-bulky and not restrictive. If the armor normally imposes disadvantage on Dexterity (Stealth) checks or has a Strength requirement, this version doesn't.",
            0, 0, 0, null, 0, 0));
        list.add(i("armor_plus_1", "Armor, +1", "PHB", "armor", "rare",
            false, "You have a +1 bonus to AC while wearing this armor. (Applies to any armor type: leather, chain mail, plate, etc.)",
            1, 0, 0, null, 0, 0));
        list.add(i("armor_plus_2", "Armor, +2", "PHB", "armor", "very-rare",
            false, "You have a +2 bonus to AC while wearing this armor.",
            2, 0, 0, null, 0, 0));
        list.add(i("armor_plus_3", "Armor, +3", "PHB", "armor", "legendary",
            false, "You have a +3 bonus to AC while wearing this armor.",
            3, 0, 0, null, 0, 0));
        list.add(i("demon_armor", "Demon Armor", "PHB", "armor", "very-rare",
            true, "Plate armor. AC 18. Your unarmed strikes deal 1d8 slashing. The armor can't be removed while attuned (remove curse required).",
            0, 0, 0, null, 0, 0));
        list.add(i("dragon_scale_mail", "Dragon Scale Mail", "PHB", "armor", "very-rare",
            true, "Scale mail made from dragon scales. +1 bonus to AC. Advantage on saves against the Frightful Presence and breath weapons of dragons of that type. Resistance to the damage type associated with the dragon.",
            1, 0, 0, null, 0, 0));
        list.add(i("dwarven_plate", "Dwarven Plate", "PHB", "armor", "very-rare",
            false, "Plate armor with +2 AC bonus. When an effect moves you against your will, you can use your reaction to reduce the movement by up to 10 feet.",
            2, 0, 0, null, 0, 0));
        list.add(i("elven_chain", "Elven Chain", "PHB", "armor", "rare",
            false, "Chain shirt. +1 bonus to AC. You are considered proficient with this armor even if you lack proficiency with medium armor.",
            1, 0, 0, null, 0, 0));
        list.add(i("plate_of_etherealness", "Plate Armor of Etherealness", "PHB", "armor", "legendary",
            true, "While wearing this armor, you can cast Etherealness (no spell slot required). You remain ethereal until you remove the armor, which ends the spell.",
            0, 0, 0, null, 0, 0));
        list.add(i("glamoured_studded_leather", "Glamoured Studded Leather", "PHB", "armor", "rare",
            true, "Studded leather. +1 bonus to AC. As a bonus action, change the armor's appearance to any kind of nonmagical armor or normal clothing.",
            1, 0, 0, null, 0, 0));

        // ── PHB — Shields ─────────────────────────────────────────────────────
        list.add(i("shield_plus_1", "Shield, +1", "PHB", "shield", "uncommon",
            false, "While holding this shield, you have a +1 bonus to AC in addition to the shield's normal bonus.",
            1, 0, 0, null, 0, 0));
        list.add(i("shield_plus_2", "Shield, +2", "PHB", "shield", "rare",
            false, "While holding this shield, you have a +2 bonus to AC in addition to the shield's normal bonus.",
            2, 0, 0, null, 0, 0));
        list.add(i("shield_plus_3", "Shield, +3", "PHB", "shield", "very-rare",
            false, "While holding this shield, you have a +3 bonus to AC in addition to the shield's normal bonus.",
            3, 0, 0, null, 0, 0));
        list.add(i("animated_shield", "Animated Shield", "PHB", "shield", "very-rare",
            true, "While holding this shield, you can speak its command word as a bonus action to animate it. It floats in your space and protects you as if you were wielding it, freeing both hands. Lasts 1 minute, then requires a short rest.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Weapons ─────────────────────────────────────────────────────
        list.add(i("weapon_plus_1", "Weapon, +1", "PHB", "weapon", "uncommon",
            false, "You have a +1 bonus to attack and damage rolls made with this magic weapon.",
            0, 1, 1, null, 0, 0));
        list.add(i("weapon_plus_2", "Weapon, +2", "PHB", "weapon", "rare",
            false, "You have a +2 bonus to attack and damage rolls made with this magic weapon.",
            0, 2, 2, null, 0, 0));
        list.add(i("weapon_plus_3", "Weapon, +3", "PHB", "weapon", "very-rare",
            false, "You have a +3 bonus to attack and damage rolls made with this magic weapon.",
            0, 3, 3, null, 0, 0));
        list.add(i("berserker_axe", "Berserker Axe", "PHB", "weapon", "rare",
            true, "You gain +1 to attack and damage rolls. Curse: each time you attune to this weapon you must succeed on a DC 15 WIS save or become attuned to its curse (berserk in combat).",
            0, 1, 1, null, 0, 0));
        list.add(i("dancing_sword", "Dancing Sword", "PHB", "weapon", "very-rare",
            true, "You can toss this magic sword into the air (bonus action) and speak its command word, after which it animates and attacks on its own.",
            0, 0, 0, null, 0, 0));
        list.add(i("defender", "Defender", "PHB", "weapon", "legendary",
            true, "You gain +3 to attack and damage rolls. Each turn when you attack with it, you can transfer some or all of its attack bonus to your AC instead.",
            0, 3, 3, null, 0, 0));
        list.add(i("dragon_slayer", "Dragon Slayer", "PHB", "weapon", "rare",
            false, "+1 to attack and damage rolls. Against dragons, deals an extra 3d6 damage of the weapon's type.",
            0, 1, 1, null, 0, 0));
        list.add(i("dwarven_thrower", "Dwarven Thrower", "PHB", "weapon", "very-rare",
            true, "While you carry this warhammer, you gain +3 to attack and damage rolls. Returning thrown weapon (20/60 ft). Deals +1d8 extra damage to giants.",
            0, 3, 3, null, 0, 0));
        list.add(i("flame_tongue", "Flame Tongue", "PHB", "weapon", "rare",
            true, "Command word lights the blade with fire (20-ft radius light). You gain +2 to attack/damage. On hit, 2d6 extra fire damage. Extinguishable with another command word.",
            0, 2, 2, null, 0, 0));
        list.add(i("frost_brand", "Frost Brand", "PHB", "weapon", "very-rare",
            true, "While you hold this sword, you gain +1 to attack/damage and resistance to fire damage. On a hit, deals 1d6 extra cold damage. Once per hour, sheds cold to extinguish fires within 30 ft.",
            0, 1, 1, null, 0, 0));
        list.add(i("giant_slayer", "Giant Slayer", "PHB", "weapon", "rare",
            false, "+1 to attack and damage. Against giants, deals an extra 2d6 damage and the target must make a DC 15 STR save or fall prone.",
            0, 1, 1, null, 0, 0));
        list.add(i("holy_avenger", "Holy Avenger", "PHB", "weapon", "legendary",
            true, "+3 to attack and damage rolls. On hit vs fiends/undead, +2d10 radiant damage. 30-ft radius aura gives allies advantage on saves against spells and magical effects.",
            0, 3, 3, null, 0, 0));
        list.add(i("javelin_of_lightning", "Javelin of Lightning", "PHB", "weapon", "uncommon",
            false, "+2 to attack. When thrown (range 30/120), speak the command word to deal 4d6 lightning damage to the target and 2d6 lightning to each creature within 5 ft of the path (DC 13 DEX save for half). Becomes non-magical after use.",
            0, 2, 0, null, 0, 0));
        list.add(i("luck_blade", "Luck Blade", "PHB", "weapon", "legendary",
            true, "+1 to attack and damage. You gain +1 to saving throws. Once per day, reroll any d20. May have 1-3 wishes stored (DM choice), usable once each.",
            0, 1, 1, null, 0, 0));
        list.add(i("mace_of_disruption", "Mace of Disruption", "PHB", "weapon", "rare",
            true, "When you hit a fiend or undead, it takes an extra 2d6 radiant damage. It must make a DC 15 WIS save or be frightened of you until the end of your next turn (if 25 HP or fewer remaining).",
            0, 0, 0, null, 0, 0));
        list.add(i("mace_of_smiting", "Mace of Smiting", "PHB", "weapon", "rare",
            false, "+1 to attack. Against constructs, +2d6 extra damage on a hit; if target has 25 HP or fewer, the target is destroyed.",
            0, 1, 1, null, 0, 0));
        list.add(i("mace_of_terror", "Mace of Terror", "PHB", "weapon", "rare",
            true, "3 charges. Expend 1 charge (action) to terrorize creatures within 30 ft: DC 15 WIS save or frightened for 1 minute (save each turn). Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("nine_lives_stealer", "Nine Lives Stealer", "PHB", "weapon", "very-rare",
            true, "+2 to attack and damage. When you score a critical hit, the target must make a DC 15 CON save or die instantly (if not immune to death effects). Can kill in this way 1d8+1 times.",
            0, 2, 2, null, 0, 0));
        list.add(i("oathbow", "Oathbow", "PHB", "weapon", "very-rare",
            true, "Whisper a command while drawing an arrow to swear enmity against a target in range. +3 to attack and damage against your sworn enemy, dealing 3d6 extra piercing damage. Disadvantage on attacks against others while you have a sworn enemy.",
            0, 3, 3, null, 0, 0));
        list.add(i("rapier_sharpness", "Sword of Sharpness", "PHB", "weapon", "very-rare",
            true, "+3 to attack and damage. When you roll a 20 on an attack, the target takes an extra 14 slashing damage and you can sever a limb (DM discretion). On a miss by 1 (rolls exactly 1 less than AC), deal 1d10 slashing damage.",
            0, 3, 3, null, 0, 0));
        list.add(i("sun_blade", "Sun Blade", "PHB", "weapon", "rare",
            true, "+2 to attack and damage rolls. Deals radiant instead of slashing damage. Counts as a longsword. As a bonus action, extend or retract the blade. Deals +1d8 radiant to undead.",
            0, 2, 2, null, 0, 0));
        list.add(i("sword_of_life_stealing", "Sword of Life Stealing", "PHB", "weapon", "rare",
            true, "+2 to attack and damage. When you hit and roll a 20, the target takes 10 extra necrotic damage and you gain 10 THP (if target isn't a construct or undead).",
            0, 2, 2, null, 0, 0));
        list.add(i("trident_of_fish_command", "Trident of Fish Command", "PHB", "weapon", "uncommon",
            true, "+2 to attack and damage. 3 charges. Expend 1 charge (action) to cast Dominate Beast on a beast with swimming speed (DC 15).",
            0, 2, 2, null, 0, 0));
        list.add(i("vorpal_sword", "Vorpal Sword", "PHB", "weapon", "legendary",
            true, "+3 to attack and damage rolls. Ignores resistance to slashing damage. On a roll of 20, the target must make a DC 15 CON save or be decapitated (if it has a head). Creatures immune to critical hits are immune.",
            0, 3, 3, null, 0, 0));
        list.add(i("vicious_weapon", "Vicious Weapon", "PHB", "weapon", "rare",
            false, "When you roll a 20 on an attack, the target takes an extra 7 damage of the weapon's type.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Rings ───────────────────────────────────────────────────────
        list.add(i("ring_of_protection", "Ring of Protection", "PHB", "ring", "rare",
            true, "+1 bonus to AC and saving throws.",
            1, 0, 0, null, 0, 0));
        list.add(i("ring_of_spell_storing", "Ring of Spell Storing", "PHB", "ring", "rare",
            true, "This ring stores up to 5 levels of spells (anyone can cast into it). While attuned, you can cast any spell stored in it using the original caster's ability and save DC.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_evasion", "Ring of Evasion", "PHB", "ring", "rare",
            true, "3 charges. When you fail a DEX saving throw, use your reaction to expend 1 charge and succeed instead. Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_feather_falling", "Ring of Feather Falling", "PHB", "ring", "rare",
            true, "You fall at 60 feet per round and take no damage from falling while wearing this ring.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_free_action", "Ring of Free Action", "PHB", "ring", "rare",
            true, "While wearing this ring, difficult terrain doesn't cost you extra movement, and magic can't reduce your speed or cause you to be paralyzed or restrained.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_invisibility", "Ring of Invisibility", "PHB", "ring", "legendary",
            true, "While wearing this ring, you can become invisible as an action. Ends if you attack, cast a spell, or remove the ring.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_mind_shielding", "Ring of Mind Shielding", "PHB", "ring", "uncommon",
            true, "Immune to magic that detects thoughts, sense alignment, or divines your nature. Creatures can't communicate telepathically without your consent.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_regeneration", "Ring of Regeneration", "PHB", "ring", "very-rare",
            true, "Regain 1d6 HP every 10 minutes if you have at least 1 HP. Regrown severed limbs in 1d6+1 days.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_resistance", "Ring of Resistance", "PHB", "ring", "rare",
            true, "Resistance to one type of damage (determined when found).",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_spell_turning", "Ring of Spell Turning", "PHB", "ring", "legendary",
            true, "Advantage on saves against spells targeting only you. Roll a d6; on a 1-5, the spell is reflected back at its caster.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_swimming", "Ring of Swimming", "PHB", "ring", "uncommon",
            false, "Swimming speed of 40 ft.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_warmth", "Ring of Warmth", "PHB", "ring", "uncommon",
            true, "Resistance to cold damage. You and your gear are comfortable in temperatures as low as -50 degrees Fahrenheit.",
            0, 0, 0, null, 0, 0));
        list.add(i("ring_of_water_walking", "Ring of Water Walking", "PHB", "ring", "uncommon",
            false, "Stand and walk on any liquid as if it were solid ground.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Wondrous Items ──────────────────────────────────────────────
        list.add(i("amulet_of_health", "Amulet of Health", "PHB", "wondrous", "rare",
            true, "Your CON score is 19 while you wear this amulet. Has no effect if your CON is already 19 or higher.",
            0, 0, 0, null, 0, 0));
        list.add(i("amulet_of_proof_against_detection", "Amulet of Proof Against Detection and Location", "PHB", "wondrous", "uncommon",
            true, "While wearing this amulet, you are hidden from divination magic. You can't be targeted by such magic or perceived through magical scrying sensors.",
            0, 0, 0, null, 0, 0));
        list.add(i("bag_of_holding", "Bag of Holding", "PHB", "wondrous", "uncommon",
            false, "This bag's interior space is much larger than its outside dimensions. Up to 500 lb of items fitting in a 64-cubic-foot space. Retrieval is an action. If overloaded, punctured, or torn, it ruptures and is destroyed.",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_hill", "Belt of Hill Giant Strength", "PHB", "wondrous", "uncommon",
            true, "Your STR score is 21 while attuned. (No effect if STR is already 21 or higher.)",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_stone", "Belt of Stone Giant Strength", "PHB", "wondrous", "rare",
            true, "Your STR score is 23 while attuned.",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_frost", "Belt of Frost Giant Strength", "PHB", "wondrous", "rare",
            true, "Your STR score is 23 while attuned.",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_fire", "Belt of Fire Giant Strength", "PHB", "wondrous", "very-rare",
            true, "Your STR score is 25 while attuned.",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_cloud", "Belt of Cloud Giant Strength", "PHB", "wondrous", "legendary",
            true, "Your STR score is 27 while attuned.",
            0, 0, 0, null, 0, 0));
        list.add(i("belt_of_giant_strength_storm", "Belt of Storm Giant Strength", "PHB", "wondrous", "legendary",
            true, "Your STR score is 29 while attuned.",
            0, 0, 0, null, 0, 0));
        list.add(i("boots_of_elvenkind", "Boots of Elvenkind", "PHB", "wondrous", "uncommon",
            false, "Advantage on DEX (Stealth) checks.",
            0, 0, 0, null, 0, 0));
        list.add(i("boots_of_speed", "Boots of Speed", "PHB", "wondrous", "rare",
            true, "Click the heels as a bonus action to double your walking speed for 1 minute (usable once per long rest). While active, creatures have disadvantage on opportunity attacks against you.",
            0, 0, 0, null, 0, 30));
        list.add(i("boots_of_striding_and_springing", "Boots of Striding and Springing", "PHB", "wondrous", "uncommon",
            true, "Walking speed becomes 30 ft unless higher. Jump distance triples.",
            0, 0, 0, null, 0, 0));
        list.add(i("boots_of_the_winterlands", "Boots of the Winterlands", "PHB", "wondrous", "uncommon",
            true, "Resistance to cold damage. Ignore difficult terrain from ice/snow. Comfortable in temperatures as low as -50 degrees Fahrenheit.",
            0, 0, 0, null, 0, 0));
        list.add(i("bracers_of_archery", "Bracers of Archery", "PHB", "wondrous", "uncommon",
            true, "+2 damage on ranged attacks with longbows and shortbows.",
            0, 0, 2, null, 0, 0));
        list.add(i("bracers_of_defense", "Bracers of Defense", "PHB", "wondrous", "rare",
            true, "+2 bonus to AC while you aren't wearing armor or using a shield.",
            2, 0, 0, null, 0, 0));
        list.add(i("brooch_of_shielding", "Brooch of Shielding", "PHB", "wondrous", "uncommon",
            true, "Resistance to force damage and immunity to the Magic Missile spell.",
            0, 0, 0, null, 0, 0));
        list.add(i("cap_of_water_breathing", "Cap of Water Breathing", "PHB", "wondrous", "uncommon",
            false, "While wearing this cap underwater, you can breathe normally.",
            0, 0, 0, null, 0, 0));
        list.add(i("cape_of_the_mountebank", "Cape of the Mountebank", "PHB", "wondrous", "rare",
            false, "Use an action to cast Dimension Door from the cape (once per day). A cloud of smoke appears at your origin.",
            0, 0, 0, null, 0, 0));
        list.add(i("carpet_of_flying", "Carpet of Flying", "PHB", "wondrous", "very-rare",
            false, "You can speak its command word to make the carpet hover and fly. Speed 80 ft (3x5 ft, 1 passenger), 60 ft (4x6 ft, 2 passengers), or 40 ft (5x7 ft, 4 passengers).",
            0, 0, 0, null, 0, 0));
        list.add(i("circlet_of_blasting", "Circlet of Blasting", "PHB", "wondrous", "uncommon",
            false, "While wearing this circlet, you can use an action to cast Scorching Ray (3rd-level; attack bonus +5) once per day.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_arachnida", "Cloak of Arachnida", "PHB", "wondrous", "very-rare",
            true, "Spider Climb speed equal to walking speed. Immune to webs. Can cast Web once per day. Spiders regard you as non-threatening.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_displacement", "Cloak of Displacement", "PHB", "wondrous", "rare",
            true, "Attackers have disadvantage on attack rolls against you. Suppressed when you take damage until start of your next turn.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_elvenkind", "Cloak of Elvenkind", "PHB", "wondrous", "uncommon",
            true, "Advantage on DEX (Stealth) checks. Disadvantage on Perception checks against you.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_protection", "Cloak of Protection", "PHB", "wondrous", "uncommon",
            true, "+1 bonus to AC and saving throws.",
            1, 0, 0, null, 0, 0));
        list.add(i("cloak_of_the_bat", "Cloak of the Bat", "PHB", "wondrous", "rare",
            true, "Advantage on DEX (Stealth) checks. Flying speed of 40 ft in dim light or darkness (no need to move). Can cast Polymorph (bat form only) once per day.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_the_manta_ray", "Cloak of the Manta Ray", "PHB", "wondrous", "uncommon",
            false, "Breathe underwater and have a swimming speed of 60 ft while wearing this cloak.",
            0, 0, 0, null, 0, 0));
        list.add(i("crystal_ball", "Crystal Ball", "PHB", "wondrous", "very-rare",
            true, "While touching it, cast Scrying (DC 17) at will.",
            0, 0, 0, null, 0, 0));
        list.add(i("cube_of_force", "Cube of Force", "PHB", "wondrous", "rare",
            true, "6 charges. Create a cube-shaped field around you using charges, blocking gases, non-living matter, or all matter. Regains 1d20 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("deck_of_illusions", "Deck of Illusions", "PHB", "wondrous", "uncommon",
            false, "A set of 34 cards. Draw a card to create a Major Image illusion.",
            0, 0, 0, null, 0, 0));
        list.add(i("decanter_of_endless_water", "Decanter of Endless Water", "PHB", "wondrous", "uncommon",
            false, "1 pint of fresh water per minute (stream), or 30 gallons (fountain), or geyser (30-ft cone, 1d4 bludgeoning, DC 13 STR save).",
            0, 0, 0, null, 0, 0));
        list.add(i("dimensional_shackles", "Dimensional Shackles", "PHB", "wondrous", "rare",
            false, "Bind a creature of small-large size. Shackled creature can't use teleportation or planar travel.",
            0, 0, 0, null, 0, 0));
        list.add(i("dust_of_disappearance", "Dust of Disappearance", "PHB", "wondrous", "uncommon",
            false, "Invisible for 2d4 minutes. Anything worn/carried also becomes invisible. Revealed by See Invisibility or Truesight.",
            0, 0, 0, null, 0, 0));
        list.add(i("dust_of_dryness", "Dust of Dryness", "PHB", "wondrous", "uncommon",
            false, "Sprinkle on liquid to absorb it (up to 15 feet cube per pinch, stored in pellet). Smash pellet to release water.",
            0, 0, 0, null, 0, 0));
        list.add(i("eyes_of_charming", "Eyes of Charming", "PHB", "wondrous", "uncommon",
            true, "3 charges. Cast Charm Person (DC 13). Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("eyes_of_minute_seeing", "Eyes of Minute Seeing", "PHB", "wondrous", "uncommon",
            false, "Advantage on INT (Investigation) checks involving minute detail within 1 foot.",
            0, 0, 0, null, 0, 0));
        list.add(i("eyes_of_the_eagle", "Eyes of the Eagle", "PHB", "wondrous", "uncommon",
            true, "Advantage on WIS (Perception) checks relying on sight. In clear conditions, see up to 1 mile clearly.",
            0, 0, 0, null, 0, 0));
        list.add(i("figurine_of_wondrous_power_obsidian_steed", "Figurine of Wondrous Power (Obsidian Steed)", "PHB", "wondrous", "very-rare",
            true, "Can become a nightmare (night horse) for up to 24 hours, once per week. Can carry you to the Ethereal Plane.",
            0, 0, 0, null, 0, 0));
        list.add(i("gauntlets_of_ogre_power", "Gauntlets of Ogre Power", "PHB", "wondrous", "uncommon",
            true, "Your STR score is 19 while you wear these gauntlets. (No effect if STR is already 19 or higher.)",
            0, 0, 0, null, 0, 0));
        list.add(i("gem_of_brightness", "Gem of Brightness", "PHB", "wondrous", "uncommon",
            false, "50 charges. Cast Daylight (1 charge), blinding flash 30 ft (DC 15 CON, 5 charges), or beam at creature (2d8 radiant, DC 15 CON or blinded 1 min, 5 charges). Regains no charges.",
            0, 0, 0, null, 0, 0));
        list.add(i("gem_of_seeing", "Gem of Seeing", "PHB", "wondrous", "rare",
            true, "3 charges. Gain Truesight out to 120 ft for 10 minutes. Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("gloves_of_missile_snaring", "Gloves of Missile Snaring", "PHB", "wondrous", "uncommon",
            true, "When a ranged weapon attack hits you, use your reaction to reduce damage by 1d10 + DEX mod. If reduced to 0, you catch the missile.",
            0, 0, 0, null, 0, 0));
        list.add(i("gloves_of_swimming_and_climbing", "Gloves of Swimming and Climbing", "PHB", "wondrous", "uncommon",
            true, "Swimming and climbing speed equal to your walking speed, without the usual penalty.",
            0, 0, 0, null, 0, 0));
        list.add(i("goggles_of_night", "Goggles of Night", "PHB", "wondrous", "uncommon",
            false, "Darkvision of 60 ft. If you already have Darkvision, its range increases by 60 ft.",
            0, 0, 0, null, 0, 0));
        list.add(i("hat_of_disguise", "Hat of Disguise", "PHB", "wondrous", "uncommon",
            true, "Cast Disguise Self at will.",
            0, 0, 0, null, 0, 0));
        list.add(i("headband_of_intellect", "Headband of Intellect", "PHB", "wondrous", "uncommon",
            true, "Your INT score is 19 while you wear this headband. (No effect if INT is already 19 or higher.)",
            0, 0, 0, null, 0, 0));
        list.add(i("helm_of_brilliance", "Helm of Brilliance", "PHB", "wondrous", "very-rare",
            true, "Stores 30 gems (up to). Grants fire resistance. Can cast Prismatic Spray, Wall of Fire, Fireball (using stored gems). Emits light and eventually collapses as gems are spent.",
            0, 0, 0, null, 0, 0));
        list.add(i("helm_of_comprehending_languages", "Helm of Comprehending Languages", "PHB", "wondrous", "uncommon",
            false, "Cast Comprehend Languages at will.",
            0, 0, 0, null, 0, 0));
        list.add(i("helm_of_telepathy", "Helm of Telepathy", "PHB", "wondrous", "uncommon",
            true, "Cast Detect Thoughts at will (DC 13). As an action, send a telepathic message to detected creature.",
            0, 0, 0, null, 0, 0));
        list.add(i("ioun_stone_absorption", "Ioun Stone (Absorption)", "PHB", "wondrous", "very-rare",
            true, "While orbiting your head, absorbs spells of 4th level or lower, granting them as spell slots. Absorbs 20 spell levels total before burning out.",
            0, 0, 0, null, 0, 0));
        list.add(i("ioun_stone_agility", "Ioun Stone (Agility)", "PHB", "wondrous", "very-rare",
            true, "DEX +2 (to maximum of 20).",
            0, 0, 0, Map.of("DEX", 2), 0, 0));
        list.add(i("ioun_stone_fortitude", "Ioun Stone (Fortitude)", "PHB", "wondrous", "very-rare",
            true, "CON +2 (to maximum of 20).",
            0, 0, 0, Map.of("CON", 2), 0, 0));
        list.add(i("ioun_stone_insight", "Ioun Stone (Insight)", "PHB", "wondrous", "very-rare",
            true, "WIS +2 (to maximum of 20).",
            0, 0, 0, Map.of("WIS", 2), 0, 0));
        list.add(i("ioun_stone_intellect", "Ioun Stone (Intellect)", "PHB", "wondrous", "very-rare",
            true, "INT +2 (to maximum of 20).",
            0, 0, 0, Map.of("INT", 2), 0, 0));
        list.add(i("ioun_stone_leadership", "Ioun Stone (Leadership)", "PHB", "wondrous", "very-rare",
            true, "CHA +2 (to maximum of 20).",
            0, 0, 0, Map.of("CHA", 2), 0, 0));
        list.add(i("ioun_stone_mastery", "Ioun Stone (Mastery)", "PHB", "wondrous", "legendary",
            true, "Proficiency bonus +1.",
            0, 0, 0, null, 0, 0));
        list.add(i("ioun_stone_protection", "Ioun Stone (Protection)", "PHB", "wondrous", "rare",
            true, "+1 bonus to AC.",
            1, 0, 0, null, 0, 0));
        list.add(i("ioun_stone_reserve", "Ioun Stone (Reserve)", "PHB", "wondrous", "rare",
            true, "Stores up to 3 spell levels. Cast stored spells at will.",
            0, 0, 0, null, 0, 0));
        list.add(i("ioun_stone_strength", "Ioun Stone (Strength)", "PHB", "wondrous", "very-rare",
            true, "STR +2 (to maximum of 20).",
            0, 0, 0, Map.of("STR", 2), 0, 0));
        list.add(i("ioun_stone_sustenance", "Ioun Stone (Sustenance)", "PHB", "wondrous", "rare",
            true, "No need to eat or drink.",
            0, 0, 0, null, 0, 0));
        list.add(i("lantern_of_revealing", "Lantern of Revealing", "PHB", "wondrous", "uncommon",
            false, "Invisible creatures and objects within 30 ft are revealed in the lantern's light.",
            0, 0, 0, null, 0, 0));
        list.add(i("manual_of_bodily_health", "Manual of Bodily Health", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, CON increases by 2 (max 22). Then becomes a mundane book for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("manual_of_gainful_exercise", "Manual of Gainful Exercise", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, STR increases by 2 (max 22). Then becomes mundane for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("manual_of_quickness_of_action", "Manual of Quickness of Action", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, DEX increases by 2 (max 22). Then becomes mundane for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("medallion_of_thoughts", "Medallion of Thoughts", "PHB", "wondrous", "uncommon",
            true, "3 charges. Cast Detect Thoughts (DC 13). Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("mirror_of_life_trapping", "Mirror of Life Trapping", "PHB", "wondrous", "very-rare",
            false, "12 cells. Creatures who see their reflection make a DC 15 CHA save or be trapped. Interact with trapped creatures or release them.",
            0, 0, 0, null, 0, 0));
        list.add(i("necklace_of_adaptation", "Necklace of Adaptation", "PHB", "wondrous", "uncommon",
            true, "Breathe in any environment. Advantage on saves vs gases and vapors.",
            0, 0, 0, null, 0, 0));
        list.add(i("necklace_of_fireballs", "Necklace of Fireballs", "PHB", "wondrous", "rare",
            false, "3-9 beads. Detach and throw a bead to cast a Fireball (DC 15, damage based on number of beads merged).",
            0, 0, 0, null, 0, 0));
        list.add(i("necklace_of_prayer_beads", "Necklace of Prayer Beads", "PHB", "wondrous", "rare",
            true, "2-4 special beads. Each can cast Bless, Cure Wounds (2nd-level), Lesser Restoration, Heroism, Branding Smite (3rd-level), Guiding Bolt (4th-level), or Wind Walk. Recharges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("pearl_of_power", "Pearl of Power", "PHB", "wondrous", "uncommon",
            true, "Once per day, recover one expended spell slot of 3rd level or lower.",
            0, 0, 0, null, 0, 0));
        list.add(i("periapt_of_health", "Periapt of Health", "PHB", "wondrous", "uncommon",
            false, "Immune to disease. Any disease currently affecting you has no effect.",
            0, 0, 0, null, 0, 0));
        list.add(i("periapt_of_proof_against_poison", "Periapt of Proof Against Poison", "PHB", "wondrous", "rare",
            false, "Immunity to poison damage and the poisoned condition.",
            0, 0, 0, null, 0, 0));
        list.add(i("periapt_of_wound_closure", "Periapt of Wound Closure", "PHB", "wondrous", "uncommon",
            true, "Stabilize at 0 HP automatically. Roll twice and take the higher on hit dice.",
            0, 0, 0, null, 0, 0));
        list.add(i("portable_hole", "Portable Hole", "PHB", "wondrous", "rare",
            false, "Unfolds to a 6-foot-wide, 10-foot-deep extradimensional hole. Can carry creatures and objects; they don't need air for up to 10 minutes.",
            0, 0, 0, null, 0, 0));
        list.add(i("restorative_ointment", "Restorative Ointment", "PHB", "wondrous", "uncommon",
            false, "3 uses. As an action, apply to restore 2d8+2 HP and remove poison and disease.",
            0, 0, 0, null, 0, 0));
        list.add(i("rope_of_climbing", "Rope of Climbing", "PHB", "wondrous", "uncommon",
            false, "60-foot silk rope that can animate on command. Can tie/untie itself, move up to 10 ft per round, and support 3,000 lb.",
            0, 0, 0, null, 0, 0));
        list.add(i("scarab_of_protection", "Scarab of Protection", "PHB", "wondrous", "legendary",
            true, "12 charges. Immunity to the harm caused by death spells. Expend a charge to give advantage on a save against a spell. When 0 charges remain, destroyed.",
            0, 0, 0, null, 0, 0));
        list.add(i("sending_stones", "Sending Stones", "PHB", "wondrous", "uncommon",
            false, "A pair. As an action, send a 25-word message to the other stone's holder once per day.",
            0, 0, 0, null, 0, 0));
        list.add(i("slippers_of_spider_climbing", "Slippers of Spider Climbing", "PHB", "wondrous", "uncommon",
            true, "Spider Climb speed equal to walking speed.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_charming", "Staff of Charming", "PHB", "wondrous", "rare",
            true, "10 charges. Cast Charm Person (1 charge), Command (1 charge), or Comprehend Languages (1 charge). Regains 1d8+2 at dawn. If 0, 5% chance it crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("tome_of_clear_thought", "Tome of Clear Thought", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, INT increases by 2 (max 22). Then mundane for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("tome_of_leadership_and_influence", "Tome of Leadership and Influence", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, CHA increases by 2 (max 22). Then mundane for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("tome_of_understanding", "Tome of Understanding", "PHB", "wondrous", "very-rare",
            false, "After 48 hours studying, WIS increases by 2 (max 22). Then mundane for 100 years.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_binding", "Wand of Binding", "PHB", "wondrous", "rare",
            true, "7 charges. Cast Hold Monster (1 charge per spell level). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_enemy_detection", "Wand of Enemy Detection", "PHB", "wondrous", "rare",
            true, "7 charges. Sense the presence of hostile creatures within 60 ft for 1 minute. Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_fear", "Wand of Fear", "PHB", "wondrous", "rare",
            true, "7 charges. Frighten creatures in a 60-ft cone (DC 15 WIS save) or cast Command (1 charge). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_fireballs", "Wand of Fireballs", "PHB", "wondrous", "rare",
            true, "7 charges. Cast Fireball (3rd-level or higher, 1 charge per spell level above 3rd; DC 15). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_lightning_bolts", "Wand of Lightning Bolts", "PHB", "wondrous", "rare",
            true, "7 charges. Cast Lightning Bolt (3rd-level or higher; DC 15). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_magic_missiles", "Wand of Magic Missiles", "PHB", "wondrous", "uncommon",
            false, "7 charges. Fire 1-3 Magic Missiles (expend charges equal to darts). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_paralysis", "Wand of Paralysis", "PHB", "wondrous", "rare",
            true, "7 charges. Expend 1 charge to fire a beam; target makes DC 15 CON save or is paralyzed for 1 minute. Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_polymorph", "Wand of Polymorph", "PHB", "wondrous", "very-rare",
            true, "7 charges. Cast Polymorph (DC 15). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_secrets", "Wand of Secrets", "PHB", "wondrous", "uncommon",
            false, "3 charges. If secret doors or traps are within 30 ft, the wand pulses and points toward the nearest. Regains 1d3 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_web", "Wand of Web", "PHB", "wondrous", "uncommon",
            true, "7 charges. Cast Web (DC 15). Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_wonder", "Wand of Wonder", "PHB", "wondrous", "rare",
            true, "7 charges. Roll d100 for a random wild magic effect. Regains 1d6+1 charges at dawn.",
            0, 0, 0, null, 0, 0));
        list.add(i("winged_boots", "Winged Boots", "PHB", "wondrous", "uncommon",
            true, "Flying speed equal to walking speed for up to 4 hours per day (split into 1-minute intervals).",
            0, 0, 0, null, 0, 0));
        list.add(i("wings_of_flying", "Wings of Flying", "PHB", "wondrous", "rare",
            true, "Speak the command word to sprout wings; flying speed of 60 ft for 1 hour. Recharges at dawn.",
            0, 0, 0, null, 0, 0));

        // ── PHB — Staves ──────────────────────────────────────────────────────
        list.add(i("staff_of_fire", "Staff of Fire", "PHB", "staff", "very-rare",
            true, "10 charges. Resistance to fire damage. Cast Burning Hands (1 charge), Fireball (3 charges), or Wall of Fire (4 charges) (DC 17). Regains 1d6+4 at dawn. If 0, 5% chance it crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_frost", "Staff of Frost", "PHB", "staff", "very-rare",
            true, "10 charges. Resistance to cold damage. Cast Cone of Cold (5 charges), Fog Cloud (1 charge), Ice Storm (4 charges), or Wall of Ice (4 charges) (DC 17). Regains 1d6+4 at dawn. If 0, 5% chance it crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_healing", "Staff of Healing", "PHB", "staff", "rare",
            true, "10 charges. Cast Cure Wounds, Lesser Restoration, or Mass Cure Wounds. Regains 1d6+4 at dawn. If 0, 5% chance it crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_power", "Staff of Power", "PHB", "staff", "very-rare",
            true, "20 charges. +2 to attack/spell attack, AC, saves. Cast Cone of Cold, Fireball, Globe of Invulnerability, Hold Monster, Levitate, Lightning Bolt, Magic Missile, Ray of Enfeeblement, Wall of Force. Retributive strike on destruction.",
            2, 2, 0, null, 0, 0));
        list.add(i("staff_of_striking", "Staff of Striking", "PHB", "staff", "very-rare",
            true, "10 charges. +3 to attack and damage. Expend 1-3 charges for 1d6 extra damage per charge. Regains 1d6+4 at dawn. If 0, 5% chance it crumbles.",
            0, 3, 3, null, 0, 0));
        list.add(i("staff_of_swarming_insects", "Staff of Swarming Insects", "PHB", "staff", "rare",
            true, "10 charges. Cast Giant Insect, Insect Plague, or create a swarm. Regains 1d6+4 at dawn. If 0, 5% chance it crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_the_magi", "Staff of the Magi", "PHB", "staff", "legendary",
            true, "50 charges. Absorb spells, +2 attack/spell attack. Cast many powerful spells. Retributive strike on destruction.",
            0, 2, 0, null, 0, 0));
        list.add(i("staff_of_the_python", "Staff of the Python", "PHB", "staff", "uncommon",
            true, "Become a giant constrictor snake (use action). Snake follows commands. Reverts or dies when it drops to 0 HP.",
            0, 0, 0, null, 0, 0));
        list.add(i("staff_of_thunder_and_lightning", "Staff of Thunder and Lightning", "PHB", "staff", "legendary",
            true, "+2 to attack. 5 properties (each recharging at dawn): Lightning, Thunder, Lightning Strike, Thunderclap, Thunder and Lightning.",
            0, 2, 0, null, 0, 0));

        // ── XGtE — Wondrous Items ─────────────────────────────────────────────
        list.add(i("bead_of_force", "Bead of Force", "XGtE", "wondrous", "rare",
            false, "Throw the bead (60 ft). Creatures within 10 ft make a DC 15 DEX save or take 5d4 bludgeoning and be enclosed in a sphere of force for 1 minute.",
            0, 0, 0, null, 0, 0));
        list.add(i("cauldron_of_plenty", "Cauldron of Plenty", "XGtE", "wondrous", "rare",
            false, "1-foot-diameter iron cauldron. Once per day, cook a hearty stew; up to 36 servings. Stew cures disease, neutralizes poison, grants max HP from HD for 24 hours.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_billowing", "Cloak of Billowing", "XGtE", "wondrous", "common",
            false, "As a bonus action, this cloak billows dramatically.",
            0, 0, 0, null, 0, 0));
        list.add(i("cloak_of_many_fashions", "Cloak of Many Fashions", "XGtE", "wondrous", "common",
            false, "While wearing this cloak, you can use a bonus action to change its style, color, and apparent quality.",
            0, 0, 0, null, 0, 0));
        list.add(i("clockwork_amulet", "Clockwork Amulet", "XGtE", "wondrous", "common",
            false, "When you make an attack roll, you can forgo rolling the d20 and treat it as if you had rolled a 10. Once per long rest.",
            0, 0, 0, null, 0, 0));
        list.add(i("coiling_grasp_tattoo", "Coiling Grasp Tattoo", "XGtE", "wondrous", "uncommon",
            true, "Produced by a magic tattoo needle. Once per day, extend the tattoo as a magical grasping hand to attempt to grapple a creature within 15 ft.",
            0, 0, 0, null, 0, 0));
        list.add(i("eldritch_claw_tattoo", "Eldritch Claw Tattoo", "XGtE", "wondrous", "uncommon",
            true, "Unarmed strikes with the tattooed limb are magical and deal 1d6 more damage. Once per day, the hand extends up to 15 ft for 1 minute.",
            0, 0, 0, null, 0, 0));
        list.add(i("ghost_step_tattoo", "Ghost Step Tattoo", "XGtE", "wondrous", "very-rare",
            true, "3 charges (regain 1d3 at dawn). Expend 1 to become incorporeal until end of your turn (move through objects/creatures; take 1d10 force at end of turn if inside).",
            0, 0, 0, null, 0, 0));
        list.add(i("helm_of_awareness", "Helm of Awareness", "XGtE", "wondrous", "very-rare",
            true, "+5 to initiative. You can't be surprised while you are not incapacitated.",
            0, 0, 0, null, 0, 0));
        list.add(i("moon_touched_sword", "Moon-Touched Sword", "XGtE", "wondrous", "common",
            false, "In darkness, the blade of this sword sheds moonlight, creating dim light in a 15-foot radius.",
            0, 0, 0, null, 0, 0));
        list.add(i("pot_of_awakening", "Pot of Awakening", "XGtE", "wondrous", "common",
            false, "Grow and awaken a tree shrub that attacks nearby enemies. Takes 30 days of cultivation.",
            0, 0, 0, null, 0, 0));
        list.add(i("self_absorbing_shield", "Shield of Expression", "XGtE", "wondrous", "common",
            false, "While holding this shield, you can use a bonus action to alter the expression of the face embossed on it.",
            0, 0, 0, null, 0, 0));
        list.add(i("smoldering_armor", "Smoldering Armor", "XGtE", "wondrous", "common",
            false, "Wisps of harmless smoke rise from the armor.",
            0, 0, 0, null, 0, 0));
        list.add(i("barrier_tattoo_uncommon", "Barrier Tattoo (Uncommon)", "XGtE", "wondrous", "uncommon",
            true, "While not wearing armor, your AC is 12 + DEX mod.",
            0, 0, 0, null, 0, 0));
        list.add(i("barrier_tattoo_rare", "Barrier Tattoo (Rare)", "XGtE", "wondrous", "rare",
            true, "While not wearing armor, your AC is 15 + DEX mod (max +2).",
            0, 0, 0, null, 0, 0));
        list.add(i("barrier_tattoo_very_rare", "Barrier Tattoo (Very Rare)", "XGtE", "wondrous", "very-rare",
            true, "While not wearing armor, your AC is 18.",
            0, 0, 0, null, 0, 0));
        list.add(i("blood_fury_tattoo", "Blood Fury Tattoo", "XGtE", "wondrous", "legendary",
            true, "10 charges (regain 1d6+4 at dawn). When you hit with an attack, expend 1 charge for +4d6 necrotic + regain same HP. When damaged by a creature, use reaction + 1 charge to make one weapon attack.",
            0, 0, 0, null, 0, 0));

        // ── TCoE — Wondrous Items ─────────────────────────────────────────────
        list.add(i("absorbing_tattoo", "Absorbing Tattoo", "TCoE", "wondrous", "very-rare",
            true, "Resistance to one damage type (chosen when created). Reaction when taking that damage: become immune to it this turn and regain HP equal to half the damage dealt. Once per long rest.",
            0, 0, 0, null, 0, 0));
        list.add(i("all_purpose_tool", "All-Purpose Tool", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attacks and save DC. Cast Guidance as a bonus action. Use as any artisan's tool.",
            0, 1, 0, null, 0, 0));
        list.add(i("amulet_of_the_devout", "Amulet of the Devout", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. Regain 1 use of Channel Divinity per day.",
            0, 0, 0, null, 1, 0));
        list.add(i("arcane_grimoire", "Arcane Grimoire", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. Add 1 spell of your choice to your spellbook per day.",
            0, 0, 0, null, 1, 0));
        list.add(i("astral_shard", "Astral Shard", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. Can use it as spellcasting focus. Teleport 10 ft when you cast a Metamagic spell.",
            0, 0, 0, null, 1, 0));
        list.add(i("bloodwell_vial", "Bloodwell Vial", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. When you roll initiative with 0 sorcery points, regain 5 sorcery points.",
            0, 0, 0, null, 1, 0));
        list.add(i("cauldron_of_rebirth", "Cauldron of Rebirth", "TCoE", "wondrous", "very-rare",
            true, "Once per day, place a dead humanoid in the cauldron. After 1 hour it is resurrected with no memory of death. After 3 uses, the cauldron crumbles.",
            0, 0, 0, null, 0, 0));
        list.add(i("crystalline_chronicle", "Crystalline Chronicle", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. A crystal orb that acts as your spellbook and spellcasting focus.",
            0, 0, 0, null, 1, 0));
        list.add(i("duplicitous_manuscript", "Duplicitous Manuscript", "TCoE", "wondrous", "uncommon",
            true, "+1/+2/+3 to spell attack rolls and spell save DC. An illusory duplicate of any text appears in the manuscript when you open it.",
            0, 0, 0, null, 1, 0));
        list.add(i("hell_hound_cloak", "Hell Hound Cloak", "TCoE", "wondrous", "rare",
            true, "Cursed item: can't be removed without a Remove Curse. While worn, you can use an action to exhale fire (3d6 fire, DC 13 DEX for half) up to 3 times per day.",
            0, 0, 0, null, 0, 0));
        list.add(i("illuminator_tattoo", "Illuminator's Tattoo", "TCoE", "wondrous", "common",
            true, "While holding a writing instrument, you can write with it, and the words shine with a faint luminescence. You can read any writing.",
            0, 0, 0, null, 0, 0));
        list.add(i("lifewell_tattoo", "Lifewell Tattoo", "TCoE", "wondrous", "very-rare",
            true, "Regain HP equal to your HP max once (automatic) when you would drop to 0 HP. After this triggers, the tattoo fades.",
            0, 0, 0, null, 0, 0));
        list.add(i("masquerade_tattoo", "Masquerade Tattoo", "TCoE", "wondrous", "uncommon",
            true, "Cast Disguise Self at will. When using it, you can make the tattoo invisible or move it to another part of your body.",
            0, 0, 0, null, 0, 0));
        list.add(i("orb_of_shielding", "Orb of Shielding", "TCoE", "wondrous", "common",
            true, "As a reaction to taking energy damage (choose type at creation), reduce damage by 1d4. Usable as a spellcasting focus.",
            0, 0, 0, null, 0, 0));
        list.add(i("outer_essence_shard", "Outer Essence Shard", "TCoE", "wondrous", "rare",
            true, "+1 to spell attack rolls and spell save DC. When you spend Sorcery Points, trigger an outer planar effect based on shard type.",
            0, 0, 0, null, 1, 0));
        list.add(i("puzzle_box", "Puzzle Box", "TCoE", "wondrous", "common",
            false, "Opens when a specific sequence of actions is performed. Usually contains something valuable. Can be locked and reset.",
            0, 0, 0, null, 0, 0));
        list.add(i("ruby_of_the_war_mage", "Ruby of the War Mage", "TCoE", "wondrous", "common",
            true, "Affix this gem to a simple or martial weapon. The weapon counts as a spellcasting focus. +1 to spell attack rolls with that weapon.",
            0, 0, 0, null, 0, 0));
        list.add(i("spellwrought_tattoo", "Spellwrought Tattoo", "TCoE", "wondrous", "common",
            false, "Cast the spell tattooed on your skin once, then the tattoo vanishes.",
            0, 0, 0, null, 0, 0));
        list.add(i("teeth_of_dahlver_nar", "Teeth of Dahlver-Nar", "TCoE", "wondrous", "legendary",
            false, "A pouch of 16 ivory teeth. Plant one in the ground as an action to summon a creature based on which tooth (from a d20 table).",
            0, 0, 0, null, 0, 0));
        list.add(i("wand_of_wonder_tcoe", "Wand of Orcus", "TCoE", "wondrous", "legendary",
            true, "+3 to attack and damage. Cast Animate Dead at will. Various lich/undead powers. Cursed: DC 17 CHA save each day or fall under Orcus's influence.",
            0, 3, 3, null, 0, 0));

        return list;
    }

    // ── Factory helper ────────────────────────────────────────────────────────

    private MagicItemTemplate i(String id, String name, String source, String cat, String rarity,
                                 boolean attune, String desc, int acBonus, int atkBonus, int dmgBonus,
                                 Map<String, Integer> abilityBonuses, int saveDcBonus, int speedBonus) {
        return new MagicItemTemplate(id, name, source, cat, rarity, attune, desc,
                                     acBonus, atkBonus, dmgBonus, abilityBonuses, saveDcBonus, speedBonus);
    }

    // ── Inner record ──────────────────────────────────────────────────────────

    public record MagicItemTemplate(
        String id,
        String name,
        String source,
        String category,
        String rarity,
        boolean requiresAttunement,
        String description,
        int acBonus,
        int attackBonus,
        int damageBonus,
        Map<String, Integer> abilityBonuses,
        int saveDcBonus,
        int speedBonus
    ) {}
}
