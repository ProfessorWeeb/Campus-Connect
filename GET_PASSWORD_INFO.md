# Campus Connect - Password Information

## User Account: daniel.underwood@mga.edu

**Status:** ✅ Password has been reset

**Login Credentials:**
- **Email:** `daniel.underwood@mga.edu`
- **Password:** `password123`

## Important Notes

⚠️ **Passwords are stored as BCrypt hashes** - The original password cannot be retrieved from the database. If you forget your password, you'll need to reset it.

## How to Reset Password (if needed)

### Option 1: Using the Reset Script
Run the batch script:
```cmd
reset_user_password.bat
```

### Option 2: Manual SQL Reset
1. Connect to PostgreSQL:
   ```cmd
   pgsql\bin\psql.exe -U postgres -d campusconnect
   ```

2. Generate a new BCrypt hash using the Spring Boot application, or use this SQL:
   ```sql
   -- You'll need to generate a proper BCrypt hash first
   -- Using the PasswordResetUtil.java utility class
   UPDATE users SET password = '<BCRYPT_HASH>' WHERE email = 'daniel.underwood@mga.edu';
   ```

### Option 3: Use the Java Utility
1. Navigate to backend directory
2. Compile and run:
   ```cmd
   cd backend
   mvnw.cmd compile exec:java -Dexec.mainClass="com.campusconnect.util.PasswordResetUtil" -Dexec.args="your_new_password"
   ```
3. Copy the generated hash and use it in the SQL UPDATE command

## Security Reminder

- Change the default password after first login
- Use a strong password (minimum 6 characters, but recommended 8+ with mixed case, numbers, and symbols)
- Never share your password

