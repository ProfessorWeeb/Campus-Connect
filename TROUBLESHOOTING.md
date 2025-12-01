# Troubleshooting Guide

## Backend Not Starting

If you're getting "Cannot connect to server" errors, follow these steps:

### 1. Check if Backend is Running

**Windows:**
```cmd
netstat -ano | findstr :8080
```

**Mac/Linux:**
```bash
lsof -i :8080
```

If nothing shows up, the backend is not running.

### 2. Start the Backend Manually

Navigate to the backend directory and run:

**Windows:**
```cmd
cd backend
mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
cd backend
./mvnw spring-boot:run
```

Or if you have Maven installed:
```cmd
mvn spring-boot:run
```

### 3. Check for Port Conflicts

If port 8080 is already in use, you can:
- Stop the other application using port 8080
- Change the port in `backend/src/main/resources/application.properties`:
  ```
  server.port=8081
  ```
  Then update the frontend API calls to use port 8081.

### 4. Check Backend Logs

Look for errors in the console where you started the backend. Common issues:
- **Port already in use**: Change the port or stop the conflicting application
- **Database connection error**: Check H2 database configuration
- **Compilation errors**: Run `mvn clean install` first

### 5. Verify Backend is Ready

Once started, you should see:
```
Started CampusConnectApplication in X.XXX seconds
```

You can test the backend directly:
```cmd
curl http://localhost:8080/api/auth/register
```

## Frontend Issues

### Registration/Login Errors

The frontend now shows detailed error messages:
- **Connection errors**: Backend is not running
- **Validation errors**: Check your input fields
- **Server errors**: Check backend logs

### Clear Browser Cache

If you're seeing old errors:
1. Open browser DevTools (F12)
2. Right-click refresh button
3. Select "Empty Cache and Hard Reload"

## Common Solutions

### Backend Won't Start

1. **Check Java version**: Need Java 17+
   ```cmd
   java -version
   ```

2. **Clean and rebuild**:
   ```cmd
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

3. **Check for missing dependencies**: Make sure all Maven dependencies downloaded

### Frontend Can't Connect

1. **Verify backend URL**: Should be `http://localhost:8080`
2. **Check CORS**: Backend should allow `http://localhost:3000`
3. **Check firewall**: Windows Firewall might be blocking the connection

### Database Issues

The app uses H2 in-memory database by default. Data resets on restart.

To use PostgreSQL:
1. Update `application.properties`
2. Uncomment PostgreSQL configuration
3. Comment out H2 configuration
4. Make sure PostgreSQL is running

## Getting Help

If issues persist:
1. Check backend console for error messages
2. Check browser console (F12) for frontend errors
3. Verify both servers are running on correct ports
4. Try restarting both servers

