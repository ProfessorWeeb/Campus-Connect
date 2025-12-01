# How to Start PostgreSQL

## Method 1: Using Services (Easiest)

1. **Press `Win + R`**
2. **Type:** `services.msc` and press Enter
3. **Find** "postgresql" service (might be named like `postgresql-x64-18`)
4. **Right-click** on it
5. **Select** "Start"

## Method 2: Using Command Line

Run the script I created:
```cmd
start_postgresql.bat
```

Or manually:
```cmd
net start postgresql-x64-18
```
(Version number may vary - try 17, 16, 15, etc.)

## Method 3: Check Service Name First

To find the exact service name:
```cmd
sc query | findstr postgres
```

Then start it:
```cmd
net start [service-name-from-above]
```

## Verify It's Running

After starting, verify:
```cmd
cd "C:\Program Files\pgsql\bin"
pg_isready.exe -U postgres
```

Should show: `localhost:5432 - accepting connections`

## Quick Alternative: Use H2 Instead

If you just want to test the application quickly, you can use H2 (in-memory database) instead:

```cmd
cd backend
run_with_h2.bat
```

This doesn't require PostgreSQL at all!

## After Starting PostgreSQL

1. **Create the database** (if not already created):
   ```cmd
   cd "C:\Program Files\pgsql\bin"
   psql.exe -U postgres -c "CREATE DATABASE campusconnect;"
   ```
   (Password: `password123`)

2. **Start your backend**:
   ```cmd
   cd backend
   mvnw.cmd spring-boot:run
   ```

