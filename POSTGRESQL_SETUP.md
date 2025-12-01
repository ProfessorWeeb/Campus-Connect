# PostgreSQL Setup Guide

## Prerequisites

1. **Install PostgreSQL** (if not already installed)
   - Download from: https://www.postgresql.org/download/windows/
   - Or use: `winget install PostgreSQL.PostgreSQL` (Windows)
   - Default installation includes:
     - PostgreSQL server
     - pgAdmin (database management tool)
     - Command line tools

## Setup Steps

### 1. Start PostgreSQL Service

**Windows:**
- Open Services (Win+R → `services.msc`)
- Find "postgresql-x64-XX" service
- Right-click → Start (if not running)

Or via command line:
```cmd
net start postgresql-x64-16
```
(Version number may vary)

### 2. Create Database

**Option A: Using pgAdmin (GUI)**
1. Open pgAdmin
2. Connect to PostgreSQL server (default password is what you set during installation)
3. Right-click "Databases" → Create → Database
4. Name: `campusconnect`
5. Click Save

**Option B: Using Command Line (psql)**
```cmd
psql -U postgres
```
Then run:
```sql
CREATE DATABASE campusconnect;
\q
```

**Option C: Using SQL Command**
```cmd
psql -U postgres -c "CREATE DATABASE campusconnect;"
```

### 3. Update Application Configuration

The `application.properties` file has been updated with:
- **URL**: `jdbc:postgresql://localhost:5432/campusconnect`
- **Username**: `postgres` (default)
- **Password**: `postgres` (change this to your PostgreSQL password)

**Important:** Update the password in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

### 4. Verify Connection

Test the connection:
```cmd
psql -U postgres -d campusconnect
```

If successful, you'll see:
```
campusconnect=#
```

## Default PostgreSQL Credentials

- **Username**: `postgres`
- **Password**: Usually set during installation (commonly `postgres` or `admin`)
- **Port**: `5432` (default)
- **Host**: `localhost`

## Troubleshooting

### Connection Refused
- Make sure PostgreSQL service is running
- Check if port 5432 is available: `netstat -ano | findstr :5432`

### Authentication Failed
- Verify username and password in `application.properties`
- Check PostgreSQL authentication settings in `pg_hba.conf`

### Database Doesn't Exist
- Create the database using one of the methods above
- Verify with: `psql -U postgres -l` (lists all databases)

### Port Already in Use
- Check what's using port 5432: `netstat -ano | findstr :5432`
- Change PostgreSQL port in `postgresql.conf` if needed

## Restart Backend

After configuring PostgreSQL:
1. Stop the backend server (Ctrl+C)
2. Restart it:
   ```cmd
   cd backend
   mvnw.cmd spring-boot:run
   ```

The application will automatically:
- Connect to PostgreSQL
- Create tables based on your entities
- Be ready to use!

## Verify It's Working

1. Check backend logs for: "HikariPool-1 - Start completed"
2. Try registering a user in the frontend
3. Check database in pgAdmin or psql:
   ```sql
   SELECT * FROM users;
   ```

## Switching Back to H2 (if needed)

If you want to switch back to H2 for development:
1. Comment out PostgreSQL lines in `application.properties`
2. Uncomment H2 lines
3. Restart backend

