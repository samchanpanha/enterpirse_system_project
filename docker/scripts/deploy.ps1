# ─────────────────────────────────────────────────────────────────────────────
# Report System — Deploy script (Windows PowerShell 5+)
#
# Usage:
#   .\deploy.ps1 local up          Build + start all services (dev compose)
#   .\deploy.ps1 local down        Stop + remove containers (keeps volumes)
#   .\deploy.ps1 local logs [svc]  Tail logs (all or specific service)
#   .\deploy.ps1 local restart     Restart all services
#   .\deploy.ps1 local build       Build all images only (no start)
#   .\deploy.ps1 local status      Show container status
#   .\deploy.ps1 local clean       Stop + remove containers AND volumes
#
#   .\deploy.ps1 prod up           Build prod images + start (prod compose)
#   .\deploy.ps1 prod down         Stop prod
#   .\deploy.ps1 prod push [reg]   Build + tag + push to registry
#   .\deploy.ps1 prod status       Show prod container status
#
# Environment variables (or use .env file):
#   $env:REGISTRY    e.g. docker.io/myorg, ghcr.io/myorg
#   $env:IMAGE_TAG   e.g. v1.0.0, latest  (default: latest)
# ─────────────────────────────────────────────────────────────────────────────

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true, Position=0)] [ValidateSet("local","prod")] [string]$Mode,
    [Parameter(Mandatory=$true, Position=1)] [string]$Action,
    [Parameter(Mandatory=$false, Position=2)] [string]$Arg
)

$ErrorActionPreference = "Stop"

# ─── Resolve paths ────────────────────────────────────────────────────────────
$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$ProjectRoot = Split-Path -Parent $ProjectRoot
$DockerDir   = Join-Path $ProjectRoot "docker"

$ComposeFile     = Join-Path $DockerDir "docker-compose.yml"
$ComposeProdFile = Join-Path $DockerDir "docker-compose.prod.yml"
$EnvFile         = Join-Path $DockerDir ".env"

# Default values (overridden by .env or env vars)
if (-not $env:REGISTRY)  { $env:REGISTRY  = "reportsystem" }
if (-not $env:IMAGE_TAG) { $env:IMAGE_TAG = "latest" }

$Services = @(
    "eureka", "gateway",
    "auth-service", "property-service", "restaurant-service", "inventory-service",
    "finance-service", "payment-service", "reporting-service",
    "nuxt-web"
)

# ─── Output helpers ───────────────────────────────────────────────────────────
function Write-Log  { param($m) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $m" -ForegroundColor Blue }
function Write-Ok   { param($m) Write-Host "[OK] $m" -ForegroundColor Green }
function Write-Warn { param($m) Write-Host "[!]  $m" -ForegroundColor Yellow }
function Write-Fail { param($m) Write-Host "[X]  $m" -ForegroundColor Red; exit 1 }
function Write-Header { param($m) Write-Host ""; Write-Host "=== $m ===" -ForegroundColor Cyan; Write-Host "" }

# ─── Load .env if present ────────────────────────────────────────────────────
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $val  = $matches[2].Trim()
            Set-Item -Path "env:$name" -Value $val
        }
    }
    Write-Ok "Loaded $EnvFile"
}

# ─── Prerequisite checks ─────────────────────────────────────────────────────
function Test-Docker {
    try {
        $null = docker --version
        $null = docker info 2>$null
        docker compose version 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "compose plugin missing" }
    } catch {
        Write-Fail "Docker not available. Install Docker Desktop: https://docker.com"
    }
    Write-Ok "Docker available"
}

function Test-Maven {
    if ($Action -in "up","build") {
        $mvn = Get-Command mvn -ErrorAction SilentlyContinue
        if ($mvn) {
            Write-Ok "Maven available"
        } else {
            Write-Warn "mvn not in PATH — backend JARs must already exist in services/*/target/"
        }
    }
}

# ─── Helpers ────────────────────────────────────────────────────────────────
function Invoke-MavenBuild {
    Write-Header "Building backend JARs"
    Set-Location $ProjectRoot
    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvn) {
        & mvn clean package -DskipTests -q
        if ($LASTEXITCODE -ne 0) { Write-Fail "Maven build failed" }
        Write-Ok "Maven build complete"
    } else {
        $jars = Get-ChildItem -Path "$ProjectRoot/services" -Recurse -Filter "*-service-*.jar" `
                              -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notlike "*/original-*" }
        if (-not $jars) { Write-Fail "No JARs found in services/*/target/ and mvn is not available" }
        Write-Warn "Skipping Maven build (mvn not found) — assuming JARs are present"
    }
}

function Invoke-Compose {
    param([string]$File, [string[]]$Args2)
    # docker compose uses the dirname of -f as the project directory for
    # relative paths. We must pass --project-directory explicitly so build
    # contexts (e.g. ./frontend/...) resolve from PROJECT_ROOT, not from
    # the compose file's directory.
    & docker compose --project-directory $ProjectRoot -f $File @Args2
    if ($LASTEXITCODE -ne 0) { Write-Fail "docker compose failed: $Args2" }
}

# ─── Local mode ─────────────────────────────────────────────────────────────
function Local-Up {
    Write-Header "LOCAL: Build + start"
    Test-Docker
    Invoke-MavenBuild
    Invoke-Compose $ComposeFile @("up","-d","--build")
    Wait-Healthy
    Local-Status
}

function Local-Down {
    Write-Header "LOCAL: Stop"
    Test-Docker
    Invoke-Compose $ComposeFile @("down")
    Write-Ok "Stopped"
}

function Local-Logs {
    Test-Docker
    if ($Arg) {
        Invoke-Compose $ComposeFile @("logs","-f",$Arg)
    } else {
        Invoke-Compose $ComposeFile @("logs","-f")
    }
}

function Local-Restart {
    Write-Header "LOCAL: Restart"
    Test-Docker
    Invoke-Compose $ComposeFile @("restart")
    Wait-Healthy
    Local-Status
}

function Local-Build {
    Write-Header "LOCAL: Build images only"
    Test-Docker
    Invoke-MavenBuild
    Invoke-Compose $ComposeFile @("build")
    Write-Ok "Images built"
}

function Local-Status {
    Write-Header "LOCAL: Status"
    Test-Docker
    Invoke-Compose $ComposeFile @("ps")
    Write-Host ""
    Write-Log "Service URLs:"
    Write-Host "  - Eureka       ->  http://localhost:8761"
    Write-Host "  - API Gateway  ->  http://localhost:8080"
    Write-Host "  - Nuxt Web     ->  http://localhost:3000"
    Write-Host "  - MinIO UI     ->  http://localhost:9001  (minioadmin / minioadmin)"
    Write-Host "  - Zipkin       ->  http://localhost:9411"
}

function Local-Clean {
    Write-Header "LOCAL: Clean (containers + volumes + images)"
    Test-Docker
    Write-Warn "This will DELETE all data volumes. Continue? [y/N]"
    $confirm = Read-Host
    if ($confirm -notmatch '^[Yy]$') { Write-Host "Aborted."; return }
    Invoke-Compose $ComposeFile @("down","-v","--rmi","local")
    Write-Ok "Cleaned"
}

function Wait-Healthy {
    Write-Log "Waiting for services to become healthy (max 180s)..."
    $target = (docker compose --project-directory $ProjectRoot -f $ComposeFile config --services 2>$null | Measure-Object).Count
    $elapsed = 0
    while ($elapsed -lt 180) {
        $healthy = 0
        try {
            $services = docker compose --project-directory $ProjectRoot -f $ComposeFile ps --format json 2>$null | ConvertFrom-Json -ErrorAction Stop
            $healthy = ($services | Where-Object { $_.Health -eq "healthy" }).Count
        } catch {}
        if ($healthy -ge $target -and $target -gt 0) {
            Write-Ok "All $target services healthy"
            return
        }
        Start-Sleep -Seconds 5
        $elapsed += 5
        Write-Host "." -NoNewline
    }
    Write-Host ""
    Write-Warn "Timeout: $healthy/$target services healthy after 180s"
    Write-Warn "Run '.\deploy.ps1 local logs' to inspect"
}

# ─── Prod mode ──────────────────────────────────────────────────────────────
function Prod-Up {
    Write-Header "PROD: Build + start (image tag: $($env:IMAGE_TAG))"
    Test-Docker
    Invoke-MavenBuild
    Invoke-Compose $ComposeProdFile @("up","-d")
    Prod-Status
}

function Prod-Down {
    Write-Header "PROD: Stop"
    Test-Docker
    Invoke-Compose $ComposeProdFile @("down")
    Write-Ok "Stopped"
}

function Prod-Push {
    Write-Header "PROD: Build + tag + push"
    Test-Docker
    Invoke-MavenBuild
    $pushReg = if ($Arg) { $Arg } else { $env:REGISTRY }
    if (-not $pushReg -or $pushReg -eq "reportsystem") {
        Write-Fail "Set REGISTRY env var or pass registry as argument, e.g. .\deploy.ps1 prod push ghcr.io/myorg"
    }

    Set-Location $ProjectRoot
    foreach ($svc in $Services) {
        $dockerfile = switch ($svc) {
            "eureka"   { "infrastructure/eureka/Dockerfile" }
            "gateway"  { "infrastructure/gateway/Dockerfile" }
            "nuxt-web" { "frontend/report-system-web/Dockerfile" }
            default    { "services/$svc/Dockerfile" }
        }
        $img = "$pushReg/${svc}:$($env:IMAGE_TAG)"
        Write-Log "Building $img"
        & docker build -t $img -f $dockerfile $ProjectRoot
        if ($LASTEXITCODE -ne 0) { Write-Fail "Build failed for $svc" }
    }

    Write-Log "Pushing images..."
    foreach ($svc in $Services) {
        $img = "$pushReg/${svc}:$($env:IMAGE_TAG)"
        & docker push $img
        if ($LASTEXITCODE -ne 0) { Write-Fail "Push failed for $img" }
    }
    Write-Ok "All images pushed to $pushReg"
    Write-Host ""
    Write-Host "On the target host, set REGISTRY and IMAGE_TAG, then run:"
    Write-Host "  \$env:REGISTRY=`"$pushReg`"; \$env:IMAGE_TAG=`"$($env:IMAGE_TAG)`"; .\deploy.ps1 prod up"
}

function Prod-Status {
    Write-Header "PROD: Status"
    Test-Docker
    Invoke-Compose $ComposeProdFile @("ps")
    Write-Host ""
    Write-Log "Image registry: $($env:REGISTRY)"
    Write-Log "Image tag:      $($env:IMAGE_TAG)"
}

# ─── Main dispatch ───────────────────────────────────────────────────────────
switch ($Mode) {
    "local" {
        switch ($Action) {
            "up"      { Local-Up }
            "down"    { Local-Down }
            "logs"    { Local-Logs }
            "restart" { Local-Restart }
            "build"   { Local-Build }
            "status"  { Local-Status }
            "clean"   { Local-Clean }
            default   { Write-Fail "Unknown local action: $Action  (try: up|down|logs|restart|build|status|clean)" }
        }
    }
    "prod" {
        switch ($Action) {
            "up"     { Prod-Up }
            "down"   { Prod-Down }
            "push"   { Prod-Push }
            "status" { Prod-Status }
            default  { Write-Fail "Unknown prod action: $Action  (try: up|down|push|status)" }
        }
    }
    default { Write-Fail "Unknown mode: $Mode  (try: local|prod)" }
}
