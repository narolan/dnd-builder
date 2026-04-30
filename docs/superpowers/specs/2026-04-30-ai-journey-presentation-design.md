# Design Spec: "From ChatGPT to Claude Code" Presentation

**Date:** 2026-04-30
**Author:** Jorgi + Claude
**Status:** Approved

---

## Overview

A 15-20 minute conference/team talk for developer/engineer audiences, structured as a Hero's Journey. The goal is to inspire other consultants to start using AI-assisted development tools — specifically by showing that the journey through ChatGPT → Copilot → Claude Code is what *builds* the skills to use Claude Code effectively. You don't skip levels.

---

## Deliverable

- **Format:** Self-contained HTML file using reveal.js (CDN)
- **Theme:** Deep Blue / Dramatic — navy/dark background, crimson (`#e94560`) accent, monospace code fragments
- **Slides:** 20 core slides + speaker notes on every slide
- **Audience:** Developers and engineers (some non-technical staff may attend)
- **Length:** 15-20 minutes

---

## Story Arc

### ACT 1 — The Ordinary World & The Call (slides 1-4)
Set the scene: developer life before AI. Introduce ChatGPT as the first tool. Honest assessment: great for ideas, wildly inconsistent results, lives outside the IDE.

### ACT 2 — The Ordeal: Copilot (slides 5-9)
Copilot round 1: rage quit after a week. Too much babysitting, more work than without. The turning point: discovering context files (CLAUDE.md, AGENT.md). Copilot round 2: finally feels like a junior team member. The hidden lesson: you just became a prompt engineer without realising it.

### ACT 3 — The Transformation: Claude Code (slides 10-14)
Claude Code enters — not a plugin, a CLI agent that reads the entire codebase. Full pair programmer, sparring partner, reviewer. Superpowers plugin: custom skills, hooks, agents tuned to your workflow. **The Number:** 10-12 tickets/sprint → 30+ tickets/sprint. Same quality, no regressions. What changed? Not the AI — you.

### ACT 4 — The Return (slides 15-20)
The virtuous cycle: better context → better output → better instincts → better prompts. "You don't skip levels" — each tool in the journey built the skill for the next. Practical how-to: install Claude Code, write your first CLAUDE.md, run your first task. Honest expectations: week 1 awkward, month 1 surprising, month 3 you can't go back. Call to action. **Final slide:** "Made by Claude & Jorgi" — the mic drop meta-moment.

---

## Slide List

| # | Title | Act | Key Message |
|---|-------|-----|-------------|
| 01 | Title | 1 | "From ChatGPT to Claude Code: A Dev's Journey" |
| 02 | The Problem | 1 | You ship features. Slowly. Everyone's talking about AI. |
| 03 | Enter ChatGPT | 1 | The hype, the promise, the first taste |
| 04 | ChatGPT Reality Check | 1 | Great for ideas. Wildly inconsistent. Outside your IDE. |
| 05 | Enter Copilot | 2 | Integrated, inline, Microsoft-backed. This must be it. |
| 06 | Week 1: Rage Quit | 2 | More work. Constant corrections. Gave up. |
| 07 | The Missing Ingredient | 2 | It wasn't the tool. It was context. CLAUDE.md. AGENT.md. |
| 08 | Copilot: Round 2 | 2 | Junior team member. Picks up smaller tasks. |
| 09 | The Insight | 2 | You just became a prompt engineer without realising it. |
| 10 | Enter Claude Code | 3 | Not a plugin. A CLI agent that reads your entire codebase. |
| 11 | Your New Pair Programmer | 3 | Sparring partner. Reviewer. Pushes back when you're wrong. |
| 12 | Superpowers Unlocked | 3 | Skills, hooks, agents — Claude tuned to how YOU work. |
| 13 | THE NUMBER | 3 | 10-12 tickets/sprint → 30+. Same quality. No regressions. |
| 14 | What Changed? | 3 | Not the AI. You. You learned to communicate with machines. |
| 15 | The Virtuous Cycle | 4 | Better context → better output → better instincts → repeat |
| 16 | You Don't Skip Levels | 4 | ChatGPT taught asking. Copilot taught context. Claude rewards both. |
| 17 | Start Your Journey | 4 | Install, write first CLAUDE.md, run first task |
| 18 | What to Expect | 4 | Week 1 awkward. Month 1 surprising. Month 3 no going back. |
| 19 | The Call to Action | 4 | "Stop coding alone. Start the journey." |
| 20 | Made by Claude & Jorgi | 4 | The mic drop. This presentation was built with Claude Code. |

---

## Technical Notes

- Single self-contained `.html` file, reveal.js loaded from CDN
- Speaker notes via reveal.js `<aside class="notes">` on every slide
- Keyboard: Space/arrow to advance, `S` to open speaker view
- No build step, no dependencies — open in any browser
- Crimson `#e94560`, navy `#1a1a2e` / `#16213e` / `#0f3460`, white text
- Monospace fragments for tool names, commands, file names
- Slide 13 ("THE NUMBER") gets dramatic full-screen treatment: giant counter animation

---

## Key Quotes / Copy

- *"It wasn't the tool. It was context."*
- *"You don't skip levels."*
- *"Week 1 awkward. Month 1 surprising. Month 3 you can't go back."*
- *"Stop coding alone."*
- *"Made by Claude & Jorgi"*
