package com.dnd.builder.service;

import com.dnd.builder.model.BackgroundDefinition;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BackgroundRegistry {

    private final List<BackgroundDefinition> backgrounds;
    private final Map<String, BackgroundDefinition> byId;

    public BackgroundRegistry() {
        backgrounds = buildBackgrounds();
        byId = new HashMap<>();
        backgrounds.forEach(b -> byId.put(b.getId(), b));
    }

    public List<BackgroundDefinition> getAllBackgrounds() { return Collections.unmodifiableList(backgrounds); }
    public BackgroundDefinition getById(String id)        { return byId.get(id); }

    private List<BackgroundDefinition> buildBackgrounds() {
        return List.of(
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
        );
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
