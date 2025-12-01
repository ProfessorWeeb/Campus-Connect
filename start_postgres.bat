@echo off
echo Starting PostgreSQL Server...
cd /d "%~dp0pgsql\bin"
start "PostgreSQL Server" cmd /k "postgres.exe -D ..\data"
echo.
echo PostgreSQL server is starting in a new window.
echo Wait a few seconds for it to be ready.
echo.
pause

