package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.SpellDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemorySpellRepository implements com.dnd.builder.core.port.out.SpellRepository {

    private final List<SpellDefinition> spells;
    private final Map<String, SpellDefinition> byId;

    public InMemorySpellRepository() {
        spells = buildSpells();
        byId   = new HashMap<>();
        spells.forEach(s -> byId.put(s.getId(), s));
    }

    public List<SpellDefinition> getAllSpells() { return Collections.unmodifiableList(spells); }
    public SpellDefinition findById(String id)  { return byId.get(id); }

    /** Spells available for a given class, optionally filtered by level */
    public List<SpellDefinition> findByClass(String classId, Integer level) {
        return spells.stream()
            .filter(s -> s.getClasses() != null && s.getClasses().contains(classId))
            .filter(s -> level == null || s.getLevel() == level)
            .sorted(Comparator.comparingInt(SpellDefinition::getLevel)
                              .thenComparing(SpellDefinition::getName))
            .collect(Collectors.toList());
    }

    public List<SpellDefinition> findCantripsForClass(String classId) { return findByClass(classId, 0); }
    public List<SpellDefinition> findLevel1ForClass(String classId)   { return findByClass(classId, 1); }
    public List<SpellDefinition> level2For(String classId)            { return findByClass(classId, 2); }

    // ── Spell com.dnd.builder shorthand ───────────────────────────────────────────────
    private static SpellDefinition s(String id, String name, int lvl, String school,
                                      String ct, String range, String dur, boolean conc,
                                      boolean ritual, String comp, String desc,
                                      String... classes) {
        var sp = new SpellDefinition();
        sp.setId(id); sp.setName(name); sp.setLevel(lvl); sp.setSchool(school);
        sp.setCastingTime(ct); sp.setRange(range); sp.setDuration(dur);
        sp.setConcentration(conc); sp.setRitual(ritual); sp.setComponents(comp);
        sp.setDescription(desc); sp.setClasses(List.of(classes));
        return sp;
    }

    private List<SpellDefinition> buildSpells() {
        var list = new ArrayList<SpellDefinition>();

        // ══ CANTRIPS (Level 0) ═══════════════════════════════════════════════
        list.add(s("acid_splash","Acid Splash",0,"Conjuration","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Hurl a bubble of acid. One creature (or two adj.) takes 1d6 acid damage on failed DEX save.",
            "wizard","sorcerer"));
        list.add(s("blade_ward","Blade Ward",0,"Abjuration","1 action","Self","1 round",false,false,"V,S",
            "Until end of your next turn, you have resistance to bludgeoning, piercing, and slashing damage from weapon attacks.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("chill_touch","Chill Touch",0,"Necromancy","1 action","120 ft","1 round",false,false,"V,S",
            "Create spectral skeletal hand. Ranged spell attack; 1d8 necrotic damage, prevents regaining HP until next turn.",
            "sorcerer","warlock","wizard"));
        list.add(s("dancing_lights","Dancing Lights",0,"Evocation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "Create up to 4 torch-sized lights within 60 ft of each other. Move them up to 60 ft as a bonus action.",
            "bard","sorcerer","wizard"));
        list.add(s("druidcraft","Druidcraft",0,"Transmutation","1 action","30 ft","Instantaneous",false,false,"V,S",
            "Minor nature tricks: predict weather, make flower bloom, create small sensory effect.",
            "druid"));
        list.add(s("eldritch_blast","Eldritch Blast",0,"Evocation","1 action","120 ft","Instantaneous",false,false,"V,S",
            "A beam of crackling energy. Ranged spell attack; 1d10 force damage. Extra beams at higher levels.",
            "warlock"));
        list.add(s("fire_bolt","Fire Bolt",0,"Evocation","1 action","120 ft","Instantaneous",false,false,"V,S",
            "Ranged spell attack; 1d10 fire damage. Ignites flammable objects.",
            "sorcerer","wizard"));
        list.add(s("friends","Friends",0,"Enchantment","1 action","Self","Up to 1 min",true,false,"S,M",
            "Advantage on CHA checks against one non-hostile creature. Creature knows afterwards.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("guidance","Guidance",0,"Divination","1 action","Touch","Up to 1 min",true,false,"V,S",
            "Add 1d4 to one ability check of your choice while concentrating.",
            "cleric","druid"));
        list.add(s("light","Light",0,"Evocation","1 action","Touch","1 hour",false,false,"V,M",
            "An object sheds bright light in a 20-ft radius and dim light another 20 ft. DEX save to resist.",
            "bard","cleric","sorcerer","wizard"));
        list.add(s("mage_hand","Mage Hand",0,"Conjuration","1 action","30 ft","1 minute",false,false,"V,S",
            "Spectral floating hand that can manipulate objects up to 10 lbs.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("mending","Mending",0,"Transmutation","1 minute","Touch","Instantaneous",false,false,"V,S,M",
            "Repairs a single break or tear in an object.",
            "bard","cleric","druid","sorcerer","wizard"));
        list.add(s("message","Message",0,"Transmutation","1 action","120 ft","1 round",false,false,"V,S,M",
            "Point at a creature; whisper a message only they can hear and reply to.",
            "bard","sorcerer","wizard"));
        list.add(s("minor_illusion","Minor Illusion",0,"Illusion","1 action","30 ft","1 minute",false,false,"S,M",
            "Create a sound or image of an object. Investigation vs. spell save DC to disbelieve.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("poison_spray","Poison Spray",0,"Conjuration","1 action","10 ft","Instantaneous",false,false,"V,S",
            "Project a puff of toxic gas. CON save or 1d12 poison damage.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("prestidigitation","Prestidigitation",0,"Transmutation","1 action","10 ft","Up to 1 hr",false,false,"V,S",
            "Minor magical tricks: light/snuff a flame, soil/clean an object, chill/warm/flavor food, mark an object, create a small illusion.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("produce_flame","Produce Flame",0,"Conjuration","1 action","Self","10 minutes",false,false,"V,S",
            "Flame in hand sheds light; can hurl it as a ranged spell attack (1d8 fire).",
            "druid"));
        list.add(s("ray_of_frost","Ray of Frost",0,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Ranged spell attack; 1d8 cold damage and speed reduced by 10 ft until start of your next turn.",
            "sorcerer","wizard"));
        list.add(s("resistance","Resistance",0,"Abjuration","1 action","Touch","Up to 1 min",true,false,"V,S,M",
            "Add 1d4 to one saving throw of your choice while concentrating.",
            "cleric","druid"));
        list.add(s("sacred_flame","Sacred Flame",0,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "DEX saving throw or 1d8 radiant damage. Ignores cover.",
            "cleric"));
        list.add(s("shillelagh","Shillelagh",0,"Transmutation","1 bonus action","Touch","1 minute",false,false,"V,S,M",
            "Your club/quarterstaff uses WIS for attacks and deals 1d8.",
            "druid"));
        list.add(s("shocking_grasp","Shocking Grasp",0,"Evocation","1 action","Touch","Instantaneous",false,false,"V,S",
            "Melee spell attack with advantage vs. metal armor; 1d8 lightning, target can't take reactions.",
            "sorcerer","wizard"));
        list.add(s("spare_the_dying","Spare the Dying",0,"Necromancy","1 action","Touch","Instantaneous",false,false,"V,S",
            "A living creature with 0 HP becomes stable.",
            "cleric"));
        list.add(s("thaumaturgy","Thaumaturgy",0,"Transmutation","1 action","30 ft","Up to 1 min",false,false,"V",
            "Manifest minor wonder: booming voice, flame flickers, tremors, wide eyes, change door state.",
            "cleric"));
        list.add(s("thorn_whip","Thorn Whip",0,"Transmutation","1 action","30 ft","Instantaneous",false,false,"V,S,M",
            "Melee spell attack; 1d6 piercing and pull creature 10 ft toward you.",
            "druid"));
        list.add(s("true_strike","True Strike",0,"Divination","1 action","30 ft","Up to 1 round",true,false,"S",
            "Gain insight into a creature's defenses; advantage on first attack against it next turn.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("vicious_mockery","Vicious Mockery",0,"Enchantment","1 action","60 ft","Instantaneous",false,false,"V",
            "WIS save or take 1d4 psychic damage and disadvantage on next attack roll.",
            "bard"));

        // ══ LEVEL 1 SPELLS ═══════════════════════════════════════════════════
        list.add(s("alarm","Alarm",1,"Abjuration","1 minute","30 ft","8 hours",false,true,"V,S,M",
            "Set a ward on an area 20-ft cube; mental or audible alarm when a tiny or larger creature enters.",
            "ranger","wizard"));
        list.add(s("animal_friendship","Animal Friendship",1,"Enchantment","1 action","30 ft","24 hours",false,false,"V,S,M",
            "Convince a beast you mean no harm. WIS save (DC = spell save). Intelligence must be 3 or lower.",
            "bard","druid","ranger"));
        list.add(s("armor_of_agathys","Armor of Agathys",1,"Abjuration","1 action","Self","1 hour",false,false,"V,S,M",
            "Gain 5 temporary HP; any creature striking you takes 5 cold damage while you have these THP.",
            "warlock"));
        list.add(s("arms_of_hadar","Arms of Hadar",1,"Conjuration","1 action","Self (10-ft)","Instantaneous",false,false,"V,S",
            "All creatures within 10 ft: STR save or take 2d6 necrotic and lose their reaction.",
            "warlock"));
        list.add(s("bane","Bane",1,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Up to 3 creatures: CHA save or subtract 1d4 from attack rolls and saving throws.",
            "bard","cleric"));
        list.add(s("bless","Bless",1,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Up to 3 creatures add 1d4 to attack rolls and saving throws.",
            "cleric","paladin"));
        list.add(s("burning_hands","Burning Hands",1,"Evocation","1 action","Self (15-ft cone)","Instantaneous",false,false,"V,S",
            "15-ft cone; DEX save or 3d6 fire damage (half on save).",
            "sorcerer","wizard"));
        list.add(s("charm_person","Charm Person",1,"Enchantment","1 action","30 ft","1 hour",false,false,"V,S",
            "WIS save or charmed by you for 1 hour. Advantage if in combat.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("chromatic_orb","Chromatic Orb",1,"Evocation","1 action","90 ft","Instantaneous",false,false,"V,S,M",
            "Ranged spell attack; 3d8 of chosen damage type (acid, cold, fire, lightning, poison, or thunder).",
            "sorcerer","wizard"));
        list.add(s("color_spray","Color Spray",1,"Illusion","1 action","Self (15-ft cone)","1 round",false,false,"V,S,M",
            "Blind creatures with 6d10 HP, starting from lowest, for 1 round.",
            "sorcerer","wizard"));
        list.add(s("command","Command",1,"Enchantment","1 action","60 ft","1 round",false,false,"V",
            "WIS save or follow a one-word command on its next turn: Approach, Drop, Flee, Grovel, Halt.",
            "cleric","paladin"));
        list.add(s("comprehend_languages","Comprehend Languages",1,"Divination","1 action","Self","1 hour",false,true,"V,S,M",
            "Understand spoken/written language you hear or see.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("cure_wounds","Cure Wounds",1,"Evocation","1 action","Touch","Instantaneous",false,false,"V,S",
            "Restore 1d8 + spellcasting ability modifier HP. No effect on undead or constructs.",
            "bard","cleric","druid","paladin","ranger"));
        list.add(s("detect_evil_good","Detect Evil and Good",1,"Divination","1 action","Self","Up to 10 min",true,false,"V,S",
            "Know if aberrations, celestials, elementals, fey, fiends, or undead are within 30 ft.",
            "cleric","paladin"));
        list.add(s("detect_magic","Detect Magic",1,"Divination","1 action","Self","Up to 10 min",true,true,"V,S",
            "Sense magic within 30 ft, see a faint aura, and learn its school.",
            "bard","cleric","druid","paladin","ranger","sorcerer","wizard"));
        list.add(s("detect_poison_disease","Detect Poison and Disease",1,"Divination","1 action","Self","Up to 10 min",true,true,"V,S,M",
            "Sense the presence and location of poisons, venomous creatures, and diseases within 30 ft.",
            "cleric","druid","paladin","ranger"));
        list.add(s("disguise_self","Disguise Self",1,"Illusion","1 action","Self","1 hour",false,false,"V,S",
            "Change your appearance including clothing. Investigation check vs. spell save DC to detect.",
            "bard","sorcerer","wizard"));
        list.add(s("dissonant_whispers","Dissonant Whispers",1,"Enchantment","1 action","60 ft","Instantaneous",false,false,"V",
            "WIS save or take 3d6 psychic and use reaction to flee; half on save.",
            "bard"));
        list.add(s("divine_favor","Divine Favor",1,"Evocation","1 bonus action","Self","Up to 1 min",true,false,"V,S",
            "Your weapon attacks deal an extra 1d4 radiant damage.",
            "paladin"));
        list.add(s("entangle","Entangle",1,"Conjuration","1 action","90 ft","Up to 1 min",true,false,"V,S",
            "20-ft square; STR save or restrained for duration. Difficult terrain.",
            "druid"));
        list.add(s("expeditious_retreat","Expeditious Retreat",1,"Transmutation","1 bonus action","Self","Up to 10 min",true,false,"V,S",
            "Dash as a bonus action for the duration.",
            "sorcerer","warlock","wizard"));
        list.add(s("faerie_fire","Faerie Fire",1,"Evocation","1 action","60 ft","Up to 1 min",true,false,"V",
            "20-ft cube; DEX save or outlined in light. Attacks have advantage; can't benefit from invisibility.",
            "bard","druid"));
        list.add(s("false_life","False Life",1,"Necromancy","1 action","Self","1 hour",false,false,"V,S,M",
            "Gain 1d4+4 temporary hit points.",
            "sorcerer","wizard"));
        list.add(s("feather_fall","Feather Fall",1,"Transmutation","1 reaction","60 ft","1 minute",false,false,"V,M",
            "Up to 5 falling creatures descend 60 ft/round and take no fall damage.",
            "bard","sorcerer","wizard"));
        list.add(s("find_familiar","Find Familiar",1,"Conjuration","1 hour","10 ft","Instantaneous",false,true,"V,S,M",
            "Summon a familiar (spirit in animal form) that obeys your commands.",
            "wizard"));
        list.add(s("fog_cloud","Fog Cloud",1,"Conjuration","1 action","120 ft","Up to 1 hour",true,false,"V,S",
            "20-ft radius heavily obscured sphere of fog.",
            "druid","ranger","sorcerer","wizard"));
        list.add(s("goodberry","Goodberry",1,"Transmutation","1 action","Touch","Instantaneous",false,false,"V,S,M",
            "Up to 10 berries appear; each restores 1 HP and provides sustenance for a day.",
            "druid","ranger"));
        list.add(s("grease","Grease",1,"Conjuration","1 action","60 ft","1 minute",false,false,"V,S,M",
            "10-ft square of slick grease. DEX save or fall prone. Difficult terrain.",
            "wizard"));
        list.add(s("guiding_bolt","Guiding Bolt",1,"Evocation","1 action","120 ft","1 round",false,false,"V,S",
            "Ranged spell attack; 4d6 radiant damage. Next attack against target has advantage.",
            "cleric"));
        list.add(s("healing_word","Healing Word",1,"Evocation","1 bonus action","60 ft","Instantaneous",false,false,"V",
            "Restore 1d4 + spellcasting ability modifier HP at range.",
            "bard","cleric","druid"));
        list.add(s("hellish_rebuke","Hellish Rebuke",1,"Evocation","1 reaction","60 ft","Instantaneous",false,false,"V,S",
            "Reaction when damaged; 2d10 fire damage, DEX save for half.",
            "warlock"));
        list.add(s("heroism","Heroism",1,"Enchantment","1 action","Touch","Up to 1 min",true,false,"V,S",
            "Creature is immune to frightened; gains temp HP equal to your spellcasting modifier each turn.",
            "bard","paladin"));
        list.add(s("hex","Hex",1,"Enchantment","1 bonus action","90 ft","Up to 1 hour",true,false,"V,S,M",
            "Curse a creature; deal extra 1d6 necrotic on each hit and impose disadvantage on chosen ability checks.",
            "warlock"));
        list.add(s("identify","Identify",1,"Divination","1 minute","Touch","Instantaneous",false,true,"V,S,M",
            "Determine what spells affect a creature or the properties of a magic item.",
            "bard","wizard"));
        list.add(s("ice_knife","Ice Knife",1,"Conjuration","1 action","60 ft","Instantaneous",false,false,"S,M",
            "Ranged spell attack; 1d10 piercing. Then 2d6 cold in 5-ft radius, DEX save for half.",
            "druid","sorcerer","wizard"));
        list.add(s("inflict_wounds","Inflict Wounds",1,"Necromancy","1 action","Touch","Instantaneous",false,false,"V,S",
            "Melee spell attack; 3d10 necrotic damage.",
            "cleric"));
        list.add(s("jump","Jump",1,"Transmutation","1 action","Touch","1 minute",false,false,"V,S,M",
            "Triple a creature's jump distance.",
            "druid","ranger","sorcerer","wizard"));
        list.add(s("longstrider","Longstrider",1,"Transmutation","1 action","Touch","1 hour",false,false,"V,S,M",
            "Increase a creature's speed by 10 ft.",
            "bard","druid","ranger","wizard"));
        list.add(s("mage_armor","Mage Armor",1,"Abjuration","1 action","Touch","8 hours",false,false,"V,S,M",
            "AC = 13 + DEX modifier for an unarmored willing creature.",
            "sorcerer","wizard"));
        list.add(s("magic_missile","Magic Missile",1,"Evocation","1 action","120 ft","Instantaneous",false,false,"V,S",
            "Three darts each deal 1d4+1 force damage. Always hits.",
            "sorcerer","wizard"));
        list.add(s("protection_evil_good","Protection from Evil and Good",1,"Abjuration","1 action","Touch","Up to 10 min",true,false,"V,S,M",
            "Protected creature can't be charmed/frightened/possessed by: aberrations, celestials, elementals, fey, fiends, undead.",
            "cleric","paladin","warlock","wizard"));
        list.add(s("ray_of_sickness","Ray of Sickness",1,"Necromancy","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Ranged spell attack; 2d8 poison damage. CON save or poisoned until end of your next turn.",
            "sorcerer","wizard"));
        list.add(s("sanctuary","Sanctuary",1,"Abjuration","1 bonus action","30 ft","1 minute",false,false,"V,S,M",
            "WIS save to attack protected creature. If creature attacks, spell ends.",
            "cleric"));
        list.add(s("shield","Shield",1,"Abjuration","1 reaction","Self","1 round",false,false,"V,S",
            "+5 AC until start of your next turn, including against the triggering attack.",
            "sorcerer","wizard"));
        list.add(s("shield_of_faith","Shield of Faith",1,"Abjuration","1 bonus action","60 ft","Up to 10 min",true,false,"V,S,M",
            "+2 AC bonus to a creature.",
            "cleric","paladin"));
        list.add(s("silent_image","Silent Image",1,"Illusion","1 action","60 ft","Up to 10 min",true,false,"V,S,M",
            "Create a 15-ft cube visual illusion. Investigation vs. spell save DC to disbelieve.",
            "bard","sorcerer","wizard"));
        list.add(s("sleep","Sleep",1,"Enchantment","1 action","90 ft","1 minute",false,false,"V,S,M",
            "Roll 5d8; that many HP worth of creatures fall unconscious, starting from lowest.",
            "bard","sorcerer","wizard"));
        list.add(s("speak_with_animals","Speak with Animals",1,"Divination","1 action","Self","10 minutes",false,true,"V,S",
            "Communicate with beasts for the duration.",
            "bard","druid","ranger"));
        list.add(s("tashas_laughter","Tasha's Hideous Laughter",1,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "WIS save or creature falls prone laughing, incapacitated for duration.",
            "bard","wizard"));
        list.add(s("thunderwave","Thunderwave",1,"Evocation","1 action","Self (15-ft cube)","Instantaneous",false,false,"V,S",
            "CON save or take 2d8 thunder and pushed 10 ft (half damage no push on save).",
            "bard","druid","sorcerer","wizard"));
        list.add(s("unseen_servant","Unseen Servant",1,"Conjuration","1 action","60 ft","1 hour",false,true,"V,S,M",
            "Invisible mindless force performs simple tasks within 60 ft.",
            "bard","warlock","wizard"));
        list.add(s("witch_bolt","Witch Bolt",1,"Evocation","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Ranged spell attack; 1d12 lightning. On subsequent turns, deal damage automatically as an action.",
            "sorcerer","warlock","wizard"));
        list.add(s("wrathful_smite","Wrathful Smite",1,"Evocation","1 bonus action","Self","Up to 1 min",true,false,"V",
            "Next hit deals extra 1d6 psychic; WIS save or frightened.",
            "paladin"));
        list.add(s("hunters_mark","Hunter's Mark",1,"Divination","1 bonus action","90 ft","Up to 1 hour",true,false,"V",
            "Mark a creature; deal extra 1d6 on weapon attacks against it. Bonus action to switch marks.",
            "ranger"));
        list.add(s("absorb_elements","Absorb Elements",1,"Abjuration","1 reaction","Self","1 round",false,false,"S",
            "Reaction to elemental damage; gain resistance, then add 1d6 of that type to next melee hit.",
            "druid","ranger","wizard"));

        // ══ LEVEL 2 SPELLS ═══════════════════════════════════════════════════
        list.add(s("aid","Aid",2,"Abjuration","1 action","30 ft","8 hours",false,false,"V,S,M",
            "Up to 3 creatures: max HP +5 for duration.",
            "cleric","paladin"));
        list.add(s("alter_self","Alter Self",2,"Transmutation","1 action","Self","Up to 1 hour",true,false,"V,S",
            "Change appearance, gain natural weapons (1d6 + STR), or gain a swim/climb speed.",
            "sorcerer","wizard"));
        list.add(s("arcane_lock","Arcane Lock",2,"Abjuration","1 action","Touch","Until dispelled",false,false,"V,S,M",
            "Lock a door, window, gate. DC +10 to open.",
            "wizard"));
        list.add(s("blindness_deafness","Blindness/Deafness",2,"Necromancy","1 action","30 ft","1 minute",false,false,"V",
            "CON save or blind or deafened.",
            "bard","cleric","sorcerer","wizard"));
        list.add(s("blur","Blur",2,"Illusion","1 action","Self","Up to 1 min",true,false,"V",
            "Attackers have disadvantage against you.",
            "sorcerer","wizard"));
        list.add(s("calm_emotions","Calm Emotions",2,"Enchantment","1 action","60 ft","Up to 1 min",true,false,"V,S",
            "CHA save; suppress charmed/frightened, or make creatures indifferent to attacks.",
            "bard","cleric"));
        list.add(s("crown_of_madness","Crown of Madness",2,"Enchantment","1 action","120 ft","Up to 1 min",true,false,"V,S",
            "WIS save or charmed and must attack nearest creature each turn.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("darkness","Darkness",2,"Evocation","1 action","60 ft","Up to 10 min",true,false,"V,M",
            "Magical darkness in 15-ft sphere. Blindsight can see through.",
            "sorcerer","warlock","wizard"));
        list.add(s("darkvision","Darkvision",2,"Transmutation","1 action","Touch","8 hours",false,false,"V,S,M",
            "Creature can see in darkness up to 60 ft.",
            "druid","ranger","sorcerer","wizard"));
        list.add(s("enhance_ability","Enhance Ability",2,"Transmutation","1 action","Touch","Up to 1 hour",true,false,"V,S,M",
            "Grant one of: Bear's Endurance (CON adv+2d6 THP), Bull's Strength (STR adv+carry), Cat's Grace (DEX adv, no fall dmg), Eagle's Splendor (CHA adv), Fox's Cunning (INT adv), Owl's Wisdom (WIS adv).",
            "bard","cleric","druid","sorcerer","wizard"));
        list.add(s("enlarge_reduce","Enlarge/Reduce",2,"Transmutation","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Enlarge: creature/object doubled in size, +1d4 weapon damage, ADV STR. Reduce: half size, -1d4, DIS STR.",
            "sorcerer","wizard"));
        list.add(s("flaming_sphere","Flaming Sphere",2,"Conjuration","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "5-ft sphere deals 2d6 fire to adjacent; DEX save for half. Move as bonus action.",
            "druid","wizard"));
        list.add(s("gust_of_wind","Gust of Wind",2,"Evocation","1 action","Self (60-ft line)","Up to 1 min",true,false,"V,S,M",
            "60-ft long, 10-ft wide gust; STR save or pushed 15 ft, difficult terrain.",
            "druid","sorcerer","wizard"));
        list.add(s("hold_person","Hold Person",2,"Enchantment","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "WIS save or paralyzed. Save each turn to end.",
            "bard","cleric","druid","sorcerer","warlock","wizard"));
        list.add(s("invisibility","Invisibility",2,"Illusion","1 action","Touch","Up to 1 hour",true,false,"V,S,M",
            "Target invisible until it attacks or casts.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("knock","Knock",2,"Transmutation","1 action","60 ft","Instantaneous",false,false,"V",
            "Open a lock, stuck door, or chest. Loud knock audible 300 ft.",
            "wizard"));
        list.add(s("levitate","Levitate",2,"Transmutation","1 action","60 ft","Up to 10 min",true,false,"V,S,M",
            "Target rises up to 20 ft. CON save to resist if unwilling.",
            "sorcerer","wizard"));
        list.add(s("magic_weapon","Magic Weapon",2,"Transmutation","1 bonus action","Touch","Up to 1 hour",true,false,"V,S",
            "Nonmagical weapon becomes +1 magic weapon.",
            "paladin","wizard"));
        list.add(s("misty_step","Misty Step",2,"Conjuration","1 bonus action","Self","Instantaneous",false,false,"V",
            "Teleport up to 30 ft to an unoccupied space you can see.",
            "sorcerer","warlock","wizard"));
        list.add(s("moonbeam","Moonbeam",2,"Evocation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "5-ft cylinder of moonlight; CON save or 2d10 radiant. Shapechangers have DIS on save.",
            "druid"));
        list.add(s("pass_without_trace","Pass Without Trace",2,"Abjuration","1 action","Self","Up to 1 hour",true,false,"V,S,M",
            "+10 bonus to Stealth for self and companions; can't be tracked by non-magical means.",
            "druid","ranger"));
        list.add(s("prayer_of_healing","Prayer of Healing",2,"Evocation","10 minutes","30 ft","Instantaneous",false,false,"V",
            "Up to 6 creatures regain 2d8 + spellcasting modifier HP.",
            "cleric"));
        list.add(s("scorching_ray","Scorching Ray",2,"Evocation","1 action","120 ft","Instantaneous",false,false,"V,S",
            "Three rays; each ranged spell attack for 2d6 fire.",
            "sorcerer","wizard"));
        list.add(s("shatter","Shatter",2,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S,M",
            "10-ft radius burst; CON save or 3d8 thunder damage. DIS on saves for inorganic material.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("silence","Silence",2,"Illusion","1 action","120 ft","Up to 10 min",true,true,"V,S",
            "20-ft radius sphere; no sound in/out. Spells with verbal components can't be cast inside.",
            "bard","cleric","ranger"));
        list.add(s("spike_growth","Spike Growth",2,"Transmutation","1 action","150 ft","Up to 10 min",true,false,"V,S,M",
            "20-ft radius becomes difficult terrain; moving through deals 2d4 per 5 ft.",
            "druid","ranger"));
        list.add(s("spiritual_weapon","Spiritual Weapon",2,"Evocation","1 bonus action","60 ft","1 minute",false,false,"V,S",
            "Spectral weapon attacks; bonus action for +spell attack, 1d8 + WIS force.",
            "cleric"));
        list.add(s("suggestion","Suggestion",2,"Enchantment","1 action","30 ft","Up to 8 hours",true,false,"V,M",
            "WIS save or creature follows a reasonable suggestion.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("web","Web",2,"Conjuration","1 action","60 ft","Up to 1 hour",true,false,"V,S,M",
            "20-ft cube of sticky webs; DEX save or restrained. Flammable.",
            "sorcerer","wizard"));

        return list;
    }
}
