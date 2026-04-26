@echo off
setlocal EnableExtensions EnableDelayedExpansion

set ROOT=%~dp0
set RUNDIR=%ROOT%.run

echo Stopping project processes...

if exist "%RUNDIR%\frontend.pid" (
  for /f "usebackq delims=" %%p in ("%RUNDIR%\frontend.pid") do (
    taskkill //PID %%p //T //F >nul 2>&1
    echo Tried stop frontend PID %%p
  )
  del /f /q "%RUNDIR%\frontend.pid" >nul 2>&1
)

if exist "%RUNDIR%\backend.pid" (
  for /f "usebackq delims=" %%p in ("%RUNDIR%\backend.pid") do (
    taskkill //PID %%p //T //F >nul 2>&1
    echo Tried stop backend PID %%p
  )
  del /f /q "%RUNDIR%\backend.pid" >nul 2>&1
)

call :KillPort 7245
call :KillPort 8080
call :KillPort 8081

echo.
echo Stopped.
pause
exit /b 0

:KillPort
set PORT=%~1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING" /C:":%PORT% .*侦听"') do (
  if not "%%a"=="0" (
    taskkill //PID %%a //T //F >nul 2>&1
    echo Killed PID %%a on port %PORT%
  )
)
goto :eof
