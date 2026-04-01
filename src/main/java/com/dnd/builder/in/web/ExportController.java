package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
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

@Controller
public class ExportController {

    private final ObjectMapper objectMapper;

    public ExportController() { this.objectMapper = new ObjectMapper(); }

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
