# PostgreSQL Installation Guide

## Option 1: Manual Installation (Recommended)

1. **Download PostgreSQL**
   - Go to: https://www.postgresql.org/download/windows/
   - Click "Download the installer"
   - Download the latest version (17 or 16)

2. **Run the Installer**
   - Run the downloaded `.exe` file
   - Follow the installation wizard
   - **Important settings:**
     - Installation directory: Default is fine
     - Components: Select all (PostgreSQL Server, pgAdmin 4, Command Line Tools)
     - Data directory: Default is fine
     - Password: **Remember this password!** (You'll need it for the app)
     - Port: **5432** (default - keep this)
     - Locale: Default

3. **Complete Installation**
   - Finish the installation
   - pgAdmin 4 will open automatically

## Option 2: Using Chocolatey (if installed)

```cmd
choco install postgresql16
```

## After Installation

### 1. Start PostgreSQL Service

**Check if service is running:**
```cmd
sc query postgresql-x64-16
```

**Start the service:**
```cmd
net start postgresql-x64-16
```

(Version number may vary - check Services for exact name)

### 2. Create Database

**Using pgAdmin (GUI):**
1. Open pgAdmin 4
2. Connect to PostgreSQL server (use the password you set during installation)
3. Right-click "Databases" → Create → Database
4. Name: `campusconnect`
5. Click Save

**Using Command Line:**
```cmd
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
```

Then:
```sql
CREATE DATABASE campusconnect;
\q
```

### 3. Update Application Configuration

Edit `backend/src/main/resources/application.properties`:

Change line 7 to match your PostgreSQL password:
```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

### 4. Restart Backend

```cmd
cd backend
mvnw.cmd spring-boot:run
```

## Quick Test

After setup, test the connection:
```cmd
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d campusconnect
```

If successful, you'll see: `campusconnect=#`

## Troubleshooting

**Service won't start:**
- Check Windows Services (Win+R → `services.msc`)
- Find PostgreSQL service and start it manually

**Can't connect:**
- Verify password in `application.properties`
- Check if port 5432 is available: `netstat -ano | findstr :5432`

**Database doesn't exist:**
- Create it using pgAdmin or psql command above

