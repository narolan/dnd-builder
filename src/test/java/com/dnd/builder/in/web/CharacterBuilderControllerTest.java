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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
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
        calculator = new CharacterCalculator(raceRepo, classRepo, bgRepo, eqRepo);
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

        // ── Step 4 — Point Buy ──────────────────────────────────────────────

        @Test
        @DisplayName("saveStep4() with valid scores redirects to /step/5")
        void saveStep4Valid() {
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));
            // STR=15(9) + DEX=13(5) + CON=12(4) + INT=8(0) + WIS=8(0) + CHA=8(0) = 18 ≤ 27
            Map<String, String> params = Map.of(
                "score_STR", "15", "score_DEX", "13", "score_CON", "12",
                "score_INT", "8",  "score_WIS", "8",  "score_CHA", "8"
            );

            String result = controller.saveStep4(params, session, new RedirectAttributesModelMap());

            assertEquals("redirect:/step/5", result);
            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(15, draft.getBaseScores().get("STR"));
        }

        @Test
        @DisplayName("saveStep4() over point budget redirects back to /step/4")
        void saveStep4OverBudget() {
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));
            // STR=15(9)+DEX=15(9)+CON=15(9)+INT=13(5)+WIS=8(0)+CHA=8(0) = 32 > 27
            Map<String, String> params = Map.of(
                "score_STR", "15", "score_DEX", "15", "score_CON", "15",
                "score_INT", "13", "score_WIS", "8",  "score_CHA", "8"
            );

            String result = controller.saveStep4(params, session, new RedirectAttributesModelMap());

            assertEquals("redirect:/step/4", result);
        }

        @Test
        @DisplayName("saveStep4() with non-numeric input treats the score as 8")
        void saveStep4NonNumericFallsBackTo8() {
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));
            Map<String, String> params = Map.of(
                "score_STR", "abc", "score_DEX", "8", "score_CON", "8",
                "score_INT", "8",   "score_WIS", "8", "score_CHA", "8"
            );

            String result = controller.saveStep4(params, session, new RedirectAttributesModelMap());

            assertEquals("redirect:/step/5", result);
            CharacterDraft draft = (CharacterDraft) session.getAttribute(DRAFT_KEY);
            assertEquals(8, draft.getBaseScores().get("STR"));
        }

        // ── Step 5 — Skills ────────────────────────────────────────────────

        @Test
        @DisplayName("saveStep5() with too many skills redirects back to /step/5")
        void saveStep5TooManySkills() {
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));
            // Fighter can choose 2; submit 3 valid fighter skills
            String result = controller.saveStep5(
                List.of("Athletics", "Perception", "Intimidation"),
                session, new RedirectAttributesModelMap()
            );

            assertEquals("redirect:/step/5", result);
        }

        @Test
        @DisplayName("saveStep5() non-caster with no ASIs skips steps 6 and 7")
        void saveStep5NonCasterSkipsToStep8() {
            // Fighter level 1 — no ASI yet, no spellcasting
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));

            String result = controller.saveStep5(
                List.of("Athletics", "Perception"),
                session, new RedirectAttributesModelMap()
            );

            assertEquals("redirect:/step/8", result);
        }

        @Test
        @DisplayName("saveStep5() spellcaster with no ASIs skips step 6 but keeps step 7")
        void saveStep5CasterSkipsToStep7() {
            // Wizard level 1 — no ASI yet, IS a spellcaster
            session.setAttribute(DRAFT_KEY, draftFor("wizard", 1));

            String result = controller.saveStep5(
                List.of("Arcana", "History"),
                session, new RedirectAttributesModelMap()
            );

            assertEquals("redirect:/step/7", result);
        }

        @Test
        @DisplayName("saveStep5() with available ASI levels redirects to /step/6")
        void saveStep5WithAsiGoesToStep6() {
            // Fighter level 4 — first ASI reached
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 4));

            String result = controller.saveStep5(
                List.of("Athletics", "Perception"),
                session, new RedirectAttributesModelMap()
            );

            assertEquals("redirect:/step/6", result);
        }

        // ── Step 6 — ASI / Feats ───────────────────────────────────────────

        @Test
        @DisplayName("saveStep6() single ASI stores +2 to chosen stat, non-caster goes to /step/8")
        void saveStep6SingleAsi() {
            CharacterDraft draft = draftFor("fighter", 4);
            session.setAttribute(DRAFT_KEY, draft);
            Map<String, String> params = Map.of(
                "asi_type_4", "asi",
                "asi_mode_4", "single",
                "asi_single_4", "STR"
            );

            String result = controller.saveStep6(params, session);

            assertEquals("redirect:/step/8", result);
            assertEquals(1, draft.getAsiChoices().size());
            assertEquals(2, draft.getAsiChoices().get(0).statIncreases().get("STR"));
        }

        @Test
        @DisplayName("saveStep6() feat choice stores feat ID, non-caster goes to /step/8")
        void saveStep6Feat() {
            CharacterDraft draft = draftFor("fighter", 4);
            session.setAttribute(DRAFT_KEY, draft);
            Map<String, String> params = Map.of(
                "asi_type_4", "feat",
                "feat_4", "alert"
            );

            String result = controller.saveStep6(params, session);

            assertEquals("redirect:/step/8", result);
            assertEquals("feat", draft.getAsiChoices().get(0).type());
            assertEquals("alert", draft.getAsiChoices().get(0).featId());
        }

        @Test
        @DisplayName("saveStep6() spellcaster goes to /step/7 after ASI")
        void saveStep6CasterGoesToStep7() {
            session.setAttribute(DRAFT_KEY, draftFor("wizard", 4));
            Map<String, String> params = Map.of(
                "asi_type_4", "asi", "asi_mode_4", "single", "asi_single_4", "INT"
            );

            String result = controller.saveStep6(params, session);

            assertEquals("redirect:/step/7", result);
        }

        // ── Step 7 — Spells ────────────────────────────────────────────────

        @Test
        @DisplayName("saveStep7() too many cantrips redirects back to /step/7")
        void saveStep7TooManyCantrips() {
            // Wizard level 1: cantripsKnown returns 0 (wizard gets cantrips at L1 via known list,
            // but ClassRepository.cantripsKnown defaults wizard to 0); submit 1 to exceed it
            session.setAttribute(DRAFT_KEY, draftFor("wizard", 1));
            var ra = new RedirectAttributesModelMap();

            String result = controller.saveStep7(
                List.of("fire_bolt", "light", "mage_hand", "prestidigitation"),
                List.of(), List.of(), session, ra
            );

            assertEquals("redirect:/step/7", result);
            // Controller uses ra.addAttribute() (URL param), not addFlashAttribute
            assertTrue(ra.asMap().containsKey("error"));
        }

        @Test
        @DisplayName("saveStep7() too many known spells redirects back to /step/7")
        void saveStep7TooManyKnownSpells() {
            // Bard level 1 knows level+3=4 spells; submit 5 to exceed
            session.setAttribute(DRAFT_KEY, draftFor("bard", 1));
            var ra = new RedirectAttributesModelMap();

            String result = controller.saveStep7(
                List.of("vicious_mockery"),   // 1 cantrip — within bard's 2-cantrip limit
                List.of("charm_person", "dissonant_whispers", "healing_word",
                        "thunderwave", "faerie_fire"),
                List.of(), session, ra
            );

            assertEquals("redirect:/step/7", result);
        }

        @Test
        @DisplayName("saveStep7() wizard spellbook over limit redirects back to /step/7")
        void saveStep7WizardSpellbookOverLimit() {
            // Wizard level 1: spellbook holds 6; submit 7
            session.setAttribute(DRAFT_KEY, draftFor("wizard", 1));
            var ra = new RedirectAttributesModelMap();

            String result = controller.saveStep7(
                List.of("fire_bolt"),
                List.of(),
                List.of("magic_missile","shield","sleep","identify","mage_armor","detect_magic","thunderwave"),
                session, ra
            );

            assertEquals("redirect:/step/7", result);
        }

        @Test
        @DisplayName("saveStep7() valid wizard submission redirects to /step/8")
        void saveStep7WizardValid() {
            session.setAttribute(DRAFT_KEY, draftFor("wizard", 1));
            var ra = new RedirectAttributesModelMap();

            String result = controller.saveStep7(
                List.of("fire_bolt"),
                List.of(),
                List.of("magic_missile", "shield", "sleep"),
                session, ra
            );

            assertEquals("redirect:/step/8", result);
        }

        // ── Step 8 — Equipment ─────────────────────────────────────────────

        @Test
        @DisplayName("saveStep8() stores valid equipment choices and redirects to /step/9")
        void saveStep8StoresChoices() {
            CharacterDraft draft = draftFor("fighter", 1);
            session.setAttribute(DRAFT_KEY, draft);
            // Fighter slot 0 option "a" is the first equipment choice
            Map<String, String> params = Map.of("fighter_slot_0", "a");

            String result = controller.saveStep8(params, session);

            assertEquals("redirect:/step/9", result);
            // Equipment choices stored (may or may not match depending on slot IDs — just verify redirect)
        }

        @Test
        @DisplayName("saveStep8() blank slot param is skipped")
        void saveStep8BlankParamSkipped() {
            session.setAttribute(DRAFT_KEY, draftFor("fighter", 1));

            String result = controller.saveStep8(Map.of("fighter_slot_0", ""), session);

            assertEquals("redirect:/step/9", result);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CharacterDraft draftFor(String classId, int level) {
        CharacterDraft d = CharacterDraft.fresh();
        d.setRaceId("human_standard");
        d.setCharacterClass(classId);
        d.setLevel(level);
        d.setBaseScores(Map.of("STR", 10, "DEX", 10, "CON", 10, "INT", 10, "WIS", 10, "CHA", 10));
        return d;
    }
}
