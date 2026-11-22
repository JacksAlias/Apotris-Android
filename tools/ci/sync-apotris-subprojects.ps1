#!/usr/bin/env pwsh
# Clone gitea-hosted Meson subprojects that are not in the wrap DB and were never committed
# (listed in main/apotris/.gitmodules). Safe to re-run: skips if meson.build already exists.
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$subprojects = Join-Path $repoRoot "main\apotris\subprojects"

# The Android checkout contains a partial SDL2 source directory (only a few
# Android compatibility files). Meson sees the directory and skips extracting
# the sdl2.wrap archive, then fails because meson.build is absent. Remove that
# incomplete directory so Meson can populate it from the pinned wrap archive.
$sdl2Source = Join-Path $subprojects "SDL2-2.28.5"
if ((Test-Path $sdl2Source) -and -not (Test-Path (Join-Path $sdl2Source "meson.build"))) {
    Write-Host "Removing incomplete SDL2 source tree; Meson will restore it from sdl2.wrap"
    Remove-Item $sdl2Source -Recurse -Force
}

# Paths match main/apotris/.gitmodules (Meson subproject names: general-tools, openmpt, SoLoud).
$repos = @(
    @{ Name = "general-tools"; Url = "https://gitea.com/Apotris/general-tools.git"; Branch = "main" },
    @{ Name = "openmpt";       Url = "https://gitea.com/Apotris/libopenmpt.git"; Branch = "main" },
    @{ Name = "SoLoud";        Url = "https://gitea.com/Apotris/SoLoud.git";     Branch = "main" }
)

foreach ($r in $repos) {
    $dest = Join-Path $subprojects $r.Name
    $marker = Join-Path $dest "meson.build"
    if (Test-Path $marker) {
        Write-Host "OK: $($r.Name) already present"
        continue
    }
    Write-Host "Cloning $($r.Name) (branch $($r.Branch)) ..."
    if (Test-Path $dest) {
        Remove-Item $dest -Recurse -Force -ErrorAction SilentlyContinue
    }
    git clone --depth 1 --branch $r.Branch $r.Url $dest
    if ($LASTEXITCODE -ne 0) { throw "git clone failed: $($r.Name)" }
}

Write-Host "Apotris subproject sync done."
