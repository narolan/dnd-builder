package com.dnd.builder.out.persistence;

import com.dnd.builder.core.model.BackgroundDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryBackgroundRepository implements com.dnd.builder.core.port.out.BackgroundRepository {

    private final List<BackgroundDefinition> backgrounds;
    private final Map<String, BackgroundDefinition> byId;

    public InMemoryBackgroundRepository() {
        backgrounds = buildBackgrounds();
        byId = new HashMap<>();
        backgrounds.forEach(b -> byId.put(b.getId(), b));
    }

    public List<BackgroundDefinition> findAll() { return Collections.unmodifiableList(backgrounds); }
    public BackgroundDefinition findById(String id)        { return byId.get(id); }

    private List<BackgroundDefinition> buildBackgrounds() {
        var list = new ArrayList<BackgroundDefinition>();
        list.addAll(List.of(
            bg("acolyte",      "Acolyte",
               List.of("Insight","Religion"),
               List.of(), 2,
               "A holy symbol, a prayer book, 5 sticks of incense, vestments, common clothes, a belt pouch with 15 gp",
               "Shelter of the Faithful",
               "You command respect from those who share your faith. You and your companions can expect free healing and care at temples.",
               "You served in a temple, devoting yourself to the study of divine texts.",
               15),

            bg("charlatan",    "Charlatan",
               List.of("Deception","Sleight of Hand"),
               List.of("Disguise kit","Forgery kit"), 0,
               "Fine clothes, a disguise kit, tools of your con (10 stoppered bottles of colored liquid, a set of weighted dice), a belt pouch with 15 gp",
               "False Identity",
               "You have created a second identity. You can also forge documents if you have seen an example of the kind of document.",
               "You were always in the right place to profit when someone else was gullible.",
               15),

            bg("criminal",     "Criminal",
               List.of("Deception","Stealth"),
               List.of("Thieves' tools","Gaming set (one type)"), 0,
               "A crowbar, dark common clothes with a hood, a belt pouch with 15 gp",
               "Criminal Contact",
               "You have a reliable contact who acts as your liaison to a network of criminals. You know how to get messages to your contact.",
               "You lived a life on the wrong side of the law.",
               15),

            bg("entertainer",  "Entertainer",
               List.of("Acrobatics","Performance"),
               List.of("Disguise kit","Musical instrument (one type)"), 0,
               "A musical instrument (one of your choice), the favor of an admirer, costume clothes, a belt pouch with 15 gp",
               "By Popular Demand",
               "You can always find a place to perform, usually in an inn or tavern, and receive free lodging and food there.",
               "You thrive in front of an audience.",
               15),

            bg("folk_hero",    "Folk Hero",
               List.of("Animal Handling","Survival"),
               List.of("Artisan's tools (one type)","Vehicles (land)"), 0,
               "A set of artisan's tools (one of your choice), a shovel, an iron pot, common clothes, a belt pouch with 10 gp",
               "Rustic Hospitality",
               "Common folk will shelter you from the law and your enemies, given they don't risk their lives doing so.",
               "You came from a humble background and are destined to be a hero.",
               10),

            bg("guild_artisan","Guild Artisan",
               List.of("Insight","Persuasion"),
               List.of("Artisan's tools (one type)"), 1,
               "A set of artisan's tools (one of your choice), a letter of introduction from your guild, traveler's clothes, a belt pouch with 15 gp",
               "Guild Membership",
               "Your guild provides you with lodgings and food, and helps find work in your trade. It also provides legal assistance.",
               "You are a member of a craft guild.",
               15),

            bg("hermit",       "Hermit",
               List.of("Medicine","Religion"),
               List.of("Herbalism kit"), 1,
               "A scroll case stuffed full of notes, a winter blanket, common clothes, an herbalism kit, 5 gp",
               "Discovery",
               "Your quiet seclusion produced a unique discovery, insight, or revelation that you keep secret.",
               "You lived in seclusion for years, contemplating life.",
               5),

            bg("noble",        "Noble",
               List.of("History","Persuasion"),
               List.of("Gaming set (one type)"), 1,
               "Fine clothes, a signet ring, a scroll of pedigree, a purse with 25 gp",
               "Position of Privilege",
               "You are welcome in high society and people assume you have the right to be wherever you are. Common folk accommodate you.",
               "You were born into aristocracy and understand its privileges.",
               25),

            bg("outlander",    "Outlander",
               List.of("Athletics","Survival"),
               List.of("Musical instrument (one type)"), 1,
               "A staff, a hunting trap, a trophy from an animal you killed, traveler's clothes, a belt pouch with 10 gp",
               "Wanderer",
               "You have an excellent memory for maps and geography, and can always recall the general layout of terrain around you.",
               "You grew up in the wild, far from civilization.",
               10),

            bg("sage",         "Sage",
               List.of("Arcana","History"),
               List.of(), 2,
               "A bottle of black ink, a quill, a small knife, a letter from a dead colleague posing a question you have not yet been able to answer, common clothes, a belt pouch with 10 gp",
               "Researcher",
               "When you attempt to learn or recall a piece of lore, if you do not know the information, you often know where and from whom you can obtain it.",
               "You spent years learning the lore of the multiverse.",
               10),

            bg("sailor",       "Sailor",
               List.of("Athletics","Perception"),
               List.of("Navigator's tools","Vehicles (water)"), 0,
               "A belaying pin (club), 50 feet of silk rope, a lucky charm, common clothes, a belt pouch with 10 gp",
               "Ship's Passage",
               "You can secure free passage on sailing ships for yourself and companions. You might have to help crew the ship occasionally.",
               "You sailed the seas for years.",
               10),

            bg("soldier",      "Soldier",
               List.of("Athletics","Intimidation"),
               List.of("Gaming set (one type)","Vehicles (land)"), 0,
               "An insignia of rank, a trophy taken from a fallen enemy, a set of bone dice or a deck of cards, common clothes, a belt pouch with 10 gp",
               "Military Rank",
               "You have a military rank from your career as a soldier. Soldiers loyal to your former military organization recognize your rank.",
               "You are a veteran of military service.",
               10),

            bg("urchin",       "Urchin",
               List.of("Sleight of Hand","Stealth"),
               List.of("Thieves' tools","Disguise kit"), 0,
               "A small knife, a map of the city you grew up in, a pet mouse, a token to remember your parents by, common clothes, a belt pouch with 10 gp",
               "City Secrets",
               "You know the secret patterns and flow of cities. You can find passages through a city that others would miss.",
               "You grew up on the streets of a city.",
               10)
        ));

        // ── SCAG Backgrounds ────────────────────────────────────────────────────────
        list.add(bg("city_watch",          "City Watch",
               List.of("Athletics","Insight"),
               List.of(), 2,
               "A uniform, a horn you can use to call for help, a set of manacles, a belt pouch with 10 gp",
               "Watcher's Eye",
               "Your experience in enforcing the law grants you the ability to find information about local criminals and their activities. You can find City Watch outposts and garrisons.",
               "You have served the community where you grew up.",
               10));
        list.add(bg("clan_crafter",        "Clan Crafter",
               List.of("History","Insight"),
               List.of("Artisan's tools (one type)"), 1,
               "A set of artisan's tools (one of your choice), a maker's mark chisel, a set of traveler's clothes, a pouch with 5 gp and a gem worth 10 gp",
               "Respect of the Stout Folk",
               "Dwarves and others who respect the craft will give you lodging and food among dwarven communities, and assist you in finding work.",
               "You are a member of an artisan community, steeped in the old ways of crafting.",
               15));
        list.add(bg("cloistered_scholar",  "Cloistered Scholar",
               List.of("History","Arcana"),
               List.of(), 2,
               "The scholar's robes of your cloister, a writing kit, a borrowed book on the subject of your current study, a belt pouch with 10 gp",
               "Library Access",
               "Though others must often endure extensive negotiations, you have free access to the library where you studied and most other libraries. Libraries may want you to share interesting lore.",
               "You spent years learning the lore of the multiverse in a great library.",
               10));
        list.add(bg("courtier",            "Courtier",
               List.of("Insight","Persuasion"),
               List.of(), 2,
               "A set of fine clothes and a belt pouch with 5 gp",
               "Court Functionary",
               "Your knowledge of how bureaucracies function lets you gain access to the records and inner workings of any noble court or government. You gain access to information almost all others are denied.",
               "Your ability to mediate conflict and understand the motivations of others was cultivated at the highest levels of society.",
               5));
        list.add(bg("far_traveler",        "Far Traveler",
               List.of("Insight","Perception"),
               List.of("Musical instrument (one type)"), 1,
               "One set of traveler's clothes, any one musical instrument or gaming set you are proficient with, a poorly drawn map from your homeland, a token from your homeland, a belt pouch with 5 gp",
               "All Eyes on You",
               "Your foreign origin attracts attention, making you memorable and sometimes opening doors as novelty. Merchants and scholars may seek you out for information about your homeland.",
               "You came from a distant land far beyond the Sword Coast.",
               5));
        list.add(bg("inheritor",           "Inheritor",
               List.of("Survival","History"),
               List.of("Gaming set (one type)"), 0,
               "A set of traveler's clothes, a signet, letter, or other mark of your inheritance, any trinket from your background, a belt pouch with 15 gp",
               "Inheritance",
               "Choose or randomly determine your inheritance from the Inheritor's Inheritance table. Work with your DM to decide its exact nature. Your inheritance may be sought by dangerous people.",
               "You are heir to something of great value — not just in coin but in meaning and responsibility.",
               15));
        list.add(bg("knight_of_the_order", "Knight of the Order",
               List.of("Persuasion","History"),
               List.of("Musical instrument (one type)"), 1,
               "A set of traveler's clothes, a signet, banner, or seal representing your place in the order, and a belt pouch with 10 gp",
               "Knightly Regard",
               "Those who share your order will extend you courtesy and temporary lodging. Your order may also provide tactical assistance if you face a major threat.",
               "You have sworn service to a knightly order, dedicating yourself to its ideals.",
               10));
        list.add(bg("mercenary_veteran",   "Mercenary Veteran",
               List.of("Athletics","Persuasion"),
               List.of("Gaming set (one type)","Vehicles (land)"), 0,
               "A uniform of your company (traveler's clothes in quality), an insignia of your rank, a gaming set of your choice, and a belt pouch with 10 gp",
               "Mercenary Life",
               "You know the mercenary life as only someone who has experienced it can. You are able to identify mercenary companies by their emblems and know how to find hiring halls.",
               "You served as a paid soldier in a mercenary company.",
               10));
        list.add(bg("urban_bounty_hunter", "Urban Bounty Hunter",
               List.of("Deception","Stealth"),
               List.of("Thieves' tools","Gaming set (one type)"), 0,
               "A set of clothes appropriate to your duties and a belt pouch with 20 gp",
               "Ear to the Ground",
               "You are in frequent contact with people in the segment of society that your quarry moves through. These people provide information about your targets.",
               "You tracked down criminals and other fugitives for pay in an urban environment.",
               20));
        list.add(bg("uthgardt_tribe_member","Uthgardt Tribe Member",
               List.of("Athletics","Survival"),
               List.of("Musical instrument (one type)"), 1,
               "A hunting trap, a totemic token or set of tattoos marking your tribal affiliation, a set of traveler's clothes, and a belt pouch with 10 gp",
               "Uthgardt Heritage",
               "You have an excellent knowledge of the Northern lands. You can find twice the normal food and water for yourself and up to 5 others per day. Uthgardt communities will give you shelter.",
               "You are a member of one of the Uthgardt barbarian tribes of the North.",
               10));
        list.add(bg("waterdhavian_noble",  "Waterdhavian Noble",
               List.of("History","Persuasion"),
               List.of("Gaming set (one type)"), 1,
               "A set of fine clothes, a signet ring or brooch, a scroll of pedigree, a skin of fine zzar or wine, a belt pouch with 20 gp",
               "Kept in Style",
               "While in Waterdeep or elsewhere in the North, your house pays your expenses. This covers a comfortable lifestyle. Your connections can provide access to noble estates and private clubs.",
               "You are a member of one of the noble families of Waterdeep.",
               20));

        return list;
    }

    private BackgroundDefinition bg(String id, String name,
                                    List<String> skills, List<String> tools,
                                    int langs, String equipment,
                                    String featureName, String featureDesc,
                                    String personality, int gold) {
        var b = new BackgroundDefinition();
        b.setId(id); b.setName(name);
        b.setSkillProficiencies(skills); b.setToolProficiencies(tools);
        b.setBonusLanguages(langs); b.setEquipment(equipment);
        b.setFeatureName(featureName); b.setFeatureDesc(featureDesc);
        b.setPersonality(personality); b.setGold(gold);
        return b;
    }
}
