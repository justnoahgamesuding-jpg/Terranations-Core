param(
    [string]$SourceDir = "examples/resourcepacks/terranations_hud_pack",
    [string[]]$OutputZips = @(
        "terranations_hud_pack.zip",
        "terranations_hud_pack_font.zip",
        "terranations_hud_pack_font_clean.zip"
    )
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$sourcePath = Join-Path $root $SourceDir

if (-not (Test-Path $sourcePath)) {
    throw "Source resource-pack folder not found: $sourcePath"
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$sourcePath = (Resolve-Path $sourcePath).Path
$prefixLength = $sourcePath.Length + 1
$files = Get-ChildItem -Path $sourcePath -Recurse -File | Sort-Object FullName

foreach ($outputZip in $OutputZips) {
    $zipPath = Join-Path $root $outputZip
    if (Test-Path $zipPath) {
        Remove-Item $zipPath -Force
    }

    $archive = [System.IO.Compression.ZipFile]::Open($zipPath, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($file in $files) {
            $entryName = $file.FullName.Substring($prefixLength).Replace('\', '/')
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                $archive,
                $file.FullName,
                $entryName,
                [System.IO.Compression.CompressionLevel]::Optimal
            ) | Out-Null
        }
    } finally {
        $archive.Dispose()
    }
}
