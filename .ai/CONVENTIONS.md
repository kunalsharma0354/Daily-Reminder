# CONVENTIONS

- Naming: Entity suffix (`ReminderEntity`), Dao suffix, Impl suffix, UseCase verb (`CreateReminder`), Screen/ViewModel/Card/Section suffix.
- Compose: stateless cards/sections take data + lambdas; Screen collects StateFlow; VM only delegates to use cases; no business logic in Composables/VM.
- Coroutines: `viewModelScope` for UI, `Dispatchers.IO` in receivers/workers, Flow `WhileSubscribed(5000)`, `distinctUntilChanged` where needed.
- Dates: `java.time.LocalDate` device zone via `DateTimeUtils.today()`, epochDay Long in DB, never UTC for "today".
- Theming: Material3 tokens only (`colorScheme.primary` etc.), custom `ElectricBlue/NeonLime` in theme, 16dp cards, 48dp targets, contentDescription on all icons/heatmap cells.
- Haptics: light on press, confirm on Done via View.performHapticFeedback; no vibration API directly.
- Permissions: contextual request, rationale banner + Settings deep-link, graceful fallback.
- Testing: UseCase with MockK + `runTest`, pure JVM utils, Room in-memory when Robolectric available; Turbine for Flow.
