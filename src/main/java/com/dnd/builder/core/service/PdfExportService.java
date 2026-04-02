package com.dnd.builder.core.service;

import com.dnd.builder.core.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Generates PDF character sheets styled like official D&D 5e sheets.
 */
@Service
public class PdfExportService {

    private static final Color GOLD = new Color(201, 164, 64);
    private static final Color DARK_BG = new Color(37, 29, 14);
    private static final Color CREAM = new Color(245, 240, 220);

    public byte[] generatePdf(CharacterDraft draft, DerivedStats derived,
                              String raceName, String className, String bgName,
                              List<String> spellNames, List<ClassFeature> features) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, GOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, GOLD);
        Font labelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(180, 160, 120));
        Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
        Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
        Font statFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
        Font modFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

        // Title
        String charName = draft.getCharacterName() != null && !draft.getCharacterName().isEmpty()
                ? draft.getCharacterName() : "Character";
        Paragraph title = new Paragraph(charName, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Subtitle
        String subtitle = String.format("%s %s %d", raceName, className, draft.getLevel());
        Paragraph sub = new Paragraph(subtitle, headerFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(20);
        document.add(sub);

        // Core info table
        PdfPTable coreTable = new PdfPTable(5);
        coreTable.setWidthPercentage(100);
        coreTable.setSpacingAfter(20);
        addCoreCell(coreTable, "Background", bgName, labelFont, valueFont);
        addCoreCell(coreTable, "Alignment", draft.getAlignment().isEmpty() ? "—" : draft.getAlignment(), labelFont, valueFont);
        addCoreCell(coreTable, "Prof Bonus", "+" + derived.getProficiencyBonus(), labelFont, valueFont);
        addCoreCell(coreTable, "AC", String.valueOf(derived.getArmorClass()), labelFont, valueFont);
        addCoreCell(coreTable, "Speed", derived.getSpeed() + " ft", labelFont, valueFont);
        document.add(coreTable);

        // Ability Scores
        Paragraph statsHeader = new Paragraph("Ability Scores", headerFont);
        statsHeader.setSpacingAfter(10);
        document.add(statsHeader);

        PdfPTable statsTable = new PdfPTable(6);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(20);

        for (String stat : List.of("STR", "DEX", "CON", "INT", "WIS", "CHA")) {
            int score = derived.getFinalScores().get(stat);
            int mod = derived.getModifiers().get(stat);
            String modStr = (mod >= 0 ? "+" : "") + mod;

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.BOX);
            cell.setBorderColor(GOLD);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph statPara = new Paragraph();
            statPara.setAlignment(Element.ALIGN_CENTER);
            statPara.add(new Chunk(stat + "\n", labelFont));
            statPara.add(new Chunk(String.valueOf(score) + "\n", statFont));
            statPara.add(new Chunk(modStr, modFont));
            cell.addElement(statPara);
            statsTable.addCell(cell);
        }
        document.add(statsTable);

        // Combat Stats
        PdfPTable combatTable = new PdfPTable(4);
        combatTable.setWidthPercentage(100);
        combatTable.setSpacingAfter(20);
        addCoreCell(combatTable, "Max HP", String.valueOf(derived.getMaxHitPoints()), labelFont, valueFont);
        addCoreCell(combatTable, "Hit Dice", draft.getLevel() + "d" + derived.getHitDice(), labelFont, valueFont);
        addCoreCell(combatTable, "Initiative", (derived.getInitiative() >= 0 ? "+" : "") + derived.getInitiative(), labelFont, valueFont);
        addCoreCell(combatTable, "Passive Perception", String.valueOf(derived.getPassivePerception()), labelFont, valueFont);
        document.add(combatTable);

        // Saving Throws
        Paragraph savesHeader = new Paragraph("Saving Throws", headerFont);
        savesHeader.setSpacingAfter(10);
        document.add(savesHeader);

        StringBuilder savesText = new StringBuilder();
        for (String stat : List.of("STR", "DEX", "CON", "INT", "WIS", "CHA")) {
            int val = derived.getSavingThrows().get(stat);
            boolean prof = derived.getSavingThrowProficiencies().contains(stat);
            savesText.append(stat).append(": ").append(val >= 0 ? "+" : "").append(val);
            if (prof) savesText.append(" *");
            savesText.append("   ");
        }
        Paragraph saves = new Paragraph(savesText.toString(), bodyFont);
        saves.setSpacingAfter(15);
        document.add(saves);

        // Skills
        Paragraph skillsHeader = new Paragraph("Skills", headerFont);
        skillsHeader.setSpacingAfter(10);
        document.add(skillsHeader);

        PdfPTable skillsTable = new PdfPTable(3);
        skillsTable.setWidthPercentage(100);
        skillsTable.setSpacingAfter(20);

        var skills = derived.getSkillBonuses();
        var profSkills = derived.getAllSkillProficiencies();
        int count = 0;
        for (var entry : skills.entrySet()) {
            String skill = entry.getKey();
            int bonus = entry.getValue();
            boolean prof = profSkills.contains(skill);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(3);

            String text = (prof ? "* " : "  ") + skill + ": " + (bonus >= 0 ? "+" : "") + bonus;
            cell.addElement(new Paragraph(text, bodyFont));
            skillsTable.addCell(cell);
            count++;
        }
        // Fill remaining cells
        while (count % 3 != 0) {
            PdfPCell empty = new PdfPCell();
            empty.setBorder(Rectangle.NO_BORDER);
            skillsTable.addCell(empty);
            count++;
        }
        document.add(skillsTable);

        // Proficiencies
        Paragraph profsHeader = new Paragraph("Proficiencies", headerFont);
        profsHeader.setSpacingAfter(10);
        document.add(profsHeader);

        if (derived.getArmorProficiencies() != null && !derived.getArmorProficiencies().isEmpty()) {
            document.add(new Paragraph("Armor: " + String.join(", ", derived.getArmorProficiencies()), bodyFont));
        }
        if (derived.getWeaponProficiencies() != null && !derived.getWeaponProficiencies().isEmpty()) {
            document.add(new Paragraph("Weapons: " + String.join(", ", derived.getWeaponProficiencies()), bodyFont));
        }
        if (derived.getToolProficiencies() != null && !derived.getToolProficiencies().isEmpty()) {
            document.add(new Paragraph("Tools: " + String.join(", ", derived.getToolProficiencies()), bodyFont));
        }
        if (derived.getLanguages() != null && !derived.getLanguages().isEmpty()) {
            document.add(new Paragraph("Languages: " + String.join(", ", derived.getLanguages()), bodyFont));
        }
        document.add(Chunk.NEWLINE);

        // Spellcasting (if applicable)
        if (derived.isSpellcaster()) {
            Paragraph spellHeader = new Paragraph("Spellcasting", headerFont);
            spellHeader.setSpacingAfter(10);
            document.add(spellHeader);

            String spellInfo = String.format("Ability: %s | Save DC: %d | Attack: +%d",
                    derived.getSpellcastingAbility(), derived.getSpellSaveDC(), derived.getSpellAttackBonus());
            document.add(new Paragraph(spellInfo, bodyFont));
            document.add(new Paragraph("Slots: " + derived.getSpellSlotSummary(), bodyFont));

            if (spellNames != null && !spellNames.isEmpty()) {
                document.add(new Paragraph("Spells: " + String.join(", ", spellNames), bodyFont));
            }
            document.add(Chunk.NEWLINE);
        }

        // Features
        if (features != null && !features.isEmpty()) {
            document.add(new Paragraph("Class Features", headerFont));
            for (ClassFeature feature : features) {
                Paragraph fp = new Paragraph();
                fp.add(new Chunk("Level " + feature.level() + " - " + feature.name() + ": ", labelFont));
                fp.add(new Chunk(feature.description(), bodyFont));
                document.add(fp);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addCoreCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(GOLD);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph para = new Paragraph();
        para.setAlignment(Element.ALIGN_CENTER);
        para.add(new Chunk(label + "\n", labelFont));
        para.add(new Chunk(value, valueFont));
        cell.addElement(para);
        table.addCell(cell);
    }
}
