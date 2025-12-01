@echo off
echo ========================================
echo Backend Startup Diagnostic
echo ========================================
echo.

echo [1] Checking if PostgreSQL service is running...
sc query | findstr /i postgres >nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ PostgreSQL service found
    sc query | findstr /i "postgres.*RUNNING" >nul
    if %ERRORLEVEL% EQU 0 (
        echo ✓ PostgreSQL service is RUNNING
    ) else (
        echo ✗ PostgreSQL service is NOT running
        echo   Please start it using start_postgres_service.bat
    )
) else (
    echo ✗ PostgreSQL service not found
)

echo.
echo [2] Checking if database 'campusconnect' exists...
cd /d "%~dp0pgsql\bin"
if exist psql.exe (
    echo SELECT 1 FROM pg_database WHERE datname='campusconnect'; | psql.exe -U postgres -d postgres -t 2>nul | findstr "1" >nul
    if %ERRORLEVEL% EQU 0 (
        echo ✓ Database 'campusconnect' exists
    ) else (
        echo ✗ Database 'campusconnect' does NOT exist
        echo   Run: create_database.bat
    )
) else (
    echo ⚠ Could not find psql.exe to check database
)

echo.
echo [3] Testing database connection...
cd /d "%~dp0pgsql\bin"
if exist psql.exe (
    echo SELECT version(); | psql.exe -U postgres -d campusconnect -t 2>nul >nul
    if %ERRORLEVEL% EQU 0 (
        echo ✓ Can connect to database with password 'password123'
    ) else (
        echo ✗ Cannot connect to database
        echo   Check password in application.properties
    )
) else (
    echo ⚠ Could not find psql.exe to test connection
)

echo.
echo [4] Checking if port 8080 is available...
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% EQU 0 (
    echo ✗ Port 8080 is already in use!
    echo   Another application is using this port
) else (
    echo ✓ Port 8080 is available
)

echo.
echo ========================================
echo Diagnostic Complete
echo ========================================
echo.
echo Next steps:
echo 1. Make sure PostgreSQL service is running
echo 2. Make sure database 'campusconnect' exists
echo 3. Verify password in application.properties matches PostgreSQL password
echo 4. Try starting backend again: start_backend.bat
echo.
pause

