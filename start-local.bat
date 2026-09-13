@echo off
title Online Exam & Proctoring System - Local Launcher
echo ========================================================
echo   Starting Online Examination and Proctoring System
echo ========================================================

set BASE_DIR=%~dp0

echo [1/2] Starting Spring Boot Backend on http://localhost:8080 ...
start "Backend - Spring Boot (Port 8080)" cmd /k "cd /d "%BASE_DIR%backend" && java -jar target\proctor-0.0.1-SNAPSHOT.jar"

timeout /t 5 /nobreak >nul

echo [2/2] Starting Frontend on http://localhost:3000 ...
start "Frontend - Vite (Port 3000)" cmd /k "cd /d "%BASE_DIR%frontend" && npm run dev"

echo.
echo ========================================================
echo   Both services launched successfully!
echo   Frontend: http://localhost:3000
echo   Backend:  http://localhost:8080
echo   H2 Console: http://localhost:8080/h2-console
echo ========================================================
pause
