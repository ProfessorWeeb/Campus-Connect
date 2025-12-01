@echo off
echo ========================================
echo PostgreSQL Password Reset Tool
echo ========================================
echo.

echo This script will help you reset the PostgreSQL password.
echo.

echo Step 1: We need to temporarily allow local connections without password.
echo This is safe for localhost only.
echo.

pause

echo.
echo Step 2: Locating pg_hba.conf file...
set PGDATA=C:\Program Files\pgsql\data
if not exist "%PGDATA%\pg_hba.conf" (
    echo ERROR: pg_hba.conf not found at %PGDATA%
    echo Please check your PostgreSQL data directory.
    pause
    exit /b 1
)

echo Found: %PGDATA%\pg_hba.conf
echo.

echo Step 3: Creating backup of pg_hba.conf...
copy "%PGDATA%\pg_hba.conf" "%PGDATA%\pg_hba.conf.backup" >nul
echo Backup created.

echo.
echo Step 4: Modifying pg_hba.conf to allow local connections...
echo This will temporarily allow passwordless local connections.
echo.

REM Create a temporary file with modified settings
(
echo # TYPE  DATABASE        USER            ADDRESS                 METHOD
echo # IPv4 local connections:
echo host    all             all             127.0.0.1/32            trust
echo # IPv6 local connections:
echo host    all             all             ::1/128                 trust
echo # "local" is for Unix domain socket connections only
echo local   all             all                                     trust
) > "%PGDATA%\pg_hba.conf.temp"

echo.
echo IMPORTANT: You need to restart PostgreSQL service for changes to take effect.
echo.
echo Would you like to:
echo [1] Restart PostgreSQL service automatically (requires admin)
echo [2] I will restart it manually
echo.
set /p choice="Enter choice (1 or 2): "

if "%choice%"=="1" (
    echo.
    echo Stopping PostgreSQL service...
    net stop postgresql-x64-18 2>nul
    net stop postgresql-x64-17 2>nul
    net stop postgresql-x64-16 2>nul
    
    echo Copying modified pg_hba.conf...
    copy "%PGDATA%\pg_hba.conf.temp" "%PGDATA%\pg_hba.conf" >nul
    
    echo Starting PostgreSQL service...
    net start postgresql-x64-18 2>nul
    net start postgresql-x64-17 2>nul
    net start postgresql-x64-16 2>nul
    
    timeout /t 3 /nobreak >nul
) else (
    echo.
    echo Please manually:
    echo 1. Stop PostgreSQL service
    echo 2. Copy: %PGDATA%\pg_hba.conf.temp to %PGDATA%\pg_hba.conf
    echo 3. Start PostgreSQL service
    echo.
    pause
)

echo.
echo Step 5: Resetting password...
echo.
set /p newpass="Enter new password for 'postgres' user: "
set /p confirm="Confirm password: "

if not "%newpass%"=="%confirm%" (
    echo Passwords do not match!
    pause
    exit /b 1
)

cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres -c "ALTER USER postgres WITH PASSWORD '%newpass%';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Password reset successfully!
    echo.
    echo Step 6: Restoring secure pg_hba.conf...
    copy "%PGDATA%\pg_hba.conf.backup" "%PGDATA%\pg_hba.conf" >nul
    
    echo.
    echo IMPORTANT: Restart PostgreSQL service again to apply secure settings.
    echo.
    echo Your new password is: %newpass%
    echo.
    echo Update this in: backend\src\main\resources\application.properties
    echo Change line 7 to: spring.datasource.password=%newpass%
    echo.
) else (
    echo.
    echo Password reset failed. Please try again.
    echo.
)

del "%PGDATA%\pg_hba.conf.temp" >nul 2>&1
pause

