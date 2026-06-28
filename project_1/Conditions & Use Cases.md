# Habitiq — Conditions & Use Cases (Complete Reference)
**Last updated:** 29 June 2026
**Purpose:** Every condition, edge case, and use case — documented so nothing is forgotten across sessions.

---

## A. Tasks & Rotation

### A1. Task Completion
| Condition | What happens |
|-----------|--------------|
| Member marks task done | Rotation queue advances; next person becomes assignee |
| Member marks task done (overdue) | Same as above; overdue flag cleared |
| Admin marks task done for any member | Same; activity log records who did it |
| Task already completed today | Can still mark again (no dedup guard currently) |
| Retroactive date editing | Admin/user can edit the "done on" date after completion |
| Task is OOS-assigned | Should have been skipped — manual override available |

### A2. Rotation Queue
| Condition | What happens |
|-----------|--------------|
| Member goes OOS | Skipped in rotation until they return |
| Member returns from OOS | Re-enters queue at current position |
| New member joins flat | Auto-added to all existing task queues at the end |
| Member leaves / is kicked | Removed from all queues; task reassigned to `newQueue[leavingIndex % newQueue.length]` |
| Only 1 member left in queue | That member is always assignee |
| All members OOS | Rotation stalls — no skip occurs |

### A3. Task Types
| Type | Conditions |
|------|-----------|
| Regular (rotation) | daily/weekly/fortnightly/monthly frequencies |
| Group task | Multiple members assigned; sub-task per person |
| Temp task | One-off, outside rotation queue; frequency = once |
| Overdue task | nextDueDate passed; stays with current assignee |

### A4. Swap System
| Condition | What happens |
|-----------|--------------|
| Member requests swap | Sends request to target member; dashboard banner appears |
| Target accepts swap | Task reassigned; activity log; banner dismissed |
| Target declines swap | Task stays; activity log |
| Requester withdraws swap | Request cancelled before target responds (cancel/withdraw) |
| Admin sees all swaps | "All Swaps" toggle on Swaps page — full flat list |
| Pending swaps badge | Mobile nav Tasks slot shows pending count; desktop sidebar counts all pending swaps |
| Member has no swaps | Dashboard swap widget hidden |

---

## B. Monthly Bills (Recurring)

### B1. Bill Templates
| Condition | What happens |
|-----------|--------------|
| Fixed amount | Admin enters amount once; same every month |
| Variable amount | Admin enters actual amount at generation time |
| Billing day | Day of month to generate instance (e.g., 1st) |
| Payer rotation | Payer rotates through participants each month |
| Collector | Separate person who collects shares from members; defaults to admin |
| Collector change | Any admin can change collector; snapshot saved on instance at generation |
| Bill template deleted | writeBatch: template + all instances + linked expenses deleted atomically |
| Bill template renamed | Updated on template; existing instances unaffected |

### B2. Bill Instances (Generation)
| Condition | What happens |
|-----------|--------------|
| Admin generates on billing date | Creates instance with current payer, splits, collector |
| Variable bill at generation | Admin prompted for actual amount; splits calculated then |
| Payer index advance | Index advances AFTER generation; instance stores paidBy at generation time (prevents wrong payer display) |
| Already generated this month | Guard prevents duplicate generation |
| Instance status: pending | Generated, not yet acted on |
| Instance status: split_generated | Payer has paid; splits visible; collection in progress |
| Instance status: paid | All collections confirmed; closed |
| Instance status: skipped | Admin manually skipped for this month |

### B3. Bill Payment & Collection
| Condition | What happens |
|-----------|--------------|
| Payer marks bill paid | Status → split_generated; collection tracking activates |
| Non-payer tries to mark paid | "Mark Paid" only shown to payer + admin |
| Collector sees toggles | Toggle for ALL other members (they collect from everyone) |
| Regular member sees toggles | Toggle ONLY for themselves (self-mark when they've paid the collector) |
| Non-collector admin sees toggles | None — no collection visibility unless they're also collector |
| Payer excluded from collection | Payer is the one paying the landlord — not paying themselves |
| Firestore rule — payer | Can update `[status, paidAt, collectedFrom]` on own instances |
| Firestore rule — collector | Can update `collectedFrom` only (not status/paidAt) |
| Instance deleted | Also removes linked expenses via writeBatch |

### B4. Member-Submitted Bills
| Condition | What happens |
|-----------|--------------|
| Member paid a bill personally | Member submits via "I paid a bill" → creates pending instance with `submittedBy` |
| Admin sees pending submissions | Inline cards in generate pipeline with member avatar + "Pending Approval" badge |
| Admin edits before approval | Can change amount and/or payer; splits recalculated atomically |
| Admin approves | `approved: true`; status → split_generated; collection begins |
| Admin rejects | Instance deleted with reason |
| Member submits for past date | Allowed (no future-date block on submissions) |
| Future-dated regular expense | Blocked in Daily Splits form (guard in date picker) |

---

## C. Daily Splits (Expenses)

### C1. Adding Expenses
| Condition | What happens |
|-----------|--------------|
| Equal split | Amount / participant count; fractions rounded |
| Custom split | Admin enters per-person amounts; must sum to total |
| Future date selected | Blocked — date picker prevents future dates |
| Missing description | Form validation blocks save |
| Activity write fails | Fire-and-forget (`void addActivity(...)`) — never blocks modal |
| Modal freeze on error | try/catch in handleSave; `setSaving(false)` on error |

### C2. Editing & Deleting Expenses
| Condition | What happens |
|-----------|--------------|
| Creator edits | Edit pencil visible in expanded row |
| Admin edits | Edit pencil visible regardless of creator |
| Non-creator non-admin edits | Edit button hidden |
| Delete | Optimistic: immediate UI update; Firestore delete async |
| Deleted expense still shows | Fixed: optimistic `filter` before Firestore call |
| Bill-linked expense deleted | No cascade reverse of bill instance status (removed) |

### C3. Balances
| Condition | What happens |
|-----------|--------------|
| MCF chain (A→B, B→C) | Not used — direct pairwise only; user only sees people they transacted with |
| Bill instances in balance calc | Excluded — Monthly Bills and Daily Splits are separate |
| Expand balance card | Shows contributing expenses per counterparty |
| Person filter | Filters transaction list to one person's shared history |
| Compact balance strip | Collapsed by default on mobile; tap to expand all cards |
| All settled | Green strip: "All balances settled — you're square!" |

### C4. Settlement
| Condition | What happens |
|-----------|--------------|
| Debtor settles | "Settle" button on "you owe them" card |
| Creditor marks received | "Mark Received" on "they owe you" card |
| Partial payment | Amount input; remaining preview shown; CTA says "Pay ₹X" |
| Full payment | CTA says "Mark as Paid" |
| Amount exceeds balance | Input goes red; CTA disabled |
| Quick-fill chips | "Pay full" / "Pay half" pre-fills input |
| Settle modal error | try/catch; modal never freezes |
| Settlement collapsed | Collapsed into divider `── ✓ Settled · ₹X · N payments ──`; tap to expand |

### C5. Month-End Close
| Condition | What happens |
|-----------|--------------|
| Admin closes month | Status → closed; carry-forward balances calculated |
| Carry-forward | Unclosed balances flow to next month as opening balance |
| Open month | Admin close button visible in expenses summary card |
| Closed month | Previous months show read-only; no new expenses to past months |

---

## D. Membership & Multi-Flat

### D1. Joining / Leaving
| Condition | What happens |
|-----------|--------------|
| Invalid invite code | `flatExists(code)` check before Firestore operations; immediate clear error |
| Valid code, open flat | Auto-joined, auto-added to all queues |
| Valid code, approval-mode flat | Join request created; admin approves/rejects |
| Non-admin leaves | Removed; tasks reassigned; switch to next flat or → onboarding |
| Admin leaves (others in flat) | Must transfer admin role first |
| Admin leaves (last member) | Warning: entire flat deleted |
| Leave flat redirect | `switchFlat(nextFlatId)` + `initFirestoreListeners(nextFlatId)` called before router.push |

### D2. Kicking Members
| Condition | What happens |
|-----------|--------------|
| Admin kicks member | writeBatch: member delete + memberCount-- (atomic) |
| Kicked member's tasks | Reassigned to `newQueue[leavingIndex % newQueue.length]` (correct next-in-queue) |
| Kicked member opens app | Sees onboarding on next open (no flat → redirect) |
| memberCount mismatch | Prevented by atomic batch — if decrement fails, delete also rolls back |

### D3. Multi-Flat
| Condition | What happens |
|-----------|--------------|
| User in multiple flats | FlatSwitcher dropdown (Gmail-style) |
| Flat switch | `switchFlat()` + `initFirestoreListeners()` — instant, no page reload |
| Premium maxFlats | 3 flats; trial/expired = 1 flat |
| Multiple flats + expired | Red sidebar card + mobile banner warning |
| Join/create additional flat | Available from inside dashboard without logout |

### D4. 8-Member Cap
| Condition | What happens |
|-----------|--------------|
| Flat at 8 members | Join attempts blocked at Firestore rules level |
| Flat after kick | memberCount decremented; cap re-opens |

---

## E. Voice Assistant

### E1. Permission & Platform
| Condition | What happens |
|-----------|--------------|
| Browser doesn't support SpeechRecognition | VoiceFallbackModal (iOS text input bottom sheet) |
| Mic not yet asked | Permission prompt shown |
| Mic allowed | startListening() → overlay |
| Mic blocked (Chrome) | MicPermissionModal: fix guide banner (how to unblock in chrome://settings) |
| Mic blocked (Safari) | Different guide for Safari settings |
| Android Chrome mic race | startListening fully synchronous; setState AFTER recognition.start() |
| iOS gesture context | TTS init happens on first user gesture to preserve audio unlock |
| Tab becomes inactive | Auto-stop listening |

### E2. Recognition & NLU
| Condition | What happens |
|-----------|--------------|
| 10s hard timeout | Auto-stop; reset to idle |
| 1.5s silence | Auto-stop; NLU processing begins |
| UNKNOWN intent | Rotating suggestion messages; no action |
| Hinglish input | Top 20 phrases mapped (ho gaya, kharcha, kitna, etc.) |
| Telugu-English input | Triggers on all 9 intents (aipoyindi, chesesa, icha, etc.) |
| Fuzzy task match | Levenshtein distance on task names (catches typos/mispronunciation) |
| Fuzzy member match | Same for member names |
| Amount "1.5k" | Parsed as 1500 |
| Amount "1.5 lakh" | Parsed as 150000 |
| Multi-flat context | ContextResolver uses activeFlatId — never bleeds to wrong flat |

### E3. Actions
| Intent | Executor | Condition |
|--------|----------|-----------|
| COMPLETE_TASK | completeTask.ts | Fuzzy matches task name; marks done via markTaskCompleted; invalidates cache |
| CREATE_EXPENSE | createExpense.ts | Creates expense; equal split; canUndo=true; 5s undo bar on card |
| QUERY_BALANCE | queryBalance.ts | Per-member or full net summary from context.balances |
| QUERY_TASKS | queryTasks.ts | Pending tasks for current user; overdue-first |
| QUERY_STATUS | queryStatus.ts | OOS vs home breakdown; member-specific lookup |
| REQUEST_SWAP | requestSwap.ts | Creates swap request to first available member |
| CREATE_TASK | createTask.ts | Task creation with frequency; all-member queue |
| GREETING | greeting.ts | Contextual greeting with pending task count |
| UNKNOWN | unknown.ts | Rotating suggestion messages |

### E4. Response Card
| Condition | What happens |
|-----------|--------------|
| Successful action | VoiceResponseCard slides up; TTS speaks response |
| Error | Error response card; TTS speaks error |
| Expense created | 5s undo bar; tapping undo deletes the expense |
| Auto-dismiss | 8s then card disappears |
| Overlay blocks tasks | Fixed — overlay no longer intercepts task taps after response |

---

## F. Subscription System

### F1. Status Transitions
| Condition | What happens |
|-----------|--------------|
| New flat created | `trialStartedAt` = now; status = 'trial'; 30-day window |
| Trial still active | Full access; no gate |
| Trial expired | status = 'expired'; AdminExpiredModal on dashboard |
| Coupon redeemed | status = 'active'; `couponExpiresAt` = now + couponDays |
| Coupon expired | status = 'expired' again |
| LEGACY_FREE flat | Old flats backfilled: 90-day trial from today (prevents hard-blocking) |

### F2. Gates
| Condition | Gated action |
|-----------|--------------|
| Expired — admin | Can't create_task, add_expense, create_bill, create_flat |
| Expired — member | Can't add_expense |
| Expired — all users | View-only access preserved |
| Expired — multi-flat | Red warning banner; can still view but not act |
| Admin tries gate | AdminExpiredModal with dismiss + "View in read-only" option |
| Member hits gate | MemberExpiredModal: "ask your admin for a coupon" hint |

### F3. Coupons
| Code | Duration | Who |
|------|----------|-----|
| HAB-WELCOME | 90 days | New admins (pre-filled in AdminWelcomeModal) |
| EARLYBIRD-2026 | 90 days | Early adopters |
| HABITIQ-BETA | 90 days | Founders/testing only |

### F4. Premium UI
| Condition | UI element |
|-----------|------------|
| status = 'active' | Crown badge on sidebar avatar |
| status = 'active' | Gold "PREMIUM" pill in logo area |
| status = 'active' | Crown emoji in dashboard greeting |
| status = 'active' | Amber "Premium" label in footer |

---

## G. Rewards Wallet

### G1. Earning Rewards
| Condition | What happens |
|-----------|--------------|
| Task completed | Reward issued (if guards pass) |
| Within 24h of last reward | Blocked: `habitiq_last_reward_at` localStorage key |
| Retroactive completion (> 2h ago) | No reward — anti-abuse guard |
| Reward pool empty | Beardo fallback coupon |
| Pool updated in Firestore | All users get new codes automatically (6h cache TTL) |

### G2. Reward Modal & Wallet
| Condition | What happens |
|-----------|--------------|
| Reward earned | RewardUnlockModal: scale spring + confetti + brand name + "View Reward" CTA |
| "Later" tapped | Modal dismissed; reward still in wallet |
| Auto-dismiss | 8s |
| RewardsWallet empty | Dashed border card with lock icon + "Complete any task to earn" |
| Reward card | Scratch-card: indigo gradient + shimmer sweep + `●●●●-●●●● · tap to reveal` |
| Tap to reveal | Bottom sheet: full code + Copy button |
| Copy tapped | Code copied; marked isRedeemed in Firestore |
| Firestore rule | Owner can read+create; update restricted to `isRedeemed` only |

---

## H. Auth & Login

### H1. Sign-In Conditions
| Condition | What happens |
|-----------|--------------|
| Google Sign-In (desktop) | Popup flow |
| Google Sign-In (mobile iOS Safari) | Redirect flow via custom authDomain proxy |
| Firebase domain not authorized | redirectError surfaced on login page; clear message |
| Email/password login | Standard Firebase emailAuth |
| Wrong password / invalid email | `getAuthErrorMessage()` maps to generic safe messages (no user enumeration) |
| Email doesn't exist | Same generic message (anti-enumeration) |
| Forgot password | "Forgot?" link → reset view → Firebase sendPasswordResetEmail |
| Password reset sent | "If an account exists, a reset link is on its way" (success always shown) |
| Session persistence | Firebase default session persistence |

### H2. Routing Guards
| Condition | What happens |
|-----------|--------------|
| Unauthenticated → /dashboard/* | Redirect to /login |
| Authenticated → /login | Redirect to /dashboard |
| /privacy or /terms | Public routes — no auth required |
| Kicked/left flat, no other flat | Redirect to /onboarding |
| New flat created | AdminWelcomeModal fires ONCE via `?new=1` query param |

---

## I. Flat Board (Find Members)

### I1. Seeker Profile
| Condition | What happens |
|-----------|--------------|
| First time | Profile creation form with lifestyle tags |
| Returning | Profile loaded from Firestore (seekerService) |
| Lifestyle tags | 25+ options across 4 categories (Work, Social, Diet, Sleep) |
| Tag saved | Ensured in Firestore via `ensureDiscoveryTag`; slugified |
| Profile visibility | Public to other authenticated users on Flat Board |

### I2. Vacancy Listings
| Condition | What happens |
|-----------|--------------|
| Flat has vacancy | Admin creates VacancyListing with rent range, location, tags |
| Seeker searches | Sees active listings with compatibility tags |
| Join request from board | `raiseJoinRequest` → flat's joinRequests subcollection |
| Approval-mode flat | Request awaits admin approval |

---

## J. Analytics & Insights

### J1. Calendar (Insights page)
| Condition | What happens |
|-----------|--------------|
| Day selected | Shows tasks completed that day |
| Member filter | Calendar filtered to one member's completions |
| No completions | Day shows as empty |
| Month navigation | Shared between calendar and stats tabs |

### J2. Stats
| Condition | What happens |
|-----------|--------------|
| Monthly filter | Shows current month's stats |
| All-time filter | Shows lifetime aggregate |
| Reliability score | Based on on-time completions vs total |
| Admin view | Sees all members |
| Member view | Sees all members (transparency by default) |

---

## K. Settings & Admin Controls

### K1. Manage Flat
| Condition | What happens |
|-----------|--------------|
| Rename flat | Admin only; updates Firestore; immediate UI update |
| Copy invite code | Copies flatId to clipboard |
| Copy invite link | Full URL with code param |
| Join mode: open | Any code-holder can join |
| Join mode: approval | Admin must approve join requests |
| NPS responses visible | Admin sees all responses in Manage Flat |
| Non-admin accesses | Redirect to dashboard with "Admin only" message |

### K2. Voice Settings
| Condition | What happens |
|-----------|--------------|
| Voice toggle off | localStorage `habitiq-voice = 'false'`; FAB tap shows "voice disabled" |
| Voice toggle on | Default; FAB tap activates listening |
| Example commands panel | Shows 9 sample commands across intents |

### K3. Danger Zone
| Condition | What happens |
|-----------|--------------|
| Admin: Erase All Expense Data | 3-step confirm → `resetAllExpensesData()` → clears all 5 subcollections in 400-doc batches |
| Admin: Delete flat | Last member: entire flat deleted. Admin with others: must transfer role |
| Member: Leave flat | Removed; next flat loaded or → onboarding |

---

## L. PWA & Performance

| Condition | What happens |
|-----------|--------------|
| First visit mobile | PWA install prompt (deferred) |
| Install app | Service worker registered; offline fallback page |
| Offline | Offline fallback shown; no data |
| Activity log read | query + limit(50) + orderBy server-side — O(50) reads always |
| Optimistic updates | Expenses delete, task complete — UI updates before Firestore confirms |
| Mock mode | `NEXT_PUBLIC_FIREBASE_*` env vars absent → seeded demo data, no DB |

---

## M. Firestore Rules (Access Control Matrix)

| Subcollection | Read | Create | Update | Delete |
|--------------|------|--------|--------|--------|
| members | any member of flat | admin | admin (role) / self (status) | admin |
| tasks | any member | admin | admin (edit) / member (mark done) | admin |
| swapRequests | any member | member (own) | `toUserId == request.auth.uid` only | admin |
| activityLog | any member | member (`userId == auth.uid` or `userId == 'system'`) | never | never |
| expenses | any member | member | creator or admin | creator or admin |
| settlements | any member | member | creator or admin | creator or admin |
| recurringBills | any member | admin | admin | admin |
| billInstances | any member | payer or admin | payer (`status,paidAt,collectedFrom`) / collector (`collectedFrom`) / admin (all) | admin |
| monthCycles | any member | admin | admin | admin |
| joinRequests | admin reads all / requester reads own | any auth user (own) | admin only | admin |
| npsResponses | admin | member (own) | never | never |
| /rewardPool | any auth user | Console only | Console only | Console only |
| /users/{uid}/rewards | owner | owner | owner (`isRedeemed` only) | never |

---

## N. What Is NOT Handled Yet (Known Gaps)

| Gap | Phase |
|-----|-------|
| Push notifications | Phase 2 |
| Settlement confirmation from recipient | Phase 3 |
| Admin flat-wide balance matrix (all pairs) | Phase 2 |
| Task photo proof | Phase 2 |
| Expense receipt photo | Phase 2 |
| Cross-currency balance conversion | Phase 2 |
| Task history > 30 days | Phase 2 |
| Flat announcements (pinned message) | Phase 2 |
| Voice: history panel (last 10 commands) | Sprint 5 |
| Voice: screen-based default intent | Sprint 5 |
| Voice: privacy analytics | Sprint 5 |
| CSP headers | Pending |
| Firebase App Check | Phase 3 |
| Firebase API Key Restrictions | Pending (Console task) |

---

*This document is the single source of truth for conditions and edge cases.*
*Update this file whenever a new condition is handled or a gap is filled.*
