param(
    [string]$OutputPath = (Join-Path $PSScriptRoot '..\docs\social-preview.png')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$screenshotPath = Join-Path $root 'docs\screenshots\home.png'
$fontPath = Join-Path $root 'launcher\app\src\main\res\font\outfit.ttf'

function New-RoundedPath([float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $radius * 2
    $path.AddArc($x, $y, $diameter, $diameter, 180, 90)
    $path.AddArc($x + $width - $diameter, $y, $diameter, $diameter, 270, 90)
    $path.AddArc($x + $width - $diameter, $y + $height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($x, $y + $height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

$canvas = New-Object System.Drawing.Bitmap 1280, 640
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

$background = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.Point 0, 0),
    (New-Object System.Drawing.Point 1280, 640),
    ([System.Drawing.Color]::FromArgb(255, 18, 13, 20)),
    ([System.Drawing.Color]::FromArgb(255, 65, 25, 21))
)
$graphics.FillRectangle($background, 0, 0, 1280, 640)

$glowPath = New-RoundedPath 555 68 685 504 30
$glowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(38, 255, 159, 108))
$graphics.FillPath($glowBrush, $glowPath)

$screenPath = New-RoundedPath 580 93 635 454 24
$graphics.SetClip($screenPath)
$screenshot = [System.Drawing.Image]::FromFile($screenshotPath)
$graphics.DrawImage($screenshot, (New-Object System.Drawing.Rectangle 580, 93, 635, 454))
$graphics.ResetClip()
$screenPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(115, 255, 255, 255)), 2
$graphics.DrawPath($screenPen, $screenPath)

$fontCollection = New-Object System.Drawing.Text.PrivateFontCollection
$fontCollection.AddFontFile($fontPath)
$family = $fontCollection.Families[0]
$brandFont = New-Object System.Drawing.Font $family, 72, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$taglineFont = New-Object System.Drawing.Font $family, 34, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$smallFont = New-Object System.Drawing.Font $family, 19, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$white = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$muted = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(220, 226, 217, 224))
$accent = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 244, 171, 133))

$graphics.DrawString('TV Home', $brandFont, $white, 62, 92)
$graphics.FillRectangle($accent, 65, 190, 78, 5)
$graphics.DrawString("A calmer Home screen`nfor Android TV", $taglineFont, $muted, 62, 230)

$pillPath = New-RoundedPath 62 426 442 58 29
$pillBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(35, 255, 255, 255))
$pillPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(65, 255, 255, 255)), 1
$graphics.FillPath($pillBrush, $pillPath)
$graphics.DrawPath($pillPen, $pillPath)
$graphics.DrawString('AD-FREE   •   OFFLINE   •   REMOTE-FIRST', $smallFont, $white, 88, 443)
$graphics.DrawString('VERSION 1.0.0', $smallFont, $muted, 65, 535)

$outputDirectory = Split-Path -Parent $OutputPath
if (-not (Test-Path $outputDirectory)) { New-Item -ItemType Directory -Path $outputDirectory | Out-Null }
$canvas.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$screenshot.Dispose()
$screenPath.Dispose()
$glowPath.Dispose()
$pillPath.Dispose()
$screenPen.Dispose()
$glowBrush.Dispose()
$pillBrush.Dispose()
$pillPen.Dispose()
$brandFont.Dispose()
$taglineFont.Dispose()
$smallFont.Dispose()
$white.Dispose()
$muted.Dispose()
$accent.Dispose()
$fontCollection.Dispose()
$background.Dispose()
$graphics.Dispose()
$canvas.Dispose()

Write-Host "Wrote $OutputPath"

