# GitHub publishing checklist

Use this checklist after reviewing the prepared repository locally. The current Git remote may still point to the upstream project, so confirm the destination before pushing.

## 1. Create or choose the repository

Suggested repository name: `tv-home`

Suggested description:

> An ad-free, offline, remote-first launcher for Android TV and Google TV.

Suggested topics:

`android-tv`, `google-tv`, `launcher`, `leanback`, `tv-launcher`, `java`, `offline`, `open-source`

If publishing to a new repository, update the remote only after checking the URL:

```text
git remote -v
git remote set-url origin https://github.com/YOUR_ACCOUNT/tv-home.git
git push -u origin main
```

Do not upload the release keystore, password file, ADB keys, `local.properties`, `dist/`, or device backups.

## 2. Configure the repository

- Upload `docs/social-preview.png` under **Settings → General → Social preview**.
- Enable Issues and keep the supplied bug and feature forms.
- Under **Settings → Code security and analysis**, enable private vulnerability reporting, Dependabot alerts, and secret scanning when available.
- Add the four Actions secrets listed in [RELEASING.md](RELEASING.md).
- Protect `main` if more than one person will maintain the project.

## 3. Review the first commit

Before committing:

```text
git diff --check
git status --short
git diff --stat
```

Confirm that the screenshots, README, license, notice, privacy statement, installation guide, support policy, security policy, issue forms, CI, and release workflow are included.

## 4. Create version 1.0.0

After the prepared commit is on `main`:

```text
git tag -a v1.0.0 -m "TV Home 1.0.0"
git push origin v1.0.0
```

The **Release APK** workflow validates version 1.0.0, runs unit tests and Android Lint, builds with the permanent signing key, verifies the signature, and creates a draft GitHub Release containing:

- `TV-Home-v1.0.0.apk`
- `TV-Home-v1.0.0.apk.sha256`

Review the draft against [the prepared release notes](releases/v1.0.0.md), download both assets once, verify the checksum, and then publish the release.

## 5. Final public check

- Open every README link from GitHub.
- Confirm all four screenshots render at full width.
- Install the APK downloaded from the draft release on a TV and complete onboarding.
- Confirm the Home button can select TV Home without disabling the stock launcher.
- Keep the permanent signing key and passwords in two secure offline backups.

