@echo off
SET "BACKEND_URL=http://localhost:8080"
SET "GROUP_NAME=CSCI 4463 - Review Session"

echo ========================================
echo Fixing CSCI 4463 Group
echo ========================================
echo.

echo Checking if backend is running...
powershell -Command "try { Invoke-WebRequest -Uri '%BACKEND_URL%/api/groups' -Method GET -TimeoutSec 5 -ErrorAction Stop | Out-Null; Write-Host 'Backend is running.' } catch { Write-Host 'Error: Backend is not running. Please start the backend first.'; exit 1 }"
IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Backend not running. Aborting.
    pause
    exit /b 1
)

echo.
echo Fixing group: %GROUP_NAME%
echo   - Removing one member
echo   - Changing to open join (not invite-only)
echo.

powershell -Command "try { $response = Invoke-WebRequest -Uri '%BACKEND_URL%/api/groups/admin/fix-group?groupName=%GROUP_NAME%&removeOneMember=true&setOpenJoin=true' -Method POST -ErrorAction Stop; Write-Host 'Success!' -ForegroundColor Green; $json = $response.Content | ConvertFrom-Json; Write-Host ''; Write-Host 'Group:' $json.groupName; Write-Host 'Group ID:' $json.groupId; Write-Host 'Current Members:' $json.currentMembers; Write-Host 'Requires Invite:' $json.requiresInvite; Write-Host ''; Write-Host 'Actions taken:'; $json.actions | ForEach-Object { Write-Host '  -' $_ }; if ($json.memberRemoved) { Write-Host 'Member Removed:' $json.memberRemoved } } catch { Write-Host 'Error:' $_.Exception.Message -ForegroundColor Red; if ($_.Exception.Response) { $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream()); $responseBody = $reader.ReadToEnd(); Write-Host 'Response:' $responseBody } }"

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo Failed to fix group. See error above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Group fixed successfully!
echo ========================================
pause
