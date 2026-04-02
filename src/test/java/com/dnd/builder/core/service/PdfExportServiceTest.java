package com.dnd.builder.core.service;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.model.ClassFeature;
import com.dnd.builder.core.model.DerivedStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PdfExportService verifying PDF generation.
 */
class PdfExportServiceTest {

    private PdfExportService service;
    private CharacterDraft draft;
    private DerivedStats derived;

    @BeforeEach
    void setUp() {
        service = new PdfExportService();
        draft = createTestDraft();
        derived = createTestDerivedStats();
    }

    @Test
    @DisplayName("generatePdf produces non-empty PDF bytes")
    void generatePdfProducesBytes() throws Exception {
        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                List.of("Shield of Faith"),
                List.of(ClassFeature.of(1, "Fighting Style", "Choose a fighting style"),
                        ClassFeature.of(1, "Second Wind", "Regain HP as a bonus action"))
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF should have content");
    }

    @Test
    @DisplayName("PDF starts with correct PDF header")
    void pdfStartsWithHeader() throws Exception {
        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                null, null
        );

        // PDF files start with %PDF-
        String header = new String(pdf, 0, 5);
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("generatePdf handles null spell names")
    void handlesNullSpells() throws Exception {
        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                null, null
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf handles null features")
    void handlesNullFeatures() throws Exception {
        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                List.of("Fireball", "Shield"),
                null
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf handles empty character name")
    void handlesEmptyCharacterName() throws Exception {
        draft.setCharacterName("");

        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                null, null
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf handles null character name")
    void handlesNullCharacterName() throws Exception {
        draft.setCharacterName(null);

        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                null, null
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf includes spellcasting info for casters")
    void includesSpellcastingInfo() throws Exception {
        derived.setSpellcaster(true);
        derived.setSpellcastingAbility("INT");
        derived.setSpellSaveDC(15);
        derived.setSpellAttackBonus(7);
        derived.setSpellSlotSummary("4 × 1st, 3 × 2nd, 2 × 3rd");

        byte[] pdf = service.generatePdf(
                draft, derived,
                "Elf", "Wizard", "Sage",
                List.of("Fireball", "Lightning Bolt", "Shield"),
                List.of(ClassFeature.of(1, "Arcane Recovery", "Recover spell slots"))
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf handles character with many features")
    void handlesManyFeatures() throws Exception {
        List<ClassFeature> manyFeatures = List.of(
                ClassFeature.of(1, "Fighting Style", "Choose a fighting style"),
                ClassFeature.of(1, "Second Wind", "Regain HP as a bonus action"),
                ClassFeature.of(2, "Action Surge", "Take an additional action"),
                ClassFeature.of(3, "Martial Archetype", "Choose Champion, Battle Master, etc."),
                ClassFeature.of(5, "Extra Attack", "Attack twice on your turn"),
                ClassFeature.of(9, "Indomitable", "Reroll a failed saving throw")
        );

        byte[] pdf = service.generatePdf(
                draft, derived,
                "Human", "Fighter", "Soldier",
                null, manyFeatures
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("generatePdf handles character with many spells")
    void handlesManySpells() throws Exception {
        derived.setSpellcaster(true);
        derived.setSpellcastingAbility("INT");
        derived.setSpellSaveDC(17);
        derived.setSpellAttackBonus(9);
        derived.setSpellSlotSummary("4 × 1st, 3 × 2nd, 3 × 3rd, 3 × 4th, 2 × 5th");

        List<String> manySpells = List.of(
                "Fireball", "Lightning Bolt", "Shield", "Magic Missile",
                "Counterspell", "Fly", "Haste", "Polymorph",
                "Cone of Cold", "Wall of Force"
        );

        byte[] pdf = service.generatePdf(
                draft, derived,
                "Elf", "Wizard", "Sage",
                manySpells,
                List.of(ClassFeature.of(1, "Arcane Recovery", "Recover spell slots"))
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    private CharacterDraft createTestDraft() {
        CharacterDraft d = CharacterDraft.fresh();
        d.setCharacterName("Test Hero");
        d.setRaceId("human_standard");
        d.setCharacterClass("fighter");
        d.setLevel(5);
        d.setAlignment("Lawful Good");
        d.setBackground("soldier");
        d.setBaseScores(Map.of("STR", 16, "DEX", 14, "CON", 14, "INT", 10, "WIS", 12, "CHA", 10));
        return d;
    }

    private DerivedStats createTestDerivedStats() {
        DerivedStats stats = new DerivedStats();

        // Final scores (using TreeMap for consistent ordering)
        Map<String, Integer> finalScores = new TreeMap<>();
        finalScores.put("STR", 17);
        finalScores.put("DEX", 15);
        finalScores.put("CON", 15);
        finalScores.put("INT", 11);
        finalScores.put("WIS", 13);
        finalScores.put("CHA", 11);
        stats.setFinalScores(finalScores);

        // Modifiers
        Map<String, Integer> modifiers = new TreeMap<>();
        modifiers.put("STR", 3);
        modifiers.put("DEX", 2);
        modifiers.put("CON", 2);
        modifiers.put("INT", 0);
        modifiers.put("WIS", 1);
        modifiers.put("CHA", 0);
        stats.setModifiers(modifiers);

        stats.setProficiencyBonus(3);
        stats.setInitiative(2);
        stats.setPassivePerception(11);
        stats.setSpeed(30);
        stats.setArmorClass(18);
        stats.setMaxHitPoints(44);
        stats.setHitDice(10);

        // Saving throws
        Map<String, Integer> savingThrows = new TreeMap<>();
        savingThrows.put("STR", 6);  // prof
        savingThrows.put("DEX", 2);
        savingThrows.put("CON", 5);  // prof
        savingThrows.put("INT", 0);
        savingThrows.put("WIS", 1);
        savingThrows.put("CHA", 0);
        stats.setSavingThrows(savingThrows);
        stats.setSavingThrowProficiencies(List.of("STR", "CON"));

        // Skills (using TreeMap for consistent ordering)
        Map<String, Integer> skillBonuses = new TreeMap<>();
        skillBonuses.put("Acrobatics", 2);
        skillBonuses.put("Animal Handling", 1);
        skillBonuses.put("Arcana", 0);
        skillBonuses.put("Athletics", 6);  // prof
        skillBonuses.put("Deception", 0);
        skillBonuses.put("History", 0);
        skillBonuses.put("Insight", 1);
        skillBonuses.put("Intimidation", 3);  // prof
        skillBonuses.put("Investigation", 0);
        skillBonuses.put("Medicine", 1);
        skillBonuses.put("Nature", 0);
        skillBonuses.put("Perception", 1);
        skillBonuses.put("Performance", 0);
        skillBonuses.put("Persuasion", 0);
        skillBonuses.put("Religion", 0);
        skillBonuses.put("Sleight of Hand", 2);
        skillBonuses.put("Stealth", 2);
        skillBonuses.put("Survival", 1);
        stats.setSkillBonuses(skillBonuses);
        stats.setAllSkillProficiencies(List.of("Athletics", "Intimidation"));

        stats.setSpellcaster(false);

        stats.setArmorProficiencies(List.of("Light", "Medium", "Heavy", "Shields"));
        stats.setWeaponProficiencies(List.of("Simple", "Martial"));
        stats.setToolProficiencies(List.of());
        stats.setLanguages(List.of("Common"));

        return stats;
    }
}
