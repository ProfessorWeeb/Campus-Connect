@echo off
echo ========================================
echo Testing Backend Connection
echo ========================================
echo.

echo [1] Checking if backend is running on port 8080...
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Backend is running on port 8080
) else (
    echo ✗ Backend is NOT running on port 8080
    echo   Please start the backend first
    goto :end
)

echo.
echo [2] Testing public endpoint (should work without auth)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/register' -Method GET -ErrorAction Stop; Write-Host '✓ Public endpoint accessible' } catch { Write-Host '✗ Cannot access backend: ' $_.Exception.Message }"

echo.
echo [3] Testing admin endpoint (should work without auth)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/admin/bots/count' -Method GET -ErrorAction Stop; Write-Host '✓ Admin endpoint accessible'; Write-Host 'Response:' $response.Content } catch { Write-Host '✗ Admin endpoint blocked: ' $_.Exception.Message }"

echo.
echo [4] Testing courses endpoint (should work without auth)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/courses' -Method GET -ErrorAction Stop; Write-Host '✓ Courses endpoint accessible' } catch { Write-Host '✗ Courses endpoint blocked: ' $_.Exception.Message }"

:end
echo.
echo ========================================
echo Test Complete
echo ========================================
pause

