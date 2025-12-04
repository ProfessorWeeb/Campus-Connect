@echo off
REM Script to package backend for Elastic Beanstalk deployment

echo ========================================
echo Packaging Backend for Elastic Beanstalk
echo ========================================
echo.

cd /d "%~dp0backend"

echo Creating ZIP file...
echo.

REM Create ZIP excluding target and git folders
powershell -Command "Get-ChildItem -Path . -Exclude target,.git,*.zip | Compress-Archive -DestinationPath campusconnect-backend.zip -Force"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to create ZIP file
    echo Make sure PowerShell is available
    pause
    exit /b 1
)

echo.
echo ========================================
echo Package created successfully!
echo ========================================
echo.
echo File: backend\campusconnect-backend.zip
echo.
echo Next steps:
echo 1. Go to AWS Console -^> Elastic Beanstalk
echo 2. Create new application
echo 3. Upload campusconnect-backend.zip
echo 4. See DEPLOY_BACKEND_CONSOLE.md for detailed instructions
echo.
pause

