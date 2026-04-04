/**
 * Level-Up modal — works on /play, /characters, and /step/9.
 *
 * Usage:
 *   LevelUp.open()          — character already in session (/play, /step/9)
 *   LevelUp.open(index)     — load character[index] from localStorage first (/characters)
 */
const LevelUp = (() => {
  const STORAGE_KEY = 'characterForge_characters';
  let _charIndex = null;  // set when opened from /characters

  // ── Public ──────────────────────────────────────────────────────────────────

  async function open(charIndex) {
    _charIndex = charIndex !== undefined ? charIndex : null;

    if (_charIndex !== null) {
      const characters = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
      const char = characters[_charIndex];
      if (!char) { alert('Character not found.'); return; }

      const loadResp = await fetch('/characters/load', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(char.draft)
      });
      if (!loadResp.ok) { alert('Failed to load character.'); return; }
    }

    const resp = await fetch('/play/levelup/options');
    if (!resp.ok) {
      const data = await resp.json().catch(() => ({}));
      alert(data.error || 'Cannot level up (already at level 20?)');
      return;
    }
    const options = await resp.json();
    _showModal(options);
  }

  // ── Modal rendering ──────────────────────────────────────────────────────────

  function _showModal(options) {
    document.getElementById('lu-modal')?.remove();

    const el = document.createElement('div');
    el.id = 'lu-modal';
    el.style.cssText = `
      position:fixed;inset:0;z-index:9999;display:flex;align-items:center;
      justify-content:center;background:rgba(0,0,0,.75);
    `;
    el.innerHTML = `
      <div style="
        background:linear-gradient(135deg,rgba(37,29,14,.98) 0%,rgba(25,20,10,.98) 100%);
        border:1px solid #c9a440;border-radius:8px;max-width:640px;width:92%;
        max-height:88vh;overflow-y:auto;font-family:'Crimson Text',serif;color:#e8dcc0;
      ">
        <div style="display:flex;justify-content:space-between;align-items:center;
                    padding:20px 24px 16px;border-bottom:1px solid rgba(201,164,64,.3)">
          <span style="font-family:'Cinzel Decorative',serif;font-size:1.3rem;color:#c9a440">
            Level Up → Level ${options.newLevel}
          </span>
          <button onclick="document.getElementById('lu-modal').remove()"
                  style="background:none;border:none;color:#c9a440;font-size:1.4rem;cursor:pointer">✕</button>
        </div>
        <div id="lu-body" style="padding:20px 24px">${_buildBody(options)}</div>
        <div style="padding:16px 24px;border-top:1px solid rgba(201,164,64,.3);text-align:right">
          <button id="lu-confirm-btn" onclick="LevelUp._confirm()"
                  style="padding:10px 24px;border:1px solid #c9a440;background:rgba(201,164,64,.15);
                         color:#c9a440;border-radius:4px;cursor:pointer;
                         font-family:'Cinzel',serif;font-size:.9rem;transition:all .2s">
            Confirm Level Up
          </button>
        </div>
      </div>
    `;
    document.body.appendChild(el);
    _wireEvents();
  }

  function _section(title, body) {
    return `
      <div style="margin-bottom:20px;padding-bottom:16px;border-bottom:1px solid rgba(201,164,64,.15)">
        <div style="font-family:'Cinzel',serif;font-size:.85rem;color:#c9a440;margin-bottom:10px;
                    text-transform:uppercase;letter-spacing:.05em">${title}</div>
        ${body}
      </div>
    `;
  }

  function _ordinal(n) {
    return ['','1st','2nd','3rd','4th','5th','6th','7th','8th','9th'][n] || (n + 'th');
  }

  function _buildBody(o) {
    let html = '';

    // ── What you gained ────────────────────────────────────────────────────────
    const gained = [];
    if (o.newFeatures?.length) {
      o.newFeatures.forEach(f => gained.push(
        `<li><strong>${f.name}</strong>${f.description ? ' — ' + f.description : ''}</li>`
      ));
    }
    gained.push(`<li>Hit point maximum: <strong>+${o.hpGain}</strong></li>`);
    if (o.profBonusChanged) gained.push(`<li>Proficiency bonus increased</li>`);
    if (o.spellSlotsChanged) gained.push(`<li>Spell slots: ${o.newSpellSlotSummary}</li>`);
    if (o.preparedSpellsGain > 0) gained.push(`<li>You can now prepare one additional spell</li>`);
    if (o.unlocksNewSpellLevel) gained.push(`<li>${_ordinal(o.newUnlockedSpellLevel)}-level spells are now accessible</li>`);
    html += _section('What you gained', `<ul style="margin:0;padding-left:20px;line-height:1.8">${gained.join('')}</ul>`);

    // ── Subclass ───────────────────────────────────────────────────────────────
    if (o.needsSubclass) {
      const opts = o.availableSubclasses.map(s =>
        `<option value="${s.id}">${s.name}${s.description ? ' — ' + s.description : ''}</option>`
      ).join('');
      html += _section('Choose Your Subclass',
        `<select id="lu-subclass" style="${_selectStyle()}">
          <option value="">Select subclass...</option>${opts}
        </select>`
      );
    }

    // ── ASI / Feat ─────────────────────────────────────────────────────────────
    if (o.needsAsi) {
      const statOpts = ['STR','DEX','CON','INT','WIS','CHA']
        .map(s => `<option value="${s}">${s}</option>`).join('');
      const featOpts = o.availableFeats.map(f =>
        `<option value="${f.id}">${f.name}${f.prerequisite ? ' (' + f.prerequisite + ')' : ''}</option>`
      ).join('');
      html += _section('Ability Score Improvement', `
        <div style="display:flex;gap:16px;margin-bottom:12px;flex-wrap:wrap">
          <label style="cursor:pointer"><input type="radio" name="lu-asi-type" value="single" checked> +2 to one ability</label>
          <label style="cursor:pointer"><input type="radio" name="lu-asi-type" value="split"> +1 / +1 to two</label>
          <label style="cursor:pointer"><input type="radio" name="lu-asi-type" value="feat"> Take a Feat</label>
        </div>
        <div id="lu-asi-single">
          <select id="lu-asi-stat1" style="${_selectStyle()}">
            <option value="">Choose ability...</option>${statOpts}
          </select>
        </div>
        <div id="lu-asi-split" style="display:none">
          <div style="display:flex;gap:8px">
            <select id="lu-asi-split1" style="${_selectStyle()}">
              <option value="">Ability 1...</option>${statOpts}
            </select>
            <select id="lu-asi-split2" style="${_selectStyle()}">
              <option value="">Ability 2...</option>${statOpts}
            </select>
          </div>
        </div>
        <div id="lu-asi-feat" style="display:none">
          <select id="lu-feat-select" style="${_selectStyle()}">
            <option value="">Choose feat...</option>${featOpts}
          </select>
        </div>
      `);
    }

    // ── New cantrips ───────────────────────────────────────────────────────────
    if (o.newCantripsCount > 0) {
      const btns = o.availableCantrips.map(s =>
        `<button type="button" class="lu-pick" data-id="${s.id}" data-type="cantrip"
                 data-max="${o.newCantripsCount}" style="${_spellBtnStyle()}"
                 onclick="LevelUp._toggleSpell(this)">${s.name}</button>`
      ).join('');
      html += _section(`New Cantrip${o.newCantripsCount > 1 ? 's' : ''} — Pick ${o.newCantripsCount}`,
        `<div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── New spells ─────────────────────────────────────────────────────────────
    if (o.newSpellsCount > 0) {
      const btns = o.availableSpells.map(s =>
        `<button type="button" class="lu-pick" data-id="${s.id}" data-type="spell"
                 data-max="${o.newSpellsCount}" style="${_spellBtnStyle()}"
                 onclick="LevelUp._toggleSpell(this)">${s.name}
           <span style="font-size:.7rem;opacity:.7">(${s.level}th)</span></button>`
      ).join('');
      html += _section(`New Spell${o.newSpellsCount > 1 ? 's' : ''} — Pick ${o.newSpellsCount}`,
        `<div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── Wizard spellbook ───────────────────────────────────────────────────────
    if (o.wizardSpellbookGain > 0) {
      const btns = o.availableSpells.map(s =>
        `<button type="button" class="lu-pick" data-id="${s.id}" data-type="spellbook"
                 data-max="${o.wizardSpellbookGain}" style="${_spellBtnStyle()}"
                 onclick="LevelUp._toggleSpell(this)">${s.name}
           <span style="font-size:.7rem;opacity:.7">(${s.level}th)</span></button>`
      ).join('');
      html += _section(`Add ${o.wizardSpellbookGain} Spells to Spellbook`,
        `<div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── Expertise ──────────────────────────────────────────────────────────────
    if (o.needsExpertise) {
      const btns = (o.eligibleExpertiseSkills || []).map(sk =>
        `<button type="button" class="lu-pick" data-id="${sk}" data-type="expertise"
                 data-max="${o.expertiseCount}" style="${_spellBtnStyle()}"
                 onclick="LevelUp._toggleSpell(this)">${sk}</button>`
      ).join('');
      html += _section(`Expertise — Pick ${o.expertiseCount} Skills`,
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Choose skills to gain Expertise (doubled proficiency bonus):</p>
         <div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── Magical Secrets ────────────────────────────────────────────────────────
    if (o.needsMagicalSecrets) {
      const grouped = {};
      (o.availableMagicalSecrets || []).forEach(sp => {
        const k = sp.level;
        if (!grouped[k]) grouped[k] = [];
        grouped[k].push(sp);
      });
      let spellHtml = '';
      Object.keys(grouped).sort((a,b) => a - b).forEach(lvl => {
        spellHtml += `<div style="margin-bottom:8px"><span style="font-size:.8rem;color:#c9a440;opacity:.8">${_ordinal(+lvl)}-level</span><br>
          <div style="display:flex;flex-wrap:wrap;gap:4px;margin-top:4px">`;
        grouped[lvl].forEach(sp => {
          spellHtml += `<button type="button" class="lu-pick" data-id="${sp.id}" data-type="magical_secret"
                                data-max="2" style="${_spellBtnStyle()}" onclick="LevelUp._toggleSpell(this)">${sp.name}</button>`;
        });
        spellHtml += '</div></div>';
      });
      html += _section('Magical Secrets — Pick 2 Spells from Any Class',
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">These spells become part of your spells known:</p>${spellHtml}`);
    }

    // ── Pact Boon ──────────────────────────────────────────────────────────────
    if (o.needsPactBoon) {
      html += _section('Choose Your Pact Boon',
        `<select id="lu-pact-boon" style="${_selectStyle()}">
          <option value="">Select boon...</option>
          <option value="chain">Pact of the Chain — Gain a familiar with special forms</option>
          <option value="blade">Pact of the Blade — Summon a magical pact weapon</option>
          <option value="tome">Pact of the Tome — Gain a Book of Shadows with 3 cantrips</option>
        </select>`);
    }

    // ── Eldritch Invocations ───────────────────────────────────────────────────
    if (o.needsInvocations) {
      const btns = (o.availableInvocations || []).map(inv =>
        `<button type="button" class="lu-pick" data-id="${inv.id}" data-type="invocation"
                 data-max="${o.newInvocationsCount}" style="${_spellBtnStyle()}"
                 title="${inv.desc}" onclick="LevelUp._toggleSpell(this)">${inv.name}</button>`
      ).join('');
      html += _section(`Eldritch Invocations — Pick ${o.newInvocationsCount}`,
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Hover for description. Filtered by level and pact requirements:</p>
         <div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── Metamagic ──────────────────────────────────────────────────────────────
    if (o.needsMetamagic) {
      const btns = (o.availableMetamagic || []).map(m =>
        `<button type="button" class="lu-pick" data-id="${m.id}" data-type="metamagic"
                 data-max="${o.newMetamagicCount}" style="${_spellBtnStyle()}"
                 title="${m.desc}" onclick="LevelUp._toggleSpell(this)">${m.name}</button>`
      ).join('');
      html += _section(`Metamagic — Pick ${o.newMetamagicCount}`,
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Hover for description. Sorcery Points (SP) costs shown in description:</p>
         <div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    // ── Ranger: Favored Enemy ──────────────────────────────────────────────────
    if (o.needsFavoredEnemy) {
      const opts = (o.favoredEnemyTypes || []).map(t =>
        `<option value="${t}">${t}</option>`
      ).join('');
      html += _section('Additional Favored Enemy',
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Advantage on Survival to track and INT checks to recall information:</p>
         <select id="lu-favored-enemy" style="${_selectStyle()}">
           <option value="">Select enemy type...</option>${opts}
         </select>`);
    }

    // ── Ranger: Natural Explorer ───────────────────────────────────────────────
    if (o.needsNaturalExplorer) {
      const opts = (o.naturalExplorerTerrains || []).map(t =>
        `<option value="${t}">${t}</option>`
      ).join('');
      html += _section('Additional Favored Terrain',
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Gain natural explorer benefits in this terrain type:</p>
         <select id="lu-natural-explorer" style="${_selectStyle()}">
           <option value="">Select terrain...</option>${opts}
         </select>`);
    }

    // ── Warlock: Mystic Arcanum ────────────────────────────────────────────────
    if (o.needsMysticArcanum) {
      const btns = (o.availableMysticArcanum || []).map(s =>
        `<button type="button" class="lu-pick" data-id="${s.id}" data-type="mystic_arcanum"
                 data-max="1" style="${_spellBtnStyle()}"
                 onclick="LevelUp._toggleSpell(this)">${s.name}</button>`
      ).join('');
      html += _section(`Mystic Arcanum — Choose a ${_ordinal(o.mysticArcanumLevel)}-Level Spell`,
        `<p style="margin:0 0 10px;font-size:.9rem;opacity:.8">Cast once per long rest without a spell slot:</p>
         <div style="display:flex;flex-wrap:wrap;gap:6px">${btns}</div>`);
    }

    return html;
  }

  function _selectStyle() {
    return 'background:rgba(0,0,0,.4);border:1px solid rgba(201,164,64,.4);' +
           'color:#e8dcc0;padding:8px 10px;border-radius:4px;width:100%;' +
           'font-family:"Crimson Text",serif;font-size:1rem;cursor:pointer';
  }

  function _spellBtnStyle() {
    return 'background:rgba(0,0,0,.3);border:1px solid rgba(201,164,64,.3);' +
           'color:#e8dcc0;padding:6px 10px;border-radius:4px;cursor:pointer;' +
           'font-family:"Crimson Text",serif;font-size:.9rem;transition:all .15s';
  }

  // ── Events ───────────────────────────────────────────────────────────────────

  function _wireEvents() {
    document.querySelectorAll('input[name="lu-asi-type"]').forEach(r => {
      r.addEventListener('change', () => {
        document.getElementById('lu-asi-single').style.display = r.value === 'single' ? '' : 'none';
        document.getElementById('lu-asi-split').style.display  = r.value === 'split'  ? '' : 'none';
        document.getElementById('lu-asi-feat').style.display   = r.value === 'feat'   ? '' : 'none';
      });
    });
  }

  function _toggleSpell(btn) {
    const type = btn.dataset.type;
    const max  = parseInt(btn.dataset.max, 10);
    const selected = document.querySelectorAll(`.lu-pick[data-type="${type}"].lu-selected`);

    if (btn.classList.contains('lu-selected')) {
      btn.classList.remove('lu-selected');
      btn.style.background = 'rgba(0,0,0,.3)';
      btn.style.borderColor = 'rgba(201,164,64,.3)';
    } else if (selected.length < max) {
      btn.classList.add('lu-selected');
      btn.style.background = 'rgba(201,164,64,.2)';
      btn.style.borderColor = '#c9a440';
    }
  }

  // ── Confirm ──────────────────────────────────────────────────────────────────

  async function _confirm() {
    const body = _gatherChoices();

    const confirmBtn = document.getElementById('lu-confirm-btn');
    if (confirmBtn) { confirmBtn.disabled = true; confirmBtn.textContent = 'Levelling up...'; }

    const resp = await fetch('/play/levelup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const result = await resp.json();

    if (!result.success) {
      alert(result.error || 'Error levelling up.');
      if (confirmBtn) { confirmBtn.disabled = false; confirmBtn.textContent = 'Confirm Level Up'; }
      return;
    }

    _saveToLocalStorage(result.draft);
    document.getElementById('lu-modal')?.remove();
    location.reload();
  }

  function _gatherChoices() {
    const body = { newSpells: [], newCantrips: [], spellbookAdditions: [] };

    // Spells: merge regular spell picks and magical secret picks
    document.querySelectorAll('.lu-pick.lu-selected[data-type="spell"]').forEach(b => body.newSpells.push(b.dataset.id));
    document.querySelectorAll('.lu-pick.lu-selected[data-type="magical_secret"]').forEach(b => body.newSpells.push(b.dataset.id));
    document.querySelectorAll('.lu-pick.lu-selected[data-type="cantrip"]').forEach(b => body.newCantrips.push(b.dataset.id));
    document.querySelectorAll('.lu-pick.lu-selected[data-type="spellbook"]').forEach(b => body.spellbookAdditions.push(b.dataset.id));

    const asiRadio = document.querySelector('input[name="lu-asi-type"]:checked');
    if (asiRadio) {
      body.asiType = asiRadio.value;
      if (asiRadio.value === 'single') {
        body.asiMode  = 'single';
        body.asiStat1 = document.getElementById('lu-asi-stat1')?.value || null;
      } else if (asiRadio.value === 'split') {
        body.asiMode  = 'split';
        body.asiStat1 = document.getElementById('lu-asi-split1')?.value || null;
        body.asiStat2 = document.getElementById('lu-asi-split2')?.value || null;
      } else if (asiRadio.value === 'feat') {
        body.featId = document.getElementById('lu-feat-select')?.value || null;
      }
    }

    const subclassSel = document.getElementById('lu-subclass');
    if (subclassSel) body.subclassId = subclassSel.value || null;

    // New choice types
    body.expertiseSkills = [...document.querySelectorAll('.lu-pick.lu-selected[data-type="expertise"]')]
      .map(b => b.dataset.id);
    body.pactBoon        = document.getElementById('lu-pact-boon')?.value ?? '';
    body.newInvocations  = [...document.querySelectorAll('.lu-pick.lu-selected[data-type="invocation"]')]
      .map(b => b.dataset.id);
    body.newMetamagic    = [...document.querySelectorAll('.lu-pick.lu-selected[data-type="metamagic"]')]
      .map(b => b.dataset.id);
    body.favoredEnemy    = document.getElementById('lu-favored-enemy')?.value ?? '';
    body.naturalExplorer = document.getElementById('lu-natural-explorer')?.value ?? '';
    body.mysticArcanumSpell = document.querySelector('.lu-pick.lu-selected[data-type="mystic_arcanum"]')
      ?.dataset.id ?? '';

    return body;
  }

  function _saveToLocalStorage(updatedDraft) {
    const characters = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    let idx = _charIndex;
    if (idx === null) {
      // From /play or /step/9 — find by name + class
      idx = characters.findIndex(c =>
        c.draft &&
        c.draft.characterName === updatedDraft.characterName &&
        c.draft.characterClass === updatedDraft.characterClass
      );
    }
    if (idx >= 0 && idx < characters.length) {
      characters[idx].draft = updatedDraft;
      characters[idx].level = updatedDraft.level;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(characters));
    }
  }

  // ── Public API ────────────────────────────────────────────────────────────────
  return { open, _confirm, _toggleSpell };
})();
