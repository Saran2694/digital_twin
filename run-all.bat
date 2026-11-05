@echo off
echo Starting Digital Twin Monitoring System...
start "Backend API" cmd /c "%~dp0run-backend.bat"
timeout /t 5 /nobreak >nul
start "Frontend Web" cmd /c "%~dp0run-frontend.bat"
echo.
echo Both Backend and Frontend are starting in separate windows.
echo Once loaded, open your browser at: http://localhost:5173
echo.
pause

