# Habitiq — Security Audit & Changes

**Date:** 2026-05-27  
**Audited by:** Claude Code (acting as ethical hacker / security reviewer)

---

## Summary

A full code security audit was performed on the Habitiq app (Next.js + Firebase + Netlify).  
**8 issues found. All 8 fixed.**

---

## Issues Found & Fixed

### 🔴 HIGH — Firestore: Any member could create/delete tasks
**File:** `firestore.rules`  
**Problem:** The `tasks` subcollection used `allow write` for all flat members. This means any member could create new tasks, delete any task, or change task assignments — even without being admin.  
**Fix:** Split into `allow create, delete` (admin only) and `allow update` (any member).

---

### 🔴 HIGH — Firestore: Any member could accept/reject ANY swap request
**File:** `firestore.rules`  
**Problem:** Swap request `allow update` only checked membership, not that the user was the intended recipient. Any member could accept or reject swaps meant for other people.  
**Fix:** Added `resource.data.toUserId == request.auth.uid` — only the person the swap was sent TO can resolve it.

---

### 🟡 MEDIUM — No HTTP security headers
**File:** `next.config.ts`  
**Problem:** No security headers were set. App was vulnerable to clickjacking (iframe embedding), MIME sniffing, leaking referrer info, and browser feature abuse.  
**Fix:** Added the following headers to all routes:
- `X-Frame-Options: DENY` — prevents clickjacking
- `X-Content-Type-Options: nosniff` — prevents MIME sniffing
- `Referrer-Policy: strict-origin-when-cross-origin` — limits referrer leakage
- `Strict-Transport-Security: max-age=31536000` — enforces HTTPS
- `Permissions-Policy: camera=(), microphone=(), geolocation=()` — locks browser features
- `X-XSS-Protection: 1; mode=block` — legacy XSS protection

---

### 🟡 MEDIUM — Auth errors exposed Firebase error codes (user enumeration)
**File:** `app/page.tsx`  
**Problem:** Raw Firebase error messages like `"auth/user-not-found"` or `"auth/wrong-password"` were shown directly to users. An attacker could probe the system to discover which emails are registered.  
**Fix:** Created a `getAuthErrorMessage()` function that maps error codes to friendly, non-revealing messages:
- `user-not-found`, `wrong-password`, `invalid-credential` → `"Invalid email or password."`
- `too-many-requests` → rate-limit warning message
- `popup-closed-by-user` → silent (no error shown)
- All others → generic fallback

---

### 🟠 LOW — `Math.random()` used for generating IDs
**File:** `store/useFlatStore.ts`  
**Problem:** Activity log IDs, task IDs, and swap request IDs were generated with `Math.random()`, which is not cryptographically secure and could produce predictable values.  
**Fix:** Replaced all three with `crypto.randomUUID()`, which uses the browser's cryptographically secure random number generator.

---

### 🟠 LOW — Google icon loaded from external CDN (svgrepo.com)
**File:** `app/page.tsx`, `public/google-icon.svg`  
**Problem:** The Google logo was fetched from `https://www.svgrepo.com/...` on every page load. This creates:
- A dependency on a third-party CDN being online
- A potential supply-chain attack vector (if svgrepo.com is compromised)
- Blocked by Content Security Policy if you ever add one  
**Fix:** Downloaded the SVG and saved it locally as `/public/google-icon.svg`. Page now serves it from your own domain.

---

### 🟠 LOW — Firestore: Activity log userId not validated
**File:** `firestore.rules`  
**Problem:** Any member could write an activity log entry with any `userId` value — including `'system'` or another member's UID — making the audit trail untrustworthy.  
**Fix:** Added validation: `request.resource.data.userId == request.auth.uid || request.resource.data.userId == 'system'`. Members can only log actions for themselves; 'system' is still allowed for automated overdue checks.

---

### 🟠 LOW — No input length limits on text fields
**Files:** `app/page.tsx`, `app/onboarding/page.tsx`  
**Problem:** Nickname, flat name, email, and password fields had no `maxLength` limits. A user could type extremely long strings that get stored in Firestore, wasting storage and potentially causing display issues.  
**Fix:** Added `maxLength` attributes:
- Nickname: 30 characters
- Flat name: 50 characters  
- Email: 254 characters (RFC 5321 standard max)
- Password: 128 characters max, 6 minimum (`minLength={6}`)

---

## What Was Already Done Correctly ✅

| Item | Status |
|------|--------|
| `.env.local` is in `.gitignore` | ✅ Safe |
| `NEXT_PUBLIC_*` keys are intentionally client-side (Firebase design) | ✅ By design |
| Mock login buttons hidden in production (`!hasKeys` check) | ✅ Correct |
| Auth routing enforced by `AuthProvider` (not just client redirects) | ✅ Good |
| Firestore rules require Firebase Auth on every operation | ✅ No anonymous access |
| `onAuthStateChanged` drives auth state (not just localStorage) | ✅ Secure |
| `SECURITY.md` explains the key exposure model | ✅ Documented |

---

## Files Changed

| File | What Changed |
|------|-------------|
| `firestore.rules` | Rewrote rules with helper functions; fixed task, swap, and activity log rules |
| `next.config.ts` | Added 6 HTTP security headers |
| `store/useFlatStore.ts` | Replaced `Math.random()` with `crypto.randomUUID()` in 3 places; added `redirectError` + `clearRedirectError` |
| `public/google-icon.svg` | New file — Google logo served locally |
| `app/page.tsx` | Localized Google icon; added `getAuthErrorMessage()`; reads `redirectError`; added `maxLength` to inputs |
| `app/onboarding/page.tsx` | Added `maxLength` to nickname and flat name inputs |

---

## Bug Fix — Google Login Silent Failure (2026-05-27)

**Symptom:** Clicking "Continue with Google" opened the account picker, user selected their Gmail, but the app silently returned to the login screen without logging in.

**Root cause (two-part):**

### Part 1 — Firebase Authorized Domains (manual fix required in console)
`flatsflow.netlify.app` was not added to Firebase → Authentication → Authorized Domains.  
Firebase rejects the Google OAuth callback from unrecognized domains, causing the sign-in to fail.

**To fix:** Go to [Firebase Console](https://console.firebase.google.com) → your project → **Authentication → Settings → Authorized domains** → click **Add domain** → enter `flatsflow.netlify.app` → Save.

### Part 2 — Silent redirect error (code fix applied)
The `getRedirectResult` error handler (mobile sign-in flow) only called `console.error` — the user saw nothing on screen. Added:
- `redirectError` field to the auth store
- Error is set in the store when redirect fails
- Login page reads `redirectError` via `useEffect` and displays it
- `getAuthErrorMessage()` now handles `unauthorized-domain` with a clear message for both popup and redirect flows

---

## Still Recommended (Future Work)

These are not urgent for a personal-use app but worth knowing:

1. **Firebase API Key Restrictions** — In Firebase Console → API Keys, restrict your key to only work from your production domain (`flatsflow.netlify.app`). This is the single most impactful thing after Firestore rules.
2. **Password strength UX** — Show a "minimum 6 characters" hint below the password field so users know before they hit an error.
3. **Content Security Policy (CSP)** — Harder to set up (Firebase uses many origins), but fully locks down script/resource loading. Consider when the app matures.
4. **Remove a member feature** — Currently there's no way to remove a member from a flat. If a roommate leaves, they still have read access to all flat data. Add an admin "remove member" action.
5. **Rate limiting** — Firebase's free tier has built-in throttling, but consider Firebase App Check for additional abuse prevention.
