# Discover — Use a Flat (APK)

**Date:** 22 August 2026  
**APK:** `Habitiq-FINAL-debug.apk`  
**Firebase:** `garbage-f79f7` · **Package:** `habitiq.app`

---

## Scope (Aug 22 evening)

Sai narrowed scope to **Use a flat / Find a flat only**. Find a person is a minimal stub (“coming soon”). No Figma vacancy frames existed — UI was designed in-app using Habitiq brand tokens (`FigmaColors`) and Home admin segmented-control patterns.

---

## DB fields used (real Firestore / web schema)

Embedded on `flats/{flatId}.vacancy` (matches web `VacancyListing` in `useFlatStore.ts`):

| Field | Type | Used for |
|-------|------|----------|
| `active` | boolean | List only when true |
| `city` | string | Search + filter |
| `area` | string | Search + filter |
| `rentPerHead` | number? | Card display + rent min/max filter |
| `currency` | string | Display (INR default) |
| `bedsAvailable` | number | Room type filter (1 = private, 2+ = shared) |
| `preferredGender` | string | Filter + card pill (`any`, `male`, `female`, `women_only`) |
| `lifestyle` | string[] | Tag chips + lifestyle filter |
| `customTags` | string[] | Tag chips + lifestyle filter |
| `existingMembersGender` | string? | Parsed (future card label) |
| `about` | string | Card body |
| `updatedAt` | ISO string | “Listed within” filter (7/30/90 days) |

On flat doc (parent):

| Field | Type | Used for |
|-------|------|----------|
| `name` | string | Card title |
| `flatType` | string | Filter + pill (`apartment`, `house`) |
| `adminUid` | string | Contact poster → chat |
| `memberCount` | number | “Habitiq member · N in flat” trust label |

`discoveryTags` collection is documented in backend spec but **not wired** — filter tag options use the same vocabulary as `vacancy.lifestyle` / `customTags` on web.

---

## Filters implemented (Use a flat)

| Filter | Wired | Notes |
|--------|-------|-------|
| City / area | ✅ | Inline search + filter sheet |
| Rent min / max | ✅ | `rentPerHead` |
| Gender preference | ✅ | `preferredGender` |
| Flat type | ✅ | `flatType` on flat doc |
| Room type | ✅ | Derived from `bedsAvailable` |
| Listed within | ✅ | `updatedAt` proxy (no `availableFrom` in schema) |
| Lifestyle & tags | ✅ | Matches `lifestyle[]` + `customTags[]` |

Active filters show as removable chips; “Clear all” resets.

---

## UI decisions (no Figma frames)

Designed to match existing Habitiq Android quality bar:

| Element | Decision |
|---------|----------|
| **Dual switch** | Same segmented control as Home admin toggle — violet `FigmaColors.Primary` selected, 16dp radius, border `SurfaceBorder` |
| **Header** | “Discover” 26sp bold + subtitle; no chat icon (deferred with Find a person) |
| **Search** | `FigmaTextField` with search icon — matches create-flat wizard inputs |
| **Filter button** | Tonal icon in `PrimaryLight`; badge shows active filter count |
| **Safety banner** | `WarningBg` per trust doc — “Never pay before viewing” + link to `/safety` |
| **Vacancy cards** | 18dp radius, 1dp border, gradient icon tile, rent right-aligned, tag chips in `PrimaryLight`, meta pills in `SurfaceMuted`, teal verified member label, full-width “Contact about this room” CTA |
| **Empty / loading** | Centered illustration tile + copy distinguishing “no listings” vs “no matches” |
| **Find a person** | Centered stub — “coming next” |

**Figma file `WDCmDL7REv0Xo5dfn0sn4u`:** No dedicated vacancy/discover frames found; design tokens from `FigmaColors` (file `WDCmDL7REv0Xo5dfn0sn4u` home/create-flat frames).

---

## Code map

| File | Role |
|------|------|
| `ui/discover/UseAFlatDiscover.kt` | Polished Use-a-flat list, cards, search, chips |
| `ui/discover/DiscoverFilterSheet.kt` | Bottom sheet filters |
| `ui/DiscoverBoardScreen.kt` | Dual mode shell + Find person stub + chat handoff |
| `discover/DiscoverFilters.kt` | Filter models + `DiscoverFilterLogic` |
| `data/DiscoveryRepository.kt` | Real-time vacancy listener + full field parse |

---

## Build & install

```powershell
cd C:\garbage\android
.\gradlew.bat --stop
.\gradlew.bat clean assembleDebug
Copy-Item app\build\outputs\apk\debug\app-debug.apk C:\garbage\releases\Habitiq-FINAL-debug.apk -Force
adb install -r C:\garbage\releases\Habitiq-FINAL-debug.apk
```

Open app → **Discover** → **Use a flat** → search / filter / browse cards.

---

## Deferred

- `discoveryTags` Firestore collection for dynamic tag vocabulary
- Figma-polished vacancy frames (when design ships)
- Company / college / hometown signals (not in schema)
- Numerical / Plus trust tiers (no documented formula)

Find Flatmate, looking posts, connect state machine, report/block, and in-app chat-after-accept shipped 23 Aug 2026. See `project_1/DISCOVERY_WORKING_SPEC.md`.
