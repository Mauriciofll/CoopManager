param(
    [int]$Port = 8088,
    [string]$Bind = "127.0.0.1"
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$prefix = "http://$Bind`:$Port/"
$listener = [System.Net.HttpListener]::new()
$listener.Prefixes.Add($prefix)

function Get-ContentType {
    param([string]$Path)

    switch ([System.IO.Path]::GetExtension($Path).ToLowerInvariant()) {
        ".html" { "text/html; charset=utf-8" }
        ".css" { "text/css; charset=utf-8" }
        ".js" { "application/javascript; charset=utf-8" }
        ".json" { "application/json; charset=utf-8" }
        ".webmanifest" { "application/manifest+json; charset=utf-8" }
        ".png" { "image/png" }
        ".jpg" { "image/jpeg" }
        ".jpeg" { "image/jpeg" }
        ".svg" { "image/svg+xml" }
        default { "application/octet-stream" }
    }
}

try {
    $listener.Start()
    Write-Host "CoopManager Mobile em $prefix`mobile/"
    Write-Host "Pressione Ctrl+C para parar."

    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $requestPath = [System.Uri]::UnescapeDataString($context.Request.Url.AbsolutePath.TrimStart("/"))
        if ([string]::IsNullOrWhiteSpace($requestPath)) {
            $requestPath = "mobile/index.html"
        }
        if ($requestPath.EndsWith("/")) {
            $requestPath += "index.html"
        }

        $fullPath = [System.IO.Path]::GetFullPath((Join-Path $root $requestPath))
        if (!$fullPath.StartsWith($root, [System.StringComparison]::OrdinalIgnoreCase) -or !(Test-Path -LiteralPath $fullPath -PathType Leaf)) {
            $context.Response.StatusCode = 404
            $bytes = [System.Text.Encoding]::UTF8.GetBytes("Arquivo não encontrado.")
        } else {
            $context.Response.StatusCode = 200
            $context.Response.ContentType = Get-ContentType $fullPath
            $bytes = [System.IO.File]::ReadAllBytes($fullPath)
        }

        $context.Response.ContentLength64 = $bytes.Length
        $context.Response.OutputStream.Write($bytes, 0, $bytes.Length)
        $context.Response.OutputStream.Close()
    }
} finally {
    if ($listener.IsListening) {
        $listener.Stop()
    }
    $listener.Close()
}
