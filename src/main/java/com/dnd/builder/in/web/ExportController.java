package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.model.ClassFeature;
import com.dnd.builder.core.port.out.*;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.core.service.PdfExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ExportController {

    private final ObjectMapper objectMapper;
    private final CharacterCalculator calculator;
    private final PdfExportService pdfService;
    private final RaceRepository raceRepository;
    private final ClassRepository classRepository;
    private final BackgroundRepository backgroundRepository;
    private final SpellRepository spellRepository;

    public ExportController(CharacterCalculator calculator, PdfExportService pdfService,
                            RaceRepository raceRepository, ClassRepository classRepository,
                            BackgroundRepository backgroundRepository, SpellRepository spellRepository) {
        this.objectMapper = new ObjectMapper();
        this.calculator = calculator;
        this.pdfService = pdfService;
        this.raceRepository = raceRepository;
        this.classRepository = classRepository;
        this.backgroundRepository = backgroundRepository;
        this.spellRepository = spellRepository;
    }

    /**
     * Download current draft as a JSON file.
     * The filename uses the character's name or a fallback.
     */
    @GetMapping("/export")
    public void exportJson(HttpSession session, HttpServletResponse response) throws IOException {
        CharacterDraft draft = getDraft(session);
        String name = draft.getCharacterName();
        if (name == null || name.isBlank()) name = "character";

        String filename = URLEncoder.encode(name.replaceAll("[^a-zA-Z0-9_\\-]", "_"), StandardCharsets.UTF_8)
                        + "_dnd5e.json";

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(response.getWriter(), draft);
    }

    /**
     * Download current draft as a PDF character sheet.
     */
    @GetMapping("/export/pdf")
    public void exportPdf(HttpSession session, HttpServletResponse response) throws Exception {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);

        // Get display names
        var race = raceRepository.findById(draft.getRaceId());
        var cls = classRepository.findById(draft.getCharacterClass());
        var bg = backgroundRepository.findById(draft.getBackground());

        String raceName = race != null ? race.name() : "Unknown";
        String className = cls != null ? cls.getName() : "Unknown";
        String bgName = bg != null ? bg.getName() : "Unknown";

        // Gather spells
        List<String> spellNames = new ArrayList<>();
        for (var id : draft.getChosenCantrips()) {
            var sp = spellRepository.findById(id);
            if (sp != null) spellNames.add(sp.getName() + " (cantrip)");
        }
        for (var id : draft.getChosenSpells()) {
            var sp = spellRepository.findById(id);
            if (sp != null) spellNames.add(sp.getName());
        }
        for (var id : draft.getSpellbookSpells()) {
            var sp = spellRepository.findById(id);
            if (sp != null) spellNames.add(sp.getName() + " (book)");
        }

        // Gather features
        List<ClassFeature> features = null;
        if (cls != null && cls.getFeatures() != null) {
            features = cls.getFeatures().stream()
                    .filter(f -> f.level() <= draft.getLevel())
                    .toList();
        }

        // Generate PDF
        byte[] pdfBytes = pdfService.generatePdf(draft, derived, raceName, className, bgName, spellNames, features);

        // Set response
        String name = draft.getCharacterName();
        if (name == null || name.isBlank()) name = "character";
        String filename = URLEncoder.encode(name.replaceAll("[^a-zA-Z0-9_\\-]", "_"), StandardCharsets.UTF_8)
                + "_sheet.pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
    }

    /**
     * Import a previously exported JSON file.
     * Replaces session draft with the uploaded data.
     */
    @PostMapping("/import")
    public String importJson(@RequestParam("file") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("importError", "No file selected.");
            return "redirect:/step/9";
        }
        try {
            CharacterDraft imported = objectMapper.readValue(file.getInputStream(), CharacterDraft.class);
            // Ensure highestStepReached allows full review
            imported.setHighestStepReached(10);
            session.setAttribute(CharacterBuilderController.DRAFT_KEY, imported);
            ra.addFlashAttribute("importSuccess", "Character \"" + safeLabel(imported.getCharacterName()) + "\" imported successfully.");
            return "redirect:/step/9";
        } catch (IOException e) {
            ra.addFlashAttribute("importError", "Could not parse file: " + e.getMessage());
            return "redirect:/step/9";
        }
    }

    private CharacterDraft getDraft(HttpSession session) {
        CharacterDraft draft = (CharacterDraft) session.getAttribute(CharacterBuilderController.DRAFT_KEY);
        return draft != null ? draft : CharacterDraft.fresh();
    }

    private String safeLabel(String name) {
        return (name == null || name.isBlank()) ? "Unnamed Character" : name;
    }
}
