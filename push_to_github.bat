@echo off
echo ========================================
echo Pushing Campus Connect to GitHub
echo ========================================
echo.

REM Remove old remote if it exists
git remote remove origin 2>nul

REM Add the correct remote
git remote add origin https://github.com/ProfessorWeeb/Campus-Connect.git

echo.
echo Remote configured. Now pushing to GitHub...
echo.

REM Push to GitHub
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Successfully pushed to GitHub!
    echo Repository: https://github.com/ProfessorWeeb/Campus-Connect
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Push failed!
    echo ========================================
    echo.
    echo Common issues:
    echo 1. Authentication required - You may need to enter your GitHub credentials
    echo 2. If you have 2FA enabled, use a Personal Access Token instead of password
    echo 3. Make sure the repository exists at: https://github.com/ProfessorWeeb/Campus-Connect
    echo.
)

pause

