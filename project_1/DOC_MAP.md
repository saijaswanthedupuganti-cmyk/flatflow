# Habitiq — Documentation Map

> Which file to open. Everything else is legacy, archive, or noise.  
> Updated: Aug 2026 · See also [[FOUNDATION]]

---

## Always start here

| Priority | File | Use for |
|----------|------|---------|
| 1 | `FOUNDATION.md` | Architecture basement, web-first rules, mobile path, repo structure |
| 2 | `Habitiq — Project Documentation.md` | Full product memory: features, schema, flows, roadmap, history |
| 3 | `About Sai.md` | Founder context |

---

## Logic & security (contracts)

| File | Use for |
|------|---------|
| `CONDITIONS.md` | Business logic: rotation, swaps, expenses, bills, settlements, rewards |
| `PERMISSIONS.md` | Who can do what (admin / member / subscription gates) |
| `../firestore.rules` | Live authorization (code — not vault prose) |

---

## Design

| File | Use for |
|------|---------|
| `../DESIGN.md` | Tokens, surfaces, UI language |
| `../UI_PLAN.md` | UI implementation roadmap |
| `../design-system/MASTER.md` | Design-system architecture |
| `../design-system/FINANCE_ARCHITECTURE.md` | Finance UI architecture |
| `PITCH_DESIGN_SYSTEM.md` | Pitch / brand presentation system |
| `design-analysis/` | Batch UI analysis notes |

---

## Features & strategy

| File | Use for |
|------|---------|
| `voice/` | Voice assistant playbook, corpus, VOICE.md |
| `FLAT_DISCOVERY_FEATURE.md` | Flat Board / Discover |
| `Habitiq_Master_Cross_Check_Document.md` | Large feature/use-case cross-check |
| `Habitiq_5_Year_Strategic_Plan.md` | Long-range strategy |
| `PRODUCTION_STATUS.md` | Snapshot of production readiness |
| `Conditions & Use Cases.md` | Broader use-case catalogue |

---

## Android

| Location | Use for |
|----------|---------|
| `../android/docs/superpowers/specs/` | Approved native designs |
| `../android/docs/superpowers/plans/` | Implementation plans |
| `C:\habitiq_jaswanth\` | Active Android app code |

---

## Root ops (keep)

| File | Use for |
|------|---------|
| `../README.md` | Repo entry for humans / GitHub |
| `../CLAUDE.md` · `../AGENTS.md` | Agent bootstrap |
| `../FIREBASE_SETUP.md` | Firebase setup |
| `../SECURITY.md` · `../SECURITY_AUDIT.md` | Security notes |
| `../HABITIQ_QA_GUIDE.md` | QA checklist |
| `../HABITIQ_COFOUNDER_BRIEF.md` | Co-founder brief |

---

## Legacy — do not extend

These are historical snapshots. **Do not add new product truth here.** Point readers to the vault canonical doc instead.

| File | Note |
|------|------|
| `../PRODUCT.md` | Older product summary (superseded by vault) |
| `../HABITIQ_MASTER_DOCUMENT.md` | Older master dump |
| `../FLATFLOW_LAUNCH_DOCUMENTATION.md` | Pre-rebrand / launch history |
| `archive/` | Session dumps (e.g. `2026-06-18.md`) |

---

## Forbidden at repo root

Do not leave or commit: `*.log`, empty `build-check.txt`, one-off `patch-*.js` / `fix-*.js`, inspect screenshots/videos, cloned third-party design repos, empty folders, HTML dumps of screens.
