@echo off
echo ========================================
echo Campus Connect - Password Reset Utility
echo ========================================
echo.
echo This script will reset the password for: daniel.underwood@mga.edu
echo Default password will be: password123
echo.
set /p CONFIRM="Continue? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Cancelled.
    pause
    exit /b
)
echo.
echo Resetting password...
echo.

cd /d "%~dp0"

REM BCrypt hash for "password123" (strength 10)
REM This hash was generated using Spring Security's BCryptPasswordEncoder
set "PASSWORD_HASH=$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

pgsql\bin\psql.exe -U postgres -d campusconnect -c "UPDATE users SET password = '%PASSWORD_HASH%' WHERE email = 'daniel.underwood@mga.edu';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Password reset successful!
    echo ========================================
    echo Email: daniel.underwood@mga.edu
    echo Password: password123
    echo.
    echo You can now login with these credentials.
    echo.
) else (
    echo.
    echo ========================================
    echo Password reset failed!
    echo ========================================
    echo Please check:
    echo 1. PostgreSQL is running
    echo 2. Database 'campusconnect' exists
    echo 3. User exists in the database
    echo 4. PostgreSQL password is correct
    echo.
    echo You can also manually run:
    echo psql -U postgres -d campusconnect
    echo Then execute:
    echo UPDATE users SET password = '%PASSWORD_HASH%' WHERE email = 'daniel.underwood@mga.edu';
)

pause

