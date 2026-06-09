param(
    [switch] $Local,
    [string] $HostName = "",
    [int] $Port = 5432,
    [string] $Database = "coopmanager",
    [string] $User = "coopmanager",
    [string] $Password = ""
)

$ErrorActionPreference = "Stop"

function Get-ConfigPath {
    if (-not [string]::IsNullOrWhiteSpace($env:APPDATA)) {
        return Join-Path $env:APPDATA "CoopManager\database.properties"
    }

    return Join-Path $HOME ".coopmanager\database.properties"
}

function Read-PlainPassword {
    param([string] $Prompt)

    $secure = Read-Host $Prompt -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

$configPath = Get-ConfigPath
$configDir = Split-Path -Parent $configPath
New-Item -ItemType Directory -Force -Path $configDir | Out-Null

if ($Local) {
    $content = @"
database.mode=local
postgres.url=jdbc:postgresql://localhost:5432/coopmanager
postgres.user=coopmanager
postgres.password=
"@
    Set-Content -LiteralPath $configPath -Encoding ASCII -Value $content
    Write-Host "CoopManager configurado para banco local em: $configPath"
    exit 0
}

if ([string]::IsNullOrWhiteSpace($HostName)) {
    $HostName = Read-Host "IP ou nome do servidor PostgreSQL"
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = Read-PlainPassword "Senha do usuário $User"
}

$url = "jdbc:postgresql://$HostName`:$Port/$Database"
$content = @"
database.mode=postgres
postgres.url=$url
postgres.user=$User
postgres.password=$Password
"@

Set-Content -LiteralPath $configPath -Encoding ASCII -Value $content
Write-Host "CoopManager configurado para banco compartilhado:"
Write-Host "  $url"
Write-Host "Arquivo salvo em:"
Write-Host "  $configPath"
Write-Host ""
Write-Host "Abra o CoopManager novamente para usar esta configuração."
