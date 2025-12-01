# PostgreSQL Installation Locations

## Default Installation Paths (Windows)

### PostgreSQL Server Files
**Standard Installation:**
- `C:\Program Files\PostgreSQL\16\` (or version number)
- `C:\Program Files\PostgreSQL\17\` (if version 17)

**Components inside:**
- `bin\` - Command line tools (psql.exe, pg_ctl.exe, etc.)
- `data\` - Database data files
- `lib\` - Libraries
- `share\` - Shared files

### pgAdmin 4 (GUI Tool)
**Default Location:**
- `C:\Program Files\PostgreSQL\16\pgAdmin 4\`
- Or separate installation: `C:\Program Files\pgAdmin 4\`

### Data Directory (Where databases are stored)
**Default Location:**
- `C:\Program Files\PostgreSQL\16\data\`
- Or custom location if you chose one during installation

## Important Paths for Campus Connect

### 1. psql Command Line Tool
**Location:**
```
C:\Program Files\PostgreSQL\16\bin\psql.exe
```

**Add to PATH:**
The installer usually adds this automatically, but if not:
1. Right-click "This PC" → Properties
2. Advanced System Settings → Environment Variables
3. Edit "Path" variable
4. Add: `C:\Program Files\PostgreSQL\16\bin`

### 2. pgAdmin 4
**Location:**
```
C:\Program Files\PostgreSQL\16\pgAdmin 4\bin\pgAdmin4.exe
```
Usually has a desktop shortcut after installation.

### 3. Service Name
**Windows Service:**
- Service name: `postgresql-x64-16` (version number varies)
- Can be found in: Services (Win+R → `services.msc`)

## Finding Your Installation

### Check if PostgreSQL is Installed:
```cmd
dir "C:\Program Files\PostgreSQL"
```

### Check Service:
```cmd
sc query | findstr postgres
```

### Check if psql is in PATH:
```cmd
where psql
```

### Find Data Directory:
```cmd
psql -U postgres -c "SHOW data_directory;"
```

## For Campus Connect Application

The application doesn't need to know the installation path directly. It connects via:
- **Host:** `localhost`
- **Port:** `5432` (default)
- **Database:** `campusconnect` (we'll create this)
- **Username:** `postgres`
- **Password:** (the one you set during installation)

All configured in: `backend/src/main/resources/application.properties`

## Custom Installation Location

If you installed PostgreSQL in a custom location:
- Check the installation directory you chose
- The structure will be the same: `[YourPath]\PostgreSQL\16\`
- Update PATH if needed to include `[YourPath]\PostgreSQL\16\bin`

## Verification

After installation, verify:
1. **Service is running:**
   ```cmd
   sc query postgresql-x64-16
   ```

2. **Can connect:**
   ```cmd
   "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
   ```

3. **Database exists:**
   ```cmd
   psql -U postgres -l
   ```

