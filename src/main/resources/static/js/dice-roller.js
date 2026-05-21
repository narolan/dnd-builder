/**
 * DiceRoller — animated 3D dice for the D&D character dashboard.
 *
 * API:
 *   DiceRoller.roll(sides, count, modifier, label, mode, elvenAccuracy)  → Promise<number>
 *   DiceRoller.rollCheck(stat, modifier, label)                           → Promise<number>
 *
 * mode: 'normal' | 'advantage' | 'disadvantage'
 * elvenAccuracy: boolean — roll 3 dice on advantage, take highest (feat)
 */
const DiceRoller = (() => {
  'use strict';

  // ── Die shape clip-paths ─────────────────────────────────────────────────────
  const SHAPES = {
    4:   'polygon(50% 2%, 98% 97%, 2% 97%)',
    6:   null,   // rounded square via border-radius
    8:   'polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%)',
    10:  'polygon(50% 2%, 97% 38%, 79% 97%, 21% 97%, 3% 38%)',
    12:  'polygon(50% 0%, 93% 25%, 93% 75%, 50% 100%, 7% 75%, 7% 25%)',
    20:  'polygon(50% 2%, 97% 38%, 79% 97%, 21% 97%, 3% 38%)',
    100: null,   // circle via border-radius
  };

  // ── Number label per die face (used during flicker to look authentic) ────────
  const FACE_COUNT = { 4: 4, 6: 6, 8: 8, 10: 10, 12: 12, 20: 20, 100: 100 };

  let _overlay = null;
  let _activeTimers = [];

  // ── Public API ───────────────────────────────────────────────────────────────

  function roll(sides, count = 1, modifier = 0, label = '', mode = 'normal', elvenAccuracy = false) {
    return new Promise(resolve => {
      _cleanup();
      _build(sides, count, modifier, label, resolve, mode, elvenAccuracy);
    });
  }

  function rollCheck(stat, modifier, label) {
    return roll(20, 1, modifier, label || `${stat} Check`);
  }

  // ── Build overlay ────────────────────────────────────────────────────────────

  function _build(sides, count, modifier, label, resolve, mode, elvenAccuracy) {
    // For adv/disadv: roll 2 dice (or 3 with Elven Accuracy on advantage)
    const diceToRoll = mode !== 'normal'
      ? (mode === 'advantage' && elvenAccuracy ? 3 : 2)
      : count;

    const results = Array.from({ length: diceToRoll }, () =>
      Math.ceil(Math.random() * sides)
    );

    let total, chosenIdx = null;
    if (mode === 'advantage') {
      const maxVal = Math.max(...results);
      chosenIdx = results.lastIndexOf(maxVal);
      total = maxVal + modifier;
    } else if (mode === 'disadvantage') {
      const minVal = Math.min(...results);
      chosenIdx = results.indexOf(minVal);
      total = minVal + modifier;
    } else {
      total = results.reduce((a, b) => a + b, 0) + modifier;
    }

    // Nat 20 / Fumble based on the result die (or single die in normal mode)
    const resultDie = chosenIdx !== null ? results[chosenIdx] : (count === 1 ? results[0] : null);
    const isNat20 = sides === 20 && resultDie === 20;
    const isNat1  = sides === 20 && resultDie === 1;
    const isCrit  = isNat20;

    _overlay = document.createElement('div');
    _overlay.id = 'dr-overlay';
    _overlay.innerHTML = `
      <div id="dr-backdrop"></div>
      <div id="dr-arena">
        <div id="dr-label-top">${label}</div>
        <div id="dr-dice-row">
          ${results.map((_, i) => _dieFaceHTML(sides, i, diceToRoll)).join('')}
        </div>
        <div id="dr-result-area">
          <div id="dr-total"></div>
          <div id="dr-formula"></div>
        </div>
        <div id="dr-particles"></div>
        ${isCrit  ? '<div id="dr-banner" class="dr-crit-banner">NATURAL 20</div>'  : ''}
        ${isNat1  ? '<div id="dr-banner" class="dr-fumble-banner">FUMBLE</div>'    : ''}
        <div id="dr-hint">click anywhere to dismiss</div>
      </div>`;

    document.body.appendChild(_overlay);
    _overlay.addEventListener('click', () => { _cleanup(); resolve(total); });

    // Inject styles once
    if (!document.getElementById('dr-styles')) _injectStyles();

    requestAnimationFrame(() => {
      _overlay.classList.add('dr-visible');
      _animate(sides, diceToRoll, modifier, results, total, isNat20, isNat1, chosenIdx, mode);
    });
  }

  function _dieFaceHTML(sides, index, count) {
    const angle = (Math.random() - 0.5) * 600;
    const tx = (Math.random() - 0.5) * 260;
    const ty = -340 - Math.random() * 180;
    const delay = index * 0.13;
    return `
      <div class="dr-die dr-die-${sides}"
           style="--rot:${angle}deg;--tx:${tx}px;--ty:${ty}px;--delay:${delay}s">
        <div class="dr-face">
          <span class="dr-num">?</span>
        </div>
      </div>`;
  }

  // ── Animation pipeline ───────────────────────────────────────────────────────

  async function _animate(sides, count, modifier, results, total, isNat20, isNat1, chosenIdx, mode) {
    const dice = [..._overlay.querySelectorAll('.dr-die')];
    const nums = dice.map(d => d.querySelector('.dr-num'));

    // Phase 1 — enter + rapid flicker
    dice.forEach((d, i) => _t(() => d.classList.add('dr-entering'), i * 130));

    const flickerMs = 65;
    const flickerIds = nums.map(n => {
      const id = setInterval(() => {
        n.textContent = Math.ceil(Math.random() * sides);
      }, flickerMs);
      _activeTimers.push(id);
      return id;
    });

    await _sleep(880);

    // Phase 2 — staggered settle
    for (let i = 0; i < dice.length; i++) {
      await _sleep(i === 0 ? 0 : 160);

      // Slow flicker countdown
      clearInterval(flickerIds[i]);
      let ticks = 5;
      await new Promise(res => {
        const slow = setInterval(() => {
          nums[i].textContent = ticks > 1
            ? Math.ceil(Math.random() * sides)
            : results[i];
          ticks--;
          if (ticks < 0) { clearInterval(slow); res(); }
        }, 130);
        _activeTimers.push(slow);
      });

      dice[i].classList.add('dr-settled');
      // Dim dice that weren't picked (advantage/disadvantage)
      if (chosenIdx !== null && i !== chosenIdx) {
        dice[i].classList.add('dr-discarded');
      } else {
        if (isNat20) dice[i].classList.add('dr-nat20');
        if (isNat1)  dice[i].classList.add('dr-nat1');
      }
    }

    await _sleep(160);

    // Phase 3 — particles + result
    _burst(isNat20 ? 44 : 18, isNat20);
    _revealTotal(total, modifier, results, sides, count, isNat20, isNat1, chosenIdx, mode);

    // Phase 4 — banner
    if (isNat20 || isNat1) {
      await _sleep(220);
      _overlay.querySelector('#dr-banner')?.classList.add('dr-banner-in');
    }
  }

  function _revealTotal(total, modifier, results, sides, count, isNat20, isNat1, chosenIdx, mode) {
    const totalEl   = _overlay.querySelector('#dr-total');
    const formulaEl = _overlay.querySelector('#dr-formula');

    totalEl.classList.add(isNat20 ? 'dr-total-crit' : isNat1 ? 'dr-total-fumble' : 'dr-total-normal');
    _countUp(totalEl, total);

    const sign = modifier > 0 ? `+${modifier}` : modifier < 0 ? `${modifier}` : '';
    let formula;

    if (chosenIdx !== null) {
      // Advantage or Disadvantage — show all rolls, arrow to chosen
      const prefix = mode === 'advantage' ? '↑ Adv' : '↓ Dis';
      const diceStr = '[' + results.join(', ') + ']';
      formula = sign
        ? `${prefix} ${diceStr} → ${results[chosenIdx]} ${sign} = ${total}`
        : `${prefix} ${diceStr} → ${results[chosenIdx]}`;
    } else {
      const diceStr = count > 1 ? `(${results.join(' + ')})` : `${results[0]}`;
      formula = sign
        ? `${diceStr} ${sign} = ${total}`
        : (count > 1 ? `${diceStr} = ${total}` : `1d${sides}`);
    }

    _t(() => _typewriter(formulaEl, formula), 280);
  }

  // ── Visual helpers ───────────────────────────────────────────────────────────

  function _countUp(el, target) {
    el.style.opacity = '1';
    el.classList.add('dr-pop');
    const dur = 540, start = performance.now();
    const step = now => {
      const t = Math.min((now - start) / dur, 1);
      const ease = 1 - Math.pow(1 - t, 3);
      el.textContent = Math.round(target * ease);
      if (t < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }

  function _typewriter(el, text) {
    el.style.opacity = '1'; el.textContent = '';
    let i = 0;
    const id = setInterval(() => {
      el.textContent += text[i++];
      if (i >= text.length) clearInterval(id);
    }, 28);
    _activeTimers.push(id);
  }

  function _burst(count, isCrit) {
    const container = _overlay.querySelector('#dr-particles');
    const palette = isCrit
      ? ['#ffd700', '#fff5b0', '#c9a440', '#ffffff', '#f0c040', '#ffe580']
      : ['#c9a440', '#8a6d20', '#e8dcc0', '#ffffff', '#d4a830'];
    for (let i = 0; i < count; i++) {
      const p = document.createElement('div');
      p.className = 'dr-particle';
      const angle = Math.random() * Math.PI * 2;
      const dist  = 70 + Math.random() * 140;
      p.style.cssText = [
        `--px:${(Math.cos(angle) * dist).toFixed(1)}px`,
        `--py:${(Math.sin(angle) * dist).toFixed(1)}px`,
        `--sz:${(2 + Math.random() * 5).toFixed(1)}px`,
        `--col:${palette[Math.floor(Math.random() * palette.length)]}`,
        `--dur:${(.45 + Math.random() * .65).toFixed(2)}s`,
        `--dl:${(Math.random() * .18).toFixed(2)}s`,
      ].join(';');
      container.appendChild(p);
    }
  }

  // ── Utilities ────────────────────────────────────────────────────────────────

  function _t(fn, ms) {
    const id = setTimeout(fn, ms);
    _activeTimers.push(id);
    return id;
  }

  function _sleep(ms) { return new Promise(r => _t(r, ms)); }

  function _cleanup() {
    _activeTimers.forEach(id => { clearTimeout(id); clearInterval(id); });
    _activeTimers = [];
    if (_overlay) {
      _overlay.classList.remove('dr-visible');
      const el = _overlay;
      setTimeout(() => el.remove(), 260);
      _overlay = null;
    }
  }

  // ── CSS injection ────────────────────────────────────────────────────────────

  function _injectStyles() {
    const s = document.createElement('style');
    s.id = 'dr-styles';
    s.textContent = `
/* ── Overlay ──────────────────────────────────────────────────── */
#dr-overlay {
  position:fixed;inset:0;z-index:99999;
  display:flex;align-items:center;justify-content:center;
  pointer-events:none;opacity:0;transition:opacity .22s;
}
#dr-overlay.dr-visible { opacity:1;pointer-events:all; }

#dr-backdrop {
  position:absolute;inset:0;
  background:radial-gradient(ellipse at 50% 42%,rgba(10,8,3,.88) 0%,rgba(4,3,1,.96) 100%);
  backdrop-filter:blur(6px);
}

#dr-arena {
  position:relative;display:flex;flex-direction:column;
  align-items:center;gap:28px;padding:52px 72px 40px;
  pointer-events:none;
}
#dr-arena>* { pointer-events:auto; }

/* ── Label ─────────────────────────────────────────────────────── */
#dr-label-top {
  font-family:'Cinzel',serif;font-size:.82rem;letter-spacing:.2em;
  color:rgba(201,164,64,.65);text-transform:uppercase;min-height:18px;
}

/* ── Dice ──────────────────────────────────────────────────────── */
#dr-dice-row {
  display:flex;gap:20px;align-items:center;justify-content:center;flex-wrap:wrap;
}

.dr-die {
  width:100px;height:100px;position:relative;opacity:0;filter:drop-shadow(0 8px 24px rgba(0,0,0,.7));
}

.dr-face {
  width:100%;height:100%;
  background:linear-gradient(145deg,#2e2208 0%,#1a1208 55%,#2a1c08 100%);
  border:2px solid #3d2e10;
  display:flex;align-items:center;justify-content:center;
  transition:border-color .25s,box-shadow .25s;
}

/* Shapes */
.dr-die-4  .dr-face { clip-path:polygon(50% 2%,98% 97%,2% 97%); }
.dr-die-6  .dr-face { border-radius:14px; }
.dr-die-8  .dr-face { clip-path:polygon(50% 0%,100% 50%,50% 100%,0% 50%); }
.dr-die-10 .dr-face { clip-path:polygon(50% 2%,97% 38%,79% 97%,21% 97%,3% 38%); }
.dr-die-12 .dr-face { clip-path:polygon(50% 0%,93% 25%,93% 75%,50% 100%,7% 75%,7% 25%); }
.dr-die-20 .dr-face { clip-path:polygon(50% 2%,97% 38%,79% 97%,21% 97%,3% 38%); }
.dr-die-100 .dr-face { border-radius:50%; }

.dr-num {
  font-family:'Cinzel Decorative','Cinzel',serif;
  font-size:2.1rem;font-weight:700;
  color:#7a5c18;text-shadow:none;
  user-select:none;line-height:1;
  transition:color .2s,text-shadow .2s;
}

/* ── ENTER animation ───────────────────────────────────────────── */
@keyframes dr-enter {
  0%   { opacity:0;transform:translate(var(--tx),var(--ty)) rotate(var(--rot)) scale(.35); }
  55%  { opacity:1; }
  78%  { transform:translate(0,10px) rotate(calc(var(--rot)*.04)) scale(1.06); }
  90%  { transform:translate(0,-5px) scale(.97); }
  100% { opacity:1;transform:translate(0,0) rotate(0deg) scale(1); }
}
@keyframes dr-wobble {
  0%,100% { transform:rotate(-4deg) scale(1); }
  25%      { transform:rotate(5deg) scale(1.03); }
  50%      { transform:rotate(-3deg) scale(.98); }
  75%      { transform:rotate(4deg) scale(1.02); }
}
.dr-die.dr-entering {
  animation:dr-enter .92s cubic-bezier(.23,1,.32,1) var(--delay,0s) forwards;
}
.dr-die.dr-entering .dr-face {
  animation:dr-wobble .16s linear infinite;
}

/* ── SETTLE ────────────────────────────────────────────────────── */
@keyframes dr-settle {
  0%  { transform:scale(1.28); }
  38% { transform:scale(.9); }
  62% { transform:scale(1.09); }
  80% { transform:scale(.97); }
  100%{ transform:scale(1); }
}
.dr-die.dr-settled { opacity:1; }
.dr-die.dr-settled .dr-face {
  animation:dr-settle .52s cubic-bezier(.34,1.56,.64,1) forwards;
  border-color:#c9a440;
  box-shadow:0 0 28px rgba(201,164,64,.35),0 0 56px rgba(201,164,64,.12),inset 0 0 18px rgba(201,164,64,.05);
}
.dr-die.dr-settled .dr-num { color:#c9a440;text-shadow:0 0 12px rgba(201,164,64,.5); }

/* ── DISCARDED (advantage/disadvantage non-chosen dice) ─────────── */
.dr-die.dr-discarded .dr-face {
  animation:dr-settle .52s cubic-bezier(.34,1.56,.64,1) forwards;
  border-color:rgba(255,255,255,.1);
  box-shadow:none;
  opacity:.35;
}
.dr-die.dr-discarded .dr-num { color:rgba(255,255,255,.2);text-shadow:none; }

/* ── NATURAL 20 ─────────────────────────────────────────────────── */
@keyframes dr-crit-glow {
  0%,100%{ box-shadow:0 0 32px rgba(255,215,0,.65),0 0 72px rgba(255,215,0,.28); }
  50%    { box-shadow:0 0 56px rgba(255,215,0,.9), 0 0 120px rgba(255,215,0,.5); }
}
.dr-die.dr-nat20 .dr-face {
  border-color:#ffd700;
  animation:dr-settle .52s cubic-bezier(.34,1.56,.64,1) forwards,
            dr-crit-glow 1.3s ease-in-out .52s infinite;
}
.dr-die.dr-nat20 .dr-num { color:#ffd700;text-shadow:0 0 20px #ffd700,0 0 40px rgba(255,215,0,.5); }

/* ── FUMBLE ─────────────────────────────────────────────────────── */
.dr-die.dr-nat1 .dr-face {
  border-color:#c03020;
  box-shadow:0 0 28px rgba(192,48,32,.5),inset 0 0 20px rgba(192,48,32,.12);
  animation:dr-settle .52s cubic-bezier(.34,1.56,.64,1) forwards;
}
.dr-die.dr-nat1 .dr-num { color:#ff5540; }

/* ── Result area ────────────────────────────────────────────────── */
#dr-result-area {
  display:flex;flex-direction:column;align-items:center;gap:8px;min-height:90px;
}

#dr-total {
  font-family:'Cinzel Decorative',serif;font-size:5rem;font-weight:900;
  line-height:1;opacity:0;
}
.dr-total-normal { color:#c9a440;text-shadow:0 0 28px rgba(201,164,64,.35); }
.dr-total-crit   { color:#ffd700;text-shadow:0 0 40px rgba(255,215,0,.65),0 0 80px rgba(255,215,0,.25); }
.dr-total-fumble { color:#ff5540;text-shadow:0 0 28px rgba(255,85,64,.4); }

@keyframes dr-pop {
  0%  { transform:scale(.4) translateY(16px); }
  60% { transform:scale(1.08) translateY(-4px); }
  80% { transform:scale(.96); }
  100%{ transform:scale(1) translateY(0); }
}
.dr-pop { animation:dr-pop .46s cubic-bezier(.34,1.56,.64,1) forwards; }

#dr-formula {
  font-family:'Crimson Text',Georgia,serif;
  font-size:1.05rem;color:rgba(232,220,192,.55);
  opacity:0;letter-spacing:.04em;transition:opacity .2s;
}

/* ── Banners ────────────────────────────────────────────────────── */
#dr-banner {
  font-family:'Cinzel Decorative',serif;font-size:1.7rem;font-weight:900;
  letter-spacing:.22em;text-transform:uppercase;
  opacity:0;transform:scale(.55) translateY(18px);
  transition:all .42s cubic-bezier(.34,1.56,.64,1);
}
#dr-banner.dr-banner-in { opacity:1;transform:scale(1) translateY(0); }
.dr-crit-banner   { color:#ffd700;text-shadow:0 0 30px #ffd700,0 0 60px rgba(255,215,0,.4); }
.dr-fumble-banner { color:#ff4422;text-shadow:0 0 24px rgba(255,68,34,.5); }

/* ── Particles ──────────────────────────────────────────────────── */
#dr-particles {
  position:absolute;inset:0;pointer-events:none;overflow:hidden;
  display:flex;align-items:center;justify-content:center;
}
@keyframes dr-fly {
  from { transform:translate(0,0) scale(1);opacity:1; }
  to   { transform:translate(var(--px),var(--py)) scale(0);opacity:0; }
}
.dr-particle {
  position:absolute;
  width:var(--sz);height:var(--sz);
  background:var(--col);border-radius:50%;
  animation:dr-fly var(--dur) ease-out var(--dl) forwards;
}

/* ── Hint ───────────────────────────────────────────────────────── */
#dr-hint {
  font-family:'Crimson Text',serif;font-size:.78rem;
  color:rgba(232,220,192,.2);letter-spacing:.08em;
}
    `;
    document.head.appendChild(s);
  }

  return { roll, rollCheck };
})();
