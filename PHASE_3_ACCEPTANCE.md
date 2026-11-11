# Phase 3 — navigation, glass sheets, and UI states

## Navigation acceptance matrix

The same transition helpers used by `MainActivity` are exercised by
`Phase3NavigationMotionTest` in both RTL and LTR directions.

| From | To | Transition | Automated coverage |
|---|---|---|---|
| Splash | Onboarding | RTL-aware horizontal slide + fade | RTL/LTR |
| Onboarding | Calibration | RTL-aware horizontal slide + fade | RTL/LTR |
| Calibration | Dashboard | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Home | Link Scanner | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Academy | Cyber Glossary | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Academy | Security Checklist | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Settings | About | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Settings | Adaptive Feedback | RTL-aware horizontal slide + fade | RTL/LTR |
| Dashboard / Settings | App Lock | RTL-aware horizontal slide + fade | RTL/LTR |
| Home | Academy | Fade-through | RTL/LTR |
| Academy | Chat | Fade-through | RTL/LTR |
| Chat | Library | Fade-through | RTL/LTR |
| Library | Resources | Fade-through | RTL/LTR |
| Resources | Settings | Fade-through | RTL/LTR |
| Settings | Home | Fade-through | RTL/LTR |
| Each secondary screen | Previous destination | Reverse horizontal slide + fade | RTL/LTR |

The instrumentation test asserts every destination is rendered, every back path
returns to Dashboard, and the dark host background remains mounted during route
transitions. CI execution status is recorded in the pull request checks.

## Glass sheets

The legacy dialogs in Secure Vault and Chat have been replaced by the shared
`GlassModalBottomSheet` host and `GlassSheet` surface. The host supplies a
modal scrim, Material drag gesture/state, and safe system insets; the surface
provides the glass material and visible drag handle.

## UI states

- Loading: `GlassLoadingState` with animated glass shimmer, used by Academy scenario generation and Secure Vault file operations.
- Empty: `GlassEmptyState` with icon and explanatory text, used by Library search results and Secure Vault.
- Error: `GlassErrorState` with retry action, used for Academy generation fallback and Chat requests. Chat retries reuse the failed prompt without duplicating the user message.

Expandable cards in Library and Cyber Glossary use `animateContentSize()`.
