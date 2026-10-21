# Contributing to TV Home

Thanks for helping make TV Home better. Small, focused pull requests are easiest to review.

## Before opening an issue

- Search existing issues first.
- For usage questions, read [SUPPORT.md](SUPPORT.md) and [docs/INSTALL.md](docs/INSTALL.md).
- Never post IP addresses, Android IDs, Wi-Fi names, ADB keys, account information, or full device dumps.
- Report security problems privately according to [SECURITY.md](SECURITY.md).

## Development setup

Install JDK 17, Android SDK Platform 34, and Build Tools 34.0.0. Then run:

```powershell
cd launcher
.\gradlew.bat --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

The installable test APK is created at `launcher/app/build/outputs/apk/release/app-release.apk`.

## Product rules

- Keep the launcher offline, ad-free, and free of analytics.
- Do not add personal package names, hidden-app lists, accounts, device IDs, or preselected services.
- Keep D-pad navigation deterministic and usable without touch input.
- Preserve Android 8.0 / API 26 compatibility.
- Avoid heavy dependencies and background work; many TVs have slow CPUs and 1 GB of RAM.
- New user-facing text must include English and Russian resources.
- Screenshots must not expose private device or network information.

## Pull requests

Include:

- A concise explanation of the change.
- The TV model and Android version used for testing, when applicable.
- Reproduction steps for bug fixes.
- Before/after screenshots for visible UI changes.
- Confirmation that unit tests, Android Lint, and a release build pass.

By contributing, you agree that your contribution is released under the repository's MIT License.
