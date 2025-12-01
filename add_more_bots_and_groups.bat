@echo off
SET "BACKEND_URL=http://localhost:8080"
SET "BOT_COUNT=150"
SET "GROUP_COUNT=25"

echo ========================================
echo Adding More Bots and Creating CSCI Groups
echo ========================================
echo.

echo Checking if backend is running on %BACKEND_URL%...
powershell -Command "try { Invoke-WebRequest -Uri '%BACKEND_URL%/api/admin/bots/count' -Method GET -TimeoutSec 5 -ErrorAction Stop | Out-Null; Write-Host 'Backend is running.' } catch { Write-Host 'Error: Backend is not running or unreachable on %BACKEND_URL%. Please start the backend first.'; exit 1 }"
IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Backend not running. Aborting.
    pause
    exit /b 1
)

echo.
echo Step 1: Adding %BOT_COUNT% more bot users...
powershell -Command "try { $response = Invoke-WebRequest -Uri '%BACKEND_URL%/api/admin/bots/add-more?count=%BOT_COUNT%' -Method POST -ErrorAction Stop; Write-Host 'Success: ' $response.Content } catch { $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream()); $responseBody = $reader.ReadToEnd(); Write-Host 'Error: ' $responseBody; exit 1 }"

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Failed to add more bots. See error above.
    pause
    exit /b 1
)

echo.
echo Step 2: Creating %GROUP_COUNT% CSCI groups (Public, Open Join)...
powershell -Command "try { $response = Invoke-WebRequest -Uri '%BACKEND_URL%/api/admin/bots/create-csci-groups?count=%GROUP_COUNT%' -Method POST -ErrorAction Stop; Write-Host 'Success: ' $response.Content } catch { $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream()); $responseBody = $reader.ReadToEnd(); Write-Host 'Error: ' $responseBody; exit 1 }"

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Failed to create CSCI groups. See error above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Process completed successfully!
echo ========================================
echo.
echo Added %BOT_COUNT% more bot users
echo Created %GROUP_COUNT% CSCI groups (Public, Open Join)
echo.
echo You can now check your frontend to see the new bots and groups.
pause

