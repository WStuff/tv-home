# Build TV Home and install it without disabling the television's stock launcher.
#
# Usage:
#   .\build-and-install.ps1 -TvHost 192.168.1.50
#   .\build-and-install.ps1 -TvHost 192.168.1.50 -Fresh
#
# -Fresh removes a previous TV Home installation and its local settings before installing.
param(
    [string]$TvHost = $env:TV_HOST,
    [switch]$Fresh
)

$ErrorActionPreference = 'Stop'
$OutputEncoding = New-Object System.Text.UTF8Encoding $false
$appId = 'com.tvhome.launcher'
$mainComponent = 'com.tvhome.launcher/com.gathin.tvhome.MainActivity'

if (-not $TvHost) {
    throw 'Pass -TvHost <TV IP address> or set $env:TV_HOST.'
}

Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $studioJdk = Get-ChildItem 'C:\Program Files\Android' -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Join-Path $_.FullName 'jbr' } |
        Where-Object { Test-Path (Join-Path $_ 'bin\java.exe') } |
        Select-Object -First 1
    if (-not $studioJdk) {
        throw 'Set JAVA_HOME to JDK 17 or newer. Android Studio includes one in its jbr directory.'
    }
    $env:JAVA_HOME = $studioJdk
}

$androidSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adbPath = Join-Path $androidSdk 'platform-tools\adb.exe'
if (-not (Test-Path $adbPath)) {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if (-not $adbCommand) { throw 'ADB was not found. Install Android SDK Platform Tools or set ANDROID_HOME.' }
    $adbPath = $adbCommand.Source
}

if (-not (Test-Path local.properties)) {
    "sdk.dir=$($androidSdk -replace '\\', '/')" | Out-File -Encoding ascii local.properties
}

.\gradlew.bat --no-daemon testReleaseUnitTest lintRelease assembleRelease
if ($LASTEXITCODE -ne 0) { throw 'Gradle build or verification failed.' }

$device = "$TvHost`:5555"
& $adbPath connect $device | Write-Host
if ($LASTEXITCODE -ne 0) { throw "Could not connect to $device." }

$deviceArgs = @('-s', $device)
$state = (& $adbPath @deviceArgs get-state 2>$null).Trim()
if ($state -ne 'device') { throw "ADB device $device is not ready. Accept the debugging prompt on the TV and try again." }

if ($Fresh) {
    & $adbPath @deviceArgs uninstall $appId | Write-Host
}

$apkPath = Join-Path $PSScriptRoot 'app\build\outputs\apk\release\app-release.apk'
& $adbPath @deviceArgs install -r $apkPath | Write-Host
if ($LASTEXITCODE -ne 0) {
    throw 'APK installation failed. If this build uses a different signing key, run again with -Fresh.'
}

# The system launcher stays enabled. Firmware that allows third-party Home apps accepts this command;
# other devices can use TV Home as an ordinary app and choose it in Settings if the vendor provides a picker.
& $adbPath @deviceArgs shell cmd package set-home-activity --user 0 $mainComponent | Write-Host
& $adbPath @deviceArgs shell am start -W -n $mainComponent | Write-Host
if ($LASTEXITCODE -ne 0) { throw 'TV Home was installed but could not be started.' }

$resolvedHome = (& $adbPath @deviceArgs shell cmd package resolve-activity --brief `
    -a android.intent.action.MAIN -c android.intent.category.HOME | Select-Object -Last 1).Trim()
if ($resolvedHome -like "$appId/*") {
    Write-Host "Installed. Home resolves to $resolvedHome"
} else {
    Write-Warning "Installed and started, but Home resolves to '$resolvedHome'. Choose TV Home in the television's default Home app settings."
}
