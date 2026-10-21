# Troubleshooting TV Home

## The launcher picker does not appear

Open the television's **Settings → Apps → Default apps → Home app** page and choose TV Home. If the firmware does not expose that page, try:

```text
adb shell cmd package set-home-activity --user 0 com.tvhome.launcher/com.gathin.tvhome.MainActivity
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Some manufacturer firmware refuses third-party default Home apps. TV Home remains usable as an ordinary app on those devices; do not disable the stock launcher to force the change.

## Onboarding does not appear after an update

Android preserves app data during `adb install -r`. To test first-run setup again, clear TV Home's local settings:

```text
adb shell pm clear com.tvhome.launcher
```

This removes the selected dock, Home apps, language, wallpaper, and ordering. It does not affect other applications.

## Installation reports an incompatible update

Android only accepts an update signed by the same key as the installed version. Public releases use the permanent TV Home release key; local development builds normally use Android's debug key.

If the installed copy contains no settings you need, remove it and install the release again:

```text
adb uninstall com.tvhome.launcher
adb install TV-Home-v1.0.0.apk
```

## The network pill says “Wi-Fi” instead of the network name

TV Home can show connection state without location access. Android 10 and newer may require location permission to expose the Wi-Fi network name. The generic label is expected when that optional permission is unavailable.

If you want the SSID label, grant location access from the television's app-permission screen or, while ADB debugging is enabled:

```text
adb shell pm grant com.tvhome.launcher android.permission.ACCESS_FINE_LOCATION
```

## An installed app is missing

TV Home lists activities that declare Android TV/Leanback or ordinary launcher support. Open **All apps** again after installing the application or restart TV Home. Phone-only APKs without a launchable TV activity may not appear or may not work with a remote.

## A remote button does not work

Directional, OK, Back, and Home buttons should work without special access. A few vendor remotes route Input or branded app buttons through their stock launcher. TV Home includes optional accessibility helpers for those devices, but they are off by default and should only be enabled from the television's Accessibility settings when needed.

When reporting a navigation problem, include the exact TV model, Android version, TV Home version, screen, focused control, and remote-button sequence.

## Return to the stock launcher

Choose the original launcher in the TV's default Home-app settings. If desired, then remove TV Home:

```text
adb uninstall com.tvhome.launcher
```

The public installer never disables or removes the stock launcher.

## Reporting diagnostics safely

Use the repository's bug form and redact IP addresses, Wi-Fi names, accounts, Android IDs, serial numbers, and ADB keys. Keep security-sensitive reports private according to [SECURITY.md](../SECURITY.md).
