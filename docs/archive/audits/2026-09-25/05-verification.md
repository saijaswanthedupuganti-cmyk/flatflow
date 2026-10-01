# Verification ledger and acceptance matrix

## Implementation checkpoint — 25 September 2026

The Android debug application was compiled after the Home/navigation and Discovery persistence implementation passes. `:app:assembleDebug` completed successfully and produced `android/app/build/outputs/apk/debug/app-debug.apk`. `:app:testDebugUnitTest` initially exposed four existing flat-error copy mismatches; `FlatException.kt` was aligned to the tested contract. The final run passed 26 tests across five suites with no failures, errors or skips. This proves compilation and the present unit-test set only.

Implemented in this checkpoint:

- Home's Tasks, task rows, Expenses, join requests, swap requests and Discovery entries now navigate to their existing feature owners.
- Inactive menu/notification affordances and the arbitrary Flat Health percentage were removed from Home.
- The large decorative Home summary was replaced with a quieter action-oriented greeting; structural emoji copy was removed.
- Bottom navigation exposes selected tab semantics; the central Add action exposes button semantics.
- Shared buttons now meet the Android 48 dp minimum and restore visible platform press feedback; shared text actions receive a larger target.
- Flat join errors now use the repository's unit-tested plain-language messages.
- Vacancy and looking-post editors validate required fields, stay visible while saving and leave only after Firestore confirms success; failed saves remain recoverable in context.
- Chat reads use participant-scoped Firestore queries. New messages require an accepted connection and retain the draft after a failed send.
- Connection requests use a stable participant/context identity to reject duplicates; local Firestore rules constrain blocked pairs, state transitions and connection-bound message creation.

No Android device or `adb` executable was available, so installation, screenshots, TalkBack, keyboard, system-back, rotation, large text, dark mode, reduced motion and real Firebase behavior remain pending. Android lint was started but did not produce a completed report in this environment, so it is not recorded as passed. The native iOS implementation remains a macOS/Xcode task governed by document 07.

## Actual checks in this audit

| Check | Result | Limit |
| --- | --- | --- |
| Product vault and feature masters | Reviewed key intent, state, finance, Discovery and profile documents | Conflicting historical statements explicitly recorded |
| Source inventory | 166 files inventoried with line counts/hashes | Inventory is not exhaustive manual line review |
| Current loader lint | Passed for `components/AuthProvider.tsx` and `components/HabitiqLoadingScreen.tsx` | No broad build result implied |
| Current loader desktop | Visually inspected at 1280 × 720 | Current dev app remained in loading state; auth delay root cause not established |
| Current loader phone | Visually inspected at 390 × 844; document width 390 | Real device/OS motion preference not tested |
| Reduced motion | CSS branch verified; movement replaced with opacity | Runtime OS emulation not performed |
| Mock homepage / auth | Rendered; account modal and fields inspected | Not real Google/email authentication |
| Mock admin login | Passed; Dashboard displayed admin controls | Seeded local state only |
| Dashboard phone/desktop | Captured; phone screenshot inspected, width 390 | Other viewport/state combinations pending |
| Expenses first entry | Automatic generation modal reproduced and captured | No real bills generated |
| Expense form | Filled description and ₹100 amount; showed four ₹25 shares | Custom split/closed-month/negative/error cases pending |
| Mock expense save | Passed; history row added, payer receivable increased by ₹75 | Does not validate Firestore rules or native parity |
| Task management | Rendered admin list and three-type chooser | No production task mutation; full wizard validation pending |
| Profile | Rendered and visually inspected | No leave/transfer/delete action performed |
| Insights and Activity | Rendered content inspected | No complete historical data correctness claim |
| Other supporting routes | Source navigation inspected; some initial navigation captures contain only loading status | Do not count loading snapshots as route passes |
| Android device availability | Device list empty | Native runtime QA blocked by missing device/emulator |
| Native source | Key routing, form, Manage, balance, messaging and lifecycle owners traced | No APK build or device visual comparison in this audit |
| Existing full test suite | Not run as a valid acceptance suite | Auth assumptions outdated; environment must be made deterministic first |
| Production | No deployment, no live data changes | No release certification |

Local mock preview used a source copy without environment files, explicitly setting the existing mock sentinel `NEXT_PUBLIC_FIREBASE_API_KEY=YOUR_API_KEY`. This avoids changing the existing developer server or live account. It is not production configuration advice.

## Required release matrix

Status for the cases below is **pending implementation/verification**, except where the actual ledger above records a limited pass.

| Area | Mandatory cases | Observable success |
| --- | --- | --- |
| Public discovery | Zero/one/many results; actual filters; image failure; removed record; Back/reload/share | Truthful content; clear next action; context preserved |
| Authentication | Empty/invalid/wrong credential; Google cancel; popup blocked; offline; double submit; expired session | Clear useful error, no lost input/intent, controls recover |
| Recovery | Valid/invalid email format, send failure, back to sign in | Generic success, no enumeration, user can retry |
| Invite | Signed-out URL, invalid/full/already-member/approval/auto | Same code survives login; correct preview; no premature membership |
| Account resolution | Missing profile, load failure, removed flat, many flats | Distinguish absence/failure; safe next route |
| Create | First flat, explicit second flat, repeated tap, process death | One intended flat, persisted resume, no silent reuse of wrong flat |
| Tasks | Member/admin, mine/all, empty, overdue, everyone away, complete retry | Correct responsibility and queue; no unauthorized controls |
| Swaps | Own task, invalid recipient, pending duplicate, accept/decline, expired task | Correct actor/state; clear result; no silent transfer |
| Expenses | Equal/custom, rounding, payer absent, no participants, non-finite amount, retry | Valid sum, single saved record, preserved input |
| Currency | INR/USD same pair, foreign-currency settlement, deferred and bill-linked rows | Separate currency totals and correct exclusions |
| Bills | Fixed/variable, changed collector, generated twice, self-collection | One instance, accurate payer vs collector, valid permission |
| Month close | Outstanding debt, closed-period write, carry-forward, reopen policy | Explicit outcome; no debt duplication/loss |
| Discovery connections | Own listing, no flat, sender/recipient/unrelated, decline/block/expiry | Valid state transitions and backend enforcement |
| Chat | Query denial, empty, offline, pagination, 100+ messages, blocked participant | Error is not empty; complete permitted conversation |
| Profile | Account vs discovery edit, long name/email, visibility off | Correct persistence scope and privacy explanation |
| Leave/delete | Last admin, many flats, failed cleanup, recent-login required | No orphaned account, clear resumable recovery |
| PWA/desktop | Install, standalone launch, update, offline/reconnect, logout | Intended route/state; no stale private data; correct installation messaging |
| Permissions | Notification/location/biometric denied, unavailable | Core app remains usable; no repeated forced prompt |
| Accessibility | Keyboard, focus containment, screen reader, 200% text, contrast, reduced motion | Labels, order and states understandable without color/motion |

## Device and visual matrix

- Web: narrow phone 320/360, reference phone 390 × 844, tablet 768, desktop 1280 × 720, wide 1440; portrait/landscape and short viewport.
- Browsers: Chromium desktop, Android Chrome and iOS Safari. An in-app-browser pass is not an iOS pass.
- Native: actual Android phone or emulator at the app's supported baseline and a contemporary device; font scaling, keyboard, back gesture, permission denial, process recreation.
- Themes: supported light/dark app states and the explicit native onboarding exception.
- Input: mouse, touch, keyboard; authenticated admin, member, no-flat, pending join, multi-flat.

Use a deterministic date/timezone, fixtures and font environment for pixel comparisons. Real phone tests must cover bottom safe area, IME/keyboard, sheet scrolling and focus visibility.

## Source verification commands for the executor

```text
npm run lint
npm run build
npm run test:auth
npm run test:expenses
npm run test:tasks
npm run test:mobile
android/gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Run only after Packet 0 establishes a safe test environment and against the correct working directory. Firebase emulator rules tests must be added for the changed rules; the existing Playwright mocks do not cover backend authorization. Build/network/toolchain failures must be reported accurately rather than represented as product test failures.

## Definition of done

Every changed flow has happy, empty, loading, error, disabled and relevant permission/offline states; navigational context survives authentication and Back; backend enforces what the UI promises; web/native calculations agree with fixtures; final rendered screenshots have been inspected; approved targets match at their exact dimensions; mobile/desktop remain usable outside those dimensions; release notes state remaining limitations.

No “100% professional”, “pixel-perfect everywhere”, “production-ready” or comprehensive accessibility claim is justified by source inspection alone.
