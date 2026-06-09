param(
    [switch] $IncludeLocalData
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$DistDir = Join-Path $ProjectRoot "dist"
$AppImageDir = Join-Path $DistDir "CoopManager"
$InstallerDistDir = Join-Path $DistDir "installer"
$InstallerBuildDir = Join-Path $ProjectRoot "build\installer"
$PayloadZip = Join-Path $InstallerBuildDir "CoopManager-payload.zip"
$SeedDataDir = Join-Path $InstallerBuildDir "seed-data"
$SeedDataZip = Join-Path $InstallerBuildDir "CoopManager-seed-data.zip"
$LauncherScriptPath = Join-Path $InstallerBuildDir "launch.vbs"
$InstallScriptPath = Join-Path $InstallerBuildDir "install.ps1"
$SedPath = Join-Path $InstallerBuildDir "CoopManager-Setup.sed"
$SetupExe = Join-Path $InstallerDistDir "CoopManager-Setup.exe"
$BuildExeScript = Join-Path $PSScriptRoot "build-exe.ps1"

function Assert-Command {
    param([string] $Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "A ferramenta '$Name' nao foi encontrada no PATH."
    }
}

Assert-Command "iexpress.exe"

& powershell -NoProfile -ExecutionPolicy Bypass -File $BuildExeScript
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (-not (Test-Path $AppImageDir)) {
    throw "O app-image nao foi encontrado em $AppImageDir."
}

if (Test-Path $InstallerBuildDir) {
    Remove-Item -LiteralPath $InstallerBuildDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $InstallerBuildDir | Out-Null
New-Item -ItemType Directory -Force -Path $InstallerDistDir | Out-Null

if (Test-Path $SetupExe) {
    Remove-Item -LiteralPath $SetupExe -Force
}

Compress-Archive -Path $AppImageDir -DestinationPath $PayloadZip -CompressionLevel Optimal -Force

New-Item -ItemType Directory -Force -Path $SeedDataDir | Out-Null
$seedManifest = if ($IncludeLocalData) {
    "CoopManager local data snapshot"
} else {
    "CoopManager clean installer without local database"
}
Set-Content -LiteralPath (Join-Path $SeedDataDir "manifest.txt") -Encoding ASCII -Value $seedManifest

if ($IncludeLocalData -and -not [string]::IsNullOrWhiteSpace($env:APPDATA)) {
    $localDataDir = Join-Path $env:APPDATA "CoopManager"
    $localDatabasePath = Join-Path $localDataDir "coopmanager.dat"

    if (Test-Path $localDatabasePath) {
        $seedAppDataDir = Join-Path $SeedDataDir "CoopManager"
        New-Item -ItemType Directory -Force -Path $seedAppDataDir | Out-Null
        Copy-Item -LiteralPath $localDatabasePath -Destination (Join-Path $seedAppDataDir "coopmanager.dat") -Force
    }
}

Compress-Archive -Path (Join-Path $SeedDataDir "*") -DestinationPath $SeedDataZip -CompressionLevel Optimal -Force

$InstallScript = @'
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$AppName = "CoopManager"
$PayloadZip = Join-Path $PSScriptRoot "CoopManager-payload.zip"
$SeedDataZip = Join-Path $PSScriptRoot "CoopManager-seed-data.zip"
$DefaultInstallPath = Join-Path $env:LOCALAPPDATA "Programs\CoopManager"

function T {
    param([string] $Text)

    return [regex]::Replace($Text, "\\u([0-9A-Fa-f]{4})", {
        param($Match)
        return [string][char][Convert]::ToInt32($Match.Groups[1].Value, 16)
    })
}

function Show-Error {
    param([string] $Message)
    [System.Windows.Forms.MessageBox]::Show(
        $Message,
        $AppName,
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error
    ) | Out-Null
}

function New-Shortcut {
    param(
        [string] $ShortcutPath,
        [string] $TargetPath,
        [string] $WorkingDirectory
    )

    $directory = Split-Path -Parent $ShortcutPath
    if (-not (Test-Path $directory)) {
        New-Item -ItemType Directory -Force -Path $directory | Out-Null
    }

    $shell = New-Object -ComObject WScript.Shell
    $shortcut = $shell.CreateShortcut($ShortcutPath)
    $shortcut.TargetPath = $TargetPath
    $shortcut.WorkingDirectory = $WorkingDirectory
    $shortcut.IconLocation = $TargetPath
    $shortcut.Save()
}

function Restore-SeedData {
    param([string] $SeedZip)

    if (-not (Test-Path $SeedZip)) {
        return $null
    }

    if ([string]::IsNullOrWhiteSpace($env:APPDATA)) {
        return $null
    }

    $seedTemp = Join-Path ([System.IO.Path]::GetTempPath()) ("CoopManagerSeed_" + [Guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $seedTemp | Out-Null

    try {
        Expand-Archive -LiteralPath $SeedZip -DestinationPath $seedTemp -Force
        $sourceDataDir = Join-Path $seedTemp "CoopManager"
        $sourceDatabase = Join-Path $sourceDataDir "coopmanager.dat"

        if (-not (Test-Path $sourceDatabase)) {
            return $null
        }

        $targetDataDir = Join-Path $env:APPDATA "CoopManager"
        New-Item -ItemType Directory -Force -Path $targetDataDir | Out-Null

        $targetDatabase = Join-Path $targetDataDir "coopmanager.dat"
        $backupPath = $null

        if (Test-Path $targetDatabase) {
            $backupPath = Join-Path $targetDataDir ("coopmanager.backup-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".dat")
            Copy-Item -LiteralPath $targetDatabase -Destination $backupPath -Force
        }

        Copy-Item -LiteralPath $sourceDatabase -Destination $targetDatabase -Force

        return [pscustomobject]@{
            DataPath = $targetDataDir
            BackupPath = $backupPath
        }
    } finally {
        Remove-Item -LiteralPath $seedTemp -Recurse -Force -ErrorAction SilentlyContinue
    }
}

function Show-InstallDialog {
    $form = New-Object System.Windows.Forms.Form
    $form.Text = "Instalador do CoopManager"
    $form.StartPosition = "CenterScreen"
    $form.FormBorderStyle = "FixedDialog"
    $form.MaximizeBox = $false
    $form.MinimizeBox = $false
    $form.ClientSize = New-Object System.Drawing.Size(560, 210)

    $title = New-Object System.Windows.Forms.Label
    $title.Text = "Escolha onde instalar o CoopManager"
    $title.Font = New-Object System.Drawing.Font("Segoe UI", 12, [System.Drawing.FontStyle]::Bold)
    $title.AutoSize = $true
    $title.Location = New-Object System.Drawing.Point(18, 18)
    $form.Controls.Add($title)

    $description = New-Object System.Windows.Forms.Label
    $description.Text = T "O instalador copiar\u00E1 o aplicativo e o runtime necess\u00E1rio para a pasta indicada."
    $description.AutoSize = $true
    $description.Location = New-Object System.Drawing.Point(20, 50)
    $form.Controls.Add($description)

    $pathBox = New-Object System.Windows.Forms.TextBox
    $pathBox.Text = $DefaultInstallPath
    $pathBox.Location = New-Object System.Drawing.Point(22, 88)
    $pathBox.Size = New-Object System.Drawing.Size(420, 26)
    $form.Controls.Add($pathBox)

    $browse = New-Object System.Windows.Forms.Button
    $browse.Text = "Procurar..."
    $browse.Location = New-Object System.Drawing.Point(452, 86)
    $browse.Size = New-Object System.Drawing.Size(86, 30)
    $browse.Add_Click({
        $dialog = New-Object System.Windows.Forms.FolderBrowserDialog
        $dialog.Description = T "Escolha a pasta de instala\u00E7\u00E3o do CoopManager"
        $dialog.SelectedPath = if (Test-Path $pathBox.Text) { $pathBox.Text } else { Split-Path -Parent $pathBox.Text }
        if ($dialog.ShowDialog($form) -eq [System.Windows.Forms.DialogResult]::OK) {
            $pathBox.Text = $dialog.SelectedPath
        }
    })
    $form.Controls.Add($browse)

    $desktopShortcut = New-Object System.Windows.Forms.CheckBox
    $desktopShortcut.Text = T "Criar atalho na \u00E1rea de trabalho"
    $desktopShortcut.Checked = $true
    $desktopShortcut.AutoSize = $true
    $desktopShortcut.Location = New-Object System.Drawing.Point(22, 126)
    $form.Controls.Add($desktopShortcut)

    $install = New-Object System.Windows.Forms.Button
    $install.Text = "Instalar"
    $install.Location = New-Object System.Drawing.Point(352, 162)
    $install.Size = New-Object System.Drawing.Size(88, 32)
    $install.DialogResult = [System.Windows.Forms.DialogResult]::OK
    $form.AcceptButton = $install
    $form.Controls.Add($install)

    $cancel = New-Object System.Windows.Forms.Button
    $cancel.Text = "Cancelar"
    $cancel.Location = New-Object System.Drawing.Point(450, 162)
    $cancel.Size = New-Object System.Drawing.Size(88, 32)
    $cancel.DialogResult = [System.Windows.Forms.DialogResult]::Cancel
    $form.CancelButton = $cancel
    $form.Controls.Add($cancel)

    $result = $form.ShowDialog()
    if ($result -ne [System.Windows.Forms.DialogResult]::OK) {
        return $null
    }

    return [pscustomobject]@{
        InstallPath = $pathBox.Text.Trim()
        DesktopShortcut = $desktopShortcut.Checked
    }
}

try {
    if (-not (Test-Path $PayloadZip)) {
        throw (T "O pacote de instala\u00E7\u00E3o n\u00E3o foi encontrado.")
    }

    $selection = Show-InstallDialog
    if ($null -eq $selection) {
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($selection.InstallPath)) {
        throw (T "Informe uma pasta de instala\u00E7\u00E3o.")
    }

    $targetPath = [System.IO.Path]::GetFullPath($selection.InstallPath)
    $tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("CoopManagerInstall_" + [Guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null

    try {
        Expand-Archive -LiteralPath $PayloadZip -DestinationPath $tempRoot -Force
        $sourcePath = Join-Path $tempRoot "CoopManager"
        if (-not (Test-Path $sourcePath)) {
            throw (T "Os arquivos do aplicativo n\u00E3o foram encontrados no pacote.")
        }

        New-Item -ItemType Directory -Force -Path $targetPath | Out-Null

        $knownItems = @("app", "runtime", "bin", "conf", "legal", "lib", "CoopManager.exe", "release")
        foreach ($item in $knownItems) {
            $path = Join-Path $targetPath $item
            if (Test-Path $path) {
                Remove-Item -LiteralPath $path -Recurse -Force
            }
        }

        Copy-Item -Path (Join-Path $sourcePath "*") -Destination $targetPath -Recurse -Force

        $exePath = Join-Path $targetPath "CoopManager.exe"
        if (-not (Test-Path $exePath)) {
            throw (T "A instala\u00E7\u00E3o terminou sem encontrar o execut\u00E1vel do CoopManager.")
        }

        $startMenuShortcut = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\CoopManager\CoopManager.lnk"
        New-Shortcut -ShortcutPath $startMenuShortcut -TargetPath $exePath -WorkingDirectory $targetPath

        if ($selection.DesktopShortcut) {
            $desktopShortcut = Join-Path ([Environment]::GetFolderPath("Desktop")) "CoopManager.lnk"
            New-Shortcut -ShortcutPath $desktopShortcut -TargetPath $exePath -WorkingDirectory $targetPath
        }

        $seedResult = Restore-SeedData -SeedZip $SeedDataZip
        $successMessage = "CoopManager foi instalado com sucesso.`n`nLocal: $targetPath"
        if ($null -ne $seedResult) {
            $successMessage += "`n`nDados locais copiados para: $($seedResult.DataPath)"
            if (-not [string]::IsNullOrWhiteSpace($seedResult.BackupPath)) {
                $successMessage += "`nBackup anterior: $($seedResult.BackupPath)"
            }
        }
        $successMessage += "`n`nDeseja abrir agora?"

        $openNow = [System.Windows.Forms.MessageBox]::Show(
            $successMessage,
            $AppName,
            [System.Windows.Forms.MessageBoxButtons]::YesNo,
            [System.Windows.Forms.MessageBoxIcon]::Information
        )

        if ($openNow -eq [System.Windows.Forms.DialogResult]::Yes) {
            Start-Process -FilePath $exePath -WorkingDirectory $targetPath
        }
    } finally {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
} catch {
    Show-Error $_.Exception.Message
    exit 1
}
'@

$LauncherScript = @'
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
baseDir = fso.GetParentFolderName(WScript.ScriptFullName)
command = "powershell.exe -NoProfile -Sta -ExecutionPolicy Bypass -WindowStyle Hidden -File """ & baseDir & "\install.ps1"""
shell.CurrentDirectory = baseDir
exitCode = shell.Run(command, 0, True)
WScript.Quit exitCode
'@

Set-Content -LiteralPath $LauncherScriptPath -Encoding ASCII -Value $LauncherScript
Set-Content -LiteralPath $InstallScriptPath -Encoding ASCII -Value $InstallScript

$SedContent = @"
[Version]
Class=IEXPRESS
SEDVersion=3

[Options]
PackagePurpose=InstallApp
ShowInstallProgramWindow=0
HideExtractAnimation=1
UseLongFileName=1
InsideCompressed=0
CAB_FixedSize=0
CAB_ResvCodeSigning=0
RebootMode=N
InstallPrompt=%InstallPrompt%
DisplayLicense=%DisplayLicense%
FinishMessage=%FinishMessage%
TargetName=%TargetName%
FriendlyName=%FriendlyName%
AppLaunched=%AppLaunched%
PostInstallCmd=%PostInstallCmd%
AdminQuietInstCmd=%AdminQuietInstCmd%
UserQuietInstCmd=%UserQuietInstCmd%
SourceFiles=SourceFiles

[Strings]
InstallPrompt=
DisplayLicense=
FinishMessage=
TargetName=$SetupExe
FriendlyName=CoopManager Setup
AppLaunched=wscript.exe launch.vbs
PostInstallCmd=<None>
AdminQuietInstCmd=
UserQuietInstCmd=
FILE0="launch.vbs"
FILE1="install.ps1"
FILE2="CoopManager-payload.zip"
FILE3="CoopManager-seed-data.zip"

[SourceFiles]
SourceFiles0=$InstallerBuildDir

[SourceFiles0]
%FILE0%=
%FILE1%=
%FILE2%=
%FILE3%=
"@

Set-Content -LiteralPath $SedPath -Encoding ASCII -Value $SedContent

& iexpress.exe /N $SedPath
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

 $deadline = (Get-Date).AddMinutes(5)
 $lastSize = -1
 $stableChecks = 0
 while ((Get-Date) -lt $deadline) {
    if (Test-Path $SetupExe) {
        $currentSize = (Get-Item $SetupExe).Length
        if ($currentSize -gt 0 -and $currentSize -eq $lastSize) {
            $stableChecks++
            if ($stableChecks -ge 3) {
                break
            }
        } else {
            $stableChecks = 0
            $lastSize = $currentSize
        }
    }

    Start-Sleep -Seconds 1
}

if (-not (Test-Path $SetupExe)) {
    throw "O IExpress terminou sem gerar o instalador esperado."
}

Write-Host "Instalador gerado em:"
Write-Host $SetupExe
