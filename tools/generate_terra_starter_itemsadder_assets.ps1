Add-Type -AssemblyName System.Drawing

$root = Join-Path $PSScriptRoot "..\\examples\\itemsadder\\contents\\terra_starter_gear"
$root = [System.IO.Path]::GetFullPath($root)
$itemDir = Join-Path $root "textures\\item"
$armorDir = Join-Path $root "textures\\armor"

New-Item -ItemType Directory -Force -Path $itemDir, $armorDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $itemDir "ore"), (Join-Path $itemDir "tool"), (Join-Path $itemDir "armor"), (Join-Path $armorDir "fur_pelt"), (Join-Path $armorDir "copper_brigandine") | Out-Null

function New-Bitmap([int]$width, [int]$height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $bitmap.MakeTransparent()
    return $bitmap
}

function Color([string]$hex) {
    return [System.Drawing.ColorTranslator]::FromHtml($hex)
}

function Set-Pixel($bitmap, [int]$x, [int]$y, $color) {
    if ($x -ge 0 -and $x -lt $bitmap.Width -and $y -ge 0 -and $y -lt $bitmap.Height) {
        $bitmap.SetPixel($x, $y, $color)
    }
}

function Rect($bitmap, [int]$x, [int]$y, [int]$w, [int]$h, $color) {
    for ($py = $y; $py -lt ($y + $h); $py++) {
        for ($px = $x; $px -lt ($x + $w); $px++) {
            Set-Pixel $bitmap $px $py $color
        }
    }
}

function Draw-DiagonalHandle($bitmap, $dark, $light) {
    $points = @(
        @(4, 12), @(5, 11), @(6, 10), @(7, 9), @(8, 8), @(9, 7), @(10, 6), @(11, 5)
    )
    foreach ($point in $points) {
        Set-Pixel $bitmap $point[0] $point[1] $dark
    }
    foreach ($point in @(@(4, 13), @(5, 12), @(6, 11), @(7, 10), @(8, 9), @(9, 8), @(10, 7), @(11, 6))) {
        Set-Pixel $bitmap $point[0] $point[1] $light
    }
}

function Save-Png($bitmap, [string]$path) {
    $parent = Split-Path $path -Parent
    if ($parent -and -not (Test-Path $parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function New-PixelMapTexture([string]$path, [string[]]$rows, [hashtable]$palette) {
    $height = $rows.Count
    $width = $rows[0].Length
    $bitmap = New-Bitmap $width $height
    for ($y = 0; $y -lt $height; $y++) {
        $row = $rows[$y]
        for ($x = 0; $x -lt $width; $x++) {
            $key = [string]$row[$x]
            if ($palette.ContainsKey($key) -and $null -ne $palette[$key]) {
                Set-Pixel $bitmap $x $y (Color ([string]$palette[$key]))
            }
        }
    }
    Save-Png $bitmap $path
}

function New-OreTexture([string]$path, [string]$baseHex, [string]$shadeHex, [string]$glowHex, [bool]$refined) {
    $bitmap = New-Bitmap 16 16
    $base = Color $baseHex
    $shade = Color $shadeHex
    $glow = Color $glowHex
    if ($refined) {
        Rect $bitmap 3 9 8 3 $shade
        Rect $bitmap 4 8 6 1 $base
        Rect $bitmap 5 7 4 1 $glow
        Rect $bitmap 8 10 4 2 $base
        Rect $bitmap 9 9 2 1 $glow
        Rect $bitmap 2 10 2 2 $base
        Set-Pixel $bitmap 3 9 $glow
        Set-Pixel $bitmap 10 8 $glow
    } else {
        foreach ($cluster in @(
            @{ x = 4; y = 4; w = 4; h = 4 },
            @{ x = 8; y = 5; w = 3; h = 5 },
            @{ x = 5; y = 9; w = 5; h = 3 }
        )) {
            Rect $bitmap $cluster.x $cluster.y $cluster.w $cluster.h $shade
            Rect $bitmap ($cluster.x + 1) $cluster.y 1 $cluster.h $base
            Set-Pixel $bitmap ($cluster.x + 1) ($cluster.y + 1) $glow
            Set-Pixel $bitmap ($cluster.x + $cluster.w - 2) ($cluster.y + 1) $glow
        }
        Set-Pixel $bitmap 10 10 $glow
        Set-Pixel $bitmap 6 11 $glow
    }
    Save-Png $bitmap $path
}

function New-ToolTexture([string]$path, [string]$metalDarkHex, [string]$metalHex, [string]$metalLightHex, [string]$kind) {
    $bitmap = New-Bitmap 16 16
    $woodDark = Color "#5A3A1E"
    $woodLight = Color "#8A6037"
    $metalDark = Color $metalDarkHex
    $metal = Color $metalHex
    $metalLight = Color $metalLightHex
    Draw-DiagonalHandle $bitmap $woodDark $woodLight
    switch ($kind) {
        "sword" {
            Rect $bitmap 10 2 2 5 $metalDark
            Rect $bitmap 11 1 1 5 $metal
            Set-Pixel $bitmap 11 0 $metalLight
            Rect $bitmap 8 6 4 1 $metalLight
            Rect $bitmap 7 7 2 1 $metalDark
        }
        "pickaxe" {
            Rect $bitmap 8 2 5 2 $metalDark
            Rect $bitmap 8 1 4 1 $metal
            Set-Pixel $bitmap 8 4 $metalLight
            Set-Pixel $bitmap 12 4 $metalLight
        }
        "shovel" {
            Rect $bitmap 10 2 2 4 $metalDark
            Rect $bitmap 9 1 4 3 $metal
            Set-Pixel $bitmap 10 1 $metalLight
            Set-Pixel $bitmap 11 1 $metalLight
        }
        "axe" {
            Rect $bitmap 9 2 4 4 $metalDark
            Rect $bitmap 8 2 3 3 $metal
            Set-Pixel $bitmap 8 4 $metalLight
            Set-Pixel $bitmap 11 2 $metalLight
        }
        "hoe" {
            Rect $bitmap 9 2 4 2 $metalDark
            Rect $bitmap 9 1 3 1 $metal
            Set-Pixel $bitmap 9 4 $metalLight
        }
    }
    Save-Png $bitmap $path
}

function New-ArmorIcon([string]$path, [string]$metalDarkHex, [string]$metalHex, [string]$metalLightHex, [string]$trimHex, [string]$piece) {
    $bitmap = New-Bitmap 16 16
    $metalDark = Color $metalDarkHex
    $metal = Color $metalHex
    $metalLight = Color $metalLightHex
    $trim = Color $trimHex
    switch ($piece) {
        "helmet" {
            Rect $bitmap 4 4 8 5 $metalDark
            Rect $bitmap 5 3 6 2 $metal
            Rect $bitmap 6 2 4 1 $metalLight
            Rect $bitmap 5 9 1 2 $trim
            Rect $bitmap 10 9 1 2 $trim
        }
        "chestplate" {
            Rect $bitmap 4 3 8 9 $metalDark
            Rect $bitmap 5 4 6 7 $metal
            Rect $bitmap 6 2 4 2 $metalLight
            Rect $bitmap 3 5 1 5 $trim
            Rect $bitmap 12 5 1 5 $trim
        }
        "leggings" {
            Rect $bitmap 4 3 8 4 $metalDark
            Rect $bitmap 5 4 6 3 $metal
            Rect $bitmap 4 7 3 6 $metalDark
            Rect $bitmap 9 7 3 6 $metalDark
            Rect $bitmap 5 7 1 5 $trim
            Rect $bitmap 10 7 1 5 $trim
        }
        "boots" {
            Rect $bitmap 4 8 3 4 $metalDark
            Rect $bitmap 9 8 3 4 $metalDark
            Rect $bitmap 4 11 4 2 $metal
            Rect $bitmap 8 11 4 2 $metal
            Rect $bitmap 5 8 1 2 $trim
            Rect $bitmap 10 8 1 2 $trim
        }
    }
    Save-Png $bitmap $path
}

function New-ArmorLayer([string]$path, [string]$metalDarkHex, [string]$metalHex, [string]$metalLightHex, [string]$trimHex) {
    $bitmap = New-Bitmap 64 32
    $metalDark = Color $metalDarkHex
    $metal = Color $metalHex
    $metalLight = Color $metalLightHex
    $trim = Color $trimHex
    for ($y = 0; $y -lt 32; $y++) {
        for ($x = 0; $x -lt 64; $x++) {
            if ((($x + $y) % 6) -eq 0) {
                Set-Pixel $bitmap $x $y $metalLight
            } elseif ((($x * 2 + $y) % 5) -eq 0) {
                Set-Pixel $bitmap $x $y $trim
            } elseif ((($x + $y) % 2) -eq 0) {
                Set-Pixel $bitmap $x $y $metal
            } else {
                Set-Pixel $bitmap $x $y $metalDark
            }
        }
    }
    Rect $bitmap 0 0 64 1 $trim
    Rect $bitmap 0 31 64 1 $trim
    Rect $bitmap 0 15 64 1 $metalLight
    Save-Png $bitmap $path
}

$ores = @(
    @{ id = "raw_coal"; base = "#2D2D2F"; shade = "#171719"; glow = "#6A6A70"; refined = $false },
    @{ id = "refined_coal"; base = "#4A4A4F"; shade = "#212124"; glow = "#8B8B90"; refined = $true },
    @{ id = "raw_iron"; base = "#B8A293"; shade = "#7A6659"; glow = "#E3D4C7"; refined = $false },
    @{ id = "refined_iron"; base = "#D7D8DA"; shade = "#8F9499"; glow = "#F7F7F7"; refined = $true },
    @{ id = "raw_copper"; base = "#C98053"; shade = "#8A4F2E"; glow = "#F0BC97"; refined = $false },
    @{ id = "refined_copper"; base = "#E59A67"; shade = "#A86034"; glow = "#FFD1AE"; refined = $true },
    @{ id = "raw_gold"; base = "#C9A421"; shade = "#7E6208"; glow = "#FFE16B"; refined = $false },
    @{ id = "refined_gold"; base = "#E0BC2E"; shade = "#A57D08"; glow = "#FFF099"; refined = $true },
    @{ id = "raw_redstone"; base = "#B12828"; shade = "#6D1111"; glow = "#FF6666"; refined = $false },
    @{ id = "refined_redstone"; base = "#D9453F"; shade = "#8A1E18"; glow = "#FF8C7D"; refined = $true },
    @{ id = "raw_lapis"; base = "#3255AF"; shade = "#162761"; glow = "#85A6FF"; refined = $false },
    @{ id = "refined_lapis"; base = "#4D79D6"; shade = "#213E84"; glow = "#A8C6FF"; refined = $true },
    @{ id = "raw_diamond"; base = "#59B7C8"; shade = "#206774"; glow = "#B8F5FF"; refined = $false },
    @{ id = "refined_diamond"; base = "#80DCE9"; shade = "#2E8897"; glow = "#E0FFFF"; refined = $true },
    @{ id = "raw_emerald"; base = "#3FAF5C"; shade = "#166D32"; glow = "#93F5A8"; refined = $false },
    @{ id = "refined_emerald"; base = "#61D479"; shade = "#228A43"; glow = "#C9FFD4"; refined = $true },
    @{ id = "raw_quartz"; base = "#D4CEC9"; shade = "#8B837B"; glow = "#FFFFFF"; refined = $false },
    @{ id = "refined_quartz"; base = "#F0EBE7"; shade = "#A9A29B"; glow = "#FFFFFF"; refined = $true },
    @{ id = "raw_tin"; base = "#A7B1B7"; shade = "#5E6A72"; glow = "#E1E7EA"; refined = $false },
    @{ id = "refined_tin"; base = "#C6D0D8"; shade = "#75838D"; glow = "#F8FCFF"; refined = $true }
)

foreach ($ore in $ores) {
    New-OreTexture (Join-Path $itemDir ("ore\\{0}.png" -f $ore.id)) $ore.base $ore.shade $ore.glow $ore.refined
}

$tools = @(
    @{ id = "tin_surveyor"; dark = "#67737B"; base = "#93A1AA"; light = "#DEE6EB" },
    @{ id = "copper_pioneer"; dark = "#8B4C28"; base = "#C97B45"; light = "#F1BE92" }
)

foreach ($toolSet in $tools) {
    foreach ($kind in @("sword", "pickaxe", "shovel", "axe", "hoe")) {
        New-ToolTexture (Join-Path $itemDir ("tool\\{0}_{1}.png" -f $toolSet.id, $kind)) $toolSet.dark $toolSet.base $toolSet.light $kind
    }
}

$armors = @(
    @{ id = "fur_pelt"; dark = "#6A523B"; base = "#907255"; light = "#C5AB82"; trim = "#E9DBBB" },
    @{ id = "copper_brigandine"; dark = "#743E20"; base = "#B86A37"; light = "#E8B180"; trim = "#3B2415" }
)

foreach ($armor in $armors) {
    foreach ($piece in @("helmet", "chestplate", "leggings", "boots")) {
        New-ArmorIcon (Join-Path $itemDir ("armor\\{0}_{1}.png" -f $armor.id, $piece)) $armor.dark $armor.base $armor.light $armor.trim $piece
    }
    New-ArmorLayer (Join-Path $armorDir ("{0}\\layer_1.png" -f $armor.id)) $armor.dark $armor.base $armor.light $armor.trim
    New-ArmorLayer (Join-Path $armorDir ("{0}\\layer_2.png" -f $armor.id)) $armor.dark $armor.base $armor.light $armor.trim
}

$copperPalette = @{
    "." = $null
    "h" = "#F3C394"
    "m" = "#C97B45"
    "d" = "#8B4C28"
    "w" = "#8A6037"
    "s" = "#5A3A1E"
}
New-PixelMapTexture (Join-Path $itemDir "tool\\copper_pioneer_axe.png") @(
    "................",
    "......hmd.......",
    ".....hmmmdd.....",
    ".....mmmmmd.....",
    "......mmmd......",
    ".......dw.......",
    "......dws.......",
    ".....dws........",
    "....dws.........",
    "...dws..........",
    "..dws...........",
    ".dws............",
    "..sw............",
    "................",
    "................",
    "................"
) $copperPalette

$furPalette = @{
    "." = $null
    "c" = "#E9DBBB"
    "l" = "#C5AB82"
    "m" = "#907255"
    "d" = "#6A523B"
    "s" = "#4A3827"
}
New-PixelMapTexture (Join-Path $itemDir "armor\\fur_pelt_helmet.png") @(
    "................",
    "................",
    ".....cccc.......",
    "....clllcc......",
    "...clmmmmlc.....",
    "...lmmddmml.....",
    "...lmdssmdl.....",
    "...lmddddml.....",
    "....lmmmml......",
    ".....llll.......",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................"
) $furPalette
New-PixelMapTexture (Join-Path $itemDir "armor\\fur_pelt_chestplate.png") @(
    "................",
    ".....cccc.......",
    "....clllcc......",
    "...clmmmmlc.....",
    "...lmmddmml.....",
    "..clmddddmlc....",
    "..lmmddddmml....",
    "..lmmddddmml....",
    "..lmddddddml....",
    "..lmddddddml....",
    "..clmddddmlc....",
    "...clmmmmlc.....",
    "....clllcc......",
    "................",
    "................",
    "................"
) $furPalette
New-PixelMapTexture (Join-Path $itemDir "armor\\fur_pelt_leggings.png") @(
    "................",
    ".....cccc.......",
    "....clllcc......",
    "...clmmmmlc.....",
    "...lmmddmml.....",
    "...lmddddml.....",
    "...clmddlc......",
    "....lmddm.......",
    "...clmddlc......",
    "...lmddddm......",
    "...lmddddm......",
    "...clmddlc......",
    "....lmddm.......",
    "................",
    "................",
    "................"
) $furPalette
New-PixelMapTexture (Join-Path $itemDir "armor\\fur_pelt_boots.png") @(
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "...cc....cc.....",
    "..cllc..cllc....",
    "..lmmd..lmmd....",
    "..lmmd..lmmd....",
    "..lmmd..lmmd....",
    "..cdddccddd.....",
    "...ssss.ssss....",
    "................",
    "................",
    "................"
) $furPalette

Write-Host "Generated Terra starter ItemsAdder assets in $root"
