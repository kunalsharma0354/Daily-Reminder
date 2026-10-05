# AGENTS.md — Daily Reminder (Nexora)

Stack: Kotlin 2.0.21, Jetpack Compose Material3, MVVM + Clean (domain/data/presentation), Hilt DI, Room v2, WorkManager + AlarmManager, NotificationCompat, Coroutines+Flow, Gradle version catalogs.

Package: `com.nexora.reminder`. App: `DailyReminderApplication`. DB: `daily_reminder.db` v5 (intervalMinutes, voiceText, voiceFilePath, mode ONCE/REPEAT/DAILY_TIME, dailyTimeMinutes). Channel: `daily_reminder_channel`. Work: `midnight_rollover`.

Theme: modern monochrome black & white (Bg #000/#FFF, surfaces #11/#F5, outline #2A/#E5). No blue/lime. 20dp cards, pill buttons/FAB, bordered cards. Animations: press spring, icon bounce, chip scale, FAB scale, staggered list enter, today pulse, count-up stats, sheet slide, empty float.

Interval: per-reminder 30m–6h (`intervalMinutes` in ReminderEntity/Reminder, presets 30/60/90/120/180/360, slider 15m steps). First nudge ~1min, then every interval, 24/7. Skip uses interval (`Skip 1h`). Card shows `Every 1h • 24/7` + preset chips to change. BottomSheet has chips+slider. `UpdateInterval` use case reschedules live.

24/7 guarantee (midnight never stops):
- `ReminderAlarmReceiver` self-heals today's PENDING if missing, skips only if COMPLETED today, chains with interval, re-arms midnight watchdog.
- `ReminderActionReceiver` Done stops TODAY only, Skip uses interval.
- `MidnightScheduler` WorkManager KEEP + exact midnight alarm; `MidnightWorker` rollover (yesterday COMPLETED else MISSED → history, new PENDING, scheduleFirst 1min).
- `DailyReminderApplication` on every start: ensureChannel + schedule midnight + rescheduleAll.
- `BootReceiver` (BOOT/REPLACE/TIMEZONE/DATE) reschedules.

Key files: di/AppModule, data/local (entities v2, DAOs +updateInterval, AppDatabase v2), data/repository/ReminderRepositoryImpl, domain/model + usecase (Create w/ interval, UpdateInterval), notification (Scheduler w/ intervalFor/scheduleNext, Builder w/ interval, Alarm/Action receivers 24/7), presentation/auth (LoginScreen, LoginViewModel), presentation/dashboard (Screen mono, Card interval, Sheet slider, Stats/Heat mono, Empty float), auth (NexoraAuthApi site remindernexora, SessionManager prefs), voice (NexoraTtsApi en-US Aria, VoiceStore files/voices, VoicePlayer MediaPlayer, VoiceViewModel), ui/theme mono, util/Constants (MIN 30 MAX 360 PRESETS format/clamp).

Do: Room source of truth, Flow UI, Hilt, Material3 tokens, exact+inexact fallback, contextual perms, device-zone LocalDate, unique PendingIntent codes.
Don't: logic in VM/Composable, singletons outside Hilt, deprecated APIs, main IO, hardcoded colors, install-time perms, analytics.

Commands: `./gradlew assembleDebug`, `assembleRelease`, `test`, `lint`, `connectedCheck`.
