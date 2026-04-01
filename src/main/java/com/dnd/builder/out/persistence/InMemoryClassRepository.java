package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.ClassDefinition;
import com.dnd.builder.core.model.ClassDefinition.SpellcastingInfo;
import com.dnd.builder.core.model.SubclassDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryClassRepository implements com.dnd.builder.core.port.out.ClassRepository {

    private final List<ClassDefinition> classes;
    private final Map<String, ClassDefinition> byId;

    public InMemoryClassRepository() {
        classes = buildClasses();
        byId = new HashMap<>();
        classes.forEach(c -> byId.put(c.getId(), c));
    }

    public List<ClassDefinition> findAll() { return Collections.unmodifiableList(classes); }
    public ClassDefinition findById(String id)    { return byId.get(id); }

    public boolean isSpellcaster(String classId) {
        var cd = byId.get(classId);
        return cd != null && cd.getSpellcasting() != null;
    }

    /** Proficiency bonus by character level (same for all classes) */
    public static int proficiencyBonus(int level) {
        return 2 + (level - 1) / 4;
    }

    // ── Spell slot table: [level] → int[9] slots per spell level ─────────────
    /** Full caster (Wizard/Cleric/Druid/Bard/Sorcerer) spell slots */
    private static final int[][] FULL_CASTER_SLOTS = {
        {2,0,0,0,0,0,0,0,0}, // level 1
        {3,0,0,0,0,0,0,0,0}, // level 2
        {4,2,0,0,0,0,0,0,0}, // level 3
        {4,3,0,0,0,0,0,0,0}, // level 4
        {4,3,2,0,0,0,0,0,0}, // level 5
        {4,3,3,0,0,0,0,0,0}, // level 6
        {4,3,3,1,0,0,0,0,0}, // level 7
        {4,3,3,2,0,0,0,0,0}, // level 8
        {4,3,3,3,1,0,0,0,0}, // level 9
        {4,3,3,3,2,0,0,0,0}, // level 10
        {4,3,3,3,2,1,0,0,0}, // level 11
        {4,3,3,3,2,1,0,0,0}, // level 12
        {4,3,3,3,2,1,1,0,0}, // level 13
        {4,3,3,3,2,1,1,0,0}, // level 14
        {4,3,3,3,2,1,1,1,0}, // level 15
        {4,3,3,3,2,1,1,1,0}, // level 16
        {4,3,3,3,2,1,1,1,1}, // level 17
        {4,3,3,3,3,1,1,1,1}, // level 18
        {4,3,3,3,3,2,1,1,1}, // level 19
        {4,3,3,3,3,2,2,1,1}  // level 20
    };

    /** Warlock pact magic slots: [level] → {numSlots, slotLevel} */
    private static final int[][] WARLOCK_SLOTS = {
        {1,1},{2,1},{2,2},{2,2},{2,3},{2,3},{2,4},{2,4},{2,5},{2,5},
        {3,5},{3,5},{3,5},{3,5},{3,5},{3,5},{4,5},{4,5},{4,5},{4,5}
    };

    public static int[] fullCasterSlots(int level) {
        return level >= 1 && level <= 20 ? FULL_CASTER_SLOTS[level-1] : new int[9];
    }

    public static int[] warlockSlots(int level) {
        return level >= 1 && level <= 20 ? WARLOCK_SLOTS[level-1] : new int[]{0,0};
    }

    // ── Build all classes ─────────────────────────────────────────────────────
    private List<ClassDefinition> buildClasses() {
        return List.of(
            buildBarbarian(), buildBard(), buildCleric(), buildDruid(),
            buildFighter(), buildMonk(), buildPaladin(), buildRanger(),
            buildRogue(), buildSorcerer(), buildWarlock(), buildWizard()
        );
    }

    // ── BARBARIAN ─────────────────────────────────────────────────────────────
    private ClassDefinition buildBarbarian() {
        var c = new ClassDefinition();
        c.setId("barbarian"); c.setName("Barbarian"); c.setHitDie(12);
        c.setPrimaryAbility("STR");
        c.setSavingThrows(List.of("STR","CON"));
        c.setArmorProficiencies(List.of("Light armor","Medium armor","Shields"));
        c.setWeaponProficiencies(List.of("Simple weapons","Martial weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Animal Handling","Athletics","Intimidation","Nature","Perception","Survival"));
        c.setSpellcasting(null);
        c.setSubclassLevel(3); c.setSubclassNote("Primal Path chosen at level 3");
        c.setStartingGold(200);
        c.setSubclasses(List.of(
            sub("berserker",     "Path of the Berserker","Berserkers harness their rage into single-minded violence."),
            sub("totem_warrior", "Path of the Totem Warrior","Seekers of spiritual power; bear, eagle, or wolf."),
            sub("battlerager",   "Path of the Battlerager","Spiky armor style focused on grappling and bleeding. (SCAG)"),
            sub("ancestral",     "Path of the Ancestral Guardian","Ancestors shield your allies. (XGtE)"),
            sub("storm_herald",  "Path of the Storm Herald","Auras of desert, sea, or tundra. (XGtE)"),
            sub("zealot",        "Path of the Zealot","Divine fury, hard to kill, kept fighting. (XGtE)"),
            sub("beast",         "Path of the Beast","Natural weapons manifest in rage. (TCoE)"),
            sub("wild_magic_barb","Path of Wild Magic","Chaotic surges fuel your rage. (TCoE)")
        ));
        return c;
    }

    // ── BARD ─────────────────────────────────────────────────────────────────
    private ClassDefinition buildBard() {
        var c = new ClassDefinition();
        c.setId("bard"); c.setName("Bard"); c.setHitDie(8);
        c.setPrimaryAbility("CHA");
        c.setSavingThrows(List.of("DEX","CHA"));
        c.setArmorProficiencies(List.of("Light armor"));
        c.setWeaponProficiencies(List.of("Simple weapons","Hand crossbows","Longswords","Rapiers","Shortswords"));
        c.setToolProficiencies(List.of("Three musical instruments of your choice"));
        c.setSkillChoiceCount(3);
        c.setSkillList(List.of("Acrobatics","Animal Handling","Arcana","Athletics","Deception","History",
            "Insight","Intimidation","Investigation","Medicine","Nature","Perception","Performance",
            "Persuasion","Religion","Sleight of Hand","Stealth","Survival"));
        c.setSpellcasting(casting("full","CHA",false,2,List.of(a(1,2)),4,0));
        c.setSubclassLevel(3); c.setSubclassNote("Bard College chosen at level 3");
        c.setStartingGold(125);
        c.setSubclasses(List.of(
            sub("lore",     "College of Lore","Additional skills, Cutting Words, and extra spells."),
            sub("valor",    "College of Valor","Armor proficiency, combat inspiration, extra attack."),
            sub("glamour",  "College of Glamour","Fey-touched magic and charm. (XGtE)"),
            sub("swords",   "College of Swords","Blade flourishes and Mobile-style mobility. (XGtE)"),
            sub("whispers", "College of Whispers","Psychic Blades and Words of Terror. (XGtE)"),
            sub("eloquence","College of Eloquence","Silver Tongue and Unfailing Inspiration. (TCoE)"),
            sub("creation", "College of Creation","Song of Creation and Performance of Creation. (TCoE)")
        ));
        return c;
    }

    // ── CLERIC ────────────────────────────────────────────────────────────────
    private ClassDefinition buildCleric() {
        var c = new ClassDefinition();
        c.setId("cleric"); c.setName("Cleric"); c.setHitDie(8);
        c.setPrimaryAbility("WIS");
        c.setSavingThrows(List.of("WIS","CHA"));
        c.setArmorProficiencies(List.of("Light armor","Medium armor","Shields"));
        c.setWeaponProficiencies(List.of("Simple weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("History","Insight","Medicine","Persuasion","Religion"));
        c.setSpellcasting(casting("full","WIS",true,3,List.of(a(1,2)),0,0));
        c.setSubclassLevel(1); c.setSubclassNote("Divine Domain chosen at level 1");
        c.setStartingGold(125);
        c.setSubclasses(List.of(
            sub("knowledge","Knowledge Domain","Access to more skills and tongues."),
            sub("life",     "Life Domain","Heavy armor, powerful healing spells."),
            sub("light",    "Light Domain","Radiance and fire, Warding Flare reaction."),
            sub("nature",   "Nature Domain","Wild shaping light, heavy armor."),
            sub("tempest",  "Tempest Domain","Thunder/lightning, heavy armor, wrath of storms."),
            sub("trickery", "Trickery Domain","Deception and illusion spells, Invoke Duplicity."),
            sub("war",      "War Domain","Heavy armor, extra attack, War Priest bonus attacks."),
            sub("death",    "Death Domain","Necrotic damage and Reaper cantrips. (DMG)"),
            sub("arcana",   "Arcana Domain","Arcane magic merged with divine power. (SCAG)"),
            sub("forge",    "Forge Domain","Artisan abilities and fire resistance. (XGtE)"),
            sub("grave",    "Grave Domain","Spare the dying, prevent death, smite undead. (XGtE)"),
            sub("order",    "Order Domain","Voice of Authority and lawful magic. (TCoE)"),
            sub("peace",    "Peace Domain","Emboldening Bond and protective balm. (TCoE)"),
            sub("twilight", "Twilight Domain","Darkvision sharing and Twilight Sanctuary. (TCoE)")
        ));
        return c;
    }

    // ── DRUID ─────────────────────────────────────────────────────────────────
    private ClassDefinition buildDruid() {
        var c = new ClassDefinition();
        c.setId("druid"); c.setName("Druid"); c.setHitDie(8);
        c.setPrimaryAbility("WIS");
        c.setSavingThrows(List.of("INT","WIS"));
        c.setArmorProficiencies(List.of("Light armor","Medium armor","Shields (non-metal)"));
        c.setWeaponProficiencies(List.of("Clubs","Daggers","Darts","Javelins","Maces","Quarterstaffs","Scimitars","Sickles","Slings","Spears"));
        c.setToolProficiencies(List.of("Herbalism kit"));
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Arcana","Animal Handling","Insight","Medicine","Nature","Perception","Religion","Survival"));
        c.setSpellcasting(casting("full","WIS",true,2,List.of(a(1,2)),0,0));
        c.setSubclassLevel(2); c.setSubclassNote("Druid Circle chosen at level 2");
        c.setStartingGold(50);
        c.setSubclasses(List.of(
            sub("land",     "Circle of the Land","Expanded spells by terrain, extra spells."),
            sub("moon",     "Circle of the Moon","Wild Shape into stronger beasts, combat forms."),
            sub("dreams",   "Circle of Dreams","Balm of the Summer Court and fey connections. (XGtE)"),
            sub("shepherd", "Circle of the Shepherd","Spirit Totem auras and enhanced summoning. (XGtE)"),
            sub("spores",   "Circle of Spores","Death and decay, Symbiotic Entity. (TCoE)"),
            sub("stars",    "Circle of Stars","Constellation forms and cosmic power. (TCoE)"),
            sub("wildfire", "Circle of Wildfire","Wildfire Spirit companion and fire magic. (TCoE)")
        ));
        return c;
    }

    // ── FIGHTER ───────────────────────────────────────────────────────────────
    private ClassDefinition buildFighter() {
        var c = new ClassDefinition();
        c.setId("fighter"); c.setName("Fighter"); c.setHitDie(10);
        c.setPrimaryAbility("STR");
        c.setSavingThrows(List.of("STR","CON"));
        c.setArmorProficiencies(List.of("All armor","Shields"));
        c.setWeaponProficiencies(List.of("Simple weapons","Martial weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Acrobatics","Animal Handling","Athletics","History","Insight","Intimidation","Perception","Survival"));
        c.setSpellcasting(null);
        c.setSubclassLevel(3); c.setSubclassNote("Martial Archetype chosen at level 3");
        c.setStartingGold(200);
        c.setSubclasses(List.of(
            sub("champion",        "Champion","Critical hits on 19–20, improved critical."),
            sub("battle_master",   "Battle Master","Maneuvers fueled by Superiority Dice."),
            sub("eldritch_knight", "Eldritch Knight","Abjuration & evocation spells added to repertoire."),
            sub("purple_dragon",   "Purple Dragon Knight","Banneret, inspiring allies in battle. (SCAG)"),
            sub("arcane_archer",   "Arcane Archer","Magical arrows with various effects. (XGtE)"),
            sub("cavalier",        "Cavalier","Mounted combat mastery and marking foes. (XGtE)"),
            sub("samurai",         "Samurai","Fighting Spirit for advantage, extra proficiencies. (XGtE)"),
            sub("echo_knight",     "Echo Knight","Manifest echoes of yourself in combat. (EGtW)"),
            sub("psi_warrior",     "Psi Warrior","Telekinetic force and psionic powers. (TCoE)"),
            sub("rune_knight",     "Rune Knight","Giant runes grant magical abilities. (TCoE)")
        ));
        return c;
    }

    // ── MONK ──────────────────────────────────────────────────────────────────
    private ClassDefinition buildMonk() {
        var c = new ClassDefinition();
        c.setId("monk"); c.setName("Monk"); c.setHitDie(8);
        c.setPrimaryAbility("DEX");
        c.setSavingThrows(List.of("STR","DEX"));
        c.setArmorProficiencies(List.of("None"));
        c.setWeaponProficiencies(List.of("Simple weapons","Shortswords"));
        c.setToolProficiencies(List.of("One artisan's tool or musical instrument of your choice"));
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Acrobatics","Athletics","History","Insight","Religion","Stealth"));
        c.setSpellcasting(null);
        c.setSubclassLevel(3); c.setSubclassNote("Monastic Tradition chosen at level 3");
        c.setStartingGold(25);
        c.setSubclasses(List.of(
            sub("open_hand",       "Way of the Open Hand","Flurry pushes and prone, Wholeness of Body."),
            sub("shadow",          "Way of Shadow","Ki for darkness, stealth, and shadow teleport."),
            sub("four_elements",   "Way of the Four Elements","Elemental disciplines powered by ki."),
            sub("long_death",      "Way of the Long Death","Fear, necrotic touch, undying HP. (SCAG)"),
            sub("sun_soul",        "Way of the Sun Soul","Radiant ki bolts and Searing Sunburst. (SCAG/XGtE)"),
            sub("drunken_master",  "Way of the Drunken Master","Unpredictable movement and redirects. (XGtE)"),
            sub("kensei",          "Way of the Kensei","Weapon mastery with chosen kensei weapons. (XGtE)"),
            sub("astral_self",     "Way of the Astral Self","Manifest astral arms and visage. (TCoE)"),
            sub("mercy",           "Way of Mercy","Healing and harm with Implements of Mercy. (TCoE)")
        ));
        return c;
    }

    // ── PALADIN ───────────────────────────────────────────────────────────────
    private ClassDefinition buildPaladin() {
        var c = new ClassDefinition();
        c.setId("paladin"); c.setName("Paladin"); c.setHitDie(10);
        c.setPrimaryAbility("STR");
        c.setSavingThrows(List.of("WIS","CHA"));
        c.setArmorProficiencies(List.of("All armor","Shields"));
        c.setWeaponProficiencies(List.of("Simple weapons","Martial weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Athletics","Insight","Intimidation","Medicine","Persuasion","Religion"));
        // Paladins get spell slots at level 2; casting info still useful for the com.dnd.builder
        c.setSpellcasting(castingHalf("CHA",true));
        c.setSubclassLevel(3); c.setSubclassNote("Sacred Oath chosen at level 3");
        c.setStartingGold(200);
        c.setSubclasses(List.of(
            sub("devotion",   "Oath of Devotion","Radiant smites, Sacred Weapon, holy aura."),
            sub("ancients",   "Oath of the Ancients","Nature and light vs. darkness, Aura of Warding."),
            sub("vengeance",  "Oath of Vengeance","Vow of Enmity advantage, Relentless Avenger."),
            sub("crown",      "Oath of the Crown","Champion of law, civilization, and order. (SCAG)"),
            sub("conquest",   "Oath of Conquest","Fear and domination themed smites. (XGtE)"),
            sub("redemption", "Oath of Redemption","Peaceful resolution and protective rebuke. (XGtE)"),
            sub("glory",      "Oath of Glory","Athletic inspiration, peerless athlete. (TCoE)"),
            sub("watchers",   "Oath of the Watchers","Hunt extraplanar threats, Abjure Enemy. (TCoE)")
        ));
        return c;
    }

    // ── RANGER ────────────────────────────────────────────────────────────────
    private ClassDefinition buildRanger() {
        var c = new ClassDefinition();
        c.setId("ranger"); c.setName("Ranger"); c.setHitDie(10);
        c.setPrimaryAbility("DEX");
        c.setSavingThrows(List.of("STR","DEX"));
        c.setArmorProficiencies(List.of("Light armor","Medium armor","Shields"));
        c.setWeaponProficiencies(List.of("Simple weapons","Martial weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(3);
        c.setSkillList(List.of("Animal Handling","Athletics","Insight","Investigation","Nature","Perception","Stealth","Survival"));
        c.setSpellcasting(castingHalf("WIS",false));
        c.setSubclassLevel(3); c.setSubclassNote("Ranger Archetype chosen at level 3");
        c.setStartingGold(125);
        c.setSubclasses(List.of(
            sub("hunter",         "Hunter","Colossus Slayer, Giant Killer, or Horde Breaker."),
            sub("beastmaster",    "Beast Master","Animal companion that fights alongside you."),
            sub("gloom_stalker",  "Gloom Stalker","Dark ambush mastery, Dread Ambusher. (XGtE)"),
            sub("horizon_walker", "Horizon Walker","Planar Warrior and portal detection. (XGtE)"),
            sub("monster_slayer","Monster Slayer","Slayer's Prey, supernatural defense. (XGtE)"),
            sub("fey_wanderer",   "Fey Wanderer","Beguiling Twist and otherworldly grace. (TCoE)"),
            sub("swarmkeeper",    "Swarmkeeper","Swarm of spirits aids attacks and movement. (TCoE)"),
            sub("drakewarden",    "Drakewarden","Drake companion that grows with you. (FToD)")
        ));
        return c;
    }

    // ── ROGUE ─────────────────────────────────────────────────────────────────
    private ClassDefinition buildRogue() {
        var c = new ClassDefinition();
        c.setId("rogue"); c.setName("Rogue"); c.setHitDie(8);
        c.setPrimaryAbility("DEX");
        c.setSavingThrows(List.of("DEX","INT"));
        c.setArmorProficiencies(List.of("Light armor"));
        c.setWeaponProficiencies(List.of("Simple weapons","Hand crossbows","Longswords","Rapiers","Shortswords"));
        c.setToolProficiencies(List.of("Thieves' tools"));
        c.setSkillChoiceCount(4);
        c.setSkillList(List.of("Acrobatics","Athletics","Deception","Insight","Intimidation","Investigation","Perception","Performance","Persuasion","Sleight of Hand","Stealth"));
        c.setSpellcasting(null);
        c.setSubclassLevel(3); c.setSubclassNote("Roguish Archetype chosen at level 3");
        c.setStartingGold(100);
        c.setSubclasses(List.of(
            sub("thief",           "Thief","Fast Hands, Second-Story Work, use magic devices."),
            sub("assassin",        "Assassin","Infiltrate, disguise, auto-crit on surprised foes."),
            sub("arcane_trickster","Arcane Trickster","Enchantment/illusion spells, Mage Hand Legerdemain."),
            sub("mastermind",      "Mastermind","Master of Tactics bonus Help, infiltration. (SCAG/XGtE)"),
            sub("swashbuckler",    "Swashbuckler","Rakish Audacity, Fancy Footwork. (SCAG/XGtE)"),
            sub("inquisitive",     "Inquisitive","Insight, Ear for Deceit, Eye for Detail. (XGtE)"),
            sub("scout",           "Scout","Survivalist, Skirmisher mobility. (XGtE)"),
            sub("phantom",         "Phantom","Soul trinkets and necrotic Wails. (TCoE)"),
            sub("soulknife",       "Soulknife","Psionic blades and psychic powers. (TCoE)")
        ));
        return c;
    }

    // ── SORCERER ──────────────────────────────────────────────────────────────
    private ClassDefinition buildSorcerer() {
        var c = new ClassDefinition();
        c.setId("sorcerer"); c.setName("Sorcerer"); c.setHitDie(6);
        c.setPrimaryAbility("CHA");
        c.setSavingThrows(List.of("CON","CHA"));
        c.setArmorProficiencies(List.of("None"));
        c.setWeaponProficiencies(List.of("Daggers","Darts","Slings","Quarterstaffs","Light crossbows"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Arcana","Deception","Insight","Intimidation","Persuasion","Religion"));
        c.setSpellcasting(casting("full","CHA",false,4,List.of(a(1,2)),2,0));
        c.setSubclassLevel(1); c.setSubclassNote("Sorcerous Origin chosen at level 1");
        c.setStartingGold(75);
        c.setSubclasses(List.of(
            sub("draconic",      "Draconic Bloodline","Draconic ancestry; AC bonus, wings at level 14."),
            sub("wild",          "Wild Magic","Surges of uncontrolled magic and Tides of Chaos."),
            sub("divine_soul",   "Divine Soul","Access to cleric spells, favored by the gods. (XGtE)"),
            sub("shadow",        "Shadow Magic","Hound of Ill Omen, strength of the Shadowfell. (XGtE)"),
            sub("storm",         "Storm Sorcery","Wind Speaker, Tempestuous Magic. (XGtE/SCAG)"),
            sub("aberrant_mind", "Aberrant Mind","Psionic spells and telepathy. (TCoE)"),
            sub("clockwork_soul","Clockwork Soul","Order of Mechanus, restore balance. (TCoE)")
        ));
        return c;
    }

    // ── WARLOCK ───────────────────────────────────────────────────────────────
    private ClassDefinition buildWarlock() {
        var c = new ClassDefinition();
        c.setId("warlock"); c.setName("Warlock"); c.setHitDie(8);
        c.setPrimaryAbility("CHA");
        c.setSavingThrows(List.of("WIS","CHA"));
        c.setArmorProficiencies(List.of("Light armor"));
        c.setWeaponProficiencies(List.of("Simple weapons"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Arcana","Deception","History","Intimidation","Investigation","Nature","Religion"));
        // Warlock uses Pact Magic (short rest slots)
        c.setSpellcasting(casting("warlock","CHA",false,2,List.of(a(1,1)),2,0));
        c.setSubclassLevel(1); c.setSubclassNote("Otherworldly Patron chosen at level 1");
        c.setStartingGold(100);
        c.setSubclasses(List.of(
            sub("archfey",   "The Archfey","Fey Presence, Misty Escape, Beguiling Defenses."),
            sub("fiend",     "The Fiend","Dark One's Blessing HP, Fiend spell list."),
            sub("great_old", "The Great Old One","Awakened Mind telepathy, Entropic Ward."),
            sub("celestial", "The Celestial","Healing Light, sacred flame, radiant soul. (XGtE)"),
            sub("hexblade",  "The Hexblade","Hexblade's Curse, Hex Warrior (melee CHA). (XGtE)"),
            sub("fathomless","The Fathomless","Tentacle of the Deeps, oceanic powers. (TCoE)"),
            sub("genie",     "The Genie","Genie's Vessel, elemental gifts from dao/djinni/efreeti/marid. (TCoE)"),
            sub("undead",    "The Undead","Form of Dread and necrotic empowerment. (VRGtR)")
        ));
        return c;
    }

    // ── WIZARD ────────────────────────────────────────────────────────────────
    private ClassDefinition buildWizard() {
        var c = new ClassDefinition();
        c.setId("wizard"); c.setName("Wizard"); c.setHitDie(6);
        c.setPrimaryAbility("INT");
        c.setSavingThrows(List.of("INT","WIS"));
        c.setArmorProficiencies(List.of("None"));
        c.setWeaponProficiencies(List.of("Daggers","Darts","Slings","Quarterstaffs","Light crossbows"));
        c.setToolProficiencies(List.of());
        c.setSkillChoiceCount(2);
        c.setSkillList(List.of("Arcana","History","Insight","Investigation","Medicine","Religion"));
        // Wizard prepares spells; spellbook starts with 6
        c.setSpellcasting(casting("full","INT",true,3,List.of(a(1,2)),0,6));
        c.setSubclassLevel(2); c.setSubclassNote("Arcane Tradition chosen at level 2");
        c.setStartingGold(125);
        c.setSubclasses(List.of(
            sub("abjuration",    "School of Abjuration","Ward and Arcane Ward (temp HP bubble)."),
            sub("conjuration",   "School of Conjuration","Minor Conjuration object creation."),
            sub("divination",    "School of Divination","Portent dice, prophetic accuracy."),
            sub("enchantment",   "School of Enchantment","Hypnotic Gaze, Instinctive Charm."),
            sub("evocation",     "School of Evocation","Sculpt Spells to protect allies."),
            sub("illusion",      "School of Illusion","Improved Minor Illusion, Malleable Illusions."),
            sub("necromancy",    "School of Necromancy","Grim Harvest temp HP, undead army."),
            sub("transmutation", "School of Transmutation","Minor Alchemy, Transmuter's Stone."),
            sub("bladesinging",  "Bladesinging","Elven sword-dance combat magic. (SCAG/TCoE)"),
            sub("war_magic",     "War Magic","Arcane Deflection, Power Surge. (XGtE)"),
            sub("chronurgy",     "Chronurgy Magic","Time manipulation, Chronal Shift. (EGtW)"),
            sub("graviturgy",    "Graviturgy Magic","Gravity control, Adjust Density. (EGtW)"),
            sub("scribes",       "Order of Scribes","Awakened Spellbook, manifest mind. (TCoE)")
        ));
        return c;
    }

    // ── Helper builders ───────────────────────────────────────────────────────
    private SubclassDefinition sub(String id, String name, String desc) {
        return new SubclassDefinition(id, name, desc);
    }

    /** Build SpellcastingInfo for full/warlock casters */
    private SpellcastingInfo casting(String type, String ability, boolean prepare,
                                     int cantrips, List<int[]> slots, int known, int spellbook) {
        var si = new SpellcastingInfo();
        si.setType(type); si.setAbility(ability); si.setPrepareSpells(prepare);
        si.setCantripsAtL1(cantrips); si.setSlotsAtL1(slots);
        si.setSpellsKnownAtL1(known); si.setSpellbookSizeAtL1(spellbook);
        return si;
    }

    /** Half-caster (Paladin/Ranger) — no slots at level 1, label the casting ability */
    private SpellcastingInfo castingHalf(String ability, boolean prepare) {
        var si = new SpellcastingInfo();
        si.setType("half"); si.setAbility(ability); si.setPrepareSpells(prepare);
        si.setCantripsAtL1(0); si.setSlotsAtL1(List.of()); si.setSpellsKnownAtL1(0);
        return si;
    }

    private int[] a(int slotLevel, int count) { return new int[]{slotLevel, count}; }
}
