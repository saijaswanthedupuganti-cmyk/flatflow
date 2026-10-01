# Brief-to-implementation coverage

This index helps the executor trace supplied requirements without treating attachment statements as verified runtime facts. Duplicate correction brief is counted once. Section ranges refer to preserved evidence text files. All implementation/QA statuses are pending.

| Correction gate section | Specification/packet | Evidence required |
|---|---|---|
| 01 One system; 09 semantic colors; 40 brand | 02 design system, P01 | VIS-01/02 |
| 02–03 safe areas/system bars | 02 inset contract, P02 | VIS-03, BRAND-02 |
| 04–05 headers/back | 02 scaffold/app bar, P02 | NAV-01/02 |
| 06 bottom navigation | 01 conflict register, 04 IA, P02 | Explicit four/six decision; NAV-01 |
| 07 focused multistep flow | 02 scaffold, 04 state machine, P02/P07 | NAV-02, POST-04 |
| 08 chips | 02 component contract, P03 | VIS-05 |
| 10 alerts/overdue | 02 alerts, 04 task states, P03/P05 | VIS-06, TASK-02 |
| 11 cards; 12 arrows | 02 components, P03 | VIS-04/05, cross-screen visual review |
| 13 fields; 14 actions; 15 progress | 02 primitives, 04 validation, P03/P07 | POST-01/05, NAV-02 |
| 16–17 illustrations/crop | 02 image policy, 05 asset contract, P04/P06 | BRAND-01, DISC-03/04 |
| 18 Profile | 04 entry/profile, P05/P08 | AUTH-02, LIFE-01 |
| 19 Discover; 20 search | 04 discovery, P06 | DISC-01/02/04 |
| 21 interested people/messages | 04 social, P08 | REQ-01/02, CHAT-01/02 |
| 22 test content; 23 data presentation | 02 content policy, 04 household, P05/P06 | MONEY-01, DISC-04 |
| 24 empty; 25 loading; 26 error; 27 success | 02 states, 04 operation contracts, P03/P05–08 | VIS-06, STATE-03, REQ-01, POST-07 |
| 28–30 motion/reduced motion | 05, P04 | MOT-01/02 |
| 31 interaction states; 32 touch | 02 components, P03 | VIS-05 |
| 33 scroll; 34 keyboard | 02 inset contract, P02 | VIS-03, NAV-02 |
| 35 long content; 36 device sizes | 02 flexible layout, 06 environment matrix | VIS-04 |
| 37 accessibility | 02 contrast/semantics, 06 QA | VIS-02/04/05, MOT-01 |
| 38 hierarchy; 39 density; 41 restraint | 02 geometry/components, P03/P05/P06 | Visual comparisons and state ledger |
| 42–43 consistency/final visual QA | 06 visual procedure and full case ledger, P10 | All applicable visual/runtime cases |
| 44 foundation-first order; 45 output | 03 packet dependencies, 08 executor instructions | Execution ledger + release evidence |

## Launch/loader add-on

| Add-on section | Specification/packet | Evidence required |
|---|---|---|
| 01–03 icon/background/adaptive safety | 05 asset manifest and roles, P04 | BRAND-01 |
| 04–05 splash | 05 launch sequence, P04 | BRAND-02 |
| 06 source SVG | 01 asset provenance, 05 asset policy | Original/export hash and rendered inspection |
| 07–09 branded loader | 05 motion table, P04 | MOT-01/02, BRAND-02 |
| 10 loading types; 11 skeleton | 02 states, 05 loading contract, P03/P04 | VIS-06 |
| 12 timing; 13 transition | 05 bounded motion, P04 | Startup/transition recordings |
| 14 button loading | 02 button, P03 | No duplicate submit/layout jump |
| 15 uploads | 04 photo lifecycle, P07 | POST-02 through POST-08 |
| 16 failure; 17 offline | 04 states, P05–08 | STATE-03, POST-05/06, REQ-01 |
| 18 motion accessibility | 05, P04 | MOT-01 |
| 19 consistency; 20 white background; 21 quality; 22 QA | 05 and 06, P04/P10 | BRAND-01/02 |
| 23 experience; 24 preserve product | README scope, 03 packets | No new IA/assets/features without recorded contract |

The separate 23-section design-system gate and historical visual-QA context overlap these controls. Their current-screen claims stay historical until P00 captures runtime evidence. Direct palette and preserved artwork are current explicit user inputs.

## Additional screen-level details

- Profile groups: identity; My flat; Discovery (profile/posts); Account. Prefer labelled sections and consistent rows, not a card around each row. Show missing discovery profile with an honest setup action. Save state and errors remain visible until resolved.
- Manage attention: overdue item shows icon + “Overdue” + due information, with a restrained semantic accent and one appropriate action. Avoid a red page or repeated alert badge/button/icon all competing for attention.
- Search: persistent labelled field, clear control, optional genuine history with working clear action, selected-filter count/reset, suggestions only from actual data, and distinct results/loading/error. No hard-coded “recent” entries.
- Interested people: portrait/name/context/status and permitted action. Use whole-row details navigation where appropriate; Accept/Decline are distinct actions with pending/error state. Long names and action labels must not collide.
- Progress: actual current step and total, meaningful step title; Back/Continue stable across form steps; Publish only at final preview. Cancel/discard follows dirty-state policy. Numeric steps are progress markers, never a replacement for meaningful input icons or labels.
- Icons: retain one existing family and semantic mapping (search, location, calendar, money, person, photo, back). Numbers belong to counts/step progress, not arbitrary numbered substitutes for action icons. This resolves the earlier ambiguous “number icons” message conservatively without inventing a new icon brand.

## Screen inventory completeness rule

The minimum screen ledger in document 04 is not an exhaustive claim about this repository. P00 enumerates every reachable destination and overlay; P10 requires a disposition for each: tested, deliberately out of shipped scope with reason, or blocked. New reachable screens discovered during implementation are added to the ledger. No percentage-complete claim without a defined denominator.
