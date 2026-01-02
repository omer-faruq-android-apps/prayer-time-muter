# Prayer Time Muter

This app automatically silences your phone around prayer times using https://prayertimes.api.abdus.dev/. You can pick country/city, set per-prayer before/after offsets, give Friday noon its own offsets, and choose silent vs silent+vibrate. See below for permissions and battery notes.

## API
- Search (`search`), list (`locations`), and fetch prayer times by `location_id`
- Diyanet-compatible times: https://prayertimes.api.abdus.dev/

## Key Features
- Location selection (country/city/district)
- Per-prayer before/after silence durations
- Separate offsets for Friday noon
- Silent mode choice: pure silent or silent+vibration
- Automatic permission checks and alarm scheduling

## Permissions / Notes
- DND (Notification Policy) access is required to silence/restore; requested when starting the service.
- Exact alarm + WAKE_LOCK are declared for timely alarms.
- Network permissions (INTERNET, ACCESS_NETWORK_STATE).
- Android 13+: POST_NOTIFICATIONS is requested at runtime.
- For OEM battery killers: https://dontkillmyapp.com/
- F-Droid friendly: no proprietary SDKs; AGPL-3.0 license.

## Build (Docker)
```bash
docker run --rm -v "${PWD}:/app" prayer-time-muter
```

### One-time helper commands
Generate Gradle wrapper with jdk17 and build the image:
```bash
docker run --rm -v "${PWD}:/work" -w /work gradle:8.2-jdk17 gradle wrapper --gradle-version 8.2
docker build -t prayer-time-muter .
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

## License
AGPL-3.0. See [LICENSE](LICENSE).
