package com.dnd.builder.in.web;

import com.dnd.builder.core.model.*;
import com.dnd.builder.core.port.out.ClassRepository;
import com.dnd.builder.core.service.CharacterCalculator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.dnd.builder.in.web.CharacterBuilderController.DRAFT_KEY;

/**
 * Handles play mode features: combat dashboard, inventory, rests, etc.
 */
@Controller
@RequestMapping("/play")
public class PlayModeController {

    private final CharacterCalculator calculator;
    private final ClassRepository classRepository;

    public PlayModeController(CharacterCalculator calculator, ClassRepository classRepository) {
        this.calculator = calculator;
        this.classRepository = classRepository;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COMBAT DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("")
    public String playDashboard(HttpSession session, Model model) {
        CharacterDraft draft = getDraft(session);
        if (draft == null || draft.getCharacterClass().isEmpty()) {
            return "redirect:/step/1";
        }

        var derived = calculator.calculate(draft);
        model.addAttribute("draft", draft);
        model.addAttribute("derived", derived);
        model.addAttribute("classDef", classRepository.findById(draft.getCharacterClass()));

        // Current HP (initialize to max if not set)
        int currentHp = draft.getCurrentHp();
        if (currentHp < 0) currentHp = derived.getMaxHitPoints();
        model.addAttribute("currentHp", currentHp);

        // Spell slots
        var cd = classRepository.findById(draft.getCharacterClass());
        if (cd != null && cd.getSpellcasting() != null) {
            int[] slots;
            if ("warlock".equals(draft.getCharacterClass())) {
                int[] ws = ClassRepository.warlockSlots(draft.getLevel());
                slots = new int[]{ws[0], 0, 0, 0, 0, 0, 0, 0, 0};
            } else if ("half".equals(cd.getSpellcasting().getType())) {
                slots = ClassRepository.halfCasterSlots(draft.getLevel());
            } else {
                slots = ClassRepository.fullCasterSlots(draft.getLevel());
            }
            model.addAttribute("maxSlots", slots);
            model.addAttribute("usedSlots", draft.getUsedSpellSlots());
        }

        return "play/dashboard";
    }

    // ── HP Tracking ───────────────────────────────────────────────────────────

    @PostMapping("/hp/update")
    @ResponseBody
    public Map<String, Object> updateHp(@RequestParam int hp, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        // Clamp HP between 0 and max + temp
        hp = Math.max(0, Math.min(hp, maxHp + draft.getTempHp()));
        draft.setCurrentHp(hp);

        return Map.of("currentHp", hp, "maxHp", maxHp);
    }

    @PostMapping("/hp/temp")
    @ResponseBody
    public Map<String, Object> setTempHp(@RequestParam int temp, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setTempHp(Math.max(0, temp));
        return Map.of("tempHp", draft.getTempHp());
    }

    @PostMapping("/hp/heal")
    @ResponseBody
    public Map<String, Object> heal(@RequestParam int amount, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        int current = draft.getCurrentHp() < 0 ? maxHp : draft.getCurrentHp();
        int newHp = Math.min(maxHp, current + amount);
        draft.setCurrentHp(newHp);

        return Map.of("currentHp", newHp, "maxHp", maxHp, "healed", newHp - current);
    }

    @PostMapping("/hp/damage")
    @ResponseBody
    public Map<String, Object> damage(@RequestParam int amount, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);
        int maxHp = derived.getMaxHitPoints();

        int current = draft.getCurrentHp() < 0 ? maxHp : draft.getCurrentHp();
        int temp = draft.getTempHp();

        // Temp HP absorbs damage first
        int remaining = amount;
        if (temp > 0) {
            if (temp >= remaining) {
                draft.setTempHp(temp - remaining);
                remaining = 0;
            } else {
                remaining -= temp;
                draft.setTempHp(0);
            }
        }

        int newHp = Math.max(0, current - remaining);
        draft.setCurrentHp(newHp);

        return Map.of("currentHp", newHp, "tempHp", draft.getTempHp(), "maxHp", maxHp);
    }

    // ── Spell Slots ───────────────────────────────────────────────────────────

    @PostMapping("/slots/use")
    @ResponseBody
    public Map<String, Object> useSpellSlot(@RequestParam int level, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int[] used = draft.getUsedSpellSlots();
        if (level >= 1 && level <= 9) {
            used[level - 1]++;
            draft.setUsedSpellSlots(used);
        }
        return Map.of("level", level, "used", used[level - 1]);
    }

    @PostMapping("/slots/restore")
    @ResponseBody
    public Map<String, Object> restoreSpellSlot(@RequestParam int level, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int[] used = draft.getUsedSpellSlots();
        if (level >= 1 && level <= 9 && used[level - 1] > 0) {
            used[level - 1]--;
            draft.setUsedSpellSlots(used);
        }
        return Map.of("level", level, "used", used[level - 1]);
    }

    // ── Rests ─────────────────────────────────────────────────────────────────

    @PostMapping("/rest/short")
    @ResponseBody
    public Map<String, Object> shortRest(HttpSession session) {
        CharacterDraft draft = getDraft(session);

        // Warlock: restore pact magic slots
        if ("warlock".equals(draft.getCharacterClass())) {
            draft.setUsedSpellSlots(new int[9]);
        }

        return Map.of("message", "Short rest complete. Roll hit dice to heal.");
    }

    @PostMapping("/rest/long")
    @ResponseBody
    public Map<String, Object> longRest(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var derived = calculator.calculate(draft);

        // Restore HP to max
        draft.setCurrentHp(derived.getMaxHitPoints());
        draft.setTempHp(0);

        // Restore all spell slots
        draft.setUsedSpellSlots(new int[9]);

        // Restore half hit dice (minimum 1)
        int hitDiceToRestore = Math.max(1, draft.getLevel() / 2);
        draft.setUsedHitDice(Math.max(0, draft.getUsedHitDice() - hitDiceToRestore));

        return Map.of(
            "message", "Long rest complete!",
            "currentHp", derived.getMaxHitPoints(),
            "hitDiceRestored", hitDiceToRestore
        );
    }

    @PostMapping("/hitdice/use")
    @ResponseBody
    public Map<String, Object> useHitDie(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        int available = draft.getLevel() - draft.getUsedHitDice();
        if (available > 0) {
            draft.setUsedHitDice(draft.getUsedHitDice() + 1);
            var derived = calculator.calculate(draft);
            return Map.of(
                "success", true,
                "hitDie", derived.getHitDice(),
                "conMod", derived.getModifiers().get("CON"),
                "remaining", available - 1
            );
        }
        return Map.of("success", false, "message", "No hit dice remaining");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // INVENTORY MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        CharacterDraft draft = getDraft(session);
        if (draft == null) return "redirect:/step/1";

        model.addAttribute("draft", draft);
        model.addAttribute("derived", calculator.calculate(draft));
        model.addAttribute("attunedCount", draft.getAttunedCount());

        return "play/inventory";
    }

    @PostMapping("/inventory/add")
    @ResponseBody
    public Map<String, Object> addItem(@RequestBody InventoryItem item, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.addItem(item);
        return Map.of("success", true, "itemId", item.getId());
    }

    @PostMapping("/inventory/remove")
    @ResponseBody
    public Map<String, Object> removeItem(@RequestParam String itemId, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.removeItem(itemId);
        return Map.of("success", true);
    }

    @PostMapping("/inventory/equip")
    @ResponseBody
    public Map<String, Object> equipItem(@RequestParam String itemId, @RequestParam boolean equipped, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        for (var item : draft.getInventory()) {
            if (item.getId().equals(itemId)) {
                item.setEquipped(equipped);
                return Map.of("success", true, "equipped", equipped);
            }
        }
        return Map.of("success", false);
    }

    @PostMapping("/inventory/attune")
    @ResponseBody
    public Map<String, Object> attuneItem(@RequestParam String itemId, @RequestParam boolean attuned, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        for (var item : draft.getInventory()) {
            if (item.getId().equals(itemId)) {
                if (attuned && !draft.canAttune()) {
                    return Map.of("success", false, "error", "Maximum 3 attuned items");
                }
                item.setAttuned(attuned);
                return Map.of("success", true, "attuned", attuned, "attunedCount", draft.getAttunedCount());
            }
        }
        return Map.of("success", false);
    }

    // ── Currency ──────────────────────────────────────────────────────────────

    @PostMapping("/currency/update")
    @ResponseBody
    public Map<String, Object> updateCurrency(
            @RequestParam int platinum,
            @RequestParam int gold,
            @RequestParam int electrum,
            @RequestParam int silver,
            @RequestParam int copper,
            HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setPlatinum(Math.max(0, platinum));
        draft.setGold(Math.max(0, gold));
        draft.setElectrum(Math.max(0, electrum));
        draft.setSilver(Math.max(0, silver));
        draft.setCopper(Math.max(0, copper));
        return Map.of("success", true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONDITION TRACKING
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/conditions/add")
    @ResponseBody
    public Map<String, Object> addCondition(@RequestParam String name,
                                            @RequestParam(defaultValue = "-1") int rounds,
                                            @RequestParam(required = false) String source,
                                            HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var condition = new ActiveCondition(name, rounds);
        if (source != null && !source.isEmpty()) {
            condition.withSource(source);
        }
        draft.addCondition(condition);
        return Map.of("success", true, "conditionId", condition.getId(), "conditions", draft.getConditions());
    }

    @PostMapping("/conditions/remove")
    @ResponseBody
    public Map<String, Object> removeCondition(@RequestParam String conditionId, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.removeCondition(conditionId);
        return Map.of("success", true, "conditions", draft.getConditions());
    }

    @PostMapping("/conditions/tick")
    @ResponseBody
    public Map<String, Object> tickConditions(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        var expired = new java.util.ArrayList<String>();
        draft.getConditions().removeIf(c -> {
            if (c.tick()) {
                expired.add(c.getName());
                return true;
            }
            return false;
        });
        return Map.of("success", true, "expired", expired, "conditions", draft.getConditions());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DEATH SAVES
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/deathsaves/success")
    @ResponseBody
    public Map<String, Object> deathSaveSuccess(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setDeathSaveSuccesses(draft.getDeathSaveSuccesses() + 1);
        boolean stable = draft.isStable();
        if (stable) {
            // Stabilized - regain 1 HP after 1d4 hours, but for gameplay we just mark as stable
        }
        return Map.of(
            "successes", draft.getDeathSaveSuccesses(),
            "failures", draft.getDeathSaveFailures(),
            "stable", stable,
            "dead", false
        );
    }

    @PostMapping("/deathsaves/failure")
    @ResponseBody
    public Map<String, Object> deathSaveFailure(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setDeathSaveFailures(draft.getDeathSaveFailures() + 1);
        boolean dead = draft.isDead();
        return Map.of(
            "successes", draft.getDeathSaveSuccesses(),
            "failures", draft.getDeathSaveFailures(),
            "stable", false,
            "dead", dead
        );
    }

    @PostMapping("/deathsaves/reset")
    @ResponseBody
    public Map<String, Object> resetDeathSaves(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.resetDeathSaves();
        return Map.of("successes", 0, "failures", 0, "stable", false, "dead", false);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CONCENTRATION
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/concentration/set")
    @ResponseBody
    public Map<String, Object> setConcentration(@RequestParam String spell, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        draft.setConcentratingOn(spell);
        return Map.of("success", true, "concentratingOn", spell);
    }

    @PostMapping("/concentration/break")
    @ResponseBody
    public Map<String, Object> breakConcentration(HttpSession session) {
        CharacterDraft draft = getDraft(session);
        String was = draft.getConcentratingOn();
        draft.breakConcentration();
        return Map.of("success", true, "broken", was != null ? was : "");
    }

    @GetMapping("/concentration/check")
    @ResponseBody
    public Map<String, Object> concentrationCheck(@RequestParam int damage, HttpSession session) {
        CharacterDraft draft = getDraft(session);
        if (!draft.isConcentrating()) {
            return Map.of("required", false);
        }
        // DC is 10 or half damage, whichever is higher
        int dc = Math.max(10, damage / 2);
        var derived = calculator.calculate(draft);
        int conMod = derived.getModifiers().get("CON");
        int profBonus = derived.getProficiencyBonus();
        // Check if proficient in CON saves
        boolean proficient = derived.getSavingThrowProficiencies().contains("CON");
        int bonus = conMod + (proficient ? profBonus : 0);

        return Map.of(
            "required", true,
            "dc", dc,
            "bonus", bonus,
            "spell", draft.getConcentratingOn()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CharacterDraft getDraft(HttpSession session) {
        return (CharacterDraft) session.getAttribute(DRAFT_KEY);
    }
}
