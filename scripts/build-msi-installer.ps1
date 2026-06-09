param(
    [string] $AppVersion = "1.0.0",
    [switch] $SkipBuildExe
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$DistDir = Join-Path $ProjectRoot "dist"
$AppImageDir = Join-Path $DistDir "CoopManager"
$MsiDistDir = Join-Path $DistDir "installer-msi"
$BuildExeScript = Join-Path $PSScriptRoot "build-exe.ps1"
$WixToolsDir = Join-Path $ProjectRoot "build\wix-tools"
$WixZipPath = Join-Path $WixToolsDir "wix314-binaries.zip"
$WixExtractDir = Join-Path $WixToolsDir "wix314"
$WixUrl = "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"
$GeneratedMsi = Join-Path $MsiDistDir "CoopManager-$AppVersion.msi"
$SetupMsi = Join-Path $MsiDistDir "CoopManager-Setup.msi"
$PortableDir = Join-Path $DistDir "portable"
$PortableZip = Join-Path $PortableDir "CoopManager-Portable.zip"

function Assert-Command {
    param([string] $Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "A ferramenta '$Name' nao foi encontrada no PATH."
    }
}

function Ensure-WixTools {
    New-Item -ItemType Directory -Force -Path $WixToolsDir | Out-Null

    $candlePath = Join-Path $WixExtractDir "candle.exe"
    $lightPath = Join-Path $WixExtractDir "light.exe"

    if (-not (Test-Path $candlePath) -or -not (Test-Path $lightPath)) {
        if (-not (Test-Path $WixZipPath)) {
            Write-Host "Baixando WiX Toolset para gerar MSI..."
            Invoke-WebRequest -Uri $WixUrl -OutFile $WixZipPath
        }

        if (Test-Path $WixExtractDir) {
            Remove-Item -LiteralPath $WixExtractDir -Recurse -Force
        }

        New-Item -ItemType Directory -Force -Path $WixExtractDir | Out-Null
        Expand-Archive -LiteralPath $WixZipPath -DestinationPath $WixExtractDir -Force
    }

    $env:PATH = "$WixExtractDir;$env:PATH"
}

Assert-Command "jpackage"

if (-not $SkipBuildExe) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $BuildExeScript
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

if (-not (Test-Path $AppImageDir)) {
    throw "O app-image nao foi encontrado em $AppImageDir."
}

Ensure-WixTools

if (Test-Path $MsiDistDir) {
    Remove-Item -LiteralPath $MsiDistDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $MsiDistDir | Out-Null

& jpackage `
    --type msi `
    --name "CoopManager" `
    --app-image $AppImageDir `
    --dest $MsiDistDir `
    --app-version $AppVersion `
    --vendor "CoopManager" `
    --description "CoopManager" `
    --win-per-user-install `
    --win-dir-chooser `
    --win-menu `
    --win-menu-group "CoopManager" `
    --win-shortcut

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (-not (Test-Path $GeneratedMsi)) {
    throw "O MSI esperado nao foi gerado em $GeneratedMsi."
}

Copy-Item -LiteralPath $GeneratedMsi -Destination $SetupMsi -Force

if (Test-Path $PortableDir) {
    Remove-Item -LiteralPath $PortableDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $PortableDir | Out-Null
Compress-Archive -Path $AppImageDir -DestinationPath $PortableZip -CompressionLevel Optimal -Force

Write-Host "MSI gerado em:"
Write-Host $SetupMsi
Write-Host "ZIP portable gerado em:"
Write-Host $PortableZip
