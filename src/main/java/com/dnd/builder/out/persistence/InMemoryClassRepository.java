package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.ClassDefinition;
import com.dnd.builder.core.model.ClassDefinition.SpellcastingInfo;
import com.dnd.builder.core.model.ClassFeature;
import com.dnd.builder.core.model.SubclassDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.dnd.builder.core.model.ClassFeature.of;

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

    // Standard ASI levels for most classes
    private static final List<Integer> STANDARD_ASI = List.of(4, 8, 12, 16, 19);
    private static final List<Integer> FIGHTER_ASI = List.of(4, 6, 8, 12, 14, 16, 19);
    private static final List<Integer> ROGUE_ASI = List.of(4, 8, 10, 12, 16, 19);

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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Rage", "Enter a battle rage for +2 damage, resistance to physical damage. 2 uses per long rest."),
            of(1, "Unarmored Defense", "While not wearing armor, AC = 10 + DEX mod + CON mod."),
            of(2, "Reckless Attack", "Gain advantage on STR attack rolls, but attacks against you have advantage."),
            of(2, "Danger Sense", "Advantage on DEX saves against effects you can see (traps, spells)."),
            of(3, "Primal Path", "Choose your barbarian subclass."),
            of(5, "Extra Attack", "Attack twice when you take the Attack action."),
            of(5, "Fast Movement", "Speed increases by 10 ft while not wearing heavy armor."),
            of(7, "Feral Instinct", "Advantage on initiative. Act normally even when surprised if you rage."),
            of(9, "Brutal Critical", "Roll 1 additional weapon damage die on a critical hit."),
            of(11, "Relentless Rage", "If you drop to 0 HP while raging, DC 10 CON save to stay at 1 HP."),
            of(13, "Brutal Critical", "Roll 2 additional weapon damage dice on a critical hit."),
            of(15, "Persistent Rage", "Rage only ends early if you fall unconscious or choose to end it."),
            of(17, "Brutal Critical", "Roll 3 additional weapon damage dice on a critical hit."),
            of(18, "Indomitable Might", "STR check can't be lower than your STR score."),
            of(20, "Primal Champion", "STR and CON increase by 4. Maximum is now 24.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Spellcasting", "Cast bard spells using CHA. Learn cantrips and spells."),
            of(1, "Bardic Inspiration (d6)", "Bonus action: give ally a d6 to add to a roll. CHA mod uses per long rest."),
            of(2, "Jack of All Trades", "Add half proficiency bonus to ability checks you're not proficient in."),
            of(2, "Song of Rest (d6)", "During short rest, you and allies regain extra 1d6 HP when spending Hit Dice."),
            of(3, "Bard College", "Choose your bard subclass."),
            of(3, "Expertise", "Double proficiency bonus for two skills of your choice."),
            of(5, "Bardic Inspiration (d8)", "Inspiration die increases to d8."),
            of(5, "Font of Inspiration", "Regain Bardic Inspiration uses on short or long rest."),
            of(6, "Countercharm", "Action: allies within 30 ft have advantage on saves vs frightened/charmed."),
            of(9, "Song of Rest (d8)", "Song of Rest healing increases to d8."),
            of(10, "Bardic Inspiration (d10)", "Inspiration die increases to d10."),
            of(10, "Expertise", "Double proficiency bonus for two more skills."),
            of(10, "Magical Secrets", "Learn 2 spells from any class spell list."),
            of(13, "Song of Rest (d10)", "Song of Rest healing increases to d10."),
            of(14, "Magical Secrets", "Learn 2 more spells from any class."),
            of(15, "Bardic Inspiration (d12)", "Inspiration die increases to d12."),
            of(17, "Song of Rest (d12)", "Song of Rest healing increases to d12."),
            of(18, "Magical Secrets", "Learn 2 more spells from any class."),
            of(20, "Superior Inspiration", "If no Bardic Inspiration uses remain, regain 1 use on initiative.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Spellcasting", "Cast cleric spells using WIS. Prepare spells from full cleric list."),
            of(1, "Divine Domain", "Choose your cleric subclass at 1st level."),
            of(2, "Channel Divinity (1/rest)", "Channel divine energy. Turn Undead + domain feature."),
            of(2, "Turn Undead", "Undead within 30 ft must make WIS save or flee for 1 minute."),
            of(5, "Destroy Undead (CR 1/2)", "Turned undead CR 1/2 or lower are instantly destroyed."),
            of(6, "Channel Divinity (2/rest)", "Use Channel Divinity twice between rests."),
            of(8, "Destroy Undead (CR 1)", "Destroy undead of CR 1 or lower."),
            of(10, "Divine Intervention", "Call on deity for aid. d100 roll must be ≤ cleric level."),
            of(11, "Destroy Undead (CR 2)", "Destroy undead of CR 2 or lower."),
            of(14, "Destroy Undead (CR 3)", "Destroy undead of CR 3 or lower."),
            of(17, "Destroy Undead (CR 4)", "Destroy undead of CR 4 or lower."),
            of(18, "Channel Divinity (3/rest)", "Use Channel Divinity three times between rests."),
            of(20, "Divine Intervention (auto)", "Divine Intervention automatically succeeds.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Druidic", "Secret language of druids. Leave hidden messages others can't understand."),
            of(1, "Spellcasting", "Cast druid spells using WIS. Prepare spells from full druid list."),
            of(2, "Wild Shape", "Transform into beasts you've seen. 2 uses per short rest. Max CR 1/4."),
            of(2, "Druid Circle", "Choose your druid subclass."),
            of(4, "Wild Shape (CR 1/2)", "Transform into beasts up to CR 1/2, no swimming speed."),
            of(8, "Wild Shape (CR 1)", "Transform into beasts up to CR 1 with any movement."),
            of(18, "Timeless Body", "Age 10x slower. Can't be magically aged."),
            of(18, "Beast Spells", "Cast spells in Wild Shape form (V/S components only)."),
            of(20, "Archdruid", "Unlimited Wild Shape uses. Ignore V/S components of druid spells.")
        ));
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
        c.setAsiLevels(FIGHTER_ASI);
        c.setFeatures(List.of(
            of(1, "Fighting Style", "Choose a combat specialty: Archery, Defense, Dueling, etc."),
            of(1, "Second Wind", "Bonus action: regain 1d10 + fighter level HP. Once per short rest."),
            of(2, "Action Surge", "Take an additional action on your turn. Once per short rest."),
            of(3, "Martial Archetype", "Choose your fighter subclass."),
            of(5, "Extra Attack", "Attack twice when you take the Attack action."),
            of(9, "Indomitable", "Reroll a failed saving throw. Once per long rest."),
            of(11, "Extra Attack (2)", "Attack three times when you take the Attack action."),
            of(13, "Indomitable (2 uses)", "Use Indomitable twice per long rest."),
            of(17, "Action Surge (2 uses)", "Use Action Surge twice per short rest."),
            of(17, "Indomitable (3 uses)", "Use Indomitable three times per long rest."),
            of(20, "Extra Attack (3)", "Attack four times when you take the Attack action.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Unarmored Defense", "While unarmored, AC = 10 + DEX mod + WIS mod."),
            of(1, "Martial Arts", "Use DEX for unarmed/monk weapons. Bonus action unarmed strike."),
            of(2, "Ki", "Ki points = monk level. Fuel Flurry of Blows, Patient Defense, Step of Wind."),
            of(2, "Unarmored Movement", "Speed +10 ft while unarmored. Increases at higher levels."),
            of(3, "Monastic Tradition", "Choose your monk subclass."),
            of(3, "Deflect Missiles", "Reaction: reduce ranged attack damage by 1d10 + DEX + level."),
            of(4, "Slow Fall", "Reaction: reduce falling damage by 5 × monk level."),
            of(5, "Extra Attack", "Attack twice when you take the Attack action."),
            of(5, "Stunning Strike", "Spend 1 ki: target must CON save or be stunned until your next turn."),
            of(6, "Ki-Empowered Strikes", "Unarmed strikes count as magical for overcoming resistance."),
            of(7, "Evasion", "DEX saves for half damage: take no damage on success, half on failure."),
            of(7, "Stillness of Mind", "Action: end one charmed or frightened effect on yourself."),
            of(10, "Purity of Body", "Immune to disease and poison."),
            of(13, "Tongue of the Sun and Moon", "Understand all spoken languages; creatures understand you."),
            of(14, "Diamond Soul", "Proficient in all saving throws. Spend 1 ki to reroll a failed save."),
            of(15, "Timeless Body", "No frailty from old age. Can't be magically aged. No need for food/water."),
            of(18, "Empty Body", "4 ki: become invisible for 1 min. 8 ki: cast Astral Projection."),
            of(20, "Perfect Self", "Regain 4 ki when you roll initiative and have none.")
        ));
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
        c.setSpellcasting(castingHalf("CHA",true));
        c.setSubclassLevel(3); c.setSubclassNote("Sacred Oath chosen at level 3");
        c.setStartingGold(200);
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Divine Sense", "Action: detect celestials, fiends, undead within 60 ft. 1 + CHA mod uses."),
            of(1, "Lay on Hands", "Healing pool = 5 × paladin level. Touch to heal or cure disease/poison."),
            of(2, "Fighting Style", "Choose a combat specialty: Defense, Dueling, Great Weapon, Protection."),
            of(2, "Spellcasting", "Cast paladin spells using CHA. Prepare spells each long rest."),
            of(2, "Divine Smite", "On hit, expend spell slot for +2d8 radiant (+ 1d8 per slot level)."),
            of(3, "Divine Health", "Immune to disease."),
            of(3, "Sacred Oath", "Choose your paladin subclass."),
            of(5, "Extra Attack", "Attack twice when you take the Attack action."),
            of(6, "Aura of Protection", "You and allies within 10 ft add CHA mod to saving throws."),
            of(10, "Aura of Courage", "You and allies within 10 ft can't be frightened."),
            of(11, "Improved Divine Smite", "Melee weapon attacks deal +1d8 radiant damage."),
            of(14, "Cleansing Touch", "Action: end one spell on willing creature. CHA mod uses per long rest."),
            of(18, "Aura Improvements", "Auras extend to 30 ft radius.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Favored Enemy", "Choose a creature type. Advantage on WIS (Survival) to track, INT to recall info."),
            of(1, "Natural Explorer", "Choose a terrain. Double proficiency for INT/WIS checks, travel benefits."),
            of(2, "Fighting Style", "Choose: Archery, Defense, Dueling, or Two-Weapon Fighting."),
            of(2, "Spellcasting", "Cast ranger spells using WIS. Learn spells as you level."),
            of(3, "Ranger Archetype", "Choose your ranger subclass."),
            of(3, "Primeval Awareness", "Spend spell slot to sense aberrations/celestials/dragons/etc nearby."),
            of(5, "Extra Attack", "Attack twice when you take the Attack action."),
            of(6, "Favored Enemy", "Choose an additional favored enemy type."),
            of(6, "Natural Explorer", "Choose an additional favored terrain."),
            of(8, "Land's Stride", "Move through nonmagical difficult terrain at normal speed."),
            of(10, "Hide in Plain Sight", "+10 to Stealth checks when you spend 1 min camouflaging."),
            of(10, "Natural Explorer", "Choose an additional favored terrain."),
            of(14, "Vanish", "Bonus action to Hide. Can't be tracked by nonmagical means."),
            of(14, "Favored Enemy", "Choose an additional favored enemy type."),
            of(18, "Feral Senses", "No disadvantage attacking unseen creatures. Know invisible within 30 ft."),
            of(20, "Foe Slayer", "Add WIS mod to attack or damage roll against favored enemy (once per turn).")
        ));
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
        c.setAsiLevels(ROGUE_ASI);
        c.setFeatures(List.of(
            of(1, "Expertise", "Double proficiency bonus for two skills or thieves' tools."),
            of(1, "Sneak Attack", "1d6 extra damage on finesse/ranged attack with advantage or ally nearby."),
            of(1, "Thieves' Cant", "Secret language of thieves and hidden messages."),
            of(2, "Cunning Action", "Bonus action: Dash, Disengage, or Hide."),
            of(3, "Roguish Archetype", "Choose your rogue subclass."),
            of(3, "Sneak Attack (2d6)", "Sneak Attack increases to 2d6."),
            of(5, "Uncanny Dodge", "Reaction: halve damage from an attack you can see."),
            of(5, "Sneak Attack (3d6)", "Sneak Attack increases to 3d6."),
            of(6, "Expertise", "Double proficiency bonus for two more skills."),
            of(7, "Evasion", "DEX saves for half: take no damage on success, half on failure."),
            of(7, "Sneak Attack (4d6)", "Sneak Attack increases to 4d6."),
            of(9, "Sneak Attack (5d6)", "Sneak Attack increases to 5d6."),
            of(11, "Reliable Talent", "Minimum 10 on ability checks with proficiency."),
            of(11, "Sneak Attack (6d6)", "Sneak Attack increases to 6d6."),
            of(13, "Sneak Attack (7d6)", "Sneak Attack increases to 7d6."),
            of(14, "Blindsense", "Aware of hidden/invisible creatures within 10 ft if you can hear."),
            of(15, "Slippery Mind", "Proficiency in WIS saving throws."),
            of(15, "Sneak Attack (8d6)", "Sneak Attack increases to 8d6."),
            of(17, "Sneak Attack (9d6)", "Sneak Attack increases to 9d6."),
            of(18, "Elusive", "No attack roll has advantage against you while you aren't incapacitated."),
            of(19, "Sneak Attack (10d6)", "Sneak Attack increases to 10d6."),
            of(20, "Stroke of Luck", "Miss an attack? Hit instead. Fail an ability check? Treat as 20.")
        ));
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
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Spellcasting", "Cast sorcerer spells using CHA. Innate magic without preparation."),
            of(1, "Sorcerous Origin", "Choose your sorcerer subclass at 1st level."),
            of(2, "Font of Magic", "Sorcery points = sorcerer level. Convert to/from spell slots."),
            of(3, "Metamagic", "Choose 2 Metamagic options to twist your spells."),
            of(10, "Metamagic", "Learn an additional Metamagic option."),
            of(17, "Metamagic", "Learn an additional Metamagic option."),
            of(20, "Sorcerous Restoration", "Regain 4 sorcery points on short rest.")
        ));
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
        c.setSpellcasting(casting("warlock","CHA",false,2,List.of(a(1,1)),2,0));
        c.setSubclassLevel(1); c.setSubclassNote("Otherworldly Patron chosen at level 1");
        c.setStartingGold(100);
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Otherworldly Patron", "Choose your warlock subclass at 1st level."),
            of(1, "Pact Magic", "Cast spells using CHA. Slots restore on short rest."),
            of(2, "Eldritch Invocations", "Learn 2 invocations that enhance your abilities."),
            of(3, "Pact Boon", "Choose Pact of the Chain, Blade, or Tome."),
            of(5, "Eldritch Invocations", "Learn an additional invocation (3 total)."),
            of(7, "Eldritch Invocations", "Learn an additional invocation (4 total)."),
            of(9, "Eldritch Invocations", "Learn an additional invocation (5 total)."),
            of(11, "Mystic Arcanum (6th)", "Cast one 6th-level spell once per long rest without slot."),
            of(12, "Eldritch Invocations", "Learn an additional invocation (6 total)."),
            of(13, "Mystic Arcanum (7th)", "Cast one 7th-level spell once per long rest."),
            of(15, "Eldritch Invocations", "Learn an additional invocation (7 total)."),
            of(15, "Mystic Arcanum (8th)", "Cast one 8th-level spell once per long rest."),
            of(17, "Mystic Arcanum (9th)", "Cast one 9th-level spell once per long rest."),
            of(18, "Eldritch Invocations", "Learn an additional invocation (8 total)."),
            of(20, "Eldritch Master", "Once per long rest, spend 1 minute to regain all Pact Magic slots.")
        ));
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
        c.setSpellcasting(casting("full","INT",true,3,List.of(a(1,2)),0,6));
        c.setSubclassLevel(2); c.setSubclassNote("Arcane Tradition chosen at level 2");
        c.setStartingGold(125);
        c.setAsiLevels(STANDARD_ASI);
        c.setFeatures(List.of(
            of(1, "Spellcasting", "Cast wizard spells using INT. Prepare from your spellbook."),
            of(1, "Arcane Recovery", "Once per day during short rest, recover spell slots (up to half level)."),
            of(2, "Arcane Tradition", "Choose your wizard subclass."),
            of(18, "Spell Mastery", "Choose a 1st and 2nd level spell to cast at will without slot."),
            of(20, "Signature Spells", "Two 3rd-level spells always prepared, cast once each without slot.")
        ));
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
