@echo off
echo ========================================
echo PostgreSQL Setup Script for Campus Connect
echo ========================================
echo.

echo Step 1: Checking if PostgreSQL is installed...
where psql >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo PostgreSQL found in PATH!
    psql --version
) else (
    echo PostgreSQL not found in PATH.
    echo Please install PostgreSQL first.
    echo Download from: https://www.postgresql.org/download/windows/
    pause
    exit /b 1
)

echo.
echo Step 2: Checking PostgreSQL service...
sc query postgresql-x64-16 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo PostgreSQL service found.
    sc query postgresql-x64-16 | findstr "RUNNING" >nul
    if %ERRORLEVEL% EQU 0 (
        echo PostgreSQL service is RUNNING.
    ) else (
        echo Starting PostgreSQL service...
        net start postgresql-x64-16
    )
) else (
    echo PostgreSQL service not found. Checking other versions...
    sc query postgresql-x64-17 >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        sc query postgresql-x64-17 | findstr "RUNNING" >nul
        if %ERRORLEVEL% EQU 0 (
            echo PostgreSQL service is RUNNING.
        ) else (
            echo Starting PostgreSQL service...
            net start postgresql-x64-17
        )
    ) else (
        echo PostgreSQL service not found. Please check installation.
    )
)

echo.
echo Step 3: Creating database 'campusconnect'...
echo Please enter your PostgreSQL password when prompted:
psql -U postgres -c "CREATE DATABASE campusconnect;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Database created successfully!
) else (
    echo.
    echo Could not create database automatically.
    echo Please create it manually:
    echo   1. Open pgAdmin 4
    echo   2. Connect to PostgreSQL
    echo   3. Right-click Databases -^> Create -^> Database
    echo   4. Name: campusconnect
    echo.
)

echo.
echo Step 4: Update application.properties
echo Please edit: backend\src\main\resources\application.properties
echo Update line 7 with your PostgreSQL password:
echo    spring.datasource.password=YOUR_PASSWORD
echo.

echo Setup complete!
echo.
echo Next steps:
echo 1. Update password in application.properties
echo 2. Restart the backend server
echo.
pause

