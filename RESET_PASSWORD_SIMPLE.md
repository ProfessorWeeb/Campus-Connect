# Simple PostgreSQL Password Reset

## Method 1: Using pgAdmin 4 (Easiest)

1. **Open pgAdmin 4** from Start Menu
2. **Connect to PostgreSQL server** (you may need your current password)
3. **Right-click on "Login/Group Roles"** → **postgres** → **Properties**
4. Go to **Definition** tab
5. Enter your **new password** in both password fields
6. Click **Save**

## Method 2: Using Command Line (If you know current password)

```cmd
cd "C:\Program Files\pgsql\bin"
psql.exe -U postgres
```

Then in psql:
```sql
ALTER USER postgres WITH PASSWORD 'your_new_password';
\q
```

## Method 3: Using the Reset Script

Run the batch file I created:
```cmd
reset_postgres_password.bat
```

This will:
1. Temporarily allow passwordless connections
2. Let you set a new password
3. Restore secure settings

**Note:** This requires admin privileges to restart the PostgreSQL service.

## Method 4: Manual Reset (If you forgot password)

1. **Stop PostgreSQL service:**
   ```cmd
   net stop postgresql-x64-18
   ```
   (Version number may vary)

2. **Edit pg_hba.conf:**
   - Location: `C:\Program Files\pgsql\data\pg_hba.conf`
   - Find line: `host    all             all             127.0.0.1/32            scram-sha-256`
   - Change `scram-sha-256` to `trust` (temporarily)

3. **Start PostgreSQL:**
   ```cmd
   net start postgresql-x64-18
   ```

4. **Reset password:**
   ```cmd
   cd "C:\Program Files\pgsql\bin"
   psql.exe -U postgres -c "ALTER USER postgres WITH PASSWORD 'newpassword';"
   ```

5. **Restore pg_hba.conf:**
   - Change `trust` back to `scram-sha-256`
   - Restart PostgreSQL service

6. **Update application.properties:**
   ```properties
   spring.datasource.password=newpassword
   ```

## After Resetting

Don't forget to update:
- `backend/src/main/resources/application.properties` (line 7)
- Restart your backend server

