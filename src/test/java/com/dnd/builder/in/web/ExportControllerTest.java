package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.core.service.PdfExportService;
import com.dnd.builder.out.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.nio.charset.StandardCharsets;

import static com.dnd.builder.in.web.CharacterBuilderController.DRAFT_KEY;
import static org.junit.jupiter.api.Assertions.*;

class ExportControllerTest {

    private ExportController controller;
    private ObjectMapper objectMapper;
    private MockHttpSession session;
    private CharacterDraft draft;
    private CharacterCalculator calculator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        var raceRepo = new InMemoryRaceRepository();
        var classRepo = new InMemoryClassRepository();
        var bgRepo = new InMemoryBackgroundRepository();
        var spellRepo = new InMemorySpellRepository();
        var eqRepo = new InMemoryEquipmentRepository();
        calculator = new CharacterCalculator(raceRepo, classRepo, bgRepo, spellRepo, eqRepo);

        controller = new ExportController(
                objectMapper, calculator, new PdfExportService(),
                raceRepo, classRepo, bgRepo, spellRepo
        );

        draft = CharacterDraft.fresh();
        draft.setCharacterName("Aria");
        draft.setCharacterClass("fighter");
        draft.setRaceId("human_standard");
        draft.setLevel(3);

        session = new MockHttpSession();
        session.setAttribute(DRAFT_KEY, draft);
    }

    @Nested
    @DisplayName("JSON export")
    class JsonExport {

        @Test
        @DisplayName("exportJson() sets Content-Disposition with character name")
        void exportJsonUsesCharacterName() throws Exception {
            var response = new MockHttpServletResponse();

            controller.exportJson(session, response);

            assertEquals("application/json", response.getContentType());
            assertTrue(response.getHeader("Content-Disposition").contains("Aria"));
        }

        @Test
        @DisplayName("exportJson() falls back to 'character' when name is blank")
        void exportJsonFallbackName() throws Exception {
            draft.setCharacterName("");
            var response = new MockHttpServletResponse();

            controller.exportJson(session, response);

            assertTrue(response.getHeader("Content-Disposition").contains("character"));
        }

        @Test
        @DisplayName("exportJson() without session draft exports a fresh draft")
        void exportJsonNoSession() throws Exception {
            var response = new MockHttpServletResponse();

            controller.exportJson(new MockHttpSession(), response);

            assertEquals("application/json", response.getContentType());
            assertNotNull(response.getContentAsString());
        }

        @Test
        @DisplayName("exportJson() output is valid JSON containing the character name")
        void exportJsonIsValidJson() throws Exception {
            var response = new MockHttpServletResponse();

            controller.exportJson(session, response);

            String body = response.getContentAsString();
            CharacterDraft parsed = objectMapper.readValue(body, CharacterDraft.class);
            assertEquals("Aria", parsed.getCharacterName());
        }
    }

    @Nested
    @DisplayName("JSON import")
    class JsonImport {

        @Test
        @DisplayName("importJson() loads valid JSON and sets session draft")
        void importJsonValidFile() throws Exception {
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "aria.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            String result = controller.importJson(file, session, ra);

            assertEquals("redirect:/step/9", result);
            assertNotNull(ra.getFlashAttributes().get("importSuccess"));
            CharacterDraft loaded = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals("Aria", loaded.getCharacterName());
        }

        @Test
        @DisplayName("importJson() rejects empty file")
        void importJsonEmptyFile() throws Exception {
            var file = new MockMultipartFile("file", "empty.json", "application/json", new byte[0]);
            var ra = new RedirectAttributesModelMap();

            String result = controller.importJson(file, session, ra);

            assertEquals("redirect:/step/9", result);
            assertNotNull(ra.getFlashAttributes().get("importError"));
            assertTrue(ra.getFlashAttributes().get("importError").toString().contains("No file selected"));
        }

        @Test
        @DisplayName("importJson() rejects non-JSON content type")
        void importJsonWrongContentType() throws Exception {
            var file = new MockMultipartFile("file", "data.csv", "text/csv", "a,b,c".getBytes());
            var ra = new RedirectAttributesModelMap();

            String result = controller.importJson(file, session, ra);

            assertEquals("redirect:/step/9", result);
            assertNotNull(ra.getFlashAttributes().get("importError"));
        }

        @Test
        @DisplayName("importJson() rejects malformed JSON")
        void importJsonMalformedJson() throws Exception {
            var file = new MockMultipartFile("file", "bad.json", "application/json",
                    "not-valid-json".getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            String result = controller.importJson(file, session, ra);

            assertEquals("redirect:/step/9", result);
            String error = ra.getFlashAttributes().get("importError").toString();
            assertFalse(error.contains("Exception"), "Error must not leak exception class");
            assertFalse(error.contains("JsonParseException"), "Error must not leak exception class");
            assertTrue(error.contains("invalid character data format"));
        }

        @Test
        @DisplayName("importJson() clamps level to 1-20")
        void importJsonClampsLevel() throws Exception {
            draft.setLevel(99);
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "aria.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            controller.importJson(file, session, ra);

            CharacterDraft loaded = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(20, loaded.getLevel());
        }

        @Test
        @DisplayName("importJson() clamps negative currentHp to -1")
        void importJsonClampsCurrentHp() throws Exception {
            draft.setCurrentHp(-999);
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "aria.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            controller.importJson(file, session, ra);

            CharacterDraft loaded = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(-1, loaded.getCurrentHp());
        }

        @Test
        @DisplayName("importJson() strips XSS characters from characterName")
        void importJsonSanitizesName() throws Exception {
            draft.setCharacterName("<script>Aria</script>");
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "aria.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            controller.importJson(file, session, ra);

            CharacterDraft loaded = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertFalse(loaded.getCharacterName().contains("<"));
            assertFalse(loaded.getCharacterName().contains(">"));
        }

        @Test
        @DisplayName("importJson() sets highestStepReached to 10 (review page accessible)")
        void importJsonSetsHighestStep() throws Exception {
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "aria.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            controller.importJson(file, session, ra);

            CharacterDraft loaded = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(10, loaded.getHighestStepReached());
        }

        @Test
        @DisplayName("importJson() success message uses 'Unnamed Character' for blank name")
        void importJsonUnnamedCharacter() throws Exception {
            draft.setCharacterName("");
            String json = objectMapper.writeValueAsString(draft);
            var file = new MockMultipartFile("file", "char.json", "application/json",
                    json.getBytes(StandardCharsets.UTF_8));
            var ra = new RedirectAttributesModelMap();

            controller.importJson(file, session, ra);

            String msg = ra.getFlashAttributes().get("importSuccess").toString();
            assertTrue(msg.contains("Unnamed Character"));
        }
    }
}
