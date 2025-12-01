@echo off
echo ========================================
echo Starting PostgreSQL Service
echo ========================================
echo.

echo Attempting to start PostgreSQL service...
echo.

REM Try different PostgreSQL service names
net start postgresql-x64-18 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 18 started successfully!
    goto :done
)

net start postgresql-x64-17 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 17 started successfully!
    goto :done
)

net start postgresql-x64-16 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 16 started successfully!
    goto :done
)

net start postgresql-x64-15 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL 15 started successfully!
    goto :done
)

echo.
echo Could not find PostgreSQL service automatically.
echo.
echo Please start it manually:
echo 1. Press Win+R, type: services.msc
echo 2. Find "postgresql" service
echo 3. Right-click and select "Start"
echo.
echo Or try:
echo    net start postgresql-x64-XX
echo (Replace XX with your version number)
echo.

:done
echo.
echo Verifying PostgreSQL is running...
cd /d "%~dp0pgsql\bin"
if exist pg_isready.exe (
    pg_isready.exe -U postgres 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✓ PostgreSQL is ready!
    ) else (
        echo ✗ PostgreSQL is not responding
    )
) else (
    echo ✗ Could not find pg_isready.exe in pgsql\bin folder
    echo Please check PostgreSQL installation path
)

pause

