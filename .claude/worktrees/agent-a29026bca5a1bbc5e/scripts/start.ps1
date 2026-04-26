param(
  [switch]$SkipBuild,
  [switch]$SkipNpmInstall
)

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runDir = Join-Path $root '.run'
$logDir = Join-Path $runDir 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

function Write-PidFile([string]$name, [int]$processId) {
  Set-Content -Path (Join-Path $runDir "$name.pid") -Value $processId -Encoding ascii
}

function Start-Backend {
  $backendDir = Join-Path $root 'RoomAppoint.springboot'
  $jar = Join-Path $backendDir 'target\web-0.0.1-SNAPSHOT-exec.jar'

  if (-not $SkipBuild) {
    Push-Location $backendDir
    try {
      # Prefer system mvn. If you want to use wrapper, ensure JAVA_HOME is set correctly.
      mvn -DskipTests package
    } finally {
      Pop-Location
    }
  }

  if (-not (Test-Path $jar)) {
    throw "Backend jar not found: $jar"
  }

  $out = Join-Path $logDir 'backend.out.log'
  $err = Join-Path $logDir 'backend.err.log'

  $p = Start-Process -FilePath 'java' -ArgumentList @('-jar', $jar) -WorkingDirectory $root -PassThru -RedirectStandardOutput $out -RedirectStandardError $err
  Write-PidFile 'backend' $p.Id
  "Backend started. PID=$($p.Id)" | Write-Host
}

function Start-Frontend {
  $frontDir = Join-Path $root 'RoomAppoint.elementui'

  if (-not (Test-Path (Join-Path $frontDir 'package.json'))) {
    throw "Frontend directory not found: $frontDir"
  }

  if (-not $SkipNpmInstall) {
    $nm = Join-Path $frontDir 'node_modules'
    if (-not (Test-Path $nm)) {
      Push-Location $frontDir
      try {
        npm install
      } finally {
        Pop-Location
      }
    }
  }

  $out = Join-Path $logDir 'frontend.out.log'
  $err = Join-Path $logDir 'frontend.err.log'

  $p = Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c', 'npm', 'run', 'serve') -WorkingDirectory $frontDir -PassThru -RedirectStandardOutput $out -RedirectStandardError $err
  Write-PidFile 'frontend' $p.Id
  "Frontend started. PID=$($p.Id)" | Write-Host
}

Start-Backend
Start-Frontend

"Logs: $logDir" | Write-Host
