# Security policy

## Supported versions

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |
| Earlier development builds | No |

## Reporting a vulnerability

Do not open a public issue for a security-sensitive report. Use GitHub's **Security → Report a vulnerability** feature, or contact the repository owner privately. Include the affected version, TV model, Android version, reproduction steps, and any mitigation you found. Do not include account credentials, ADB private keys, or unrelated device data.

## Release-signing policy

- The Android release keystore and passwords must never be committed, attached to an issue, or placed in an Actions log.
- GitHub releases are signed by the permanent key stored in encrypted Actions secrets.
- APK signatures and SHA-256 checksums are verified before assets are published.
- A leaked signing key must be treated as compromised immediately.

## Data and permissions

TV Home has no advertising or analytics SDKs and does not request the `INTERNET` permission. It reads the installed launchable-app catalogue to render Home and **All apps**, and reads network state only for the status pill. See [docs/PRIVACY.md](docs/PRIVACY.md).
