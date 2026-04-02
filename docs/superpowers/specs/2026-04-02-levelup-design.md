# Level-Up Feature Design

**Date:** 2026-04-02  
**Status:** Approved

## Overview

Allow players to level up a character from any of three surfaces: the play dashboard (`/play`), the characters list (`/characters`), and the review step (`/step/9`). Leveling up opens a modal overlay that shows what the character gained and collects any required choices (ASI/Feat, subclass, new spells/cantrips). On confirmation, the session draft is updated and the updated character is saved back to localStorage.

---

## Backend

### `GET /play/levelup/options`

Returns a JSON object describing what will happen when the in-session character advances from level N → N+1. Compares N vs N+1 values using the existing `ClassRepository` static methods. Character must already be loaded into session.

**Response shape:**
```json
{
  "currentLevel": 4,
  "newLevel": 5,
  "newFeatures": [{ "name": "Extra Attack", "description": "..." }],
  "hpGain": 5,
  "profBonusChanged": false,
  "spellSlotsChanged": true,
  "newSpellSlotSummary": "4×1st, 3×2nd, 2×3rd",
  "needsAsi": false,
  "needsSubclass": false,
  "newCantripsCount": 0,
  "newSpellsCount": 1,
  "availableCantrips": [...],
  "availableSpells": [...],
  "availableFeats": [...],
  "availableSubclasses": [...],
  "maxNewSpellLevel": 3,
  "isWizard": false,
  "wizardSpellbookGain": 0
}
```

- `needsAsi`: true if `newLevel` is in `classDef.getAsiLevels()`
- `needsSubclass`: true if `newLevel == classDef.getSubclassLevel()` and draft has no subclass
- `newCantripsCount`: `cantripsKnown(newLevel) - cantripsKnown(currentLevel)`, clamped to ≥ 0
- `newSpellsCount`: `spellsKnown(newLevel) - spellsKnown(currentLevel)` for known-casters; 0 for prepared casters
- `wizardSpellbookGain`: 2 for wizards at every level beyond 1
- `availableSpells`: filtered to spells the character doesn't already know, up to `maxNewSpellLevel`
- Returns 400 if character is already level 20

### `POST /play/levelup`

Accepts the player's choices, mutates the session draft, and returns the updated draft + derived stats.

**Request body (JSON):**
```json
{
  "asiType": "asi | feat | null",
  "asiMode": "single | split | null",
  "asiStat1": "STR | null",
  "asiStat2": "DEX | null",
  "featId": "null | string",
  "newSpells": ["fireball"],
  "newCantrips": [],
  "spellbookAdditions": [],
  "subclassId": "null | string"
}
```

**Mutations applied (in order):**
1. `draft.level++`
2. If `asiType` is set: append new `AsiChoice` to `draft.asiChoices`
3. If `subclassId` set: `draft.setSubclassId(subclassId)`
4. Append `newCantrips` to `draft.chosenCantrips`
5. Append `newSpells` to `draft.chosenSpells`
6. Append `spellbookAdditions` to `draft.spellbookSpells` (wizard)

**Response:**
```json
{
  "success": true,
  "newLevel": 5,
  "draft": { ...full CharacterDraft... },
  "derived": { ...DerivedStats... }
}
```

Both endpoints live in `PlayModeController`. The `GET` endpoint is read-only (no mutation). Returns 400 if no character in session or level is already 20.

---

## Frontend

### Shared JS: `/js/levelup.js`

Included on all three pages. Exposes one function: `openLevelUpModal(characterJson?)`.

- On `/play` and `/step/9`: called with no argument (character already in session)
- On `/characters`: called with the character's localStorage JSON; first POSTs to `/characters/load`, then proceeds

**Modal flow:**
1. If character not yet in session, `POST /characters/load`
2. `GET /play/levelup/options`
3. Build and show modal DOM
4. On "Confirm Level Up": `POST /play/levelup` with choices
5. Save updated draft to localStorage: update the matching entry (by `id`) in the `characterForge_characters` array
6. Close modal and reload page

### Modal structure

```
┌────────────────────────────────────────┐
│  ⚔ Level Up! → Level 5               │
├────────────────────────────────────────┤
│  What you gained                       │
│  • Extra Attack (class feature)        │
│  • HP: +5 (max now 45)                 │
│  • Spell slots: 4×1st, 3×2nd, 2×3rd   │
│  • Proficiency bonus: +3              │
├────────────────────────────────────────┤
│  [ASI/Feat — shown only if needsAsi]   │
│  ○ +2 to one stat  ○ +1/+1  ○ Feat    │
├────────────────────────────────────────┤
│  [Subclass — shown only if needsSubclass] │
│  Select your subclass...               │
├────────────────────────────────────────┤
│  [New spells — shown if newSpellsCount > 0] │
│  Pick N new spell(s):                  │
│  [ Fireball ] [ Haste ] [ Fly ] ...   │
├────────────────────────────────────────┤
│  [New cantrips — shown if newCantripsCount > 0] │
├────────────────────────────────────────┤
│        [ Confirm Level Up ]            │
└────────────────────────────────────────┘
```

Sections with no content are hidden. If no choices are required at all (e.g. Barbarian leveling up on a non-ASI level), the modal goes straight to "What you gained" + Confirm.

### Button placement

| Page | Location |
|------|----------|
| `/play` dashboard | Header area, next to character name/level display |
| `/characters` list | New "Level Up" button on each character card |
| `/step/9` review | Below character summary |

### Styling

Matches existing play mode aesthetic: dark parchment background, Cinzel font, gold border, same `.play-card` / `.hp-btn` class conventions.

---

## Constraints & Edge Cases

- **Level 20 cap**: "Level Up" button hidden/disabled when `draft.level >= 20`; `GET /play/levelup/options` returns 400
- **Prepared casters** (Cleric, Druid, Paladin, Wizard): `newSpellsCount` is 0 — they don't pick new spells at level-up (they prepare from their full list each day). Wizard gets +2 spellbook slots
- **Half-casters** (Paladin, Ranger): `newSpellsCount` follows the Ranger progression; Paladin is a prepared caster
- **Subclass already chosen**: `needsSubclass` is false
- **No choices needed**: modal shows summary + immediate confirm (e.g. Barbarian at level 2)
- **ASI for Fighter (levels 6, 14) and Rogue (level 10)**: handled by the existing `classDef.getAsiLevels()` list
- **Variant Human feat at level 1**: already stored separately as `chosenFeatId`; not affected by this flow

---

## Files to Create/Modify

| File | Change |
|------|--------|
| `PlayModeController.java` | Add `GET /play/levelup/options` and `POST /play/levelup` |
| `src/main/resources/static/js/levelup.js` | New: shared modal JS |
| `play/dashboard.html` | Add Level Up button + include `levelup.js` |
| `characters.html` | Add Level Up button per card + include `levelup.js` |
| `steps/step9.html` | Add Level Up button + include `levelup.js` |
