#!/usr/bin/env bash
# Build TV Home and install it without disabling the television's stock launcher.
#
# Usage:
#   TV_HOST=192.168.1.50 ./build-and-install.sh
#   TV_HOST=192.168.1.50 FRESH=1 ./build-and-install.sh
set -euo pipefail

cd "$(dirname "$0")"
: "${TV_HOST:?Set TV_HOST to the TV IP address, for example TV_HOST=192.168.1.50 $0}"

APP_ID=com.tvhome.launcher
MAIN_COMPONENT=com.tvhome.launcher/com.gathin.tvhome.MainActivity
DEVICE="${TV_HOST}:5555"

if [ -z "${JAVA_HOME:-}" ]; then
  if [ -x /usr/libexec/java_home ]; then
    JAVA_HOME=$(/usr/libexec/java_home -v 17)
  elif [ -d /opt/homebrew/opt/openjdk@17 ]; then
    JAVA_HOME=/opt/homebrew/opt/openjdk@17
  else
    JAVA_HOME=$(java -XshowSettings:properties -version 2>&1 | awk '/java.home/ {print $3; exit}')
  fi
  export JAVA_HOME
fi

if [ -z "${ANDROID_HOME:-}" ]; then
  for candidate in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" /opt/homebrew/share/android-commandlinetools; do
    if [ -d "$candidate" ]; then ANDROID_HOME=$candidate; break; fi
  done
  export ANDROID_HOME
fi

ADB="${ANDROID_HOME:-}/platform-tools/adb"
if [ ! -x "$ADB" ]; then ADB=$(command -v adb || true); fi
[ -n "$ADB" ] || { echo "ADB was not found. Install Android SDK Platform Tools or set ANDROID_HOME." >&2; exit 1; }

if [ ! -f local.properties ]; then
  [ -n "${ANDROID_HOME:-}" ] || { echo "Set ANDROID_HOME before building." >&2; exit 1; }
  printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
fi

./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease

"$ADB" connect "$DEVICE"
[ "$("$ADB" -s "$DEVICE" get-state 2>/dev/null)" = device ] || {
  echo "ADB device $DEVICE is not ready. Accept the debugging prompt on the TV and try again." >&2
  exit 1
}

if [ "${FRESH:-0}" = 1 ]; then
  "$ADB" -s "$DEVICE" uninstall "$APP_ID" || true
fi

APK=app/build/outputs/apk/release/app-release.apk
"$ADB" -s "$DEVICE" install -r "$APK" || {
  echo "Installation failed. If this build uses a different signing key, run again with FRESH=1." >&2
  exit 1
}

# The system launcher stays enabled. If the vendor blocks third-party defaults, the app remains usable
# from the Apps screen and can be selected through the television's own Home app settings when offered.
"$ADB" -s "$DEVICE" shell cmd package set-home-activity --user 0 "$MAIN_COMPONENT" || true
"$ADB" -s "$DEVICE" shell am start -W -n "$MAIN_COMPONENT"

RESOLVED_HOME=$("$ADB" -s "$DEVICE" shell cmd package resolve-activity --brief \
  -a android.intent.action.MAIN -c android.intent.category.HOME | tail -n 1 | tr -d '\r')
case "$RESOLVED_HOME" in
  "$APP_ID"/*) echo "Installed. Home resolves to $RESOLVED_HOME" ;;
  *) echo "Installed and started, but Home resolves to '$RESOLVED_HOME'. Choose TV Home in the television's default Home app settings." >&2 ;;
esac
