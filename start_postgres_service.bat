@echo off
echo ========================================
echo Starting PostgreSQL Service
echo ========================================
echo.

REM Try to start PostgreSQL service (requires admin privileges)
echo Attempting to start PostgreSQL service...
echo.

REM Try different PostgreSQL service names
net start postgresql-x64-18 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 18 service started successfully!
    goto :verify
)

net start postgresql-x64-17 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 17 service started successfully!
    goto :verify
)

net start postgresql-x64-16 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 16 service started successfully!
    goto :verify
)

net start postgresql-x64-15 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 15 service started successfully!
    goto :verify
)

echo.
echo ✗ Could not start PostgreSQL service automatically.
echo.
echo Please start it manually:
echo 1. Press Win+R, type: services.msc
echo 2. Find "postgresql" service (might be postgresql-x64-18, 17, 16, etc.)
echo 3. Right-click and select "Start"
echo.
echo Or run this command as Administrator:
echo    net start postgresql-x64-18
echo.
goto :end

:verify
echo.
echo Verifying PostgreSQL is running...
cd /d "%~dp0pgsql\bin"
if exist pg_isready.exe (
    pg_isready.exe -U postgres 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✓ PostgreSQL is ready and accepting connections!
    ) else (
        echo ⚠ PostgreSQL service started but not responding yet.
        echo   Wait a few seconds and try again.
    )
) else (
    echo ⚠ Could not find pg_isready.exe to verify
    echo   But the service should be running.
)

:end
echo.
pause

