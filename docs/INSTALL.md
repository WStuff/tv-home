# Install TV Home and make it the Home screen

TV Home supports Android TV and Google TV devices running Android 8.0 or newer. It does not support Samsung Tizen, LG webOS, Roku, or Apple TV.

## Before you begin

- Download `TV-Home-v1.0.0.apk` and `TV-Home-v1.0.0.apk.sha256` from the GitHub Release.
- Install [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) on a computer.
- Keep the television's stock launcher enabled. TV Home does not require disabling or removing it.

## 1. Verify the download

Windows PowerShell:

```powershell
Get-FileHash .\TV-Home-v1.0.0.apk -Algorithm SHA256
```

macOS or Linux:

```bash
sha256sum TV-Home-v1.0.0.apk
```

Compare the result with the published `.sha256` file.

## 2. Enable ADB debugging

1. Open the TV's **Settings → Device Preferences → About** page. Names vary by manufacturer.
2. Press **Build** seven times to enable Developer options.
3. Open **Developer options** and enable **USB debugging**. If offered, also enable **Network debugging** or **Wireless debugging**.
4. Find the TV's IP address in **Settings → Network & Internet**.

## 3. Install the APK

Connect from the computer, accept the RSA prompt on the television, and install:

```text
adb connect TV_IP_ADDRESS:5555
adb devices
adb install -r TV-Home-v1.0.0.apk
```

Use the port shown by the TV if it is not `5555`. A USB connection works too; omit `adb connect` and select the device reported by `adb devices`.

## 4. Complete first-run setup

Open **TV Home** from the existing launcher. The setup contains:

1. English or Russian language selection.
2. A short introduction and remote-control guide.
3. Selection of up to four dock apps.
4. Selection of additional apps for Home.
5. Selection of one of five built-in backgrounds.
6. A final confirmation screen.

Unselected apps remain available in **All apps**. No app is uninstalled or disabled.

## 5. Make TV Home the default

Press the remote's **Home** button. If Android shows a launcher picker, choose **TV Home** and select **Always**.

If no picker appears, look for one of these paths:

- **Settings → Apps → Default apps → Home app**
- **Settings → Device Preferences → Home screen**
- **Settings → Apps → See all apps → TV Home → Open by default**

ADB fallback:

```text
adb shell cmd package set-home-activity --user 0 com.tvhome.launcher/com.gathin.tvhome.MainActivity
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

The final command should return `com.tvhome.launcher/com.gathin.tvhome.MainActivity`. Some manufacturer firmware blocks third-party default launchers; TV Home can still be opened as an ordinary TV app on those devices.

## 6. Close debugging

Turn USB/network debugging off and revoke debugging authorisations after installation. TV Home does not need ADB to run.

## Updating

Install a newer APK with `adb install -r`. Updates preserve the selected apps and settings when the APK is signed with the same release key.

## Return to the original launcher

Choose the original launcher from the TV's Home-app settings. Then, if desired:

```text
adb uninstall com.tvhome.launcher
```

This removes only TV Home and its local settings. It does not disable, enable, remove, or modify any other installed app.
