package com.dnd.builder.in.web;

import com.dnd.builder.core.model.CharacterDraft;
import com.dnd.builder.core.service.CharacterCalculator;
import com.dnd.builder.out.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import java.util.Map;

import static com.dnd.builder.in.web.CharacterBuilderController.DRAFT_KEY;
import static org.junit.jupiter.api.Assertions.*;

class CharacterBuilderControllerTest {

    private CharacterBuilderController controller;
    private MockHttpSession session;
    private CharacterCalculator calculator;

    @BeforeEach
    void setUp() {
        var raceRepo = new InMemoryRaceRepository();
        var classRepo = new InMemoryClassRepository();
        var bgRepo = new InMemoryBackgroundRepository();
        var spellRepo = new InMemorySpellRepository();
        var eqRepo = new InMemoryEquipmentRepository();
        var featRepo = new InMemoryFeatRepository();
        calculator = new CharacterCalculator(raceRepo, classRepo, bgRepo, spellRepo, eqRepo);
        controller = new CharacterBuilderController(
                raceRepo, classRepo, bgRepo, spellRepo, eqRepo, featRepo,
                calculator, new ObjectMapper()
        );
        session = new MockHttpSession();
    }

    @Nested
    @DisplayName("Navigation")
    class Navigation {

        @Test
        @DisplayName("home() redirects to /characters")
        void homeRedirects() {
            assertEquals("redirect:/characters", controller.home());
        }

        @Test
        @DisplayName("newCharacterGet() creates fresh draft and redirects to /step/1")
        void newCharacterGetCreatesDraft() {
            String result = controller.newCharacterGet(session);

            assertEquals("redirect:/step/1", result);
            assertNotNull(session.getAttribute(DRAFT_KEY));
        }

        @Test
        @DisplayName("newCharacter() POST creates fresh draft and redirects to /step/1")
        void newCharacterPostCreatesDraft() {
            String result = controller.newCharacter(session);

            assertEquals("redirect:/step/1", result);
            assertNotNull(session.getAttribute(DRAFT_KEY));
        }

        @Test
        @DisplayName("characterList() returns characters view")
        void characterListView() {
            assertEquals("characters", controller.characterList());
        }
    }

    @Nested
    @DisplayName("Character session management")
    class SessionManagement {

        @Test
        @DisplayName("loadCharacter() stores draft in session and returns success")
        void loadCharacterStoresInSession() {
            CharacterDraft draft = CharacterDraft.fresh();
            draft.setCharacterName("Aria");
            draft.setLevel(5);

            Map<String, Object> result = controller.loadCharacter(draft, session);

            assertEquals(true, result.get("success"));
            assertSame(draft, session.getAttribute(DRAFT_KEY));
        }

        @Test
        @DisplayName("getCurrentCharacter() returns exists=false with no draft")
        void getCurrentCharacterNoSession() {
            Map<String, Object> result = controller.getCurrentCharacter(session);

            assertEquals(false, result.get("exists"));
        }

        @Test
        @DisplayName("getCurrentCharacter() returns exists=true with class-selected draft")
        void getCurrentCharacterWithDraft() {
            CharacterDraft draft = new CharacterDraft();
            draft.setCharacterClass("fighter");
            draft.setRaceId("human_standard");
            session.setAttribute(DRAFT_KEY, draft);

            Map<String, Object> result = controller.getCurrentCharacter(session);

            assertEquals(true, result.get("exists"));
            assertNotNull(result.get("draft"));
        }
    }

    @Nested
    @DisplayName("Step routing")
    class StepRouting {

        @Test
        @DisplayName("showStep() with step < 1 redirects to /step/1")
        void stepZeroRedirects() {
            String result = controller.showStep(0, session, new ExtendedModelMap(), null);
            assertEquals("redirect:/step/1", result);
        }

        @Test
        @DisplayName("showStep() with step > 9 redirects to /step/1")
        void stepTenRedirects() {
            String result = controller.showStep(10, session, new ExtendedModelMap(), null);
            assertEquals("redirect:/step/1", result);
        }

        @Test
        @DisplayName("showStep() at step 1 returns wizard view")
        void step1ReturnsWizard() {
            String result = controller.showStep(1, session, new ExtendedModelMap(), null);
            assertEquals("wizard", result);
        }

        @Test
        @DisplayName("showStep() skips to highest reached + 1 if jumping ahead")
        void stepSkippingPrevented() {
            // Fresh draft starts at highestStepReached = 1; trying step/5 should redirect to step/2
            String result = controller.showStep(5, session, new ExtendedModelMap(), null);
            assertEquals("redirect:/step/2", result);
        }
    }

    @Nested
    @DisplayName("Step POST handlers")
    class StepPosts {

        @Test
        @DisplayName("saveStep1() stores raceId and redirects to /step/2")
        void saveStep1() {
            session.setAttribute(DRAFT_KEY, CharacterDraft.fresh());
            Map<String, String> params = Map.of("raceId", "human_standard");

            String result = controller.saveStep1("human_standard", params, session, null);

            assertEquals("redirect:/step/2", result);
            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals("human_standard", draft.getRaceId());
        }

        @Test
        @DisplayName("saveStep2() stores class, level and redirects to /step/3")
        void saveStep2() {
            session.setAttribute(DRAFT_KEY, CharacterDraft.fresh());

            String result = controller.saveStep2("fighter", "champion", 3, session);

            assertEquals("redirect:/step/3", result);
            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals("fighter", draft.getCharacterClass());
            assertEquals("champion", draft.getSubclassId());
            assertEquals(3, draft.getLevel());
        }

        @Test
        @DisplayName("saveStep2() clamps level to 1-20")
        void saveStep2ClampsLevel() {
            session.setAttribute(DRAFT_KEY, CharacterDraft.fresh());
            controller.saveStep2("fighter", "", 99, session);

            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(20, draft.getLevel());
        }

        @Test
        @DisplayName("saveStep3() stores background, alignment, name and redirects to /step/4")
        void saveStep3() {
            session.setAttribute(DRAFT_KEY, CharacterDraft.fresh());

            String result = controller.saveStep3("acolyte", "Neutral Good", "Aria", session);

            assertEquals("redirect:/step/4", result);
            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals("acolyte", draft.getBackground());
            assertEquals("Neutral Good", draft.getAlignment());
            assertEquals("Aria", draft.getCharacterName());
        }
    }
}
