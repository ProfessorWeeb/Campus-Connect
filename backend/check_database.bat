@echo off
echo Checking PostgreSQL connection...
echo.

cd "C:\Program Files\pgsql\bin"
echo Testing connection to PostgreSQL...
psql.exe -U postgres -d campusconnect -c "SELECT 1;" 2>&1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Database connection successful!
) else (
    echo.
    echo ✗ Database connection failed!
    echo.
    echo Possible issues:
    echo 1. Database 'campusconnect' doesn't exist
    echo 2. PostgreSQL service is not running
    echo 3. Wrong password
    echo.
    echo Creating database if it doesn't exist...
    psql.exe -U postgres -c "CREATE DATABASE campusconnect;" 2>&1
)

pause

