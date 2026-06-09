param(
    [string]$SdkRoot = "",
    [string]$GradleVersion = "9.4.1"
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$toolsDir = Join-Path $root "build\android-tools"
if ([string]::IsNullOrWhiteSpace($SdkRoot)) {
    $SdkRoot = Join-Path $root "build\android-sdk"
}

$SdkRoot = [System.IO.Path]::GetFullPath($SdkRoot)
$cmdToolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-14742923_latest.zip"
$gradleUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
$cmdToolsZip = Join-Path $toolsDir "commandlinetools-win-14742923_latest.zip"
$gradleZip = Join-Path $toolsDir "gradle-$GradleVersion-bin.zip"
$gradleDir = Join-Path $toolsDir "gradle-$GradleVersion"
$sdkManager = Join-Path $SdkRoot "cmdline-tools\latest\bin\sdkmanager.bat"
$gradle = Join-Path $gradleDir "bin\gradle.bat"

function Get-JavaHome {
    $java = Get-Command java -ErrorAction Stop
    return Split-Path (Split-Path $java.Source)
}

function Ensure-JavaTrustStore {
    $javaHome = Get-JavaHome
    $trustStore = Join-Path $toolsDir "java-cacerts-local"
    $sourceTrustStore = Join-Path $javaHome "lib\security\cacerts"

    if (-not (Test-Path $trustStore)) {
        Copy-Item -LiteralPath $sourceTrustStore -Destination $trustStore -Force
    }

    if (-not ([System.Management.Automation.PSTypeName]"TlsCertReader").Type) {
        Add-Type @'
using System;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Cryptography.X509Certificates;
public static class TlsCertReader {
  public static X509Certificate2Collection ReadChain(string host) {
    using (var client = new TcpClient(host, 443))
    using (var stream = new SslStream(client.GetStream(), false, (sender, certificate, chain, errors) => true)) {
      stream.AuthenticateAsClient(host);
      var cert = new X509Certificate2(stream.RemoteCertificate);
      var chain = new X509Chain();
      chain.Build(cert);
      var collection = new X509Certificate2Collection();
      foreach (var element in chain.ChainElements) {
        collection.Add(element.Certificate);
      }
      return collection;
    }
  }
}
'@
    }

    $keytool = Join-Path $javaHome "bin\keytool.exe"
    $hosts = @("dl.google.com", "repo.maven.apache.org", "plugins.gradle.org")
    foreach ($hostName in $hosts) {
        try {
            $chain = [TlsCertReader]::ReadChain($hostName)
            for ($i = 0; $i -lt $chain.Count; $i++) {
                $certFile = Join-Path $toolsDir ("cert-$hostName-$i.cer")
                [System.IO.File]::WriteAllBytes($certFile, $chain[$i].Export([System.Security.Cryptography.X509Certificates.X509ContentType]::Cert))
                $alias = ($hostName -replace "[^A-Za-z0-9]", "-") + "-$i"
                & $keytool -delete -alias $alias -keystore $trustStore -storepass changeit 2>$null | Out-Null
                & $keytool -importcert -noprompt -trustcacerts -alias $alias -file $certFile -keystore $trustStore -storepass changeit | Out-Null
            }
        } catch {
            Write-Warning "Não foi possível importar certificados de ${hostName}: $($_.Exception.Message)"
        }
    }

    return $trustStore
}

function Download-IfMissing {
    param(
        [string]$Url,
        [string]$Destination
    )

    if (Test-Path $Destination) {
        return
    }

    New-Item -ItemType Directory -Force -Path (Split-Path $Destination) | Out-Null
    Write-Host "Baixando $Url"
    Invoke-WebRequest -Uri $Url -OutFile $Destination
}

function Run-Checked {
    param(
        [scriptblock]$Command,
        [string]$Message
    )

    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Message (código $LASTEXITCODE)"
    }
}

function Invoke-SdkManagerWithFallback {
    param(
        [string[]]$Arguments,
        [string]$FailureMessage
    )

    $yes = 1..200 | ForEach-Object { "y" }
    $baseArgs = @("--sdk_root=$SdkRoot") + $Arguments
    $yes | & $sdkManager @baseArgs | Out-Host
    if ($LASTEXITCODE -eq 0) {
        return
    }

    Write-Warning "sdkmanager falhou via HTTPS; tentando novamente com --no_https."
    $yes = 1..200 | ForEach-Object { "y" }
    $fallbackArgs = @("--sdk_root=$SdkRoot", "--no_https") + $Arguments
    $yes | & $sdkManager @fallbackArgs | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "$FailureMessage (código $LASTEXITCODE)"
    }
}

New-Item -ItemType Directory -Force -Path $toolsDir, $SdkRoot | Out-Null

if (-not (Test-Path $sdkManager)) {
    Download-IfMissing -Url $cmdToolsUrl -Destination $cmdToolsZip
    $tempCmdTools = Join-Path $toolsDir "cmdline-tools-expanded"
    if (Test-Path $tempCmdTools) {
        Remove-Item -LiteralPath $tempCmdTools -Recurse -Force
    }
    Expand-Archive -LiteralPath $cmdToolsZip -DestinationPath $tempCmdTools -Force

    $latestDir = Join-Path $SdkRoot "cmdline-tools\latest"
    New-Item -ItemType Directory -Force -Path (Split-Path $latestDir) | Out-Null
    if (Test-Path $latestDir) {
        Remove-Item -LiteralPath $latestDir -Recurse -Force
    }
    Move-Item -LiteralPath (Join-Path $tempCmdTools "cmdline-tools") -Destination $latestDir
}

if (-not (Test-Path $gradle)) {
    Download-IfMissing -Url $gradleUrl -Destination $gradleZip
    Expand-Archive -LiteralPath $gradleZip -DestinationPath $toolsDir -Force
}

$trustStore = Ensure-JavaTrustStore
$trustStoreOpt = "-Djavax.net.ssl.trustStore=$trustStore -Djavax.net.ssl.trustStorePassword=changeit"
$env:JAVA_TOOL_OPTIONS = (($env:JAVA_TOOL_OPTIONS, $trustStoreOpt) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join " "
$env:GRADLE_OPTS = (($env:GRADLE_OPTS, $trustStoreOpt) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join " "
$env:ANDROID_HOME = $SdkRoot
$env:ANDROID_SDK_ROOT = $SdkRoot

Write-Host "Instalando/atualizando pacotes Android SDK..."
Invoke-SdkManagerWithFallback -Arguments @("platform-tools", "platforms;android-36", "build-tools;36.0.0") -FailureMessage "Falha ao instalar pacotes do Android SDK"

$localProperties = Join-Path $root "mobile-apk\local.properties"
$escapedSdkRoot = $SdkRoot.Replace("\", "\\").Replace(":", "\:")
Set-Content -Path $localProperties -Value "sdk.dir=$escapedSdkRoot" -Encoding ASCII

Write-Host "Gerando APK Android..."
Run-Checked -Message "Falha ao gerar APK" -Command {
    & $gradle -p (Join-Path $root "mobile-apk") --no-daemon assembleDebug
}

$sourceApk = Join-Path $root "mobile-apk\app\build\outputs\apk\debug\app-debug.apk"
$distDir = Join-Path $root "dist\mobile"
$targetApk = Join-Path $distDir "CoopManager-Mobile-debug.apk"
New-Item -ItemType Directory -Force -Path $distDir | Out-Null
Copy-Item -LiteralPath $sourceApk -Destination $targetApk -Force

Write-Host "APK gerado em: $targetApk"
