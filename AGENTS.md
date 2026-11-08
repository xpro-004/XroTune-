# AGENTS.md — XroTune (com.xrotune.music)

Android music/video client that talks directly to the `v-mate.mobi` API (no backend of ours).

## Build

```bash
export ANDROID_HOME=/opt/android-sdk ANDROID_SDK_ROOT=/opt/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:assembleRelease        # release APK (debug-signed unless keystore.properties exists)
./gradlew :app:testDebugUnitTest      # crypto/signing vectors
```

Toolchain: Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, KSP, JDK 17 target (JDK 21 runs the build).
Long builds must run in the background (`nohup ... > /tmp/build.log 2>&1 &`) because the terminal
caps foreground commands at ~1080s. There is **no `/dev/kvm`**, so emulator runs are not practical;
verify changes with compilation, unit tests and static inspection of the APK.

## Protocol rules (do not change)

- Base host `http://api.apk.v-mate.mobi/`; cleartext traffic is required.
- `sign = MD5(sorted("k=v" joined by '&') + "&PM9GikcERfy2yi6f")`, computed in
  `SignInterceptor`, which also injects the common params (`lang`, `country`, `ver_code`,
  `ver_name`, `os_type`, `ui_mode`, `channel_id`, plus `uid`/`token` when logged in).
- Responses are `{status, msg, data:"<base64>"}`; `data` is AES/CBC/NoPadding with
  key = IV = `rz18efAXUbdiaO7k`, trailing `0x00` stripped. `DecryptInterceptor` rewrites the body
  so downstream code only sees plain JSON.
- Native streams use a repeating 16-byte XOR keyed by type (1234 → `VidMateDayDay_up`,
  1235/1236 → `Vmate_GOgoGOgoGO`), applied by `XorDataSource` via the `xor://` scheme.
- Secrets live XOR-obfuscated (key `android.view`) in `core/crypto/Secrets.kt`. The Base64 there is
  hand-rolled (`B64`) so the crypto layer has no Android dependencies and stays unit-testable.

## API quirks discovered by probing the live host

- `/api/search/all` usually returns only slice descriptors (`{"slices":[{"type":"ytb",...}]}`), not
  rows. `MusicRepositoryImpl.search` therefore falls back to `/api/search/picture`.
- `/api/search/picture` ignores `type`/`ctype` and returns `ctype: "picture"` rows. That is the
  reliable source of browsable items, and the only endpoint that consistently returns content.
- `/api/entry/reco/moment` returns `{"items":[]}`; `/api/entry/reco/detail` requires both
  `item_id` and `ctype` (e.g. `ctype=picture`). The item's `ctype` is resolved from the in-memory
  index in `MusicRepositoryImpl`, so browse before opening a detail deep link.
- `/api/entry/reco/similar*` returns "unsupport type" for picture items, so `similar()` falls back
  to a keyword search on the cached title.
- `/api/cmsflow/banner/get`, `/api/dev/update/check`, `/api/nemo_*` return 404/business errors with
  the current parameters; the UI degrades gracefully (empty state).

## Architecture notes

- One app-scoped `ExoPlayer` in `PlayerController`; `PlaybackService` (`@AndroidEntryPoint`
  `MediaSessionService`) wraps it in a `MediaSession`. The service is started from
  `MiniPlayerHost` as soon as a song loads. The service never releases the shared player.
- Downloads use a **separate plain OkHttp client** — media comes from CDNs, not the API host, so
  the signing interceptor must not touch those requests.
- Downloaded (encrypted) streams are stored XOR-decrypted, so `encryptType` is reset to 0 on the
  completed `DownloadEntity` and offline playback needs no extra processing.
- Room stores playlists, downloads, history and search history; DataStore holds the session/token
  and preferences. `PrefsManager` implements `SessionProvider` (reads synchronously via
  `runBlocking` on the OkHttp thread).
- Song actions (play / add to playlist / download / favourite / share / similar) are handled by
  `SongActionsViewModel` + `SongActionHost` (which owns the playlist picker dialog and toasts).
