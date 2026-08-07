# Habitiq — Master Feature & Implementation Cross-Check Document

> **Document Type:** Consolidated Source of Truth  
> **Purpose:** Cross-check all features, flows, algorithms, and use cases with Claude Code  
> **Coverage:** Flat Discovery + Trust Engine + Voice Assistant + All Use Cases  
> **Total Use Cases:** 2,000+  
> **Last Updated:** 2026-06-22  
> **For:** Claude Code — Verify nothing is missing, implement what is not done  

---

## TABLE OF CONTENTS

1. [Feature Inventory (ALL)](#1-feature-inventory-all)
2. [Voice Assistant — Complete Spec](#2-voice-assistant--complete-spec)
3. [Trust & Discovery Engine — Complete Spec](#3-trust--discovery-engine--complete-spec)
4. [Use Case Matrix (2,000+)](#4-use-case-matrix-2000)
5. [Implementation Status Tracker](#5-implementation-status-tracker)
6. [Cross-Check Checklist for Claude Code](#6-cross-check-checklist-for-claude-code)
7. [Missing / Gap Analysis](#7-missing--gap-analysis)
8. [File Inventory (31 Files)](#8-file-inventory-31-files)

---

## 1. FEATURE INVENTORY (ALL)

### A. EXISTING FEATURES (v0.1.0 — MUST PRESERVE)

| # | Feature | Status | Notes |
|---|---------|--------|-------|
| 1 | Google Sign-In | ✅ LIVE | Must not break |
| 2 | Email/Password Auth | ✅ LIVE | Must not break |
| 3 | Custom Auth Domain Proxy | ✅ LIVE | iOS Safari fix |
| 4 | Create Flat + Invite Code | ✅ LIVE | Must not break |
| 5 | Join Flat via Invite Code | ✅ LIVE | Must not break |
| 6 | Smart Rotation Engine | ✅ LIVE | Core product — NEVER break |
| 7 | OOS (Out of Station) Skip/Resume | ✅ LIVE | Core rotation logic |
| 8 | Task Management (CRUD) | ✅ LIVE | Admin only for create/edit/delete |
| 9 | Task Completion (One-tap) | ✅ LIVE | Must not break |
| 10 | Retroactive Completion Date Edit | ✅ LIVE | Must not break |
| 11 | Swap Request System | ✅ LIVE | Formal accept/decline flow |
| 12 | Rotation Order Card | ✅ LIVE | Queue visualization |
| 13 | Admin Controls (My Tasks + Org View) | ✅ LIVE | Must not break |
| 14 | Member Management (Leave/Kick/Transfer) | ✅ LIVE | Must not break |
| 15 | Multi-Flat Support (Gmail-style switcher) | ✅ LIVE | Must not break |
| 16 | Analytics & Insights (Completion Grid) | ✅ LIVE | Must not break |
| 17 | Calendar View | ✅ LIVE | Must not break |
| 18 | Activity Log (Immutable Audit Trail) | ✅ LIVE | Must not break |
| 19 | Bills & Expenses Module | ✅ LIVE | Recurring + One-off + Settle Up |
| 20 | Multi-Currency (INR, USD, EUR, GBP, AED, SGD, AUD) | ✅ LIVE | Must not break |
| 21 | Real-Time Sync (Firestore onSnapshot) | ✅ LIVE | Must not break |
| 22 | PWA (Installable, Service Worker) | ✅ LIVE | Must not break |
| 23 | Privacy Policy (DPDP 2023) | ✅ LIVE | Must not break |
| 24 | Terms of Service (India Law) | ✅ LIVE | Must not break |
| 25 | Subscription System (Trial/Active/Expired) | ✅ LIVE | Coupon-gated |
| 26 | Premium UI (Crown badge, Gold pill) | ✅ LIVE | Must not break |
| 27 | LEGACY_FREE Migration | ✅ LIVE | Must not break |
| 28 | Dark Mode + Light Mode | ✅ LIVE | Must not break |
| 29 | Mobile-First UI | ✅ LIVE | Must not break |
| 30 | Member Home Screen (Redesigned) | ✅ LIVE | Must not break |
| 31 | Navigation Redesign (Home · Duties · Log · Money · Nest) | ✅ LIVE | Must not break |
| 32 | Task Bottom Sheet | ✅ LIVE | Must not break |
| 33 | Bills vs Money Separation | ✅ LIVE | Must not break |

### B. NEW FEATURES — FLAT DISCOVERY & TRUST ENGINE

| # | Feature | Phase | Priority | Status |
|---|---------|-------|----------|--------|
| 34 | **Event-Sourced Behavioral Ledger** | Phase 0 | CRITICAL | ⬜ NOT STARTED |
| 35 | **Immutable Event Log (Hash Chain)** | Phase 0 | CRITICAL | ⬜ NOT STARTED |
| 36 | **Behavioral Profiles (Materialized View)** | Phase 0 | CRITICAL | ⬜ NOT STARTED |
| 37 | **Bayesian Trust Tag Engine** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 38 | **Tier Classification (New/Reliable/Plus)** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 39 | **Explainable Tag (Why This Tag?)** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 40 | **Tag Consent Flow (5th-event trigger)** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 41 | **Internal Tag Display Only (Phase 1)** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 42 | **Anti-Gaming: Minimum Flat Size Penalty** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 43 | **Anti-Gaming: Reciprocal Bias Filter** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 44 | **Anti-Gaming: Velocity Anomaly Detection** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 45 | **Anti-Gaming: Cross-Flat Collusion Graph** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 46 | **Anti-Gaming: Sybil Attack Detection** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 47 | **PG Verification (Google Places API)** | Phase 2 | HIGH | ⬜ NOT STARTED |
| 48 | **PG Multi-Source Verification (Bayesian)** | Phase 2 | HIGH | ⬜ NOT STARTED |
| 49 | **PG Spot-Check Algorithm** | Phase 2 | MEDIUM | ⬜ NOT STARTED |
| 50 | **Discovery Search (Liquidity-Gated)** | Phase 3 | MEDIUM | ⬜ NOT STARTED |
| 51 | **Compatibility Scoring Engine** | Phase 3 | MEDIUM | ⬜ NOT STARTED |
| 52 | **Mutual Match & Contact Reveal** | Phase 3 | MEDIUM | ⬜ NOT STARTED |
| 53 | **Jurisdiction Config Layer (IN/UK/EU/AU)** | Phase 1 | HIGH | ⬜ NOT STARTED |
| 54 | **DPDP/GDPR Consent Architecture** | Phase 1 | CRITICAL | ⬜ NOT STARTED |
| 55 | **Readiness Metrics Dashboard** | Phase 0 | MEDIUM | ⬜ NOT STARTED |
| 56 | **Liquidity Metrics Dashboard** | Phase 2 | MEDIUM | ⬜ NOT STARTED |
| 57 | **A/B Testing Framework** | Phase 1 | LOW | ⬜ NOT STARTED |

### C. NEW FEATURES — VOICE ASSISTANT

| # | Feature | Priority | Status |
|---|---------|----------|--------|
| 58 | **Web Speech API Integration** | CRITICAL | ⬜ NOT STARTED |
| 59 | **SpeechRecognition Hook (React)** | CRITICAL | ⬜ NOT STARTED |
| 60 | **iOS/Safari Fallback Modal** | CRITICAL | ⬜ NOT STARTED |
| 61 | **Intent Classification Engine (9 Intents)** | CRITICAL | ⬜ NOT STARTED |
| 62 | **REJECT Intent (False-Positive Filter)** | CRITICAL | ⬜ NOT STARTED |
| 63 | **Entity Extraction (Amount/Member/Task/Desc)** | CRITICAL | ⬜ NOT STARTED |
| 64 | **Number-Word Parser (English)** | CRITICAL | ⬜ NOT STARTED |
| 65 | **Number-Word Parser (Hinglish)** | CRITICAL | ⬜ NOT STARTED |
| 66 | **Number-Word Parser (Telugu-English)** | CRITICAL | ⬜ NOT STARTED |
| 67 | **Fuzzy Matching (Levenshtein)** | CRITICAL | ⬜ NOT STARTED |
| 68 | **Context Resolver (Flat Data Cache)** | CRITICAL | ⬜ NOT STARTED |
| 69 | **Action Router (7 Actions)** | CRITICAL | ⬜ NOT STARTED |
| 70 | **COMPLETE_TASK Action** | CRITICAL | ⬜ NOT STARTED |
| 71 | **CREATE_EXPENSE Action** | CRITICAL | ⬜ NOT STARTED |
| 72 | **QUERY_BALANCE Action** | CRITICAL | ⬜ NOT STARTED |
| 73 | **QUERY_TASKS Action** | CRITICAL | ⬜ NOT STARTED |
| 74 | **QUERY_STATUS Action** | CRITICAL | ⬜ NOT STARTED |
| 75 | **REQUEST_SWAP Action** | CRITICAL | ⬜ NOT STARTED |
| 76 | **CREATE_TASK Action** | MEDIUM | ⬜ NOT STARTED |
| 77 | **GREETING Action** | LOW | ⬜ NOT STARTED |
| 78 | **REJECT Action** | CRITICAL | ⬜ NOT STARTED |
| 79 | **UNKNOWN Fallback Action** | CRITICAL | ⬜ NOT STARTED |
| 80 | **Response Formatter + Card System** | CRITICAL | ⬜ NOT STARTED |
| 81 | **Text-to-Speech (SpeechSynthesis)** | HIGH | ⬜ NOT STARTED |
| 82 | **Voice Button (Nav Center FAB)** | CRITICAL | ⬜ NOT STARTED |
| 83 | **Listening Overlay + Waveform** | HIGH | ⬜ NOT STARTED |
| 84 | **Voice Response Card (Slide-Up)** | HIGH | ⬜ NOT STARTED |
| 85 | **Voice Settings Panel** | MEDIUM | ⬜ NOT STARTED |
| 86 | **Microphone Permission Handler** | HIGH | ⬜ NOT STARTED |
| 87 | **Voice Analytics (Privacy-First)** | LOW | ⬜ NOT STARTED |
| 88 | **Contextual Awareness (Screen-Based)** | MEDIUM | ⬜ NOT STARTED |
| 89 | **Long-Press Behavior (Voice vs Manual Log)** | MEDIUM | ⬜ NOT STARTED |
| 90 | **Confirmation Flow (High-Stakes Actions)** | HIGH | ⬜ NOT STARTED |
| 91 | **Disambiguation Dialogues** | HIGH | ⬜ NOT STARTED |
| 92 | **Error Recovery + Follow-Up Suggestions** | HIGH | ⬜ NOT STARTED |
| 93 | **Hinglish Pattern Support (All Intents)** | CRITICAL | ⬜ NOT STARTED |
| 94 | **Telugu-English Pattern Support (All Intents)** | CRITICAL | ⬜ NOT STARTED |
| 95 | **Multi-Intent Sequences (Chained Commands)** | MEDIUM | ⬜ NOT STARTED |

---

## 2. VOICE ASSISTANT — COMPLETE SPEC

### 2.1 Architecture (5 Layers)

```
LAYER 1: UI (Mic Button → Overlay → Response Card → Settings)
LAYER 2: Speech Recognition (Web Speech API → iOS Fallback)
LAYER 3: NLU (Intent Classifier → Entity Extractor → Context Resolver)
LAYER 4: Action Execution (Router → 10 Action Handlers → Firestore)
LAYER 5: Response (Formatter → TTS → Visual Card)
```

### 2.2 Intent Classification (9 Intents + REJECT)

| Intent | Patterns (Total) | Languages | Key Triggers |
|--------|-----------------|-----------|--------------|
| **COMPLETE_TASK** | 200+ | EN + HINGLISH + TELUGU | done, ho gaya, aipoyindi, is clean, are washed |
| **CREATE_EXPENSE** | 250+ | EN + HINGLISH + TELUGU | spent, diye, icha, add, kharcha, pay chesa |
| **QUERY_BALANCE** | 100+ | EN + HINGLISH + TELUGU | how much, kitna, entha, show me the money, balance |
| **QUERY_TASKS** | 75+ | EN + HINGLISH + TELUGU | what are my tasks, kya karna hai, em cheyali |
| **QUERY_STATUS** | 50+ | EN + HINGLISH + TELUGU | who is home, ghar pe hai, intlo unnaru |
| **REQUEST_SWAP** | 75+ | EN + HINGLISH + TELUGU | cover, help me, busy, sick, madad karo, bimaar |
| **CREATE_TASK** | 50+ | EN + HINGLISH + TELUGU | add task, naya task, new task, daily, weekly |
| **GREETING** | 25+ | EN + HINGLISH + TELUGU | hi, hello, namaste, good morning, sup |
| **REJECT** | 150+ | EN + HINGLISH + TELUGU | time, weather, music, call, alarm, joke, news |
| **UNKNOWN** | Fallback | EN + HINGLISH + TELUGU | Random → suggestion rotation |

### 2.3 Entity Extraction Requirements

| Entity | Extraction Methods | Examples |
|--------|-------------------|----------|
| **Amount** | Regex (Rs/rupees/₹) + Bare digits + Number words (EN) + Number words (Hinglish) + Number words (Telugu) | 500, Rs 500, five hundred, paanch sau, aidu vandala |
| **Member** | Fuzzy match against flat.memberNames | Bhanu, Kiran, Rohin, Sai, nicknames |
| **Task** | Fuzzy match against flat.taskNames | Kitchen, Bathroom, Garbage, Dusting |
| **Description** | Remaining words after removing known entities | groceries, dinner, electricity, Netflix |
| **Frequency** | Regex: daily/weekly/fortnightly/monthly | daily, har din, every week |
| **Split Type** | equal/custom/percentage | split equally, sab mein, divide |
| **Date** | today/tomorrow/yesterday | aaj, kal, iroju, repu |

### 2.4 Action → Firestore Mapping

| Action | Firestore Collection | Operation | Document Shape |
|--------|---------------------|-----------|----------------|
| COMPLETE_TASK | `tasks/{taskId}` | Update | `{completed: true, completedAt: timestamp, completedBy: userId}` |
| CREATE_EXPENSE | `expenses/{expenseId}` | Create | `{amount, currency, description, createdBy, splits: [{memberId, amount, settled}], createdAt}` |
| QUERY_BALANCE | `expenses` + `balances` | Read | Compute from unsettled splits |
| QUERY_TASKS | `tasks` | Read | Filter: `assignedTo == userId && completed == false` |
| QUERY_STATUS | `flats/{flatId}/members` | Read | Filter: `isOOS == true/false` |
| REQUEST_SWAP | `swapRequests/{swapId}` | Create | `{taskId, fromUserId, toUserId, status: 'pending', createdAt}` |
| CREATE_TASK | `tasks/{taskId}` | Create | `{name, frequency, flatId, createdBy, createdAt, rotationQueue: []}` |
| GREETING | None | — | Return contextual greeting + task count |
| REJECT | None | — | Return boundary message + valid suggestions |
| UNKNOWN | None | — | Return random suggestion from rotation |

### 2.5 UI Components (6 Components)

| Component | File | Purpose | Animation |
|-----------|------|---------|-----------|
| VoiceButton | `components/voice/VoiceButton.tsx` | Nav center FAB, 3 states | Pulse (idle), expand (listening), shake (error) |
| VoiceOverlay | `components/voice/VoiceOverlay.tsx` | Full-screen listening | Fade in, spring up |
| WaveformVisualizer | `components/voice/WaveformVisualizer.tsx` | Canvas audio bars | 60fps sine+noise animation |
| VoiceResponseCard | `components/voice/VoiceResponseCard.tsx` | Slide-up result card | Slide up from bottom, auto-dismiss 8s |
| VoiceFallbackModal | `components/voice/VoiceFallbackModal.tsx` | iOS text input | Bottom sheet spring |
| VoiceSettings | `components/voice/VoiceSettings.tsx` | Settings toggles | Standard settings panel |

### 2.6 Response Templates (70+ Templates)

| Category | Count | Examples |
|----------|-------|----------|
| Success — Task Complete | 5 | "Kitchen marked done. Great work!", "Kitchen ho gaya. Accha kaam!" |
| Success — Expense Added | 5 | "Added ₹500 for groceries. Split equally.", "₹500 groceries pe add kar diya." |
| Success — Balance Query | 10 | "Bhanu owes you ₹450.", "Bhanu aapko ₹450 dene wala hai.", "Bhanu meeku ₹450 ivvali." |
| Success — Task List | 5 | "You have 3 pending tasks: Kitchen, Bathroom, Garbage." |
| Success — Status Query | 5 | "Bhanu and Kiran are home. Rohin is out of station." |
| Success — Swap Sent | 5 | "Swap request sent for Kitchen. Waiting for Bhanu." |
| Success — Task Created | 3 | "Daily Kitchen Cleaning task created." |
| Error — Unknown | 5 | "I didn't understand. Try: 'Kitchen done'" |
| Error — No Task | 3 | "Which task? Kitchen, bathroom, or garbage?" |
| Error — No Member | 3 | "I don't know 'Ramu'. Did you mean Bhanu or Kiran?" |
| Error — No Amount | 3 | "How much? Say the amount." |
| Error — Permission | 3 | "Microphone access needed. Enable in Settings." |
| Error — Not Supported | 3 | "Voice not supported. Type your command instead." |
| Follow-Up Suggestions | 15 | Context-aware next action suggestions |
| Greeting | 5 | Time-based greetings with task count |
| Reject | 5 | "I only handle tasks, expenses, and flat info." |

---

## 3. TRUST & DISCOVERY ENGINE — COMPLETE SPEC

### 3.1 Phase 0: Event Ledger (Data Foundation)

| Requirement | Detail |
|-------------|--------|
| **Table** | `behavioral_events` (partitioned by YYYYMM) |
| **Properties** | Immutable, append-only, hash chain, HMAC signature |
| **Event Types** | 16 types: CHORE_ASSIGNED, CHORE_COMPLETED, CHORE_COMPLETED_LATE, CHORE_MISSED, EXPENSE_CREATED, EXPENSE_SETTLED, EXPENSE_SETTLED_LATE, EXPENSE_DEFAULTED, DISPUTE_RAISED, DISPUTE_RESOLVED, DISPUTE_ESCALATED, MEMBER_JOINED, MEMBER_LEFT, VERIFICATION_ADDED, CONSENT_GRANTED, CONSENT_REVOKED |
| **Projection** | `behavioral_profiles` materialized view (refreshed every 6 hours) |
| **Migration** | Synthetic events for existing state (marked `isSynthetic: true`) |
| **Anti-Tamper** | SHA-256 hash chain + HMAC-SHA256 server signature |

### 3.2 Phase 1: Trust Tag Engine (Bayesian)

| Component | Formula/Rule |
|-----------|-------------|
| **Prior** | Beta(α=2, β=2) for each component |
| **Chore Reliability** | (onTime + 0.5×late) / totalChores, min 5 events |
| **Financial Reliability** | (onTime + 0.3×late) / totalExpenses, min 3 events |
| **Conflict Resolution** | resolved / totalDisputes, min 1 event |
| **Tenure Score** | <30d: 0.0, <90d: 0.5, <180d: 0.75, ≥180d: 1.0 |
| **CRI** | 0.35×chore + 0.35×finance + 0.15×conflict + 0.15×tenure |
| **Confidence Interval** | CRI ± 1.96×√Var (95% CI) |
| **Time Decay** | exp(-0.003 × daysSinceEvent), half-life ~230 days |
| **HABITIQ_PLUS** | CRI ≥ 0.90, ≥30 chore events, ≥10 finance events, ≥180d tenure, CI width ≤ 0.15 |
| **HABITIQ_RELIABLE** | CRI ≥ 0.75, ≥15 chore events, ≥5 finance events, ≥60d tenure, CI width ≤ 0.25 |
| **NEW_TO_HABITIQ** | Everything else (neutral, never punitive) |
| **Escalation Penalty** | Active escalation within 90 days → blocks tier promotion |
| **Display** | Phase 1: Internal only (own flat). Phase 3: Discovery-visible with consent. |

### 3.3 Anti-Gaming Defenses (5 Layers)

| Layer | Defense | Implementation |
|-------|---------|---------------|
| **1. Structural** | Minimum flat size (≥3) | 2-person flat events: 50% weight |
| **2. Statistical** | Velocity anomaly detection | Z-score > 2.5σ flags for review |
| **3. Graph** | Cross-flat collusion detection | SQL: shared flats ≥ 2, avg size < 3.5, correlation > 0.95 |
| **4. Identity** | Sybil attack detection | Phone suffix clustering, device fingerprint, IP timing |
| **5. Human** | Manual review queue | Risk score > 0.8 → auto-review queue |

### 3.4 Phase 2: PG Verification

| Source | Weight | Reliability | Check |
|--------|--------|-------------|-------|
| Google Places API | 0.25 | 0.85 | Fuzzy name match + phone match + location proximity |
| Phone OTP | 0.20 | 0.90 | Ownership verification |
| Government ID / GST | 0.25 | 0.95 | Business verification |
| Photo Evidence | 0.15 | 0.75 | Physical verification |
| User Reports | 0.15 | 0.60 | Negative signal (reported listings) |
| **Combined** | Bayesian log-odds | — | Status: VERIFIED (>0.85), PARTIAL (0.60-0.85), UNVERIFIED (<0.60) |

### 3.5 Phase 3: Discovery (GATED)

| Gate | Threshold | City must have |
|------|-----------|---------------|
| **Liquidity Gate** | 200 active listings + 100 active seekers + supply:demand ≥ 0.5 | All three for 3 consecutive weeks |
| **Compatibility** | 6 factors | Budget (25%), Location (20%), Timing (15%), Trust Harmony (20%), Lifestyle (10%), Availability (10%) |
| **Ranking** | MMR (Maximal Marginal Relevance) | λ=0.5 balancing relevance vs diversity |
| **Contact Reveal** | Mutual match only | Both parties accept → phone/WhatsApp revealed |
| **Privacy** | Approximate area only | Exact address NEVER in discoverable record |

---

## 4. USE CASE MATRIX (2,000+)

### 4.1 Voice Use Cases by Intent

| Intent | English | Hinglish | Telugu-English | Total |
|--------|---------|----------|---------------|-------|
| COMPLETE_TASK | 100 | 50 | 25 | 175 |
| CREATE_EXPENSE | 100 | 50 | 25 | 175 |
| CREATE_EXPENSE (with split) | 75 | 50 | 25 | 150 |
| QUERY_BALANCE | 100 | 50 | 25 | 175 |
| QUERY_TASKS | 25 | 25 | 25 | 75 |
| QUERY_STATUS | 25 | 25 | 25 | 75 |
| REQUEST_SWAP | 25 | 25 | 25 | 75 |
| CREATE_TASK | 25 | 25 | — | 50 |
| GREETING | 25 | — | — | 25 |
| UNKNOWN / FALLBACK | 100 | — | — | 100 |
| Multi-Intent Sequences | 25 | — | — | 25 |
| Error Recovery / Disambiguation | 50 | — | — | 50 |
| **SUBTOTAL VOICE** | **675** | **300** | **175** | **1,150** |

### 4.2 Trust Engine Use Cases

| Scenario | Count |
|----------|-------|
| Chore completion (on-time, late, missed) | 50 |
| Expense settlement (on-time, late, defaulted) | 50 |
| Dispute resolution (raised, resolved, escalated) | 30 |
| Member lifecycle (join, leave, transfer) | 20 |
| Verification (phone, email, ID) | 15 |
| Consent (grant, revoke, re-grant) | 15 |
| Tier computation (New → Reliable → Plus) | 30 |
| Tag explanation (Why This Tag?) | 20 |
| Appeal path (dispute tag computation) | 10 |
| Anti-gaming detection (collusion, sybil, velocity) | 25 |
| PG verification (Google Places, OTP, ID, photo, reports) | 40 |
| Discovery search (budget, location, timing, trust, lifestyle) | 50 |
| Mutual match (request, accept, decline, contact reveal) | 30 |
| **SUBTOTAL TRUST** | **385** |

### 4.3 Core App Use Cases (Existing + New)

| Scenario | Count |
|----------|-------|
| Authentication (sign up, login, logout, forgot password) | 20 |
| Flat creation (create, name, invite, join) | 20 |
| Task management (create, edit, delete, complete, swap) | 50 |
| Rotation (auto-assign, skip OOS, resume, overdue) | 30 |
| Expense (add, split, settle, delete, edit) | 40 |
| Bills (create recurring, pay, edit, delete) | 30 |
| Member management (invite, kick, leave, transfer, OOS toggle) | 25 |
| Multi-flat (switch, create second, delete) | 15 |
| Subscription (trial, coupon, premium, expired) | 20 |
| Settings (profile, notifications, theme, voice) | 15 |
| Analytics (completion grid, reliability, trends) | 15 |
| Calendar (view, filter, navigate) | 10 |
| Activity log (view, filter by type) | 10 |
| **SUBTOTAL CORE** | **300** |

### 4.4 Edge Cases & Error Handling

| Scenario | Count |
|----------|-------|
| Voice: No speech detected | 10 |
| Voice: Microphone permission denied | 10 |
| Voice: Network error during recognition | 10 |
| Voice: Ambiguous task (multiple matches) | 15 |
| Voice: Ambiguous member (multiple matches) | 15 |
| Voice: Invalid amount (too large, negative) | 10 |
| Voice: Unsupported browser | 5 |
| Voice: Timeout (10s hard limit) | 5 |
| Voice: Background tab (auto-stop) | 5 |
| Trust: Insufficient data for tier | 10 |
| Trust: Active escalation blocks tier | 5 |
| Trust: Consent revoked → UNRATED | 5 |
| Trust: Synthetic event migration | 5 |
| Trust: Collusion ring detected | 5 |
| Trust: Sybil cluster detected | 5 |
| Discovery: City below liquidity threshold | 5 |
| Discovery: No matching listings | 5 |
| Discovery: Mutual match declined | 5 |
| **SUBTOTAL EDGE** | **140** |

### 4.5 Cross-Feature Integration Use Cases

| Scenario | Count |
|----------|-------|
| Voice completes task → Trust event logged | 10 |
| Voice adds expense → Trust event logged | 10 |
| Voice queries balance → Uses real-time Firestore | 5 |
| Voice creates task → Rotation engine updated | 5 |
| Trust tag displayed in voice response | 5 |
| Trust tag displayed on member profile | 5 |
| Discovery shows trust tag on listing | 5 |
| Discovery filters by trust tier | 5 |
| Subscription gates voice features | 5 |
| Subscription gates trust tag visibility | 5 |
| OOS toggle affects rotation + voice task query | 5 |
| Swap request affects rotation + voice task query | 5 |
| **SUBTOTAL INTEGRATION** | **65** |

### GRAND TOTAL: 2,040 USE CASES

---

## 5. IMPLEMENTATION STATUS TRACKER

### Voice Assistant (95 Features)

| # | Feature | File | Status | Notes |
|---|---------|------|--------|-------|
| 1 | Voice types (`lib/voice/types.ts`) | `lib/voice/types.ts` | ⬜ | Blocker for all voice work |
| 2 | Voice constants (`lib/voice/constants.ts`) | `lib/voice/constants.ts` | ⬜ | Number word maps, thresholds |
| 3 | Voice utils (`lib/voice/utils.ts`) | `lib/voice/utils.ts` | ⬜ | Levenshtein, fuzzy match, parseNumberWords |
| 4 | Intent classifier (`lib/voice/nlu/intentClassifier.ts`) | `lib/voice/nlu/intentClassifier.ts` | ⬜ | 9 intents + REJECT, 1000+ patterns |
| 5 | Entity extractor (`lib/voice/nlu/entityExtractor.ts`) | `lib/voice/nlu/entityExtractor.ts` | ⬜ | Amount, member, task, description, frequency |
| 6 | Context resolver (`lib/voice/nlu/contextResolver.ts`) | `lib/voice/nlu/contextResolver.ts` | ⬜ | Flat data cache, 30s TTL |
| 7 | Action router (`lib/voice/actions/actionRouter.ts`) | `lib/voice/actions/actionRouter.ts` | ⬜ | Routes intent → action handler |
| 8 | Complete task action (`lib/voice/actions/completeTask.ts`) | `lib/voice/actions/completeTask.ts` | ⬜ | Firestore update: tasks/{taskId} |
| 9 | Create expense action (`lib/voice/actions/createExpense.ts`) | `lib/voice/actions/createExpense.ts` | ⬜ | Firestore create: expenses/{expenseId} |
| 10 | Query balance action (`lib/voice/actions/queryBalance.ts`) | `lib/voice/actions/queryBalance.ts` | ⬜ | Read from expenses/balances |
| 11 | Query tasks action (`lib/voice/actions/queryTasks.ts`) | `lib/voice/actions/queryTasks.ts` | ⬜ | Read from tasks |
| 12 | Query status action (`lib/voice/actions/queryStatus.ts`) | `lib/voice/actions/queryStatus.ts` | ⬜ | Read from flat members |
| 13 | Request swap action (`lib/voice/actions/requestSwap.ts`) | `lib/voice/actions/requestSwap.ts` | ⬜ | Create: swapRequests/{swapId} |
| 14 | Create task action (`lib/voice/actions/createTask.ts`) | `lib/voice/actions/createTask.ts` | ⬜ | Create: tasks/{taskId} |
| 15 | Greeting action (`lib/voice/actions/greeting.ts`) | `lib/voice/actions/greeting.ts` | ⬜ | No Firestore, contextual response |
| 16 | Reject action (`lib/voice/actions/reject.ts`) | `lib/voice/actions/reject.ts` | ⬜ | No Firestore, boundary response |
| 17 | Unknown action (`lib/voice/actions/unknown.ts`) | `lib/voice/actions/unknown.ts` | ⬜ | No Firestore, suggestion rotation |
| 18 | Response formatter (`lib/voice/response/responseFormatter.ts`) | `lib/voice/response/responseFormatter.ts` | ⬜ | Text + card + follow-up |
| 19 | TTS engine (`lib/voice/tts/speechSynthesis.ts`) | `lib/voice/tts/speechSynthesis.ts` | ⬜ | en-IN female priority, 1.2x rate |
| 20 | Permissions handler (`lib/voice/permissions.ts`) | `lib/voice/permissions.ts` | ⬜ | Custom modal, not browser default |
| 21 | Analytics (`lib/voice/analytics.ts`) | `lib/voice/analytics.ts` | ⬜ | Intent + success only, hashed transcripts |
| 22 | useVoiceAssistant hook (`hooks/useVoiceAssistant.ts`) | `hooks/useVoiceAssistant.ts` | ⬜ | SpeechRecognition, 10s timeout, silence auto-stop |
| 23 | useVoiceContext hook (`hooks/useVoiceContext.ts`) | `hooks/useVoiceContext.ts` | ⬜ | Screen-based default intent |
| 24 | VoiceButton component (`components/voice/VoiceButton.tsx`) | `components/voice/VoiceButton.tsx` | ⬜ | Nav center, 3 states, long-press |
| 25 | VoiceOverlay component (`components/voice/VoiceOverlay.tsx`) | `components/voice/VoiceOverlay.tsx` | ⬜ | Full-screen, z-50, backdrop blur |
| 26 | WaveformVisualizer (`components/voice/WaveformVisualizer.tsx`) | `components/voice/WaveformVisualizer.tsx` | ⬜ | Canvas, 20 bars, 60fps, violet gradient |
| 27 | VoiceResponseCard (`components/voice/VoiceResponseCard.tsx`) | `components/voice/VoiceResponseCard.tsx` | ⬜ | Slide-up, 8s auto-dismiss, swipe down |
| 28 | VoiceFallbackModal (`components/voice/VoiceFallbackModal.tsx`) | `components/voice/VoiceFallbackModal.tsx` | ⬜ | iOS text input, bottom sheet |
| 29 | VoiceSettings (`components/voice/VoiceSettings.tsx`) | `components/voice/VoiceSettings.tsx` | ⬜ | 3 toggles, help card, Nest integration |
| 30 | Navigation modification | `components/Navigation.tsx` | ⬜ | Replace center button with VoiceButton |
| 31 | Settings route addition | `/nest/settings/voice` | ⬜ | New settings page |
| 32 | Intent classifier tests | `__tests__/voice/intentClassifier.test.ts` | ⬜ | 500+ test cases |
| 33 | Entity extractor tests | `__tests__/voice/entityExtractor.test.ts` | ⬜ | Amount, member, task extraction |
| 34 | Action router tests | `__tests__/voice/actionRouter.test.ts` | ⬜ | End-to-end action tests |
| 35 | Performance tests | `__tests__/voice/performance.test.ts` | ⬜ | <10ms intent, <20ms entity, <100ms action |

### Trust & Discovery Engine (22 Features)

| # | Feature | File/Table | Status | Notes |
|---|---------|-----------|--------|-------|
| 36 | Behavioral events table | `behavioral_events` (SQL) | ⬜ | Partitioned, hash chain, HMAC |
| 37 | Behavioral profiles view | `behavioral_profiles` (SQL) | ⬜ | Materialized, 6h refresh |
| 38 | Event emitter hooks | `lib/events/emitters.ts` | ⬜ | Hook into chore, expense, dispute, member flows |
| 39 | Synthetic event migration | `scripts/migrateEvents.ts` | ⬜ | Convert state → synthetic events |
| 40 | Trust tag engine | `lib/trust/computeTier.ts` | ⬜ | Bayesian Beta, CRI, confidence intervals |
| 41 | Tier explanation engine | `lib/trust/explanation.ts` | ⬜ | 3 data points, plain English |
| 42 | Consent manager | `lib/trust/consent.ts` | ⬜ | 5th-event trigger, granular, revocable |
| 43 | Internal tag display | `components/trust/TrustTagBadge.tsx` | ⬜ | Own flat only, Phase 1 |
| 44 | Anti-gaming: flat size penalty | `lib/trust/antiGaming/sizePenalty.ts` | ⬜ | <3 members → 50% weight |
| 45 | Anti-gaming: reciprocal bias | `lib/trust/antiGaming/reciprocalBias.ts` | ⬜ | 2-person mutual perfection → flag |
| 46 | Anti-gaming: velocity detector | `lib/trust/antiGaming/velocity.ts` | ⬜ | Z-score > 2.5σ → anomaly |
| 47 | Anti-gaming: collusion graph | `lib/trust/antiGaming/collusionGraph.ts` | ⬜ | SQL clique detection |
| 48 | Anti-gaming: sybil detector | `lib/trust/antiGaming/sybilDetector.ts` | ⬜ | Phone suffix + device + IP clustering |
| 49 | Manual review queue | `components/admin/ReviewQueue.tsx` | ⬜ | Admin UI for flagged users |
| 50 | PG verification engine | `lib/pg/verify.ts` | ⬜ | Google Places + OTP + ID + photo + reports |
| 51 | PG listing schema | `pg_listings` (SQL) | ⬜ | Approximate area only, exact address internal |
| 52 | Spot-check algorithm | `lib/pg/spotCheck.ts` | ⬜ | 10% high-report + 5% random unverified |
| 53 | Discovery search index | `search/listings` (OpenSearch/Algolia) | ⬜ | City-based, approximate location |
| 54 | Compatibility engine | `lib/discovery/compatibility.ts` | ⬜ | 6-factor weighted scoring |
| 55 | MMR ranker | `lib/discovery/mmrRanker.ts` | ⬜ | Diversity re-ranking |
| 56 | Liquidity manager | `lib/discovery/liquidity.ts` | ⬜ | 200 listings + 100 seekers gate |
| 57 | Mutual match flow | `lib/discovery/mutualMatch.ts` | ⬜ | Request → Accept → Contact reveal |

### Core App Enhancements (8 Features)

| # | Feature | Status | Notes |
|---|---------|--------|-------|
| 58 | Jurisdiction config layer | ⬜ | IN/UK/EU/AU rules for gender filters, consent, data residency |
| 59 | Readiness metrics dashboard | ⬜ | % flats with history, avg depth, consent rate, tier distribution |
| 60 | Liquidity metrics dashboard | ⬜ | Listings/seekers per city, supply:demand ratio |
| 61 | A/B testing framework | ⬜ | Experiments table, assignments, events, statistical analysis |
| 62 | Monitoring & alerting | ⬜ | SLOs, error budgets, PagerDuty/webhook alerts |
| 63 | Cache layer (Redis) | ⬜ | Trust tags 1h TTL, listings 5m TTL, search 30s TTL |
| 64 | Async processing pipeline | ⬜ | Kafka/Redis Streams → workers → profile refresh |
| 65 | Circuit breakers & fallbacks | ⬜ | Cache fallback on tier computation failure |

---

## 6. CROSS-CHECK CHECKLIST FOR CLAUDE CODE

### Step 1: Verify Existing Features Are Intact

```
□ Google Sign-In works on Android + iOS + Desktop
□ Email/Password login works
□ Flat creation generates invite code instantly
□ Join flat via invite code works
□ Smart rotation auto-assigns tasks
□ OOS skip/resume works
□ Task completion one-tap works
□ Swap request accept/decline works
□ Expense add + split + settle works
□ Bill creation + recurring rotation works
□ Multi-flat switcher works
□ Subscription trial/active/expired states work
□ Premium UI (crown badge, gold pill) shows correctly
□ Dark mode toggle works
□ Mobile nav (Home · Duties · Log · Money · Nest) renders correctly
□ Member home screen shows tasks, money, activity, upcoming rotations
□ Task bottom sheet opens on tap
□ Bills and Money are SEPARATE sections (never mixed)
□ All existing Firestore security rules still enforce auth
□ No console errors on any screen
```

### Step 2: Verify Voice Assistant Files Exist

```
□ lib/voice/types.ts — exports all Voice* interfaces
□ lib/voice/constants.ts — number word maps, thresholds
□ lib/voice/utils.ts — levenshtein, fuzzyMatch, parseNumberWords
□ lib/voice/nlu/intentClassifier.ts — classifyIntent() returns correct intent for 500+ test cases
□ lib/voice/nlu/entityExtractor.ts — extractEntities() returns amount/member/task/description
□ lib/voice/nlu/contextResolver.ts — getFlatContext() returns members, tasks, balances
□ lib/voice/actions/actionRouter.ts — executeAction() routes all 10 intents
□ lib/voice/actions/completeTask.ts — marks task complete in Firestore
□ lib/voice/actions/createExpense.ts — creates expense with splits in Firestore
□ lib/voice/actions/queryBalance.ts — reads balances, returns formatted response
□ lib/voice/actions/queryTasks.ts — reads pending tasks, returns formatted list
□ lib/voice/actions/queryStatus.ts — reads member OOS status
□ lib/voice/actions/requestSwap.ts — creates swap request in Firestore
□ lib/voice/actions/createTask.ts — creates task in Firestore
□ lib/voice/actions/greeting.ts — returns contextual greeting
□ lib/voice/actions/reject.ts — returns boundary message with suggestions
□ lib/voice/actions/unknown.ts — returns random suggestion rotation
□ lib/voice/response/responseFormatter.ts — returns VoiceResponse with text/card/followUp
□ lib/voice/tts/speechSynthesis.ts — speaks with en-IN female voice, 1.2x rate
□ lib/voice/permissions.ts — custom permission modal, handles denied/granted/prompt
□ lib/voice/analytics.ts — logs intent + success only (no raw transcripts)
□ hooks/useVoiceAssistant.ts — SpeechRecognition hook with 10s timeout, silence auto-stop
□ hooks/useVoiceContext.ts — returns default intent based on current route
□ components/voice/VoiceButton.tsx — renders in nav center, 3 states, long-press
□ components/voice/VoiceOverlay.tsx — full-screen overlay, z-50, backdrop blur
□ components/voice/WaveformVisualizer.tsx — canvas with 20 bars, 60fps, violet gradient
□ components/voice/VoiceResponseCard.tsx — slide-up card, 8s auto-dismiss, swipe down
□ components/voice/VoiceFallbackModal.tsx — iOS text input, bottom sheet, auto-focus
□ components/voice/VoiceSettings.tsx — 3 toggles, help card, Nest settings integration
□ Navigation.tsx — center button is VoiceButton (not Log button), long-press opens Log sheet
□ /nest/settings/voice — new settings route exists
```

### Step 3: Verify Voice Test Cases Pass

```
□ "Kitchen done" → COMPLETE_TASK → task marked complete
□ "I spent 500 on groceries" → CREATE_EXPENSE → expense created, split equally
□ "How much does Bhanu owe me?" → QUERY_BALANCE → returns ₹X owed
□ "What are my tasks?" → QUERY_TASKS → returns pending task list
□ "Who is home?" → QUERY_STATUS → returns present members
□ "Can someone cover my task?" → REQUEST_SWAP → swap request created
□ "Add kitchen daily" → CREATE_TASK → task created with daily frequency
□ "Hi" → GREETING → returns contextual greeting
□ "What time is it?" → REJECT → returns boundary message
□ "Play music" → REJECT → returns boundary message
□ "Five hundred on dinner" → CREATE_EXPENSE → amount parsed as 500
□ "Paanch sau diye" → CREATE_EXPENSE → amount parsed as 500
□ "Aidu vandala icha" → CREATE_EXPENSE → amount parsed as 500
□ "Kitchen ho gaya" → COMPLETE_TASK → task marked complete
□ "Kitchen aipoyindi" → COMPLETE_TASK → task marked complete
□ "Main busy hoon" → REQUEST_SWAP → swap request created
□ "Nenu busy" → REQUEST_SWAP → swap request created
□ "Mera balance kitna hai" → QUERY_BALANCE → returns balance
□ "Na balance entha" → QUERY_BALANCE → returns balance
□ Ambiguous "Done" → asks "Which task? Kitchen, bathroom, or garbage?"
□ No amount "I spent on groceries" → asks "How much? Say the amount."
□ No member "How much does Ramu owe me?" → asks "I don't know 'Ramu'. Did you mean Bhanu or Kiran?"
□ iOS Safari → fallback modal opens, text input works
□ Android Chrome → SpeechRecognition opens, listens, returns transcript
□ 10-second timeout → auto-stops listening
□ 1.5-second silence after final result → auto-stops
□ TTS speaks response after successful action
□ Response card shows with correct text and action buttons
□ Settings: Voice toggle persists in localStorage
□ Settings: TTS toggle disables speech output
□ Settings: Transcript toggle shows/hides what user said
```

### Step 4: Verify Trust Engine Files Exist (Phase 0-1)

```
□ behavioral_events table exists with: id, event_id, timestamp, flat_id, actor_user_id, event_type, payload, previous_hash, event_hash, signature, is_synthetic, confidence, partition_key
□ behavioral_events partitioned by YYYYMM
□ behavioral_profiles materialized view exists with: user_id, chore_alpha, chore_beta, finance_alpha, finance_beta, dispute_alpha, dispute_beta, chore_reliability, finance_reliability, conflict_resolution, tenure_days, computed_tier, computed_at
□ Event emitters hooked into: chore completion, expense settlement, dispute resolution, member join/leave
□ Synthetic event migration script exists and runs without breaking existing data
□ Trust tag engine computes Beta distributions correctly
□ Tier classification: New (insufficient data), Reliable (CRI ≥ 0.75), Plus (CRI ≥ 0.90)
□ Explanation engine generates 3-data-point plain English reason
□ Consent flow triggers at 5th behavioral event (not at signup)
□ Consent is granular: "Use my activity" + "Show my tag to strangers" (separate toggles)
□ Consent withdrawal sets tag to UNRATED (never punitive)
□ Tag is internal-only in Phase 1 (never shown to strangers)
□ Jurisdiction config layer exists: IN (gender filter ON, DPDP ON), UK (gender filter OFF, GDPR ON), EU (gender filter OFF, GDPR ON), AU (state-specific)
```

### Step 5: Verify Anti-Gaming Defenses Exist

```
□ Flat size penalty: events from <3 member flats get 50% weight
□ Reciprocal bias: 2-person flat with mutual perfection → flag for review
□ Velocity detector: >15 perfect events in 7 days in new flat → anomaly flag
□ Collusion graph: SQL query detects pairs in ≥2 small flats with >0.95 correlation
□ Sybil detector: phone suffix clustering, device fingerprint, IP timing patterns
□ Manual review queue: admin UI shows flagged users with risk scores
□ Auto-suspend: risk score > 0.95 → tag set to UNRATED, manual investigation
```

### Step 6: Verify Performance & Quality Gates

```
□ Intent classification latency < 10ms
□ Entity extraction latency < 20ms
□ Action execution latency < 100ms
□ End-to-end voice latency < 2 seconds (speech → action → response)
□ Trust tag computation latency < 5 seconds (batch job)
□ Search query latency p99 < 200ms
□ Bundle size increase < 50 KB gzipped
□ Voice adoption rate target: 30% of active users try within 7 days
□ Task completion via voice target: 50% of completions after 30 days
□ Expense addition via voice target: 40% of additions after 30 days
□ Fallback rate target: < 5% per session
□ Intent accuracy target: > 95% on test corpus
□ UNKNOWN rate target: < 8%
```

---

## 7. MISSING / GAP ANALYSIS

### Critical Gaps (Must Fix Before Any Voice/Trust Work)

| # | Gap | Impact | Fix |
|---|-----|--------|-----|
| 1 | **No event emitter hooks on existing actions** | Trust engine has NO data to compute tiers | Add `emitBehavioralEvent()` calls at end of: `completeTask`, `settleExpense`, `resolveDispute`, `joinFlat`, `leaveFlat` |
| 2 | **No behavioral_events table** | Cannot store trust data | Create table + partitions + indexes |
| 3 | **No behavioral_profiles view** | Cannot compute trust tags | Create materialized view + refresh job |
| 4 | **No voice types/constants/utils files** | Cannot build voice assistant | Create `lib/voice/` directory structure |
| 5 | **No intent classifier** | Voice cannot understand commands | Build `intentClassifier.ts` with all 10 intents |
| 6 | **No entity extractor** | Voice cannot parse amounts/members/tasks | Build `entityExtractor.ts` with number-word parsers |
| 7 | **No action handlers** | Voice cannot execute commands | Build all 10 action files |
| 8 | **No useVoiceAssistant hook** | No speech recognition | Build hook with Web Speech API + iOS fallback |
| 9 | **No voice UI components** | User cannot interact with voice | Build 6 voice components |
| 10 | **Navigation still shows "Log" button** | Voice button not accessible | Replace center nav button with VoiceButton |

### High-Priority Gaps (Fix After Critical)

| # | Gap | Impact | Fix |
|---|-----|--------|-----|
| 11 | **No TTS engine** | System cannot speak responses | Build `speechSynthesis.ts` with en-IN voice |
| 12 | **No permission handler** | Mic access fails silently | Build custom permission modal |
| 13 | **No response formatter** | Responses are plain text only | Build card-based response system |
| 14 | **No waveform visualizer** | Listening state not visible | Build canvas animation |
| 15 | **No voice settings** | Users cannot disable voice | Build settings panel in Nest |
| 16 | **No context resolver** | Voice cannot resolve "Bhanu" vs "Rohan" | Build flat data cache |
| 17 | **No trust tag computation** | No reliability scores | Build Bayesian tier engine |
| 18 | **No anti-gaming layer** | System is gameable | Build 5-layer defense system |
| 19 | **No consent flow** | DPDP violation risk | Build 5th-event trigger consent |
| 20 | **No jurisdiction config** | Cannot launch in UK/EU/AU | Build country-based rule engine |

### Medium-Priority Gaps (Fix After High)

| # | Gap | Impact | Fix |
|---|-----|--------|-----|
| 21 | **No PG verification** | Cannot verify PG listings | Build Google Places + OTP + ID verification |
| 22 | **No discovery search** | Cannot match seekers to listings | Build search index + compatibility engine |
| 23 | **No mutual match flow** | Contact info leaked prematurely | Build request → accept → reveal flow |
| 24 | **No liquidity gating** | Discovery opens before ready | Build city-level metrics + gate |
| 25 | **No A/B testing framework** | Cannot optimize voice UX | Build experiments + assignments + analysis |
| 26 | **No monitoring dashboard** | Cannot detect failures | Build metrics + alerts + SLOs |
| 27 | **No async pipeline** | Profile refresh blocks UI | Build Kafka/Redis Streams + workers |
| 28 | **No cache layer** | Trust tag reads are slow | Build Redis cache with 1h TTL |
| 29 | **No circuit breakers** | Voice fails catastrophically | Build fallback to cached tier + manual mode |
| 30 | **No voice analytics** | Cannot measure adoption | Build privacy-first analytics |

---

## 8. FILE INVENTORY (31 FILES + MODIFICATIONS)

### New Files to Create (31)

| # | File Path | Purpose | Estimated Lines | Priority |
|---|-----------|---------|-----------------|----------|
| 1 | `lib/voice/types.ts` | All TypeScript interfaces | 80 | CRITICAL |
| 2 | `lib/voice/constants.ts` | Number word maps, thresholds | 60 | CRITICAL |
| 3 | `lib/voice/utils.ts` | Levenshtein, fuzzy match, parsers | 120 | CRITICAL |
| 4 | `lib/voice/nlu/intentClassifier.ts` | Intent classification (10 intents) | 400 | CRITICAL |
| 5 | `lib/voice/nlu/entityExtractor.ts` | Entity extraction (6 entity types) | 250 | CRITICAL |
| 6 | `lib/voice/nlu/contextResolver.ts` | Flat data cache (30s TTL) | 80 | CRITICAL |
| 7 | `lib/voice/actions/actionRouter.ts` | Route intent → action | 50 | CRITICAL |
| 8 | `lib/voice/actions/completeTask.ts` | Mark task complete | 120 | CRITICAL |
| 9 | `lib/voice/actions/createExpense.ts` | Create expense with splits | 150 | CRITICAL |
| 10 | `lib/voice/actions/queryBalance.ts` | Query member balances | 100 | CRITICAL |
| 11 | `lib/voice/actions/queryTasks.ts` | List pending tasks | 80 | CRITICAL |
| 12 | `lib/voice/actions/queryStatus.ts` | Check member presence | 80 | CRITICAL |
| 13 | `lib/voice/actions/requestSwap.ts` | Create swap request | 100 | CRITICAL |
| 14 | `lib/voice/actions/createTask.ts` | Create new task | 80 | MEDIUM |
| 15 | `lib/voice/actions/greeting.ts` | Contextual greeting | 60 | LOW |
| 16 | `lib/voice/actions/reject.ts` | Boundary response | 40 | CRITICAL |
| 17 | `lib/voice/actions/unknown.ts` | Suggestion rotation | 50 | CRITICAL |
| 18 | `lib/voice/response/responseFormatter.ts` | Format text + card | 100 | CRITICAL |
| 19 | `lib/voice/tts/speechSynthesis.ts` | Text-to-speech engine | 80 | HIGH |
| 20 | `lib/voice/permissions.ts` | Mic permission handler | 80 | HIGH |
| 21 | `lib/voice/analytics.ts` | Privacy-first analytics | 30 | LOW |
| 22 | `hooks/useVoiceAssistant.ts` | Speech recognition hook | 150 | CRITICAL |
| 23 | `hooks/useVoiceContext.ts` | Screen-based default intent | 30 | MEDIUM |
| 24 | `components/voice/VoiceButton.tsx` | Nav center FAB | 150 | CRITICAL |
| 25 | `components/voice/VoiceOverlay.tsx` | Listening overlay | 80 | HIGH |
| 26 | `components/voice/WaveformVisualizer.tsx` | Canvas waveform | 80 | HIGH |
| 27 | `components/voice/VoiceResponseCard.tsx` | Response card | 120 | HIGH |
| 28 | `components/voice/VoiceFallbackModal.tsx` | iOS fallback | 80 | CRITICAL |
| 29 | `components/voice/VoiceSettings.tsx` | Settings panel | 100 | MEDIUM |
| 30 | `__tests__/voice/intentClassifier.test.ts` | Intent tests (500+ cases) | 80 | CRITICAL |
| 31 | `__tests__/voice/performance.test.ts` | Performance tests | 40 | HIGH |

### Existing Files to Modify (3)

| # | File Path | Modification | Priority |
|---|-----------|-------------|----------|
| 32 | `components/Navigation.tsx` | Replace center "Log" button with `<VoiceButton />` | CRITICAL |
| 33 | `app/nest/settings/page.tsx` | Add VoiceSettings section | MEDIUM |
| 34 | `lib/firebase/events.ts` | Add `emitBehavioralEvent()` calls to all task/expense/member actions | CRITICAL |

### Database / Firestore Changes (2)

| # | Change | Priority |
|--------|--------|----------|
| 35 | Create `behavioral_events` table (SQL) or collection (Firestore) | CRITICAL |
| 36 | Create `behavioral_profiles` materialized view or computed collection | CRITICAL |

---

## APPENDIX A: LANGUAGE COVERAGE MATRIX

| Intent | English | Hinglish | Telugu-English | Total Patterns |
|--------|---------|----------|---------------|----------------|
| COMPLETE_TASK | 100 | 50 | 25 | 175 |
| CREATE_EXPENSE | 100 | 50 | 25 | 175 |
| QUERY_BALANCE | 100 | 50 | 25 | 175 |
| QUERY_TASKS | 25 | 25 | 25 | 75 |
| QUERY_STATUS | 25 | 25 | 25 | 75 |
| REQUEST_SWAP | 25 | 25 | 25 | 75 |
| CREATE_TASK | 25 | 25 | — | 50 |
| GREETING | 25 | — | — | 25 |
| REJECT | 100 | 50 | 50 | 200 |
| UNKNOWN | 100 | — | — | 100 |
| **TOTAL** | **625** | **300** | **200** | **1,125** |

## APPENDIX B: PERFORMANCE BUDGETS

| Operation | Target | Maximum | Measurement |
|-----------|--------|---------|-------------|
| Intent classification | < 10ms | < 50ms | `performance.now()` |
| Entity extraction | < 20ms | < 100ms | `performance.now()` |
| Action execution | < 100ms | < 500ms | `performance.now()` |
| TTS speech start | < 200ms | < 1s | `performance.now()` |
| End-to-end voice | < 2s | < 5s | Tap mic → response spoken |
| Trust tag computation | < 5s | < 30s | Batch job |
| Trust tag read (cached) | < 10ms | < 50ms | `performance.now()` |
| Search query | < 100ms | < 500ms | `performance.now()` |
| Search p99 latency | < 200ms | < 1s | Vercel Analytics |
| Bundle size increase | < 50KB | < 100KB | `webpack-bundle-analyzer` |

## APPENDIX C: TEST COVERAGE TARGETS

| Layer | Test Type | Target Coverage | Minimum Cases |
|-------|-----------|-----------------|---------------|
| NLU | Intent classification | > 95% | 500+ |
| NLU | Entity extraction | > 90% | 200+ |
| NLU | Number-word parsing | > 95% | 100+ |
| Actions | Complete task | > 90% | 50+ |
| Actions | Create expense | > 90% | 50+ |
| Actions | Query balance | > 90% | 30+ |
| Actions | Query tasks | > 90% | 30+ |
| Actions | Query status | > 90% | 20+ |
| Actions | Request swap | > 90% | 30+ |
| Integration | End-to-end voice | > 80% | 20+ |
| Performance | Latency benchmarks | 100% | 10+ |
| Accessibility | ARIA + screen reader | > 90% | 10+ |

---

*End of Master Cross-Check Document*
*For Claude Code: Start with Section 6 (Cross-Check Checklist). Verify each checkbox. If any file is missing, create it. If any test fails, fix it. If any feature is not implemented, build it.*
*This document is the single source of truth. Nothing discussed in any session is missing from this document.*
