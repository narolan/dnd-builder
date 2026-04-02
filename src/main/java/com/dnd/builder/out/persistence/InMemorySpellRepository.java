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

        // ── XGtE Cantrips ────────────────────────────────────────────────────
        list.add(s("create_bonfire","Create Bonfire",0,"Conjuration","1 action","60 ft","Up to 1 min",true,false,"V,S",
            "Create a 5-ft cube of fire. DEX save or 1d8 fire damage when entering or starting turn there.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("control_flames","Control Flames",0,"Transmutation","1 action","60 ft","Instantaneous or 1 hour",false,false,"S",
            "Control nonmagical fire: expand, extinguish, double/halve light, or create simple shapes.",
            "druid","sorcerer","wizard"));
        list.add(s("frostbite","Frostbite",0,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "CON save or 1d6 cold damage and disadvantage on next weapon attack roll.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("gust","Gust",0,"Transmutation","1 action","30 ft","Instantaneous",false,false,"V,S",
            "Push creature 5 ft (STR save), push object 10 ft, or create harmless sensory effect.",
            "druid","sorcerer","wizard"));
        list.add(s("infestation","Infestation",0,"Conjuration","1 action","30 ft","Instantaneous",false,false,"V,S,M",
            "CON save or 1d6 poison damage and move 5 ft in random direction.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("magic_stone","Magic Stone",0,"Transmutation","1 bonus action","Touch","1 minute",false,false,"V,S",
            "Imbue up to 3 pebbles; ranged spell attack 1d6 + spellcasting mod bludgeoning.",
            "druid","warlock"));
        list.add(s("mold_earth","Mold Earth",0,"Transmutation","1 action","30 ft","Instantaneous or 1 hour",false,false,"S",
            "Excavate 5-ft cube of loose earth, create shapes, or change terrain difficulty.",
            "druid","sorcerer","wizard"));
        list.add(s("shape_water","Shape Water",0,"Transmutation","1 action","30 ft","Instantaneous or 1 hour",false,false,"S",
            "Move/animate water, change color/opacity, or freeze for 1 hour.",
            "druid","sorcerer","wizard"));
        list.add(s("thunderclap","Thunderclap",0,"Evocation","1 action","5 ft","Instantaneous",false,false,"S",
            "All creatures within 5 ft: CON save or 1d6 thunder damage. Audible 100 ft.",
            "bard","druid","sorcerer","warlock","wizard"));
        list.add(s("toll_the_dead","Toll the Dead",0,"Necromancy","1 action","60 ft","Instantaneous",false,false,"V,S",
            "WIS save or 1d8 necrotic (1d12 if missing HP). Scales with level.",
            "cleric","warlock","wizard"));
        list.add(s("word_of_radiance","Word of Radiance",0,"Evocation","1 action","5 ft","Instantaneous",false,false,"V,M",
            "Each creature of choice within 5 ft: CON save or 1d6 radiant damage.",
            "cleric"));
        list.add(s("primal_savagery","Primal Savagery",0,"Transmutation","1 action","Self","Instantaneous",false,false,"S",
            "Teeth/nails sharpen; melee spell attack for 1d10 acid damage.",
            "druid"));

        // ── SCAG/TCoE Cantrips ───────────────────────────────────────────────
        list.add(s("booming_blade","Booming Blade",0,"Evocation","1 action","Self (5-ft radius)","1 round",false,false,"S,M",
            "Melee attack with weapon; if target moves before your next turn, 1d8 thunder damage.",
            "sorcerer","warlock","wizard"));
        list.add(s("green_flame_blade","Green-Flame Blade",0,"Evocation","1 action","Self (5-ft radius)","Instantaneous",false,false,"S,M",
            "Melee attack with weapon; fire leaps to adjacent creature for spellcasting mod fire damage.",
            "sorcerer","warlock","wizard"));
        list.add(s("lightning_lure","Lightning Lure",0,"Evocation","1 action","Self (15-ft radius)","Instantaneous",false,false,"V",
            "STR save or pulled 10 ft toward you; if within 5 ft, 1d8 lightning damage.",
            "sorcerer","warlock","wizard"));
        list.add(s("mind_sliver","Mind Sliver",0,"Enchantment","1 action","60 ft","1 round",false,false,"V",
            "INT save or 1d6 psychic and subtract 1d4 from next saving throw before end of your next turn.",
            "sorcerer","warlock","wizard"));
        list.add(s("sword_burst","Sword Burst",0,"Conjuration","1 action","Self (5-ft radius)","Instantaneous",false,false,"V",
            "All creatures within 5 ft: DEX save or 1d6 force damage.",
            "sorcerer","warlock","wizard"));

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
            "druid","ranger","sorcerer","wizard"));

        // ── XGtE Level 1 ─────────────────────────────────────────────────────
        list.add(s("catapult","Catapult",1,"Transmutation","1 action","60 ft","Instantaneous",false,false,"S",
            "Hurl object 1-5 lbs up to 90 ft. DEX save or 3d8 bludgeoning damage.",
            "sorcerer","wizard"));
        list.add(s("cause_fear","Cause Fear",1,"Necromancy","1 action","60 ft","Up to 1 min",true,false,"V",
            "WIS save or frightened. Can't move toward you. Save each turn to end.",
            "warlock","wizard"));
        list.add(s("chaos_bolt","Chaos Bolt",1,"Evocation","1 action","120 ft","Instantaneous",false,false,"V,S",
            "Ranged spell attack; 2d8+1d6 damage of random type. Doubles on d8s mean it bounces.",
            "sorcerer"));
        list.add(s("ceremony","Ceremony",1,"Abjuration","1 hour","Touch","Instantaneous",false,true,"V,S,M",
            "Perform religious ceremony: Atonement, Bless Water, Coming of Age, Dedication, Funeral Rite, Wedding.",
            "cleric","paladin"));
        list.add(s("earth_tremor","Earth Tremor",1,"Evocation","1 action","10 ft","Instantaneous",false,false,"V,S",
            "All creatures in 10 ft: DEX save or 1d6 bludgeoning and prone. Difficult terrain.",
            "bard","druid","sorcerer","wizard"));
        list.add(s("snare","Snare",1,"Abjuration","1 minute","Touch","8 hours",false,false,"S,M",
            "Create 5-ft radius magical trap. DEX save or hoisted into the air and restrained.",
            "druid","ranger","wizard"));
        list.add(s("zephyr_strike","Zephyr Strike",1,"Transmutation","1 bonus action","Self","Up to 1 min",true,false,"V",
            "Movement doesn't provoke OA. Once: advantage on attack, +1d8 force, +30 ft speed.",
            "ranger"));

        // ── TCoE Level 1 ─────────────────────────────────────────────────────
        list.add(s("tashas_caustic_brew","Tasha's Caustic Brew",1,"Evocation","1 action","Self (30-ft line)","Up to 1 min",true,false,"V,S,M",
            "30-ft line; DEX save or 2d4 acid at start of each turn until action used to remove.",
            "sorcerer","wizard"));

        // ── Strixhaven Level 1 ───────────────────────────────────────────────
        list.add(s("silvery_barbs","Silvery Barbs",1,"Enchantment","1 reaction","60 ft","Instantaneous",false,false,"V",
            "When creature succeeds on attack/check/save, force reroll and take lower. Grant advantage to another.",
            "bard","sorcerer","wizard"));

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

        // ── XGtE Level 2 ─────────────────────────────────────────────────────
        list.add(s("aganazzars_scorcher","Aganazzar's Scorcher",2,"Evocation","1 action","30 ft","Instantaneous",false,false,"V,S,M",
            "30-ft line of fire; DEX save or 3d8 fire damage (half on save).",
            "sorcerer","wizard"));
        list.add(s("dragons_breath","Dragon's Breath",2,"Transmutation","1 bonus action","Touch","Up to 1 min",true,false,"V,S,M",
            "Willing creature can use action to exhale 15-ft cone; 3d6 damage (acid/cold/fire/lightning/poison).",
            "sorcerer","wizard"));
        list.add(s("dust_devil","Dust Devil",2,"Conjuration","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "5-ft cube dust devil; creatures entering: STR save or 1d8 bludgeoning and pushed 10 ft.",
            "druid","sorcerer","wizard"));
        list.add(s("earthbind","Earthbind",2,"Transmutation","1 action","300 ft","Up to 1 min",true,false,"V",
            "STR save or flying creature's speed becomes 0 and descends 60 ft/round.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("healing_spirit","Healing Spirit",2,"Conjuration","1 bonus action","60 ft","Up to 1 min",true,false,"V,S",
            "Intangible spirit heals 1d6 when creature moves through its space (1+mod times max).",
            "druid","ranger"));
        list.add(s("mind_spike","Mind Spike",2,"Divination","1 action","60 ft","Up to 1 hour",true,false,"S",
            "3d8 psychic; WIS save for half. You know target's location while concentrating.",
            "sorcerer","warlock","wizard"));
        list.add(s("shadow_blade","Shadow Blade",2,"Illusion","1 bonus action","Self","Up to 1 min",true,false,"V,S",
            "Create sword of shadow; 2d8 psychic, finesse, light, thrown. Advantage in dim light/darkness.",
            "sorcerer","warlock","wizard"));
        list.add(s("warding_wind","Warding Wind",2,"Evocation","1 action","Self","Up to 10 min",true,false,"V",
            "Strong wind in 10-ft radius; difficult terrain, deafens, extinguishes flames, disperses gas.",
            "bard","druid","sorcerer","wizard"));

        // ── TCoE Level 2 ─────────────────────────────────────────────────────
        list.add(s("tashas_mind_whip","Tasha's Mind Whip",2,"Enchantment","1 action","90 ft","1 round",false,false,"V",
            "INT save or 3d6 psychic and can only move OR action (not both) on next turn.",
            "sorcerer","wizard"));
        list.add(s("summon_beast","Summon Beast",2,"Conjuration","1 action","90 ft","Up to 1 hour",true,false,"V,S,M",
            "Summon bestial spirit (air, land, or water). HP and attacks scale with slot level.",
            "druid","ranger"));

        // ══ LEVEL 3 SPELLS ═══════════════════════════════════════════════════
        list.add(s("animate_dead","Animate Dead",3,"Necromancy","1 minute","10 ft","Instantaneous",false,false,"V,S,M",
            "Create undead servant from bones or corpse. Obeys your commands for 24 hours.",
            "cleric","wizard"));
        list.add(s("aura_of_vitality","Aura of Vitality",3,"Evocation","1 action","Self (30-ft)","Up to 1 min",true,false,"V",
            "Healing aura; use bonus action to restore 2d6 HP to one creature within 30 ft.",
            "paladin"));
        list.add(s("beacon_of_hope","Beacon of Hope",3,"Abjuration","1 action","30 ft","Up to 1 min",true,false,"V,S",
            "Targets have advantage on WIS saves and death saves; regain maximum HP from healing.",
            "cleric"));
        list.add(s("bestow_curse","Bestow Curse",3,"Necromancy","1 action","Touch","Up to 1 min",true,false,"V,S",
            "WIS save or cursed with disadvantage on ability checks/saves, attacks, or other effect.",
            "bard","cleric","wizard"));
        list.add(s("blinding_smite","Blinding Smite",3,"Evocation","1 bonus action","Self","Up to 1 min",true,false,"V",
            "Next weapon hit deals extra 3d8 radiant; CON save or blinded.",
            "paladin"));
        list.add(s("blink","Blink",3,"Transmutation","1 action","Self","1 minute",false,false,"V,S",
            "Each turn, 50% chance to vanish to Ethereal Plane until start of next turn.",
            "sorcerer","wizard"));
        list.add(s("call_lightning","Call Lightning",3,"Conjuration","1 action","120 ft","Up to 10 min",true,false,"V,S",
            "Storm cloud deals 3d10 lightning in 5-ft radius; DEX save for half. Repeat each turn.",
            "druid"));
        list.add(s("clairvoyance","Clairvoyance",3,"Divination","10 minutes","1 mile","Up to 10 min",true,false,"V,S,M",
            "Create invisible sensor at a known location; see or hear through it.",
            "bard","cleric","sorcerer","wizard"));
        list.add(s("conjure_animals","Conjure Animals",3,"Conjuration","1 action","60 ft","Up to 1 hour",true,false,"V,S",
            "Summon fey spirits as beasts (up to 8 CR 1/4, 4 CR 1/2, 2 CR 1, or 1 CR 2).",
            "druid","ranger"));
        list.add(s("counterspell","Counterspell",3,"Abjuration","1 reaction","60 ft","Instantaneous",false,false,"S",
            "Interrupt a spell being cast. Auto-success if same level or lower; otherwise ability check.",
            "sorcerer","warlock","wizard"));
        list.add(s("create_food_water","Create Food and Water",3,"Conjuration","1 action","30 ft","Instantaneous",false,false,"V,S",
            "Create 45 pounds of food and 30 gallons of water.",
            "cleric","paladin"));
        list.add(s("crusaders_mantle","Crusader's Mantle",3,"Evocation","1 action","Self (30-ft)","Up to 1 min",true,false,"V",
            "Allies within 30 ft deal extra 1d4 radiant on weapon hits.",
            "paladin"));
        list.add(s("daylight","Daylight",3,"Evocation","1 action","60 ft","1 hour",false,false,"V,S",
            "60-ft radius of bright light; dispels darkness of 3rd level or lower.",
            "cleric","druid","paladin","ranger","sorcerer"));
        list.add(s("dispel_magic","Dispel Magic",3,"Abjuration","1 action","120 ft","Instantaneous",false,false,"V,S",
            "End spells on target. Auto-success for 3rd level or lower; otherwise ability check.",
            "bard","cleric","druid","paladin","sorcerer","warlock","wizard"));
        list.add(s("elemental_weapon","Elemental Weapon",3,"Transmutation","1 action","Touch","Up to 1 hour",true,false,"V,S",
            "Weapon becomes +1 magic and deals extra 1d4 acid/cold/fire/lightning/thunder.",
            "paladin"));
        list.add(s("fear","Fear",3,"Illusion","1 action","Self (30-ft cone)","Up to 1 min",true,false,"V,S,M",
            "WIS save or frightened and must Dash away. Save each turn while line of sight.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("feign_death","Feign Death",3,"Necromancy","1 action","Touch","1 hour",false,true,"V,S,M",
            "Willing creature appears dead; blinded, incapacitated, resistant to all but psychic.",
            "bard","cleric","druid","wizard"));
        list.add(s("fireball","Fireball",3,"Evocation","1 action","150 ft","Instantaneous",false,false,"V,S,M",
            "20-ft radius explosion; DEX save or 8d6 fire damage (half on save).",
            "sorcerer","wizard"));
        list.add(s("fly","Fly",3,"Transmutation","1 action","Touch","Up to 10 min",true,false,"V,S,M",
            "Target gains 60-ft flying speed.",
            "sorcerer","warlock","wizard"));
        list.add(s("gaseous_form","Gaseous Form",3,"Transmutation","1 action","Touch","Up to 1 hour",true,false,"V,S,M",
            "Target becomes misty cloud; fly 10 ft, resistant to nonmagical damage, can pass through cracks.",
            "sorcerer","warlock","wizard"));
        list.add(s("glyph_of_warding","Glyph of Warding",3,"Abjuration","1 hour","Touch","Until dispelled",false,false,"V,S,M",
            "Inscribe a glyph that triggers a spell or explosion when conditions are met.",
            "bard","cleric","wizard"));
        list.add(s("haste","Haste",3,"Transmutation","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Double speed, +2 AC, advantage on DEX saves, extra action (Attack/Dash/Disengage/Hide/Use).",
            "sorcerer","wizard"));
        list.add(s("hunger_of_hadar","Hunger of Hadar",3,"Conjuration","1 action","150 ft","Up to 1 min",true,false,"V,S,M",
            "20-ft sphere of darkness; difficult terrain, 2d6 cold at start, 2d6 acid at end of turn.",
            "warlock"));
        list.add(s("hypnotic_pattern","Hypnotic Pattern",3,"Illusion","1 action","120 ft","Up to 1 min",true,false,"S,M",
            "30-ft cube; WIS save or charmed, incapacitated, speed 0. Ends if damaged or shaken awake.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("leomund_tiny_hut","Leomund's Tiny Hut",3,"Evocation","1 minute","Self (10-ft)","8 hours",false,true,"V,S,M",
            "Immobile dome protects up to 9 creatures; blocks spells and elements.",
            "bard","wizard"));
        list.add(s("lightning_bolt","Lightning Bolt",3,"Evocation","1 action","Self (100-ft line)","Instantaneous",false,false,"V,S,M",
            "100-ft line; DEX save or 8d6 lightning damage (half on save).",
            "sorcerer","wizard"));
        list.add(s("magic_circle","Magic Circle",3,"Abjuration","1 minute","10 ft","1 hour",false,false,"V,S,M",
            "10-ft cylinder protects against chosen creature type; can't enter, target, or charm creatures inside.",
            "cleric","paladin","warlock","wizard"));
        list.add(s("major_image","Major Image",3,"Illusion","1 action","120 ft","Up to 10 min",true,false,"V,S,M",
            "Create illusion up to 20-ft cube with sound, smell, and temperature.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("mass_healing_word","Mass Healing Word",3,"Evocation","1 bonus action","60 ft","Instantaneous",false,false,"V",
            "Up to 6 creatures regain 1d4 + spellcasting modifier HP.",
            "cleric"));
        list.add(s("meld_into_stone","Meld into Stone",3,"Transmutation","1 action","Touch","8 hours",false,true,"V,S",
            "Step into stone surface large enough to contain you; expelled if stone is damaged.",
            "cleric","druid"));
        list.add(s("nondetection","Nondetection",3,"Abjuration","1 action","Touch","8 hours",false,false,"V,S,M",
            "Target can't be targeted by divination magic or perceived by magical scrying.",
            "bard","ranger","wizard"));
        list.add(s("phantom_steed","Phantom Steed",3,"Illusion","1 minute","30 ft","1 hour",false,true,"V,S",
            "Create quasi-real horse with 100-ft speed; fades over 1 minute if attacked.",
            "wizard"));
        list.add(s("plant_growth","Plant Growth",3,"Transmutation","1 action or 8 hours","150 ft","Instantaneous",false,false,"V,S",
            "Action: 100-ft radius becomes overgrown (4 ft movement per 1 ft). 8 hours: enriches land.",
            "bard","druid","ranger"));
        list.add(s("protection_from_energy","Protection from Energy",3,"Abjuration","1 action","Touch","Up to 1 hour",true,false,"V,S",
            "Target has resistance to one damage type: acid, cold, fire, lightning, or thunder.",
            "cleric","druid","ranger","sorcerer","wizard"));
        list.add(s("remove_curse","Remove Curse",3,"Abjuration","1 action","Touch","Instantaneous",false,false,"V,S",
            "End all curses affecting the target. Doesn't remove cursed item, but allows unequipping.",
            "cleric","paladin","warlock","wizard"));
        list.add(s("revivify","Revivify",3,"Necromancy","1 action","Touch","Instantaneous",false,false,"V,S,M",
            "Return creature dead less than 1 minute to life with 1 HP. Can't restore missing parts.",
            "cleric","paladin"));
        list.add(s("sending","Sending",3,"Evocation","1 action","Unlimited","1 round",false,false,"V,S,M",
            "Send 25-word message to known creature on same plane; they can reply with 25 words.",
            "bard","cleric","wizard"));
        list.add(s("sleet_storm","Sleet Storm",3,"Conjuration","1 action","150 ft","Up to 1 min",true,false,"V,S,M",
            "40-ft cylinder; heavily obscured, difficult terrain, DEX save or prone when entering.",
            "druid","sorcerer","wizard"));
        list.add(s("slow","Slow",3,"Transmutation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "Up to 6 creatures; WIS save or -2 AC/DEX saves, can't use reactions, one attack only.",
            "sorcerer","wizard"));
        list.add(s("speak_with_dead","Speak with Dead",3,"Necromancy","1 action","10 ft","10 minutes",false,false,"V,S,M",
            "Corpse answers up to 5 questions. Knows only what it knew in life; can't lie.",
            "bard","cleric"));
        list.add(s("speak_with_plants","Speak with Plants",3,"Transmutation","1 action","Self (30-ft)","10 minutes",false,false,"V,S",
            "Plants can communicate; difficult terrain becomes passable, plants can perform tasks.",
            "bard","druid","ranger"));
        list.add(s("spirit_guardians","Spirit Guardians",3,"Conjuration","1 action","Self (15-ft)","Up to 10 min",true,false,"V,S,M",
            "Spirits surround you; enemies in area take 3d8 radiant/necrotic, WIS save for half.",
            "cleric"));
        list.add(s("stinking_cloud","Stinking Cloud",3,"Conjuration","1 action","90 ft","Up to 1 min",true,false,"V,S,M",
            "20-ft sphere; heavily obscured, CON save or lose action retching.",
            "bard","sorcerer","wizard"));
        list.add(s("tongues","Tongues",3,"Divination","1 action","Touch","1 hour",false,false,"V,M",
            "Target understands any spoken language and can be understood by any creature.",
            "bard","cleric","sorcerer","warlock","wizard"));
        list.add(s("vampiric_touch","Vampiric Touch",3,"Necromancy","1 action","Self","Up to 1 min",true,false,"V,S",
            "Melee spell attack; 3d6 necrotic, regain HP equal to half damage dealt.",
            "warlock","wizard"));
        list.add(s("water_breathing","Water Breathing",3,"Transmutation","1 action","30 ft","24 hours",false,true,"V,S,M",
            "Up to 10 creatures can breathe underwater.",
            "druid","ranger","sorcerer","wizard"));
        list.add(s("water_walk","Water Walk",3,"Transmutation","1 action","30 ft","1 hour",false,true,"V,S,M",
            "Up to 10 creatures can walk on liquid surfaces.",
            "cleric","druid","ranger","sorcerer"));
        list.add(s("wind_wall","Wind Wall",3,"Evocation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "50-ft long, 15-ft high wall of wind; 3d8 bludgeoning, blocks arrows/gas/flying creatures.",
            "druid","ranger"));

        // ══ LEVEL 4 SPELLS ═══════════════════════════════════════════════════
        list.add(s("arcane_eye","Arcane Eye",4,"Divination","1 action","30 ft","Up to 1 hour",true,false,"V,S,M",
            "Create invisible, magical eye you can see through; 30-ft movement, 30-ft darkvision.",
            "wizard"));
        list.add(s("aura_of_life","Aura of Life",4,"Abjuration","1 action","Self (30-ft)","Up to 10 min",true,false,"V",
            "Nonhostile creatures in aura have resistance to necrotic; regain 1 HP if at 0.",
            "paladin"));
        list.add(s("aura_of_purity","Aura of Purity",4,"Abjuration","1 action","Self (30-ft)","Up to 10 min",true,false,"V",
            "Nonhostile creatures immune to disease, resistant to poison, advantage on saves vs conditions.",
            "paladin"));
        list.add(s("banishment","Banishment",4,"Abjuration","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "CHA save or banished to another plane. If native to current plane, returns when spell ends.",
            "cleric","paladin","sorcerer","warlock","wizard"));
        list.add(s("blight","Blight",4,"Necromancy","1 action","30 ft","Instantaneous",false,false,"V,S",
            "8d8 necrotic damage, CON save for half. Plants have disadvantage and take max damage.",
            "druid","sorcerer","warlock","wizard"));
        list.add(s("compulsion","Compulsion",4,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V,S",
            "WIS save or must move in direction you choose. Repeat save each turn.",
            "bard"));
        list.add(s("confusion","Confusion",4,"Enchantment","1 action","90 ft","Up to 1 min",true,false,"V,S,M",
            "10-ft sphere; WIS save or roll d10 each turn to determine random action.",
            "bard","druid","sorcerer","wizard"));
        list.add(s("conjure_minor_elementals","Conjure Minor Elementals",4,"Conjuration","1 minute","90 ft","Up to 1 hour",true,false,"V,S",
            "Summon elementals (8 CR 1/4, 4 CR 1/2, 2 CR 1, or 1 CR 2).",
            "druid","wizard"));
        list.add(s("conjure_woodland_beings","Conjure Woodland Beings",4,"Conjuration","1 action","60 ft","Up to 1 hour",true,false,"V,S,M",
            "Summon fey creatures (8 CR 1/4, 4 CR 1/2, 2 CR 1, or 1 CR 2).",
            "druid","ranger"));
        list.add(s("control_water","Control Water",4,"Transmutation","1 action","300 ft","Up to 10 min",true,false,"V,S,M",
            "Manipulate water: flood, part, redirect, or create whirlpool in 100-ft cube.",
            "cleric","druid","wizard"));
        list.add(s("death_ward","Death Ward",4,"Abjuration","1 action","Touch","8 hours",false,false,"V,S",
            "First time target would drop to 0 HP, drop to 1 HP instead.",
            "cleric","paladin"));
        list.add(s("dimension_door","Dimension Door",4,"Conjuration","1 action","500 ft","Instantaneous",false,false,"V",
            "Teleport self and one willing creature to a spot within range you can visualize.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("dominate_beast","Dominate Beast",4,"Enchantment","1 action","60 ft","Up to 1 min",true,false,"V,S",
            "WIS save or beast is charmed and obeys your telepathic commands.",
            "druid","sorcerer"));
        list.add(s("evards_black_tentacles","Evard's Black Tentacles",4,"Conjuration","1 action","90 ft","Up to 1 min",true,false,"V,S,M",
            "20-ft square; DEX save or restrained and 3d6 bludgeoning damage each turn.",
            "wizard"));
        list.add(s("fabricate","Fabricate",4,"Transmutation","10 minutes","120 ft","Instantaneous",false,false,"V,S",
            "Convert raw materials into finished products. Requires proficiency for complex items.",
            "wizard"));
        list.add(s("fire_shield","Fire Shield",4,"Evocation","1 action","Self","10 minutes",false,false,"V,S,M",
            "Resistance to cold or fire; creatures hitting you with melee take 2d8 of the other type.",
            "wizard"));
        list.add(s("freedom_of_movement","Freedom of Movement",4,"Abjuration","1 action","Touch","1 hour",false,false,"V,S,M",
            "Target's movement unaffected by difficult terrain, spells, or restraints.",
            "bard","cleric","druid","ranger"));
        list.add(s("giant_insect","Giant Insect",4,"Transmutation","1 action","30 ft","Up to 10 min",true,false,"V,S",
            "Transform up to 10 centipedes, 3 spiders, 5 wasps, or 1 scorpion into giant versions.",
            "druid"));
        list.add(s("greater_invisibility","Greater Invisibility",4,"Illusion","1 action","Touch","Up to 1 min",true,false,"V,S",
            "Target becomes invisible. Doesn't end when they attack or cast spells.",
            "bard","sorcerer","wizard"));
        list.add(s("guardian_of_faith","Guardian of Faith",4,"Conjuration","1 action","30 ft","8 hours",false,false,"V",
            "Spectral guardian; creatures within 10 ft take 20 radiant, DEX save for half. 60 HP total.",
            "cleric"));
        list.add(s("hallucinatory_terrain","Hallucinatory Terrain",4,"Illusion","10 minutes","300 ft","24 hours",false,false,"V,S,M",
            "150-ft cube terrain appears different. Investigation check vs. spell save DC.",
            "bard","druid","warlock","wizard"));
        list.add(s("ice_storm","Ice Storm",4,"Evocation","1 action","300 ft","Instantaneous",false,false,"V,S,M",
            "20-ft radius; 2d8 bludgeoning + 4d6 cold, DEX save for half. Difficult terrain for 1 round.",
            "druid","sorcerer","wizard"));
        list.add(s("locate_creature","Locate Creature",4,"Divination","1 action","Self","Up to 1 hour",true,false,"V,S,M",
            "Sense direction to a specific creature within 1000 ft. Blocked by running water.",
            "bard","cleric","druid","paladin","ranger","wizard"));
        list.add(s("mordenkainens_faithful_hound","Mordenkainen's Faithful Hound",4,"Conjuration","1 action","30 ft","8 hours",false,false,"V,S,M",
            "Phantom watchdog guards area; barks at hostiles, bites for 4d8 piercing.",
            "wizard"));
        list.add(s("mordenkainens_private_sanctum","Mordenkainen's Private Sanctum",4,"Abjuration","10 minutes","120 ft","24 hours",false,false,"V,S,M",
            "Area up to 100-ft cube is secure from scrying, teleportation, and planar travel.",
            "wizard"));
        list.add(s("otilukes_resilient_sphere","Otiluke's Resilient Sphere",4,"Evocation","1 action","30 ft","Up to 1 min",true,false,"V,S,M",
            "Enclose Large or smaller creature in shimmering sphere; immune to damage, can't leave.",
            "wizard"));
        list.add(s("phantasmal_killer","Phantasmal Killer",4,"Illusion","1 action","120 ft","Up to 1 min",true,false,"V,S",
            "WIS save or frightened by nightmare; 4d10 psychic at end of each turn, WIS save to end.",
            "wizard"));
        list.add(s("polymorph","Polymorph",4,"Transmutation","1 action","60 ft","Up to 1 hour",true,false,"V,S,M",
            "Transform creature into beast of same CR or lower. Reverts when HP drops to 0.",
            "bard","druid","sorcerer","wizard"));
        list.add(s("staggering_smite","Staggering Smite",4,"Evocation","1 bonus action","Self","Up to 1 min",true,false,"V",
            "Next weapon hit deals extra 4d6 psychic; WIS save or disadvantage on attacks and saves.",
            "paladin"));
        list.add(s("stoneskin","Stoneskin",4,"Abjuration","1 action","Touch","Up to 1 hour",true,false,"V,S,M",
            "Target has resistance to nonmagical bludgeoning, piercing, and slashing.",
            "druid","ranger","sorcerer","wizard"));
        list.add(s("stone_shape","Stone Shape",4,"Transmutation","1 action","Touch","Instantaneous",false,false,"V,S,M",
            "Reshape a Medium or smaller stone object or a 5-ft cube of stone.",
            "cleric","druid","wizard"));
        list.add(s("wall_of_fire","Wall of Fire",4,"Evocation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "60-ft wall or 20-ft ring; 5d8 fire to one side, 5d8 when entering or starting turn in wall.",
            "druid","sorcerer","wizard"));

        // ══ LEVEL 5 SPELLS ═══════════════════════════════════════════════════
        list.add(s("animate_objects","Animate Objects",5,"Transmutation","1 action","120 ft","Up to 1 min",true,false,"V,S",
            "Animate up to 10 nonmagical objects; they obey your commands and attack.",
            "bard","sorcerer","wizard"));
        list.add(s("antilife_shell","Antilife Shell",5,"Abjuration","1 action","Self (10-ft)","Up to 1 hour",true,false,"V,S",
            "Barrier prevents living creatures from passing through or reaching you.",
            "druid"));
        list.add(s("awaken","Awaken",5,"Transmutation","8 hours","Touch","Instantaneous",false,false,"V,S,M",
            "Grant beast or plant INT 10 and ability to speak; it becomes friendly to you.",
            "bard","druid"));
        list.add(s("banishing_smite","Banishing Smite",5,"Abjuration","1 bonus action","Self","Up to 1 min",true,false,"V",
            "Next weapon hit deals extra 5d10 force; if target drops below 50 HP, banished.",
            "paladin"));
        list.add(s("bigbys_hand","Bigby's Hand",5,"Evocation","1 action","120 ft","Up to 1 min",true,false,"V,S,M",
            "Create Large spectral hand; punch for 4d8, grapple, push, or block.",
            "wizard"));
        list.add(s("circle_of_power","Circle of Power",5,"Abjuration","1 action","Self (30-ft)","Up to 10 min",true,false,"V",
            "Friendly creatures in aura have advantage on saves vs magic; save for half becomes save for none.",
            "paladin"));
        list.add(s("cloudkill","Cloudkill",5,"Conjuration","1 action","120 ft","Up to 10 min",true,false,"V,S",
            "20-ft sphere of poisonous fog; 5d8 poison, CON save for half. Moves away 10 ft each turn.",
            "sorcerer","wizard"));
        list.add(s("commune","Commune",5,"Divination","1 minute","Self","1 minute",false,true,"V,S,M",
            "Contact your deity for up to 3 yes-or-no questions.",
            "cleric"));
        list.add(s("commune_with_nature","Commune with Nature",5,"Divination","1 minute","Self","Instantaneous",false,true,"V,S",
            "Learn about terrain, bodies of water, and creatures within 3 miles outdoors (300 ft underground).",
            "druid","ranger"));
        list.add(s("cone_of_cold","Cone of Cold",5,"Evocation","1 action","Self (60-ft cone)","Instantaneous",false,false,"V,S,M",
            "60-ft cone; 8d8 cold damage, CON save for half. Kills creatures become frozen statues.",
            "sorcerer","wizard"));
        list.add(s("conjure_elemental","Conjure Elemental",5,"Conjuration","1 minute","90 ft","Up to 1 hour",true,false,"V,S,M",
            "Summon CR 5 or lower elemental. Requires element source. Goes hostile if concentration breaks.",
            "druid","wizard"));
        list.add(s("contact_other_plane","Contact Other Plane",5,"Divination","1 minute","Self","1 minute",false,true,"V",
            "Contact extraplanar entity for 5 questions. DC 15 INT save or take 6d6 psychic and go insane.",
            "warlock","wizard"));
        list.add(s("contagion","Contagion",5,"Necromancy","1 action","Touch","7 days",false,false,"V,S",
            "Melee spell attack; target diseased. 3 failed CON saves makes disease permanent.",
            "cleric","druid"));
        list.add(s("creation","Creation",5,"Illusion","1 minute","30 ft","Special",false,false,"V,S,M",
            "Create nonliving object from shadow. Duration depends on material (1 day stone, 1 hour iron).",
            "sorcerer","wizard"));
        list.add(s("destructive_wave","Destructive Wave",5,"Evocation","1 action","Self (30-ft)","Instantaneous",false,false,"V",
            "30-ft radius; 5d6 thunder + 5d6 radiant/necrotic, CON save for half, knocked prone.",
            "paladin"));
        list.add(s("dispel_evil_good","Dispel Evil and Good",5,"Abjuration","1 action","Self","Up to 1 min",true,false,"V,S,M",
            "+2 AC vs aberrations/celestials/elementals/fey/fiends/undead. Can dismiss or break enchantment.",
            "cleric","paladin"));
        list.add(s("dominate_person","Dominate Person",5,"Enchantment","1 action","60 ft","Up to 1 min",true,false,"V,S",
            "WIS save or humanoid is charmed and obeys telepathic commands. Save with advantage if in combat.",
            "bard","sorcerer","wizard"));
        list.add(s("dream","Dream",5,"Illusion","1 minute","Special","8 hours",false,false,"V,S,M",
            "Enter target's dreams. Can communicate or send nightmares (WIS save or 3d6 psychic, no rest benefit).",
            "bard","warlock","wizard"));
        list.add(s("flame_strike","Flame Strike",5,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S,M",
            "10-ft cylinder; 4d6 fire + 4d6 radiant, DEX save for half.",
            "cleric"));
        list.add(s("geas","Geas",5,"Enchantment","1 minute","60 ft","30 days",false,false,"V",
            "WIS save or must follow a command. Takes 5d10 psychic if acts against it. Can't harm itself.",
            "bard","cleric","druid","paladin","wizard"));
        list.add(s("greater_restoration","Greater Restoration",5,"Abjuration","1 action","Touch","Instantaneous",false,false,"V,S,M",
            "End one: charm/petrify/curse/ability reduction/HP max reduction, or restore 1 exhaustion level.",
            "bard","cleric","druid"));
        list.add(s("hallow","Hallow",5,"Evocation","24 hours","Touch","Until dispelled",false,false,"V,S,M",
            "60-ft radius becomes holy/unholy ground. Choose extra effect: courage, protection, tongues, etc.",
            "cleric"));
        list.add(s("hold_monster","Hold Monster",5,"Enchantment","1 action","90 ft","Up to 1 min",true,false,"V,S,M",
            "WIS save or paralyzed. Save each turn to end. Works on any creature (not just humanoids).",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("insect_plague","Insect Plague",5,"Conjuration","1 action","300 ft","Up to 10 min",true,false,"V,S,M",
            "20-ft sphere of swarming locusts; 4d10 piercing, CON save for half. Difficult terrain.",
            "cleric","druid","sorcerer"));
        list.add(s("legend_lore","Legend Lore",5,"Divination","10 minutes","Self","Instantaneous",false,false,"V,S,M",
            "Learn legendary information about a person, place, or object.",
            "bard","cleric","wizard"));
        list.add(s("mass_cure_wounds","Mass Cure Wounds",5,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Up to 6 creatures regain 3d8 + spellcasting modifier HP.",
            "bard","cleric","druid"));
        list.add(s("mislead","Mislead",5,"Illusion","1 action","Self","Up to 1 hour",true,false,"S",
            "Become invisible and create illusory double you can see/speak through.",
            "bard","wizard"));
        list.add(s("modify_memory","Modify Memory",5,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V,S",
            "WIS save or charmed. While charmed, can modify up to 10 minutes of memory.",
            "bard","wizard"));
        list.add(s("planar_binding","Planar Binding",5,"Abjuration","1 hour","60 ft","24 hours",false,false,"V,S,M",
            "CHA save or celestial/elemental/fey/fiend is bound to serve you for the duration.",
            "bard","cleric","druid","wizard"));
        list.add(s("raise_dead","Raise Dead",5,"Necromancy","1 hour","Touch","Instantaneous",false,false,"V,S,M",
            "Return creature dead less than 10 days to life. -4 penalty to rolls, reduced by 1 per long rest.",
            "bard","cleric","paladin"));
        list.add(s("rary_telepathic_bond","Rary's Telepathic Bond",5,"Divination","1 action","30 ft","1 hour",false,true,"V,S,M",
            "Up to 8 willing creatures can communicate telepathically regardless of language or distance.",
            "wizard"));
        list.add(s("reincarnate","Reincarnate",5,"Transmutation","1 hour","Touch","Instantaneous",false,false,"V,S,M",
            "Return creature dead less than 10 days to life in a new, random humanoid body.",
            "druid"));
        list.add(s("scrying","Scrying",5,"Divination","10 minutes","Self","Up to 10 min",true,false,"V,S,M",
            "See and hear a specific creature on same plane. WIS save modified by familiarity.",
            "bard","cleric","druid","warlock","wizard"));
        list.add(s("seeming","Seeming",5,"Illusion","1 action","30 ft","8 hours",false,false,"V,S",
            "Disguise any number of creatures within range. Investigation vs. spell save DC.",
            "bard","sorcerer","wizard"));
        list.add(s("telekinesis","Telekinesis",5,"Transmutation","1 action","60 ft","Up to 10 min",true,false,"V,S",
            "Move creature or object up to 1000 lbs. Creatures contest STR vs. your spellcasting.",
            "sorcerer","wizard"));
        list.add(s("teleportation_circle","Teleportation Circle",5,"Conjuration","1 minute","10 ft","1 round",false,false,"V,M",
            "Create portal to a permanent circle you know the sigil sequence for.",
            "bard","sorcerer","wizard"));
        list.add(s("tree_stride","Tree Stride",5,"Conjuration","1 action","Self","Up to 1 min",true,false,"V,S",
            "Enter a tree and teleport to another tree of same kind within 500 ft.",
            "druid","ranger"));
        list.add(s("wall_of_force","Wall of Force",5,"Evocation","1 action","120 ft","Up to 10 min",true,false,"V,S,M",
            "Create invisible wall of force up to 10 panels. Nothing can physically pass through.",
            "wizard"));
        list.add(s("wall_of_stone","Wall of Stone",5,"Evocation","1 action","120 ft","Up to 10 min",true,false,"V,S,M",
            "Create nonmagical stone wall. Each 10-ft panel has AC 15 and 30 HP per inch.",
            "druid","sorcerer","wizard"));

        // ══ LEVEL 6 SPELLS ═══════════════════════════════════════════════════
        list.add(s("arcane_gate","Arcane Gate",6,"Conjuration","1 action","500 ft","Up to 10 min",true,false,"V,S",
            "Create linked teleportation portals within 500 ft; enter one, exit the other.",
            "sorcerer","warlock","wizard"));
        list.add(s("blade_barrier","Blade Barrier",6,"Evocation","1 action","90 ft","Up to 10 min",true,false,"V,S",
            "Wall of spinning blades; 6d10 slashing, DEX save for half.",
            "cleric"));
        list.add(s("chain_lightning","Chain Lightning",6,"Evocation","1 action","150 ft","Instantaneous",false,false,"V,S,M",
            "10d8 lightning to primary target; arcs to up to 3 secondary targets for 10d8 each.",
            "sorcerer","wizard"));
        list.add(s("circle_of_death","Circle of Death",6,"Necromancy","1 action","150 ft","Instantaneous",false,false,"V,S,M",
            "60-ft sphere; 8d6 necrotic damage, CON save for half.",
            "sorcerer","warlock","wizard"));
        list.add(s("conjure_fey","Conjure Fey",6,"Conjuration","1 minute","90 ft","Up to 1 hour",true,false,"V,S",
            "Summon fey creature of CR 6 or lower. Goes hostile if concentration breaks.",
            "druid","warlock"));
        list.add(s("contingency","Contingency",6,"Evocation","10 minutes","Self","10 days",false,false,"V,S,M",
            "Store a spell of 5th level or lower to trigger when specific conditions are met.",
            "wizard"));
        list.add(s("create_undead","Create Undead",6,"Necromancy","1 minute","10 ft","Instantaneous",false,false,"V,S,M",
            "Create 3 ghouls from corpses; obey your commands for 24 hours. Higher slots = stronger undead.",
            "cleric","warlock","wizard"));
        list.add(s("disintegrate","Disintegrate",6,"Transmutation","1 action","60 ft","Instantaneous",false,false,"V,S,M",
            "DEX save or 10d6+40 force damage. Target reduced to 0 HP becomes fine dust.",
            "sorcerer","wizard"));
        list.add(s("eyebite","Eyebite",6,"Necromancy","1 action","Self","Up to 1 min",true,false,"V,S",
            "Each turn, target one creature: WIS save or asleep, panicked, or sickened.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("find_the_path","Find the Path",6,"Divination","1 minute","Self","Up to 1 day",true,false,"V,S,M",
            "Know shortest route to a location. Alerts you to hazards and traps.",
            "bard","cleric","druid"));
        list.add(s("flesh_to_stone","Flesh to Stone",6,"Transmutation","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "CON save or begin turning to stone. 3 failed saves = permanent petrification.",
            "warlock","wizard"));
        list.add(s("forbiddance","Forbiddance",6,"Abjuration","10 minutes","Touch","1 day",false,true,"V,S,M",
            "40,000 sq ft area prevents planar travel; deals 5d10 radiant/necrotic to chosen creature types.",
            "cleric"));
        list.add(s("globe_of_invulnerability","Globe of Invulnerability",6,"Abjuration","1 action","Self (10-ft)","Up to 1 min",true,false,"V,S,M",
            "10-ft barrier; spells of 5th level or lower can't affect anything inside.",
            "sorcerer","wizard"));
        list.add(s("guards_and_wards","Guards and Wards",6,"Abjuration","10 minutes","Touch","24 hours",false,false,"V,S,M",
            "Ward up to 2,500 sq ft with fog, webs, doors, stairs illusions, and more.",
            "bard","wizard"));
        list.add(s("harm","Harm",6,"Necromancy","1 action","60 ft","Instantaneous",false,false,"V,S",
            "14d6 necrotic damage, CON save for half. Can't reduce target below 1 HP.",
            "cleric"));
        list.add(s("heal","Heal",6,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Restore 70 HP and end blindness, deafness, and any diseases.",
            "cleric","druid"));
        list.add(s("heroes_feast","Heroes' Feast",6,"Conjuration","10 minutes","30 ft","Instantaneous",false,false,"V,S,M",
            "Feast for 12; cures disease/poison, immunity to poison/fear, advantage on WIS saves, +2d10 max HP.",
            "cleric","druid"));
        list.add(s("magic_jar","Magic Jar",6,"Necromancy","1 minute","Self","Until dispelled",false,false,"V,S,M",
            "Project your soul into a container; possess humanoids within 100 ft (CHA save).",
            "wizard"));
        list.add(s("mass_suggestion","Mass Suggestion",6,"Enchantment","1 action","60 ft","24 hours",false,false,"V,M",
            "Up to 12 creatures; WIS save or follow a reasonable suggestion.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("move_earth","Move Earth",6,"Transmutation","1 action","120 ft","Up to 2 hours",true,false,"V,S,M",
            "Reshape terrain in a 40-ft square; raise/lower by up to 10 ft per 10 minutes.",
            "druid","sorcerer","wizard"));
        list.add(s("ottos_irresistible_dance","Otto's Irresistible Dance",6,"Enchantment","1 action","30 ft","Up to 1 min",true,false,"V",
            "Target must dance; speed 0, disadvantage on DEX saves and attacks. WIS save each turn to end.",
            "bard","wizard"));
        list.add(s("planar_ally","Planar Ally",6,"Conjuration","10 minutes","60 ft","Instantaneous",false,false,"V,S",
            "Beseech an entity for a celestial, elemental, or fiend to aid you (payment required).",
            "cleric"));
        list.add(s("programmed_illusion","Programmed Illusion",6,"Illusion","1 action","120 ft","Until dispelled",false,false,"V,S,M",
            "Create illusion up to 30-ft cube that activates when specific conditions are met.",
            "bard","wizard"));
        list.add(s("sunbeam","Sunbeam",6,"Evocation","1 action","Self (60-ft line)","Up to 1 min",true,false,"V,S,M",
            "60-ft line; 6d8 radiant and blinded, CON save for half and not blinded. Action to repeat.",
            "druid","sorcerer","wizard"));
        list.add(s("transport_via_plants","Transport via Plants",6,"Conjuration","1 action","10 ft","1 round",false,false,"V,S",
            "Create portal between two plants; any creature can use it to travel between them.",
            "druid"));
        list.add(s("true_seeing","True Seeing",6,"Divination","1 action","Touch","1 hour",false,false,"V,S,M",
            "Target has truesight (120 ft), sees invisible, sees true form, perceives portals.",
            "bard","cleric","sorcerer","warlock","wizard"));
        list.add(s("wall_of_ice","Wall of Ice",6,"Evocation","1 action","120 ft","Up to 10 min",true,false,"V,S,M",
            "Wall of 10 panels (10×10×1 ft); 10d6 cold when it appears, CON save for half.",
            "wizard"));
        list.add(s("wall_of_thorns","Wall of Thorns",6,"Conjuration","1 action","120 ft","Up to 10 min",true,false,"V,S,M",
            "60-ft wall; 7d8 slashing when entering, DEX save for half. Difficult terrain.",
            "druid"));
        list.add(s("wind_walk","Wind Walk",6,"Transmutation","1 minute","30 ft","8 hours",false,false,"V,S,M",
            "Up to 10 creatures become gaseous; 300-ft fly speed, resist nonmagical damage.",
            "druid"));
        list.add(s("word_of_recall","Word of Recall",6,"Conjuration","1 action","5 ft","Instantaneous",false,false,"V",
            "Teleport you and 5 willing creatures to a sanctuary you designated.",
            "cleric"));

        // ══ LEVEL 7 SPELLS ═══════════════════════════════════════════════════
        list.add(s("conjure_celestial","Conjure Celestial",7,"Conjuration","1 minute","90 ft","Up to 1 hour",true,false,"V,S",
            "Summon celestial of CR 4 or lower; obeys your commands.",
            "cleric"));
        list.add(s("delayed_blast_fireball","Delayed Blast Fireball",7,"Evocation","1 action","150 ft","Up to 1 min",true,false,"V,S,M",
            "Bead of fire grows by 1d6 each turn; up to 12d6 + 1d6 per turn fire, DEX save for half.",
            "sorcerer","wizard"));
        list.add(s("divine_word","Divine Word",7,"Evocation","1 bonus action","30 ft","Instantaneous",false,false,"V",
            "Based on HP: 50 or less deafened, 40 or less blinded, 30 or less stunned, 20 or less killed.",
            "cleric"));
        list.add(s("etherealness","Etherealness",7,"Transmutation","1 action","Self","Up to 8 hours",false,false,"V,S",
            "Step into Ethereal Plane; see into Material Plane 60 ft, can return as action.",
            "bard","cleric","sorcerer","warlock","wizard"));
        list.add(s("finger_of_death","Finger of Death",7,"Necromancy","1 action","60 ft","Instantaneous",false,false,"V,S",
            "7d8 + 30 necrotic, CON save for half. Humanoid killed rises as zombie under your control.",
            "sorcerer","warlock","wizard"));
        list.add(s("fire_storm","Fire Storm",7,"Evocation","1 action","150 ft","Instantaneous",false,false,"V,S",
            "Ten 10-ft cubes; 7d10 fire damage, DEX save for half. Can spare plant creatures.",
            "cleric","druid","sorcerer"));
        list.add(s("forcecage","Forcecage",7,"Evocation","1 action","100 ft","1 hour",false,false,"V,S,M",
            "Create cage or box of force; CHA save (DC 13 + spell level) to teleport out.",
            "bard","warlock","wizard"));
        list.add(s("mirage_arcane","Mirage Arcane",7,"Illusion","10 minutes","Sight","10 days",false,false,"V,S",
            "Terrain within 1-mile square looks and feels like different terrain.",
            "bard","druid","wizard"));
        list.add(s("mordenkainens_magnificent_mansion","Mordenkainen's Magnificent Mansion",7,"Conjuration","1 minute","300 ft","24 hours",false,false,"V,S,M",
            "Extradimensional dwelling with 100 servants, banquet, and accommodations.",
            "bard","wizard"));
        list.add(s("mordenkainens_sword","Mordenkainen's Sword",7,"Evocation","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "Spectral sword; 3d10 force as bonus action, crits on 19-20.",
            "bard","wizard"));
        list.add(s("plane_shift","Plane Shift",7,"Conjuration","1 action","Touch","Instantaneous",false,false,"V,S,M",
            "Transport up to 8 willing creatures to another plane, or banish unwilling (CHA save).",
            "cleric","druid","sorcerer","warlock","wizard"));
        list.add(s("prismatic_spray","Prismatic Spray",7,"Evocation","1 action","Self (60-ft cone)","Instantaneous",false,false,"V,S",
            "60-ft cone; roll d8 for each creature: fire, acid, lightning, poison, cold, petrify, banish, or two effects.",
            "sorcerer","wizard"));
        list.add(s("project_image","Project Image",7,"Illusion","1 action","500 miles","Up to 1 day",true,false,"V,S,M",
            "Create illusory double you can see/hear through and cast spells from.",
            "bard","wizard"));
        list.add(s("regenerate","Regenerate",7,"Transmutation","1 minute","Touch","1 hour",false,false,"V,S,M",
            "Regain 4d8 + 15 HP, then 1 HP per minute. Regrow lost limbs in 2 minutes.",
            "bard","cleric","druid"));
        list.add(s("resurrection","Resurrection",7,"Necromancy","1 hour","Touch","Instantaneous",false,false,"V,S,M",
            "Return creature dead up to 100 years to life. Restores missing body parts.",
            "bard","cleric"));
        list.add(s("reverse_gravity","Reverse Gravity",7,"Transmutation","1 action","100 ft","Up to 1 min",true,false,"V,S,M",
            "50-ft radius cylinder; creatures fall upward 100 ft, DEX save to grab something.",
            "druid","sorcerer","wizard"));
        list.add(s("sequester","Sequester",7,"Transmutation","1 action","Touch","Until dispelled",false,false,"V,S,M",
            "Willing creature or object becomes invisible and can't be located. Can set trigger.",
            "wizard"));
        list.add(s("simulacrum","Simulacrum",7,"Illusion","12 hours","Touch","Until dispelled",false,false,"V,S,M",
            "Create duplicate of creature with half HP. Obeys your commands absolutely.",
            "wizard"));
        list.add(s("symbol","Symbol",7,"Abjuration","1 minute","Touch","Until dispelled",false,false,"V,S,M",
            "Inscribe glyph that triggers: death, discord, fear, hopelessness, insanity, pain, sleep, or stunning.",
            "bard","cleric","wizard"));
        list.add(s("teleport","Teleport",7,"Conjuration","1 action","10 ft","Instantaneous",false,false,"V",
            "Transport self and up to 8 creatures anywhere on same plane. Accuracy depends on familiarity.",
            "bard","sorcerer","wizard"));

        // ══ LEVEL 8 SPELLS ═══════════════════════════════════════════════════
        list.add(s("antimagic_field","Antimagic Field",8,"Abjuration","1 action","Self (10-ft)","Up to 1 hour",true,false,"V,S,M",
            "10-ft sphere; all magic is suppressed within.",
            "cleric","wizard"));
        list.add(s("antipathy_sympathy","Antipathy/Sympathy",8,"Enchantment","1 hour","60 ft","10 days",false,false,"V,S,M",
            "Object repels or attracts a type of creature. WIS save to resist each hour.",
            "druid","wizard"));
        list.add(s("clone","Clone",8,"Necromancy","1 hour","Touch","Instantaneous",false,false,"V,S,M",
            "Create inert clone. When original dies, soul transfers to clone if available.",
            "wizard"));
        list.add(s("control_weather","Control Weather",8,"Transmutation","10 minutes","Self (5-mile)","Up to 8 hours",true,false,"V,S,M",
            "Change weather conditions within 5-mile radius over 1d4 × 10 minutes.",
            "cleric","druid","wizard"));
        list.add(s("demiplane","Demiplane",8,"Conjuration","1 action","60 ft","1 hour",false,false,"S",
            "Create door to 30-ft empty room; can connect to a demiplane you've created before.",
            "warlock","wizard"));
        list.add(s("dominate_monster","Dominate Monster",8,"Enchantment","1 action","60 ft","Up to 1 hour",true,false,"V,S",
            "WIS save or creature is charmed and obeys your telepathic commands.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("earthquake","Earthquake",8,"Evocation","1 action","500 ft","Up to 1 min",true,false,"V,S,M",
            "100-ft radius; difficult terrain, fissures open, structures collapse.",
            "cleric","druid","sorcerer"));
        list.add(s("feeblemind","Feeblemind",8,"Enchantment","1 action","150 ft","Instantaneous",false,false,"V,S,M",
            "4d6 psychic; INT save or INT and CHA become 1. Can't cast spells. Save every 30 days.",
            "bard","druid","warlock","wizard"));
        list.add(s("glibness","Glibness",8,"Transmutation","1 action","Self","1 hour",false,false,"V",
            "CHA checks have minimum roll of 15. Magic can't determine if you're lying.",
            "bard","warlock"));
        list.add(s("holy_aura","Holy Aura",8,"Abjuration","1 action","Self (30-ft)","Up to 1 min",true,false,"V,S,M",
            "Creatures in aura have advantage on saves, attackers have disadvantage, fiends are blinded.",
            "cleric"));
        list.add(s("incendiary_cloud","Incendiary Cloud",8,"Conjuration","1 action","150 ft","Up to 1 min",true,false,"V,S",
            "20-ft sphere of smoke and embers; 10d8 fire, DEX save for half. Moves 10 ft each turn.",
            "sorcerer","wizard"));
        list.add(s("maze","Maze",8,"Conjuration","1 action","60 ft","Up to 10 min",true,false,"V,S",
            "Banish creature to labyrinthine demiplane; DC 20 INT check to escape.",
            "wizard"));
        list.add(s("mind_blank","Mind Blank",8,"Abjuration","1 action","Touch","24 hours",false,false,"V,S",
            "One creature immune to psychic damage, mind reading, divination, and charm.",
            "bard","wizard"));
        list.add(s("power_word_stun","Power Word Stun",8,"Enchantment","1 action","60 ft","Instantaneous",false,false,"V",
            "Creature with 150 HP or less is stunned; CON save each turn to end.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("sunburst","Sunburst",8,"Evocation","1 action","150 ft","Instantaneous",false,false,"V,S,M",
            "60-ft sphere; 12d6 radiant and blinded for 1 minute, CON save for half and not blinded.",
            "druid","sorcerer","wizard"));
        list.add(s("telepathy","Telepathy",8,"Evocation","1 action","Unlimited","24 hours",false,false,"V,S,M",
            "Telepathic link with willing creature on same plane; communicate regardless of language.",
            "wizard"));
        list.add(s("tsunami","Tsunami",8,"Conjuration","1 minute","Sight","Up to 6 rounds",true,false,"V,S",
            "300-ft long, 300-ft high wave; 6d10 bludgeoning per round, STR save for half.",
            "druid"));

        // ══ LEVEL 9 SPELLS ═══════════════════════════════════════════════════
        list.add(s("astral_projection","Astral Projection",9,"Necromancy","1 hour","10 ft","Special",false,false,"V,S,M",
            "Project yourself and up to 8 others into the Astral Plane. Silver cord connects to body.",
            "cleric","warlock","wizard"));
        list.add(s("foresight","Foresight",9,"Divination","1 minute","Touch","8 hours",false,false,"V,S,M",
            "Target can't be surprised; advantage on attacks, checks, saves; attackers have disadvantage.",
            "bard","druid","warlock","wizard"));
        list.add(s("gate","Gate",9,"Conjuration","1 action","60 ft","Up to 1 min",true,false,"V,S,M",
            "Portal to another plane. Can summon specific creature by name (no save).",
            "cleric","sorcerer","wizard"));
        list.add(s("imprisonment","Imprisonment",9,"Abjuration","1 minute","30 ft","Until dispelled",false,false,"V,S,M",
            "WIS save or imprisoned: buried, chained, hedged, minimus, slumber, etc.",
            "warlock","wizard"));
        list.add(s("mass_heal","Mass Heal",9,"Evocation","1 action","60 ft","Instantaneous",false,false,"V,S",
            "Restore 700 HP divided among any creatures within 60 ft. Cures diseases/blindness/deafness.",
            "cleric"));
        list.add(s("meteor_swarm","Meteor Swarm",9,"Evocation","1 action","1 mile","Instantaneous",false,false,"V,S",
            "Four 40-ft spheres; 20d6 fire + 20d6 bludgeoning, DEX save for half.",
            "sorcerer","wizard"));
        list.add(s("power_word_heal","Power Word Heal",9,"Evocation","1 action","Touch","Instantaneous",false,false,"V,S",
            "Restore all HP. End charm, frightened, paralyzed, stunned. Can stand for free.",
            "bard"));
        list.add(s("power_word_kill","Power Word Kill",9,"Enchantment","1 action","60 ft","Instantaneous",false,false,"V",
            "Creature with 100 HP or less dies instantly. No save.",
            "bard","sorcerer","warlock","wizard"));
        list.add(s("prismatic_wall","Prismatic Wall",9,"Abjuration","1 action","60 ft","10 minutes",false,false,"V,S",
            "Seven-layer wall; each layer has different effect and requires different spell to dispel.",
            "wizard"));
        list.add(s("shapechange","Shapechange",9,"Transmutation","1 action","Self","Up to 1 hour",true,false,"V,S,M",
            "Transform into any creature of your CR or lower. Retain mental stats, gain physical stats.",
            "druid","wizard"));
        list.add(s("storm_of_vengeance","Storm of Vengeance",9,"Conjuration","1 action","Sight","Up to 1 min",true,false,"V,S",
            "360-ft storm; each round different effect: thunder, acid rain, lightning, hail, etc.",
            "druid"));
        list.add(s("time_stop","Time Stop",9,"Transmutation","1 action","Self","Instantaneous",false,false,"V",
            "Stop time for 1d4+1 turns. Ends if you affect another creature or its possessions.",
            "sorcerer","wizard"));
        list.add(s("true_polymorph","True Polymorph",9,"Transmutation","1 action","30 ft","Up to 1 hour",true,false,"V,S,M",
            "Transform creature into another creature, or object into creature. Permanent if concentrated 1 hour.",
            "bard","warlock","wizard"));
        list.add(s("true_resurrection","True Resurrection",9,"Necromancy","1 hour","Touch","Instantaneous",false,false,"V,S,M",
            "Return creature dead up to 200 years to life. Restores body completely if needed.",
            "cleric","druid"));
        list.add(s("weird","Weird",9,"Illusion","1 action","120 ft","Up to 1 min",true,false,"V,S",
            "Each creature in 30-ft sphere: WIS save or frightened, 4d10 psychic per turn.",
            "wizard"));
        list.add(s("wish","Wish",9,"Conjuration","1 action","Self","Instantaneous",false,false,"V",
            "Duplicate any 8th-level spell, or create other effects. Stress may prevent future Wishes.",
            "sorcerer","wizard"));

        return list;
    }
}
