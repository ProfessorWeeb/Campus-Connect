@echo off
echo ========================================
echo Seeding Bots with 25 Groups
echo ========================================
echo.

echo Checking if backend is running...
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Backend is NOT running on port 8080
    echo   Please start the backend first using: start_backend.bat
    echo.
    pause
    exit /b 1
)

echo ✓ Backend is running
echo.
echo Seeding 100 bots with 25 groups...
echo.

powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/admin/bots/seed?count=100&groups=25' -Method POST -ErrorAction Stop; Write-Host ''; Write-Host '✓ SUCCESS! Bots seeded successfully!' -ForegroundColor Green; Write-Host ''; $json = $response.Content | ConvertFrom-Json; Write-Host 'Bots Created:' $json.botsCreated; Write-Host 'Groups Created:' $json.groupsCreated; Write-Host ''; $json | ConvertTo-Json -Depth 3 } catch { Write-Host ''; Write-Host '✗ ERROR: Failed to seed bots' -ForegroundColor Red; Write-Host 'Error Details:' $_.Exception.Message -ForegroundColor Yellow; if ($_.Exception.Response) { Write-Host 'Status Code:' $_.Exception.Response.StatusCode.value__ }; Write-Host ''; Write-Host 'Troubleshooting:'; Write-Host '1. Make sure backend is running: start_backend.bat'; Write-Host '2. Make sure backend was restarted after security config changes'; Write-Host '3. Check backend logs for errors'; Write-Host '' }"

echo.
pause

