# How to Start PostgreSQL for Campus Connect

## ⚠️ Important: Two Different Methods

You have **two different batch files** that start PostgreSQL in different ways:

### Method 1: `start_postgres.bat`
- **What it does:** Starts PostgreSQL directly as a process (not a Windows service)
- **How it works:** Runs `postgres.exe` directly from the `pgsql\bin` folder
- **Use this if:** PostgreSQL was not installed as a Windows service
- **Pros:** Simple, works without service installation
- **Cons:** Must keep the window open, stops when window closes

### Method 2: `start_postgresql.bat` 
- **What it does:** Starts PostgreSQL as a Windows service
- **How it works:** Uses `net start` to start the service
- **Use this if:** PostgreSQL was installed as a Windows service
- **Pros:** Runs in background, persists after closing terminal
- **Cons:** Requires service to be installed

## 🎯 Recommended Approach

**Use `start_postgres.bat`** if you're not sure which one works:

1. Double-click `start_postgres.bat`
2. A new window will open with PostgreSQL running
3. **Keep that window open** while using the backend
4. Start the backend in a separate window

## ✅ Quick Start Steps

1. **Start PostgreSQL:**
   ```
   start_postgres.bat
   ```
   (Keep the window open!)

2. **Start Backend:**
   ```
   start_backend.bat
   ```
   (Or: `cd backend` then `mvnw.cmd spring-boot:run`)

3. **Seed Bots:**
   ```
   seed_bots.bat
   ```

## 🔍 Troubleshooting

**If backend stops immediately:**
- Make sure PostgreSQL window is still open
- Check that PostgreSQL is listening on port 5432
- Verify database `campusconnect` exists

**If you see "port already in use":**
- Only one PostgreSQL instance can run at a time
- Close any other PostgreSQL windows/processes
- Check Task Manager for `postgres.exe` processes

