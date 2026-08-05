# Login/Signup/Home Visual Redesign — Design

**Date:** 2026-07-11
**Status:** Approved by Sai, ready for implementation planning

## Context

Login, Signup, and Home were built bare-bones in the very first plan (plain `OutlinedTextField`/`Button`, no color, no branding) — deliberately, since no visual direction existed yet. Since then, Create/Join Flat got a full dark/illustrated treatment. The mismatch reads as "not polished" / "not working." Separately, Sai found a real bug: a stray Android system title bar ("Habitiq" in a gray bar) covers the top of every screen — this is the OS's default ActionBar, never explicitly disabled, not a Compose rendering issue.

**Also flagged, not yet root-caused:** Google Sign-In produces "Something went wrong. Please try again." (the generic fallback message) for Sai during real device testing. This message is shared by several different underlying causes (Credential Manager failure, Firebase rejection, etc.) and can't be diagnosed from the text alone. Per the project's debugging discipline, this needs a live `logcat` capture during reproduction before any fix is proposed — this spec does NOT include a pre-written fix for it; the implementation plan's final task starts with evidence-gathering, not a guessed patch.

## In scope

### 1. Fix the stray ActionBar (bug fix, not a design choice)

`app/src/main/res/values/styles.xml`'s `Theme.Habitiq` currently extends `android:Theme.Material.Light` (no `NoActionBar` variant), so Android draws its own title bar showing the app label, covering the top of every Compose screen. Switch to `android:Theme.Material.Light.NoActionBar` and set `android:windowBackground` to the real dark canvas color so there's no light flash before Compose draws.

### 2. Real design tokens, not invented colors

Pulled directly from `C:\garbage\DESIGN.md` (the already-established Habitiq brand system):

| Token | Value | Use |
|---|---|---|
| Canvas (background) | `#0C0B0F` | Screen background |
| Ink (primary text) | `#F4F3F8` | Body/heading text |
| Ink-mute (placeholder) | `#514E61` | Placeholder/disabled text |
| Primary (violet) | `#7C3AED` | Primary button fill |
| Primary-soft | `#A78BFA` | Secondary violet moments |
| On-primary | `#0C0B0F` | **Text on violet fill — per "Button Law," never white text on a violet primary button** |
| Input background | `#1A1820` | Text field fill |
| Input border | `#2A2635` | Text field border (violet on focus) |
| Error | `#EF4444` | Error text/border |

Corner radius: 12px on buttons, 8px on inputs (DESIGN.md's canonical scale).

### 3. Login screen

Small wordmark treatment at top (not a full illustration — utility screen, not a story moment): "Habitiq" + tagline "Split expenses. Manage chores. Run your shared home." (reusing the web app's own established tagline for consistency). Below: email/password fields styled per the tokens above, a primary "Log in" button (violet fill, dark text per Button Law), a ghost-style "Sign up" link (transparent, hairline border, ink text), and "Sign in with Google" styled as a light/white secondary button (`#F4F3F8` fill, dark text) — matches both the design system's "secondary confirm" token AND Google's own brand button guidelines.

### 4. Signup screen

Same visual treatment, mirrored copy ("Create your account").

### 5. Home screen

No logic changes — the existing flat-status routing, sign-out-moved-to-Settings, retry button, etc. all stay exactly as built. Only restyle the container/text/buttons to the same token set.

## Explicitly deferred

- **Bundling the real Inter typeface.** DESIGN.md specifies Inter; the native app currently uses the system default (Roboto on Android). Bundling Inter needs font files added as an Android font resource — a separate small task, not blocking this pass. Sizes/weights are matched; the exact typeface is not.
- **The Google Sign-In "Something went wrong" bug's actual fix.** Root cause unknown until a live `logcat` capture during reproduction. The implementation plan's final task performs that capture and fixes whatever it actually finds — it does not pre-suppose a fix.

## Testing

Manual on-device verification only (visual changes + a live-diagnosed bug fix, not unit-testable business logic).
