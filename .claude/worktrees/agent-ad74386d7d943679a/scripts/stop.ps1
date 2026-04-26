param(
  [switch]$ForcePortKill
)

$ErrorActionPreference = 'SilentlyContinue'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runDir = Join-Path $root '.run'

function Stop-ByPidFile([string]$name) {
  $pidFile = Join-Path $runDir "$name.pid"
  if (-not (Test-Path $pidFile)) {
    return
  }

  $pid = (Get-Content $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
  if ($pid -match '^\d+$') {
    Stop-Process -Id ([int]$pid) -Force -ErrorAction SilentlyContinue
    "Stopped $name. PID=$pid" | Write-Host
  }

  Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
}

function Stop-ByPortNetTcp([int]$port) {
  $conns = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
  foreach ($c in $conns) {
    Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
    "Killed process on port $port. PID=$($c.OwningProcess)" | Write-Host
  }
}

function Stop-ByPortNetstat([int]$port) {
  $lines = netstat -ano | Select-String ":$port" | ForEach-Object { $_.Line }
  foreach ($line in $lines) {
    if ($line -match 'LISTENING\s+(\d+)$') {
      $pid = [int]$Matches[1]
      Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
      "Killed process on port $port. PID=$pid" | Write-Host
    }
  }
}

function Stop-ByPort([int]$port) {
  # Prefer Get-NetTCPConnection, but fall back to netstat parsing
  try {
    Stop-ByPortNetTcp $port
  } catch {
    Stop-ByPortNetstat $port
  }
}

Stop-ByPidFile 'frontend'
Stop-ByPidFile 'backend'

if ($ForcePortKill) {
  Stop-ByPort 7245
  Stop-ByPort 8080
  Stop-ByPort 8081
}
