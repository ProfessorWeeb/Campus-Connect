# PostgreSQL Setup for Campus Connect

## ✅ PostgreSQL is Installed!

**Location:** `C:\Program Files\pgsql`  
**Version:** PostgreSQL 18.1

## Next Steps

### 1. Create the Database

Run this command (you'll be prompted for your PostgreSQL password):

```cmd
cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres -c "CREATE DATABASE campusconnect;"
```

**Or use the batch file I created:**
```cmd
create_database.bat
```

### 2. Update Application Configuration

Edit `backend/src/main/resources/application.properties` and update line 7 with your PostgreSQL password:

```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

**Current setting:** `postgres` (change if your password is different)

### 3. Verify Database Was Created

```cmd
cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres -l
```

Look for `campusconnect` in the list.

### 4. Restart Backend Server

Stop the current backend (Ctrl+C) and restart:
```cmd
cd backend
mvnw.cmd spring-boot:run
```

## Quick Test

After restarting, the backend should:
- Connect to PostgreSQL successfully
- Create all tables automatically
- Be ready to use!

You can verify by checking the backend logs for:
- "HikariPool-1 - Start completed"
- No connection errors

## Troubleshooting

**If you get "password authentication failed":**
- Check the password in `application.properties`
- Make sure it matches your PostgreSQL password

**If you get "database does not exist":**
- Run the CREATE DATABASE command above
- Or use pgAdmin 4 to create it manually

**If PostgreSQL service is not running:**
- Open Services (Win+R → `services.msc`)
- Find PostgreSQL service and start it
- Or check `C:\Program Files\pgsql\data\` for service name

## Using pgAdmin 4 (GUI Alternative)

1. Open pgAdmin 4 from Start Menu
2. Connect to PostgreSQL server
3. Right-click "Databases" → Create → Database
4. Name: `campusconnect`
5. Click Save

