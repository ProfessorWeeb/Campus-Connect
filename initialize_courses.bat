@echo off
echo ========================================
echo Initializing Courses in Backend
echo ========================================
echo.

echo Checking if backend is running...
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ Backend is NOT running on port 8080
    echo   Please start the backend first using: start_backend.bat
    echo   Or: cd backend ^&^& mvnw.cmd spring-boot:run
    echo.
    pause
    exit /b 1
)

echo ✓ Backend is running
echo.
echo Initializing courses...
echo.

powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/courses/initialize' -Method POST -ErrorAction Stop; Write-Host ''; Write-Host '✓ SUCCESS! Courses initialized!' -ForegroundColor Green; Write-Host ''; Write-Host 'Response:' $response.Content; Write-Host '' } catch { Write-Host ''; Write-Host '✗ ERROR: Failed to initialize courses' -ForegroundColor Red; Write-Host 'Error:' $_.Exception.Message -ForegroundColor Yellow; if ($_.Exception.Response) { Write-Host 'Status Code:' $_.Exception.Response.StatusCode.value__ }; Write-Host ''; Write-Host 'Make sure:'; Write-Host '1. Backend is running on http://localhost:8080'; Write-Host '2. Backend has been restarted after recent changes'; Write-Host '' }"

echo.
pause

