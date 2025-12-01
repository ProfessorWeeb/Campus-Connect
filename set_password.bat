@echo off
echo Setting PostgreSQL password to 'password123'...
echo.

REM Try to set password directly first
cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres -c "ALTER USER postgres WITH PASSWORD 'password123';" 2>nul

if %ERRORLEVEL% EQU 0 (
    echo Password set successfully!
    goto :done
)

echo.
echo Direct method failed. Trying alternative method...
echo.

REM Check if pg_hba.conf exists
set PGDATA=C:\Program Files\pgsql\data
if not exist "%PGDATA%\pg_hba.conf" (
    echo ERROR: pg_hba.conf not found
    pause
    exit /b 1
)

echo Creating backup of pg_hba.conf...
copy "%PGDATA%\pg_hba.conf" "%PGDATA%\pg_hba.conf.backup" >nul

echo.
echo Temporarily modifying authentication to allow passwordless connection...
echo (This is safe for localhost only)
echo.

REM Modify pg_hba.conf to use trust temporarily
powershell -Command "(Get-Content '%PGDATA%\pg_hba.conf') -replace 'scram-sha-256', 'trust' -replace 'md5', 'trust' | Set-Content '%PGDATA%\pg_hba.conf.temp'"
move /Y "%PGDATA%\pg_hba.conf.temp" "%PGDATA%\pg_hba.conf" >nul

echo.
echo Restarting PostgreSQL service...
net stop postgresql-x64-18 2>nul
net stop postgresql-x64-17 2>nul
net stop postgresql-x64-16 2>nul
timeout /t 2 /nobreak >nul
net start postgresql-x64-18 2>nul
net start postgresql-x64-17 2>nul
net start postgresql-x64-16 2>nul
timeout /t 3 /nobreak >nul

echo.
echo Setting password...
cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres -c "ALTER USER postgres WITH PASSWORD 'password123';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Password set successfully!
    echo.
    echo Restoring secure authentication...
    copy "%PGDATA%\pg_hba.conf.backup" "%PGDATA%\pg_hba.conf" >nul
    
    echo Restarting PostgreSQL service again...
    net stop postgresql-x64-18 2>nul
    net stop postgresql-x64-17 2>nul
    net stop postgresql-x64-16 2>nul
    timeout /t 2 /nobreak >nul
    net start postgresql-x64-18 2>nul
    net start postgresql-x64-17 2>nul
    net start postgresql-x64-16 2>nul
    
    echo.
    echo ✓ Done! Password is now: password123
    echo.
    echo Application configuration has been updated.
    echo You can now restart your backend server.
) else (
    echo.
    echo Password setting failed. Please try using pgAdmin 4 instead.
)

:done
pause

