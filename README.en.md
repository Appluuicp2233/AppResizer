# Screen Density Adjuster · AppResizer

[中文](README.md) · [English](README.en.md) · [日本語](README.ja.md)

> Notice: Part of this project was completed by AI. The introduction and changelog may contain errors, please verify carefully. The application was also developed with AI assistance.

> A system-level display adjustment tool running on the Xposed / EdXposed / LSPosed framework. It sets the display density (DPI) and resolution **independently for each app** without changing the global system parameters.

| Item | Value |
| --- | --- |
| Package | `com.appresizer` |
| Latest version | 2.0.5 |
| Author | Appluuicp2233 |
| Owner | AllToolBox |
| License | MIT License |
| minSdk / targetSdk | 24 (Android 7.0) / 25 |

---

## 1. Overview

Screen Density Adjuster is an Xposed module. It injects the configuration into the process of the target app you select, overriding only that app's DPI and resolution. The system itself and other apps are unaffected.

- Configure DPI and resolution per app, without interference between apps.
- Five injection methods, switchable per app.
- Two app-list retrieval modes: system interface and root interface.
- Built-in global crash protection; the error log can be copied or exported for feedback.
- The UI supports Simplified Chinese, English, and Japanese, switchable in Settings.

## 2. Features

### 2.1 Per-app display adjustment

| Feature | Description |
| --- | --- |
| DPI adjustment | Set a separate display density for any app (range 72 – 960) |
| Resolution override | Set a separate width × height for the target app (range 320 – 4320) |
| Injection method | Configuration / Metrics / Resources / UI / Try all five at once |
| Process termination | Force stop / Normal stop / Do not terminate |
| System apps | Toggle whether built-in system apps are shown in the list |
| Search | Filter in real time by app name or package name |

### 2.2 Injection methods

| Value | Name | Description |
| --- | --- | --- |
| 0 | Configuration injection | Rewrites the target app's `Configuration` and display metrics; best compatibility (default) |
| 1 | Metrics injection | Rewrites only display metrics; suits most UI apps |
| 2 | Resources management | Applied through the system resource manager; suits multi-process apps |
| 3 | UI injection | Rewrites resources per activity; suits late injection scenarios |
| 4 | Try all five at once | Tries the methods in sequence to improve the chance of taking effect |

### 2.3 App-list retrieval modes

| Mode | Description |
| --- | --- |
| System interface | Retrieves the app list through the system `PackageManager` (default, no root required) |
| Root interface | Retrieves all apps through `su -c pm list packages -u`, including hidden / protected apps; falls back to the system interface when root is unavailable |

## 3. Requirements

1. Android 7.0 (API 24) or later.
2. Xposed / EdXposed / LSPosed framework installed.
3. Enable this module in the framework and restart the device for it to take effect.
4. Root access (to make the configuration file globally readable, terminate the target app process, and enumerate apps in root mode).

## 4. Installation and usage

1. Install the APK, enable this app in the module list of the framework manager, and restart the device.
2. First launch: read and agree to the user agreement → browse the wizard (Welcome / Permissions / Security / Usage) → tap Enter.
3. On first entry to the main screen, choose in order: injection method (default Configuration injection) → app-list retrieval mode.
4. Tap a target app in the list to open its details page and set DPI and resolution.
5. Tap Save in the top right; the target app process is terminated, and the new configuration takes effect the next time the app opens.
6. From the second launch onward, the app goes straight to the main screen and no longer shows the wizard.

> If an injection method does not work for a particular app, switch to another method on that app's details page.

## 5. Build

### 5.1 Environment

| Item | Version / Requirement |
| --- | --- |
| Gradle | 7.0.2 (`gradlew` included) |
| Android Gradle Plugin | 7.0.4 |
| JDK | 11 |
| compileSdkVersion | 25 |
| Build-Tools | 30.0.3 |

### 5.2 Commands

```bash
# Build unsigned release
./gradlew assembleRelease

# Build debug
./gradlew assembleDebug
```

The Xposed API is included with `compileOnly`:

```gradle
compileOnly 'de.robv.android.xposed:api:82'
```

If it cannot be resolved from the network, place `api-82.jar` in `app/libs/` and change it to:

```gradle
compileOnly files('libs/api-82.jar')
```

### 5.3 Signing

All historical versions use the same signing key. Certificate SHA-256:

```
9d629fe2503e3678916373d9f87ec36d55cf935164f963c78d9cec6265011d4d
```

It can be installed over previous versions; installation will not fail due to signature mismatch.

## 6. Project structure

```
AppResizer/
├── build.gradle / settings.gradle / gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/
├── LICENSE
├── README.md / README.en.md / README.ja.md
├── CHANGELOG.md
├── icon/                         Store icons
└── app/
    ├── build.gradle / proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/xposed_init
        ├── java/com/appresizer/
        │   ├── AppResizerApp.java        Application (200 DPI + locale)
        │   ├── BaseActivity.java          Base activity
        │   ├── LocaleUtils.java           Locale / density context helper
        │   ├── MainActivity.java          Main screen
        │   ├── WizardActivity.java        Wizard / first-launch agreement
        │   ├── AppSettingsActivity.java   App details
        │   ├── SettingsActivity.java      Settings (with language switch)
        │   ├── SearchActivity.java        Search
        │   ├── AppListAdapter.java / AppInfo.java
        │   ├── DialogUtils.java           Shared dialogs
        │   ├── ConfigManager.java         Configuration storage
        │   ├── EnvChecker.java / ShellUtils.java
        │   ├── CrashHandler.java / CrashActivity.java  Global crash protection
        │   └── XposedHook.java             Injection entry
        └── res/
            ├── values/                    Simplified Chinese (default)
            ├── values-en/                 English
            ├── values-ja/                 Japanese
            ├── layout / menu / drawable / mipmap-*
```

## 7. How the configuration is read by Xposed (Android 7+ note)

Since Android 7.0, `MODE_WORLD_READABLE` is disabled, and Xposed's `XSharedPreferences` cannot read the app's private SharedPreferences. Therefore, after each save, the app runs via root:

```sh
chmod 755 <dataDir>
chmod 755 <dataDir>/shared_prefs
chmod 644 <dataDir>/shared_prefs/appresizer_config.xml
```

If the configuration does not take effect, filter `AppResizer` in the Xposed log:

- `AppResizer apply <package> dpi=...`: configuration read successfully, injection executed.
- `AppResizer prefs unreadable`: permissions were not changed successfully; check root authorization.

## 8. Privacy and compliance

- The app requests no internet permission and does not collect, upload, or share personal information.
- All configurations are stored only in local SharedPreferences and are cleared on uninstall.
- Root access is used only when you actively save configuration, terminate a process, or enumerate apps in root mode.
- This tool adjusts the display parameters of installed apps on your device. Do not use it to evade platform risk control, tamper with others' devices, or for any illegal purpose.

## 9. License

This project is open sourced under the **MIT License**. See [LICENSE](LICENSE). The change history is in [CHANGELOG.md](CHANGELOG.md).
