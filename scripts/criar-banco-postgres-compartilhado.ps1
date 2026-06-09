param(
    [string] $HostName = "localhost",
    [int] $Port = 5432,
    [string] $AdminUser = "postgres",
    [string] $AdminPassword = "",
    [string] $Database = "coopmanager",
    [string] $AppUser = "coopmanager",
    [string] $AppPassword = "",
    [switch] $ConfigureThisComputer,
    [switch] $OpenFirewall
)

$ErrorActionPreference = "Stop"

function Assert-Command {
    param([string] $Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "A ferramenta '$Name' não foi encontrada no PATH. Instale o PostgreSQL ou adicione a pasta bin ao PATH."
    }
}

function Assert-Identifier {
    param(
        [string] $Value,
        [string] $Name
    )

    if ($Value -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') {
        throw "$Name deve conter apenas letras, números e _, começando por letra ou _."
    }
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

function Sql-Literal {
    param([string] $Value)
    return $Value.Replace("'", "''")
}

Assert-Command "psql"
Assert-Identifier -Value $Database -Name "Database"
Assert-Identifier -Value $AppUser -Name "AppUser"

if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    $AdminPassword = Read-PlainPassword "Senha do administrador PostgreSQL ($AdminUser)"
}

if ([string]::IsNullOrWhiteSpace($AppPassword)) {
    $AppPassword = Read-PlainPassword "Senha que o CoopManager usará no banco ($AppUser)"
}

$env:PGPASSWORD = $AdminPassword
$appUserLiteral = Sql-Literal $AppUser
$appPasswordLiteral = Sql-Literal $AppPassword
$databaseLiteral = Sql-Literal $Database

$roleSql = @"
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$appUserLiteral') THEN
        CREATE ROLE "$AppUser" LOGIN PASSWORD '$appPasswordLiteral';
    ELSE
        ALTER ROLE "$AppUser" WITH LOGIN PASSWORD '$appPasswordLiteral';
    END IF;
END
`$`$;
"@

& psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c $roleSql
if ($LASTEXITCODE -ne 0) {
    throw "Não foi possível criar/atualizar o usuário $AppUser."
}

$exists = & psql -h $HostName -p $Port -U $AdminUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$databaseLiteral'"
if ($LASTEXITCODE -ne 0) {
    throw "Não foi possível verificar se o banco $Database existe."
}

if (($exists | Out-String).Trim() -ne "1") {
    & psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE ""$Database"" OWNER ""$AppUser"""
    if ($LASTEXITCODE -ne 0) {
        throw "Não foi possível criar o banco $Database."
    }
}

& psql -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "ALTER DATABASE ""$Database"" OWNER TO ""$AppUser"""
if ($LASTEXITCODE -ne 0) {
    throw "Não foi possível ajustar o dono do banco $Database."
}

if ($OpenFirewall) {
    try {
        New-NetFirewallRule -DisplayName "CoopManager PostgreSQL" -Direction Inbound -Protocol TCP -LocalPort $Port -Action Allow -ErrorAction Stop | Out-Null
        Write-Host "Regra de firewall criada para a porta $Port."
    } catch {
        Write-Warning "Não foi possível criar a regra de firewall automaticamente: $($_.Exception.Message)"
    }
}

if ($ConfigureThisComputer) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "configurar-banco-compartilhado.ps1") `
        -HostName $HostName `
        -Port $Port `
        -Database $Database `
        -User $AppUser `
        -Password $AppPassword
}

Write-Host ""
Write-Host "Banco compartilhado pronto:"
Write-Host "  Host: $HostName"
Write-Host "  Porta: $Port"
Write-Host "  Banco: $Database"
Write-Host "  Usuário do app: $AppUser"
Write-Host ""
Write-Host "Em cada computador cliente, configure o CoopManager com:"
Write-Host "  powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configurar-banco-compartilhado.ps1 -HostName $HostName -Port $Port -Database $Database -User $AppUser"
