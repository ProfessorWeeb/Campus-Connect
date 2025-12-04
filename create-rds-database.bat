@echo off
REM Script to create RDS PostgreSQL database using AWS CLI
REM Make sure AWS CLI is installed and configured first

echo ========================================
echo CampusConnect RDS Database Setup
echo ========================================
echo.

REM Check if AWS CLI is installed
where aws >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: AWS CLI is not installed or not in PATH
    echo Please install AWS CLI from: https://aws.amazon.com/cli/
    pause
    exit /b 1
)

echo AWS CLI found!
echo.

REM Get user input
set /p DB_PASSWORD="Enter master database password (min 8 chars): "
set /p DB_USERNAME="Enter master username [postgres]: "
if "%DB_USERNAME%"=="" set DB_USERNAME=postgres

set /p REGION="Enter AWS region [us-east-1]: "
if "%REGION%"=="" set REGION=us-east-1

echo.
echo Creating RDS database instance...
echo This will take 5-10 minutes...
echo.

REM Create RDS instance
aws rds create-db-instance ^
    --db-instance-identifier campusconnect-db ^
    --db-instance-class db.t3.micro ^
    --engine postgres ^
    --engine-version 16.1 ^
    --master-username %DB_USERNAME% ^
    --master-user-password %DB_PASSWORD% ^
    --allocated-storage 20 ^
    --storage-type gp2 ^
    --db-name campusconnect ^
    --publicly-accessible ^
    --backup-retention-period 7 ^
    --storage-encrypted ^
    --region %REGION%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to create RDS instance
    echo Check your AWS credentials and permissions
    pause
    exit /b 1
)

echo.
echo ========================================
echo Database creation initiated!
echo ========================================
echo.
echo Waiting for database to be available...
echo This may take 5-10 minutes...
echo.

REM Wait for database to be available
aws rds wait db-instance-available --db-instance-identifier campusconnect-db --region %REGION%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Database creation failed or timed out
    pause
    exit /b 1
)

echo.
echo ========================================
echo Database is now available!
echo ========================================
echo.

REM Get database endpoint
echo Getting database endpoint...
for /f "tokens=*" %%i in ('aws rds describe-db-instances --db-instance-identifier campusconnect-db --region %REGION% --query "DBInstances[0].Endpoint.Address" --output text') do set DB_ENDPOINT=%%i

for /f "tokens=*" %%i in ('aws rds describe-db-instances --db-instance-identifier campusconnect-db --region %REGION% --query "DBInstances[0].Endpoint.Port" --output text') do set DB_PORT=%%i

echo.
echo ========================================
echo Database Information:
echo ========================================
echo Endpoint: %DB_ENDPOINT%
echo Port: %DB_PORT%
echo Database Name: campusconnect
echo Username: %DB_USERNAME%
echo.
echo IMPORTANT: Save this information!
echo.
echo Next steps:
echo 1. Update backend/src/main/resources/application-prod.properties
echo 2. Configure security group to allow backend access
echo.
pause

