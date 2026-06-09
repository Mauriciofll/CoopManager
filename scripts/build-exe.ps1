$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$BuildDir = Join-Path $ProjectRoot "build\classes"
$DistDir = Join-Path $ProjectRoot "dist"
$JarDir = Join-Path $DistDir "jar"
$JarPath = Join-Path $JarDir "CoopManager.jar"
$LibDir = Join-Path $ProjectRoot "lib"
$AppImageDir = Join-Path $DistDir "CoopManager"
$ExePath = Join-Path $AppImageDir "CoopManager.exe"
$IconSource = Join-Path $ProjectRoot "assets\CoopManager.png"
$IconPath = Join-Path $ProjectRoot "build\CoopManager.ico"
$PostgresDriverVersion = "42.7.11"
$PostgresDriverPath = Join-Path $LibDir "postgresql-$PostgresDriverVersion.jar"

function New-CoopManagerIcon {
    param(
        [string] $SourcePath,
        [string] $DestinationPath
    )

    Add-Type -AssemblyName System.Drawing

    $sizes = @(256, 128, 64, 48, 32, 16)
    $source = [System.Drawing.Image]::FromFile($SourcePath)
    $images = New-Object System.Collections.Generic.List[byte[]]

    try {
        foreach ($size in $sizes) {
            $cropFactor = switch ($size) {
                256 { 1.00 }
                128 { 0.96 }
                64 { 0.88 }
                48 { 0.82 }
                32 { 0.74 }
                16 { 0.68 }
                default { 1.00 }
            }

            $sourceSquare = [int][Math]::Round([Math]::Min($source.Width, $source.Height) * $cropFactor)
            $sourceX = [int][Math]::Round(($source.Width - $sourceSquare) / 2)
            $sourceY = [int][Math]::Round(($source.Height - $sourceSquare) / 2)
            $sourceRectangle = New-Object System.Drawing.Rectangle $sourceX, $sourceY, $sourceSquare, $sourceSquare
            $destinationRectangle = New-Object System.Drawing.Rectangle 0, 0, $size, $size
            $bitmap = New-Object System.Drawing.Bitmap $size, $size
            $graphics = [System.Drawing.Graphics]::FromImage($bitmap)

            try {
                $graphics.Clear([System.Drawing.Color]::Transparent)
                $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
                $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
                $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
                $graphics.DrawImage(
                    $source,
                    $destinationRectangle,
                    $sourceRectangle,
                    [System.Drawing.GraphicsUnit]::Pixel
                )

                $memory = New-Object System.IO.MemoryStream
                $imageWriter = New-Object System.IO.BinaryWriter $memory
                try {
                    $imageWriter.Write([UInt32]40)
                    $imageWriter.Write([Int32]$size)
                    $imageWriter.Write([Int32]($size * 2))
                    $imageWriter.Write([UInt16]1)
                    $imageWriter.Write([UInt16]32)
                    $imageWriter.Write([UInt32]0)
                    $imageWriter.Write([UInt32]($size * $size * 4))
                    $imageWriter.Write([Int32]0)
                    $imageWriter.Write([Int32]0)
                    $imageWriter.Write([UInt32]0)
                    $imageWriter.Write([UInt32]0)

                    for ($y = $size - 1; $y -ge 0; $y--) {
                        for ($x = 0; $x -lt $size; $x++) {
                            $color = $bitmap.GetPixel($x, $y)
                            $imageWriter.Write([byte]$color.B)
                            $imageWriter.Write([byte]$color.G)
                            $imageWriter.Write([byte]$color.R)
                            $imageWriter.Write([byte]$color.A)
                        }
                    }

                    $maskStride = [Math]::Ceiling($size / 32) * 4
                    $imageWriter.Write((New-Object byte[] ($maskStride * $size)))
                    $imageWriter.Flush()
                    $images.Add($memory.ToArray())
                } finally {
                    $imageWriter.Dispose()
                    $memory.Dispose()
                }
            } finally {
                $graphics.Dispose()
                $bitmap.Dispose()
            }
        }
    } finally {
        $source.Dispose()
    }

    $directory = Split-Path -Parent $DestinationPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null

    $stream = [System.IO.File]::Open($DestinationPath, [System.IO.FileMode]::Create)
    $writer = New-Object System.IO.BinaryWriter $stream

    try {
        $writer.Write([UInt16]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]$sizes.Count)

        $offset = 6 + (16 * $sizes.Count)
        for ($index = 0; $index -lt $sizes.Count; $index++) {
            $size = $sizes[$index]
            $png = $images[$index]

            $writer.Write([byte]$(if ($size -eq 256) { 0 } else { $size }))
            $writer.Write([byte]$(if ($size -eq 256) { 0 } else { $size }))
            $writer.Write([byte]0)
            $writer.Write([byte]0)
            $writer.Write([UInt16]1)
            $writer.Write([UInt16]32)
            $writer.Write([UInt32]$png.Length)
            $writer.Write([UInt32]$offset)

            $offset += $png.Length
        }

        foreach ($png in $images) {
            $writer.Write($png)
        }
    } finally {
        $writer.Dispose()
        $stream.Dispose()
    }
}

function Ensure-PostgresDriver {
    if (Test-Path $PostgresDriverPath) {
        return
    }

    New-Item -ItemType Directory -Force -Path $LibDir | Out-Null
    $url = "https://repo1.maven.org/maven2/org/postgresql/postgresql/$PostgresDriverVersion/postgresql-$PostgresDriverVersion.jar"
    try {
        Write-Host "Baixando driver PostgreSQL $PostgresDriverVersion..."
        Invoke-WebRequest -Uri $url -OutFile $PostgresDriverPath
    } catch {
        Write-Warning "Não foi possível baixar o driver PostgreSQL. O modo local continuará funcionando, mas PostgreSQL exigirá um postgresql-*.jar em lib/."
    }
}

if (Test-Path $BuildDir) {
    Remove-Item -LiteralPath $BuildDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
New-Item -ItemType Directory -Force -Path $JarDir | Out-Null
Get-ChildItem -Path $JarDir -Filter "*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Ensure-PostgresDriver

$Sources = Get-ChildItem -Path (Join-Path $ProjectRoot "src") -Recurse -Filter "*.java" |
    Sort-Object FullName |
    ForEach-Object { $_.FullName }

& javac -encoding UTF-8 -d $BuildDir @Sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (Test-Path $IconSource) {
    $AssetOutputDir = Join-Path $BuildDir "assets"
    New-Item -ItemType Directory -Force -Path $AssetOutputDir | Out-Null
    Copy-Item -LiteralPath $IconSource -Destination (Join-Path $AssetOutputDir "CoopManager.png") -Force
}

& jar --create --file $JarPath --main-class Main -C $BuildDir .
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (Test-Path $LibDir) {
    Get-ChildItem -Path $LibDir -Filter "*.jar" | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $JarDir -Force
    }
}

if (Test-Path $AppImageDir) {
    Remove-Item -LiteralPath $AppImageDir -Recurse -Force
}

if (Test-Path $IconSource) {
    New-CoopManagerIcon -SourcePath $IconSource -DestinationPath $IconPath
}

$JpackageArgs = @(
    "--type", "app-image",
    "--name", "CoopManager",
    "--input", $JarDir,
    "--main-jar", "CoopManager.jar",
    "--main-class", "Main",
    "--dest", $DistDir,
    "--app-version", "1.0.0",
    "--vendor", "CoopManager"
)

if (Test-Path $IconPath) {
    $JpackageArgs += @("--icon", $IconPath)
}

& jpackage @JpackageArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Aplicativo gerado em:"
Write-Host $ExePath
