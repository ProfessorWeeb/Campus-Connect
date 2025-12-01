@echo off
echo ========================================
echo Creating Campus Connect Database
echo ========================================
echo.

echo Attempting to create database 'campusconnect'...
echo You will be prompted for the PostgreSQL password.
echo.

"C:\Program Files\pgsql\bin\psql.exe" -U postgres -c "CREATE DATABASE campusconnect;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Database 'campusconnect' created successfully!
    echo.
    echo Next steps:
    echo 1. Update password in: backend\src\main\resources\application.properties
    echo 2. Restart the backend server
) else (
    echo.
    echo Database creation failed. Possible reasons:
    echo - PostgreSQL service is not running
    echo - Wrong password entered
    echo - Database already exists
    echo.
    echo To check if database exists:
    echo   "C:\Program Files\pgsql\bin\psql.exe" -U postgres -l
    echo.
)

pause

