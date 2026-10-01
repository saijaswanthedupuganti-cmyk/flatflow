# Brand asset, launch and motion specification

## Asset source and permitted processing

The supplied master is `assets/brand/HABITIQAPP.svg`. It has a 220 × 217 viewBox and wraps an embedded 1263 × 1246 PNG. The extracted original raster is preserved at `evidence/supplied-brand.png` for inspection. It shows a folded teal/turquoise mark. Treat it as supplied artwork, not an instruction-bearing document.

Preserve its shape, color transitions, proportions and transparency. Do not trace, recolor, add a letter, round the artwork itself, flatten its baked-in gradients, or replace it with a generic house. Keep UI color tokens separate from embedded artwork colors. Use the master viewBox ratio when exporting/rendering; never stretch to a square. The square icon canvas contains the centered mark with preserved aspect ratio.

The add-on refers to an approved “Code Agent” mark, but this pass has not established a distinct file for that name. Use the supplied master as the candidate asset; if the owner identifies another exact approved file, map roles explicitly before exports. Do not silently assume two different names mean two interchangeable designs.

Create an asset manifest during P04 with original hash, export tool/command, output dimensions, alpha/background policy and intended role. Preserve source; export into existing Android/web asset owners. Use a real renderer that resolves the SVG's embedded image. Android VectorDrawable conversion is unsuitable for this raster-wrapped input. Inspect the rendered result for blank output, altered gradients, cropping and unintended black background before integrating.

| Role | Proposed treatment | Required verification |
|---|---|---|
| Launcher | White square/adaptive background, centered supplied mark, OS owns mask | Circle, squircle, rounded-square and small launcher preview |
| System splash | White surface and static preserved brand mark within OS icon constraints | Android pre-12 and 12+ cold/warm launch |
| In-app initial load | Same white surface; preserved mark plus brief factual loading label when needed | No double animation or visual jump after system splash |
| Header/wordmark | Preserve existing approved brand typography; mark scaled proportionately | Legibility at small size and no distortion |
| Inline loading | Small native progress indicator in action/list context | Never a full-screen branded takeover for a minor operation |

Adaptive icon foreground and system splash have different safe areas. Use the current Android template/documentation when preparing assets and test masks; do not reuse a full-bleed launcher bitmap blindly as a splash icon. Reference: [Android splash screen guidance](https://developer.android.com/develop/ui/views/launch/splash-screen).

## Launch implementation sequence

1. Inspect existing manifest, Theme.Habitiq, launcher resources and startup activity. Establish the existing splash dependency/API choice before adding a library.
2. Set consistent white launch/post-splash backgrounds, and system-bar icon appearance appropriate for light surfaces. Replace the current dark window background and coral launcher background at their actual owners.
3. Configure the platform/compat splash on the existing main activity. Keep static artwork during OS-controlled launch. Avoid a second splash activity, fixed minimum delay, spinner inside the launcher icon, or bespoke fake progress percentage.
4. Hand over to actual auth/bootstrap state. If additional network data is pending, show contextual loading with cancel/retry/error handling; cached permitted content can render honestly with refresh status.
5. Record cold launch, warm launch, resume and supported deep-link launch. Confirm no black/coral flash, clipped logo, duplicated logo sequence or wrong initial destination.

System splash dimensions/timing differ by OS; the acceptance criterion is preserved artwork and coherent transition, not identical OS rendering on every phone. Android supplies the initial splash behavior. [Official implementation guidance](https://developer.android.com/develop/ui/views/launch/splash-screen)

## Proposed motion tokens

These are bounded implementation values, not observations of current behavior. Put them in the existing Hq motion owner, not in each screen. Reuse native Material transitions when they already satisfy the intent.

| Interaction | Normal motion | Reduced motion |
|---|---|---|
| Button press | Scale 1 → 0.98 → 1, 100–140 ms; retain ripple/pressed color | Color/ripple only, no scale |
| Chip/segment selection | Color/content transition 120–160 ms, no bounce or width jump | Immediate selection; focus retained |
| Screen content entering | One opacity transition 160–200 ms; optional 6dp translation once | No translation; immediate or short opacity |
| Dialog/sheet | Platform transition, interruptible, synchronized scrim | Platform reduced behavior; no extra spring |
| Request/save success | One restrained check appearance, 160–200 ms | Static success icon and text |
| Branded bootstrap loader | Static mark; optional opacity 0.85↔1 over 1,400 ms while truly waiting | Static mark + factual text; accessible busy state |
| Progress for photo upload | Real bytes/total; stable bar and label; no simulated advancement | Same factual bar without interpolation |
| Skeleton | Stable geometry, optional subtle native shimmer only when justified | Static placeholders |

No orbiting particles, spinning brand mark, tilt loop, pulsing shadow, perpetual decorative motion or confetti. Do not repeatedly animate lists as users scroll, or replay whole-screen entrances after every state update. Motion must not delay navigation, validation or first content. Native control feedback should remain recognizable.

Use a lifecycle-aware observer/platform mechanism for motion preference; the existing once-read animation-scale value can become stale after settings changes. Verify all animation paths, including infinite transitions, respect the setting. Restore stable state on background/foreground; cancel obsolete animations when navigating away. No retained animation jobs after composition exits.

## Illustrations and imagery

Keep existing approved illustrations where they explain onboarding, no-flat setup or an empty state. Inventory files and ownership before adding new artwork. Use one consistent treatment; never turn a decorative illustration into evidence of a real listing. Avoid generating replacements in this correction pass unless a specific missing asset is approved. For future new illustrations, provide a separate art brief, export manifest and visual review.

Real listing photos occupy predictable containers with aspect-ratio-preserving crop; default suggested card media ratio is 4:3 pending existing layout comparison. Include actual photo count and accessible image controls only if there is more than one image. Broken/missing image gets a neutral placeholder with honest copy, not a stock room presented as real.

## Completion evidence

Asset contact sheet, masked launcher previews, launch recordings on relevant OS versions, normal/reduced-motion videos, actual upload progress capture and interruption checks. A screenshot cannot verify motion, interruption or launch timing. Generated icon files alone do not prove launcher integration.
