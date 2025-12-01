@echo off
echo ========================================
echo Starting Campus Connect Backend
echo ========================================
echo.

cd /d "%~dp0backend"

echo Checking if port 8080 is available...
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠ Port 8080 is already in use!
    echo Please stop the application using port 8080 first.
    echo.
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot backend...
echo (This may take 30-60 seconds on first run)
echo.
echo Press Ctrl+C to stop the backend
echo.

mvnw.cmd spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo Backend failed to start!
    echo ========================================
    echo.
    echo Common issues:
    echo 1. PostgreSQL is not running - Start it first
    echo 2. Database connection error - Check application.properties
    echo 3. Port 8080 is in use - Stop other applications
    echo 4. Compilation errors - Check the error messages above
    echo.
    pause
)

