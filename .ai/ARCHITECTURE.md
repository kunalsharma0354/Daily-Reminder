# ARCHITECTURE

## Modules (single :app)
```
com.nexora.reminder/
  data/local/ (Room entities+DAOs+AppDatabase)
  data/repository/ (ReminderRepositoryImpl)
  domain/model/ (Reminder, DayStatus, Stats, HeatmapDay)
  domain/repository/ (interface)
  domain/usecase/ (7 use cases)
  notification/ (Helper, Scheduler, Builder, AlarmReceiver, ActionReceiver, BootReceiver, MidnightWorker/Scheduler/Receiver)
  presentation/dashboard/ (Screen, VM, Card, Stats, Heatmap, Empty, Sheet)
  di/ (AppModule, RepositoryModule)
  ui/theme/ (navy/blue/lime)
  util/ (Constants, DateTime, Permission, Haptics)
```

## Data flow
UI → VM → UseCase → Repository → (Room + Scheduler) → Flow → UI. Room single source of truth. `observeRemindersToday = combine(activeReminders, todayCompletions)`.

## Notification cycle
`create()` → PENDING + `scheduleFirst(1min)` → `AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP)` unique reqCode `id*10+offset` immutable PendingIntent → `ReminderAlarmReceiver` → `NotificationManagerCompat.notify(id)` with Done/Skip actions → chain `schedule(30min)`. Done cancels alarms+notification, writes COMPLETED+history. Skip re-schedules 30min.

Fallback: if `!canScheduleExactAlarms()` → `setAndAllowWhileIdle` inexact, UI banner.

## Midnight rollover
`MidnightScheduler.schedule()`: WorkManager 24h periodic with `setInitialDelay(delayToMidnight)` + exact midnight alarm → `MidnightReceiver` → one-time `MidnightWorker` → `HandleMidnightRollover`: yesterday COMPLETED else MISSED → History, new PENDING today, `scheduleFirst()` each. Handles missed days on boot (next run processes).

## Heatmap/stats
Stats from `history.observeAll()`: distinct dates COMPLETED vs MISSED, streak = consecutive COMPLETED ending today/yesterday. Heatmap 112 days: history map + live today completions + active check → COMPLETED/MISSED/INACTIVE/TODAY_*.

## DI
Hilt Singleton: AppDatabase, DAOs, Scheduler, Helper, Repo binding. Worker is manual (no HiltWorkerFactory) for reliability — builds own DB+repo.
