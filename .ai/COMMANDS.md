# COMMANDS

```bash
./gradlew assembleDebug      # debug APK
./gradlew assembleRelease    # release (minify off for dev)
./gradlew test               # unit tests (CreateReminder, DateTime)
./gradlew lint               # Android lint
./gradlew connectedCheck     # instrumented + Compose UI tests
./gradlew --refresh-dependencies  # after catalog change
```
JDK 17 required. Min 26, Target/Compile 34.
