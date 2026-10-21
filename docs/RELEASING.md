# Releasing TV Home

Version 1.0.0 uses application ID `com.tvhome.launcher`. Do not change the application ID or release-signing key after publication: Android treats a different ID as another app and rejects updates signed by another key.

## Required GitHub Actions secrets

Store these under **Repository Settings → Secrets and variables → Actions**:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Keep the original `.jks` file and passwords in at least two secure offline backups. They must never be committed to Git.

The permanent TV Home release certificate has this SHA-256 fingerprint:

```text
D7:A2:71:C0:94:85:3D:83:70:6C:3A:D8:11:70:48:0C:21:C3:B8:02:E9:E5:82:74:24:53:76:A5:27:CD:0E:E4
```

## Pre-release checklist

1. Update `versionCode` and `versionName` in `launcher/app/build.gradle`.
2. Update `CHANGELOG.md`, `README.md`, and `docs/releases/vX.Y.Z.md`.
3. Run `testReleaseUnitTest`, `lintRelease`, and `assembleRelease`.
4. Test onboarding, dock navigation, **All apps**, backgrounds, and Home assignment on a real television.
5. Confirm that screenshots contain no private device or network information.
6. Run `git diff --check` and review the complete diff.
7. Confirm that no APK, keystore, password, `local.properties`, ADB key, or backup directory is staged.

## Automated signed release

The release workflow can be started in either of two ways:

- Push a semantic-version tag such as `v1.0.0`.
- Run **Release APK** manually and enter the same `vX.Y.Z` value.

The workflow validates the tag against Gradle, runs tests and Android Lint, builds with the permanent key, verifies the APK signature, writes a SHA-256 checksum, and creates a draft GitHub Release containing both assets.

Review the draft, verify the notes and assets, then publish it. GitHub automatically adds source ZIP and tarball links for the tagged commit.

## Local signed build

```powershell
cd launcher
.\gradlew.bat --no-daemon testReleaseUnitTest lintRelease assembleRelease `
  -PreleaseStoreFile=C:\secure\tv-home-release.jks `
  -PreleaseStorePassword=... `
  -PreleaseKeyAlias=tv-home-release `
  -PreleaseKeyPassword=...
```

Local builds without these properties use the Android debug key and are suitable only for testing.
