package com.dnd.builder.service;

import com.dnd.builder.model.BackgroundDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BackgroundRegistry verifying D&D 5e 2014 PHB background accuracy.
 */
class BackgroundRegistryTest {

    private BackgroundRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BackgroundRegistry();
    }

    @Test
    @DisplayName("All 13 PHB backgrounds are present")
    void allBackgroundsPresent() {
        assertEquals(13, registry.getAllBackgrounds().size());
    }

    @Test
    @DisplayName("Acolyte has Insight and Religion skills")
    void acolyteSkills() {
        BackgroundDefinition bg = registry.getById("acolyte");
        assertNotNull(bg);
        assertEquals(List.of("Insight", "Religion"), bg.getSkillProficiencies());
        assertEquals(2, bg.getBonusLanguages());
    }

    @Test
    @DisplayName("Charlatan has Deception and Sleight of Hand skills")
    void charlatanSkills() {
        BackgroundDefinition bg = registry.getById("charlatan");
        assertNotNull(bg);
        assertEquals(List.of("Deception", "Sleight of Hand"), bg.getSkillProficiencies());
        assertTrue(bg.getToolProficiencies().contains("Disguise kit"));
        assertTrue(bg.getToolProficiencies().contains("Forgery kit"));
    }

    @Test
    @DisplayName("Criminal has Deception and Stealth skills")
    void criminalSkills() {
        BackgroundDefinition bg = registry.getById("criminal");
        assertNotNull(bg);
        assertEquals(List.of("Deception", "Stealth"), bg.getSkillProficiencies());
        assertTrue(bg.getToolProficiencies().contains("Thieves' tools"));
    }

    @Test
    @DisplayName("Entertainer has Acrobatics and Performance skills")
    void entertainerSkills() {
        BackgroundDefinition bg = registry.getById("entertainer");
        assertNotNull(bg);
        assertEquals(List.of("Acrobatics", "Performance"), bg.getSkillProficiencies());
    }

    @Test
    @DisplayName("Folk Hero has Animal Handling and Survival skills")
    void folkHeroSkills() {
        BackgroundDefinition bg = registry.getById("folk_hero");
        assertNotNull(bg);
        assertEquals(List.of("Animal Handling", "Survival"), bg.getSkillProficiencies());
    }

    @Test
    @DisplayName("Guild Artisan has Insight and Persuasion skills")
    void guildArtisanSkills() {
        BackgroundDefinition bg = registry.getById("guild_artisan");
        assertNotNull(bg);
        assertEquals(List.of("Insight", "Persuasion"), bg.getSkillProficiencies());
        assertEquals(1, bg.getBonusLanguages());
    }

    @Test
    @DisplayName("Hermit has Medicine and Religion skills")
    void hermitSkills() {
        BackgroundDefinition bg = registry.getById("hermit");
        assertNotNull(bg);
        assertEquals(List.of("Medicine", "Religion"), bg.getSkillProficiencies());
        assertTrue(bg.getToolProficiencies().contains("Herbalism kit"));
    }

    @Test
    @DisplayName("Noble has History and Persuasion skills")
    void nobleSkills() {
        BackgroundDefinition bg = registry.getById("noble");
        assertNotNull(bg);
        assertEquals(List.of("History", "Persuasion"), bg.getSkillProficiencies());
    }

    @Test
    @DisplayName("Outlander has Athletics and Survival skills")
    void outlanderSkills() {
        BackgroundDefinition bg = registry.getById("outlander");
        assertNotNull(bg);
        assertEquals(List.of("Athletics", "Survival"), bg.getSkillProficiencies());
    }

    @Test
    @DisplayName("Sage has Arcana and History skills")
    void sageSkills() {
        BackgroundDefinition bg = registry.getById("sage");
        assertNotNull(bg);
        assertEquals(List.of("Arcana", "History"), bg.getSkillProficiencies());
        assertEquals(2, bg.getBonusLanguages());
    }

    @Test
    @DisplayName("Sailor has Athletics and Perception skills")
    void sailorSkills() {
        BackgroundDefinition bg = registry.getById("sailor");
        assertNotNull(bg);
        assertEquals(List.of("Athletics", "Perception"), bg.getSkillProficiencies());
        assertTrue(bg.getToolProficiencies().contains("Navigator's tools"));
    }

    @Test
    @DisplayName("Soldier has Athletics and Intimidation skills")
    void soldierSkills() {
        BackgroundDefinition bg = registry.getById("soldier");
        assertNotNull(bg);
        assertEquals(List.of("Athletics", "Intimidation"), bg.getSkillProficiencies());
    }

    @Test
    @DisplayName("Urchin has Sleight of Hand and Stealth skills")
    void urchinSkills() {
        BackgroundDefinition bg = registry.getById("urchin");
        assertNotNull(bg);
        assertEquals(List.of("Sleight of Hand", "Stealth"), bg.getSkillProficiencies());
        assertTrue(bg.getToolProficiencies().contains("Thieves' tools"));
        assertTrue(bg.getToolProficiencies().contains("Disguise kit"));
    }

    @Test
    @DisplayName("All backgrounds have a feature name and description")
    void backgroundFeatures() {
        for (BackgroundDefinition bg : registry.getAllBackgrounds()) {
            assertNotNull(bg.getFeatureName(), bg.getName() + " should have a feature name");
            assertFalse(bg.getFeatureName().isEmpty(), bg.getName() + " feature name should not be empty");
            assertNotNull(bg.getFeatureDesc(), bg.getName() + " should have a feature description");
            assertFalse(bg.getFeatureDesc().isEmpty(), bg.getName() + " feature description should not be empty");
        }
    }

    @Test
    @DisplayName("All backgrounds have equipment")
    void backgroundEquipment() {
        for (BackgroundDefinition bg : registry.getAllBackgrounds()) {
            assertNotNull(bg.getEquipment(), bg.getName() + " should have equipment");
            assertFalse(bg.getEquipment().isEmpty(), bg.getName() + " equipment should not be empty");
        }
    }

    @Test
    @DisplayName("All backgrounds have starting gold")
    void backgroundGold() {
        for (BackgroundDefinition bg : registry.getAllBackgrounds()) {
            assertTrue(bg.getGold() > 0, bg.getName() + " should have starting gold");
        }
    }
}
