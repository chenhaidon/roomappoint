@echo off
setlocal

set ROOT=%~dp0
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%scripts\start.ps1"

echo.
echo Started. Logs are under .run\logs
pause
