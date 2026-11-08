# XroTune — Android Music App

XroTune is a fully offline-first, guest-friendly music and video browsing app for Android.
It talks **directly** to the `v-mate.mobi` API over HTTP (no backend of our own), reproduces the
request signing and response encryption of the original client, and plays streams through
Media3/ExoPlayer with background playback.

Everything works without an account. Logging in with a phone number is optional and only unlocks
cloud/profile features.

---

## 1. Feature overview

| Screen | What it does |
| --- | --- |
| **Splash** | Branded launch screen, then routes to Home |
| **Home** | Recommendation banner + trending row, pull-to-refresh |
| **Search** | Debounced real-time search, search history |
| **Song detail** | Artwork, metadata, stats, similar items, actions |
| **Full player** | Artwork, seekbar, play/pause, next/previous, shuffle, repeat |
| **Mini player** | Persistent bar above the bottom navigation on every screen |
| **Playlists** | Create / rename / delete, add & remove songs |
| **Library / Offline** | Favourites, history, downloaded songs |
| **Download manager** | Live progress, pause/cancel, delete, clear all |
| **Profile** | Guest mode + logged-in state, logout |
| **Login** | Phone number + SMS code (optional) |
| **Settings** | Wi-Fi-only downloads, quality, autoplay, history cleanup |

Bottom navigation: **Home · Search · Library · Profile**.

### Guest mode
The app is 100% usable without logging in: search, playback, local playlists, favourites,
history and downloads all work. The Profile screen shows a **Login** button while guest and
switches to user info + **Logout** once signed in. The local database is kept when you log in, so
nothing you created as a guest is lost.

---

## 2. Architecture

MVVM + Clean Architecture, wired with Hilt.

```
core/
  crypto/      Secrets (XOR-obfuscated), Signer, AesDecryptor, XorCrypto, Base64
  network/     SessionProvider / SessionState
  common/      Result + AppError, UiState, DeviceInfo, formatters
data/
  remote/      ApiService (Retrofit), DTOs + mappers, Sign/Decrypt interceptors
  local/       Room (entities, DAOs, database), DataStore preferences
  player/      PlayerController, XorDataSource, XorAwareDataSourceFactory, PlaybackService
  repository/  Music / Library / Download / User implementations
domain/
  model/       Song, Banner, AppEntry, User
  repository/  Repository interfaces
di/            Hilt modules
ui/
  navigation/  Routes + NavHost
  screens/     splash, home, search, detail, player, playlist, library,
               downloads, profile, login, settings
  components/  SongRow, SongItem, MiniPlayer, bottom bar, song action sheet/host
  viewmodel/   One view model per screen + PlayerViewModel, SongActionsViewModel
  theme/       Material 3 dark theme
```

Playback is owned by a single app-scoped `PlayerController` (one `ExoPlayer` instance).
`PlaybackService` (a `MediaSessionService`) wraps that player in a `MediaSession` for background
playback, the MediaStyle notification and lock-screen / Bluetooth controls. The service is started
as soon as a song is loaded.

---

## 3. Protocol implementation

### Base configuration
```
Base host : http://api.apk.v-mate.mobi/
Sign salt : &PM9GikcERfy2yi6f
AES key   : rz18efAXUbdiaO7k   (also used as the IV)
XOR key 1 : VidMateDayDay_up   (stream type 1234)
XOR key 2 : Vmate_GOgoGOgoGO   (stream types 1235 / 1236)
Obfuscation key : android.view
```
Secrets are stored XOR-obfuscated with `android.view` and Base64-encoded, so the plain strings
never appear in the binary. They are decoded lazily at runtime (`core/crypto/Secrets.kt`).

### Request signing (`SignInterceptor`)
Every request gets the common parameters plus:
```
paramString = sorted("key=value") joined by "&"
sign        = MD5(paramString + "&PM9GikcERfy2yi6f")
```
The interceptor merges existing query + form parameters, appends the session parameters
(`lang`, `country`, `ver_code`, `ver_name`, `os_type`, `ui_mode`, `channel_id`, and
`uid`/`token` when logged in), rebuilds the URL so the signed set matches exactly what is sent,
and finally appends `sign`.

### Response decryption (`DecryptInterceptor`)
The backend returns:
```json
{ "status": 1, "msg": "", "data": "<base64>" }
```
`data` is decrypted with **AES/CBC/NoPadding**, key = IV = `rz18efAXUbdiaO7k`; trailing `0x00`
bytes are stripped and the result is parsed back into JSON, so callers only ever see plain JSON.
If a response is not an encrypted envelope it is passed through untouched.

### Stream decryption (`XorDataSource`)
Native media streams are scrambled with a repeating 16-byte XOR. Items carrying an `encrypt_type`
are wrapped in the custom `xor://<type>/<url>` scheme; `XorAwareDataSourceFactory` recognises the
scheme, picks the key for the type, and `XorDataSource` de-scrambles on the fly. Because ExoPlayer
can seek, the absolute byte offset drives the key index so seeking stays correct.

### Endpoints used
Search (`/api/search/all`, `/api/search/picture`, `/api/search/status`), entry/recommendation
(`/api/entry/reco/*`, `/api/entry/action/{action}`), CMS (`/api/cmsflow/*`), user/login
(`/api/user/*`, `/user/update`), misc/movie/BT, prism, nemo activity, device/update, appstore,
parser/`/v3/m3u8_videourl`, and `/auth/token/refresh`. See `data/remote/ApiService.kt`.

The `/api/search/all` endpoint returns slice descriptors rather than rows for some queries, so the
repository transparently falls back to `/api/search/picture`. Detail lookups resolve the item's
`ctype` from an in-memory index built while browsing.

---

## 4. Tech stack

- 100% Kotlin, minSdk 24, targetSdk/compileSdk 34
- Jetpack Compose + Material 3 (dark theme by default)
- Hilt (dependency injection)
- Retrofit + OkHttp + Gson
- Room (playlists, downloads, history, search history)
- DataStore (session/token + preferences)
- Media3 / ExoPlayer + MediaSessionService
- Coil (image loading), WorkManager, Kotlin Coroutines/Flow
- `usesCleartextTraffic="true"` and a network security config (plain-HTTP API host)

---

## 5. Building

Requirements: JDK 17+, Android SDK with platform 34 and build-tools 34.

```bash
# Debug APK
./gradlew :app:assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# Release APK (signed with the debug key unless keystore.properties is present)
./gradlew :app:assembleRelease
# -> app/build/outputs/apk/release/app-release.apk

# Unit tests (crypto/signing vectors)
./gradlew :app:testDebugUnitTest
```

Install on a device/emulator:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Release signing
Create `keystore.properties` in the project root to sign release builds with your own key:
```properties
storeFile=/absolute/path/to/release.jks
storePassword=…
keyAlias=…
keyPassword=…
```
Without it, the release build falls back to the debug signing config.

---

## 6. Notes

- The API is reached over plain HTTP, hence cleartext traffic is explicitly allowed.
- Media files are served from CDNs, not the API host, so downloads use a separate plain HTTP
  client without the API signature.
- Encrypted native streams are stored XOR-decrypted on download, so offline playback needs no
  extra processing.
