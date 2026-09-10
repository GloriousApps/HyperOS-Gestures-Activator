# HyperOS Gestures Activator

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_launcher_art.png" alt="HyperOS Gestures Activator" width="220">
</p>

<p align="center">
  Use HyperOS 3 native full-screen gestures with third-party launchers.<br>
  Keep Back, Home, Recents and quick app switching on Xiaomi's gesture engine.
</p>

<p align="center">
  <a href="README.md">🇹🇷 Türkçe</a> · <strong>🇬🇧 English</strong>
</p>

<p align="center">
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/GloriousTR/HyperOS-Gestures-Activator?display_name=tag&style=for-the-badge&color=7357e6"></a>
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/actions/workflows/android.yml"><img alt="Android build" src="https://img.shields.io/github/actions/workflow/status/GloriousTR/HyperOS-Gestures-Activator/android.yml?branch=main&style=for-the-badge&label=Android"></a>
  <img alt="Android 15+" src="https://img.shields.io/badge/Android-15%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white">
</p>

<p align="center">
  <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0"><strong>Download v1.2.0 APK</strong></a>
  · <a href="#compatibility">Compatibility</a>
  · <a href="https://github.com/GloriousTR/HyperOS-Gestures-Activator/issues">Report an issue</a>
  · <a href="https://t.me/glorioustr">Telegram</a>
</p>

> [!IMPORTANT]
> The app requires root, Vector/LSPosed with modern libxposed API 102, and a
> one-time `WRITE_SECURE_SETTINGS` grant. It hooks system components, so use it
> only on compatible HyperOS devices with a recovery path available.

## What it does

- **Full-screen gestures:** keeps edge Back, swipe-up Home and hold-for-Recents available with third-party launchers.
- **Quick app switching:** switches between the two most recent eligible apps by swiping horizontally across the bottom gesture area.
- **Xiaomi gesture engine:** uses SystemUI, WM Shell and Xiaomi's native gesture paths instead of fake touches or `KEYCODE_HOME`.
- **System Health:** shows the root manager, Vector/LSPosed, SystemUI, Xiaomi Launcher engine and required permission state.
- **Live Diagnostics:** records success, failure and information events with filtering, system snapshots and UTF-8 report export.
- **Safe disable:** remembers and restores the previous navigation setting.
- **Customizable interface:** offers Default or Aero Glass styling with System default, AMOLED, Light Mode and Dark Mode palettes.
- **Localization:** supports 21 Android system languages, including English and Turkish.

## Gestures

| Gesture | Result |
| --- | --- |
| Swipe inward from the left or right edge | Back |
| Swipe up quickly from the bottom edge | Default Home launcher |
| Swipe up and hold | Recents |
| Swipe left or right across the bottom gesture area | Quick switch to the previous app |

## Compatibility

| Component | Status |
| --- | --- |
| HyperOS 3 / Android 16 | Supported and tested target |
| Xiaomi/POCO Global Launcher (`com.mi.android.globallauncher`) | Supported gesture engine |
| Xiaomi China Launcher (`com.miui.home`) | Supported gesture engine |
| Smart Launcher | Verified on a physical device |
| Other third-party launchers | Supported by design with the standard Android HOME intent; ROM and device testing is required |
| Vector/LSPosed | Modern libxposed API 102 required |
| Minimum Android version | Android 15 / API 35 |

The v1.0.0 validation was performed on Xiaomi `2511FPC34G` with Xiaomi/POCO
Launcher `RELEASE-6.01.05.2407-06081949`. Back, Home, Recents and two-way quick
switching worked without a launcher-process crash.

Xiaomi Launcher's icon-closing Home animation cannot be reproduced exactly because
a third-party launcher does not provide icon coordinates to SystemUI. Task-surface
and quick-switch completion animations are supplied by the firmware. To avoid
freezes, excessive repeats within 650 ms are safely ignored and logged.

## Install

1. Download and install `HyperOS-Gestures-Activator-v1.2.0.apk` from the [v1.2.0 release](https://github.com/GloriousTR/HyperOS-Gestures-Activator/releases/tag/v1.2.0).
2. Grant this permission once through ADB:

   ```powershell
   adb shell pm grant dev.glorioustr.hyperosgesturesactivator android.permission.WRITE_SECURE_SETTINGS
   ```

3. Enable the module in Vector/LSPosed. Include SystemUI and the official Xiaomi Launcher package installed on the device in its scope:

   - System UI — `com.android.systemui`
   - Xiaomi/POCO Global Launcher — `com.mi.android.globallauncher`
   - Xiaomi China Launcher — `com.miui.home`

4. Reboot the device.
5. Verify the required components on the **System Health** page.
6. Enable the feature with the **Gesture navigation** capsule.

> [!NOTE]
> For a debug APK, use `dev.glorioustr.hyperosgesturesactivator.debug` as the package name.

## Live Diagnostics

Available from the three-line top menu, Live Diagnostics records SystemUI and
Xiaomi Launcher hook readiness, HOME and navigation state, gesture results, target
task details, process and thread sources, errors and stack traces.

The screen displays the latest 1,000 events. An exported report contains every
event stored in the local device-protected SQLite database. Records are removed
only after the user confirms **Clear**. For support, share the report with
[@glorioustr](https://t.me/glorioustr).

## Build

Requirements: JDK 17 and Android SDK API 36.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

The APK must contain `META-INF/xposed/java_init.list`, `module.prop` and
`scope.list`. The repository uses one GitHub Actions workflow: `main` and pull
requests trigger a verification build, while a `v*` tag creates the signed APK,
SHA-256 checksum and GitHub Release.

See the [HyperOS 3 investigation notes](docs/hyperos3-investigation.md) for the
technical device research and test matrix.

## Security and responsible use

- Missing hooks fail gracefully and are recorded in Live Diagnostics.
- The user's independent MiuiBackGestureHook installation and settings are not modified.
- Diagnostic broadcasts are accepted only from expected system and Xiaomi Launcher processes.
- Disabling the feature restores the previous navigation value.

This independent project is not affiliated with or endorsed by Xiaomi. HyperOS,
MIUI and Xiaomi are trademarks of their respective owners.

## License

[Apache License 2.0](LICENSE). The initial LSPosed integration approach studied
[MiuiBackGestureHook 0.4.0](https://github.com/wxxsfxyzm/MiuiBackGestureHook/tree/0.4.0);
attributions are listed in [NOTICE](NOTICE).

<p align="center">
  Built for HyperOS users by <a href="https://github.com/GloriousTR">GloriousTR</a>.
</p>
