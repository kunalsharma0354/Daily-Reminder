# Daily Reminder

Daily Reminder helps you finish tasks, not just remember them. Create habits like Workout or Drink Water, get nudges every 30 min to 6 hr, once daily, repeat or fixed-time alarms with optional voice alerts. Track streaks, history and a 16-week heatmap. Reliable reminders with midnight rollover and reboot recovery. Stay consistent every day.

## Demo login (public)

The app uses Nexora auth.

- Username: `NEXORA`
- Password: `FREE`

## Features

- **Reminders** — task name + optional notification title, first nudge in 1 minute
- **Three modes**
  - Once a day — repeats at your interval until marked done, counts in stats
  - Repeat — rings every interval (e.g. every 30 min), no completion needed
  - Daily time — one alarm every day at a fixed time, with snooze
- **Custom interval** — 30 minutes to 6 hours per reminder (presets + slider)
- **Voice alerts (Nexora TTS)** — optional English voice message per reminder, spoken with each notification; locked intro "Hello! Nexora Voice Assistance Speaking:"
- **Dashboard** — live task cards, completion states, delete, interval quick-change
- **Progress** — completed days, missed days, current streak + 16-week activity heatmap
- **Reliability** — exact alarms with inexact fallback, midnight rollover, reboot recovery, works when the app is closed
- **Theme** — modern monochrome black and white, dark and light mode, smooth animations

## Tech stack

Kotlin, Jetpack Compose (Material 3), MVVM + Clean Architecture, Hilt, Room, WorkManager + AlarmManager, NotificationCompat, Coroutines + Flow, Gradle version catalogs. Auth and TTS via Nexora API.

## Setup

1. Install Android Studio (Ladybug or newer) with JDK 17.
2. Clone the repo:
   ```bash
   git clone https://github.com/kunalsharma0354/Daily-Reminder.git
   ```
3. Open the project and let Gradle sync.
4. Run on an Android 8.0+ device or emulator (minSdk 26, targetSdk 34).
5. Sign in with the demo account above and grant notification + exact-alarm permissions when asked.

## Build commands

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release APK
./gradlew test               # unit tests
./gradlew lint               # lint
./gradlew connectedCheck     # instrumented tests
```

## Project structure

```
com.nexora.reminder/
├── auth/               # Nexora login API + session
├── voice/              # Nexora TTS API + playback + file store
├── data/local/         # Room entities, DAOs, database
├── data/repository/    # Repository implementation
├── domain/             # Models, repository interface, use cases
├── notification/       # Scheduler, channels, receivers, midnight worker
├── presentation/auth/  # Login screen
├── presentation/dashboard/  # Dashboard, cards, stats, heatmap, sheets
├── di/                 # Hilt modules
├── ui/theme/           # Monochrome theme
└── util/               # Constants, date/time, permissions
```

## Permissions

- Notifications (Android 13+) — requested on first launch, settings shortcut in-app
- Exact alarms (Android 12+) — falls back to inexact scheduling if denied
- Internet — required for login and voice conversion

## License

Private project. All rights reserved.
