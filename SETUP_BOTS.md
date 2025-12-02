# Bot Groups Setup Guide

After pulling the Campus Connect repository from GitHub, you need to seed bot users and groups to populate the application with test data.

## Quick Start

1. **Start the backend** (if not already running):
   ```bash
   # Windows
   start_backend.bat
   
   # Or manually
   cd backend
   mvnw.cmd spring-boot:run
   ```

2. **Seed bots and groups**:
   ```bash
   # Windows - Easiest method
   seed_bots.bat
   
   # This will create 100 bot users and 25 groups
   ```

That's it! The bot groups should now appear in your application.

## What Gets Created

- **100 bot users** with realistic names, emails, and majors
- **25 groups** created by bots across different courses (CSCI, ITEC, ENGR, PHYS, MATH)
- Bots automatically join some groups
- Bots send messages to each other

## Manual Setup (Alternative)

If you prefer to use the API directly or customize the setup:

### Step 1: Initialize Courses (if needed)
The bot seeder will automatically initialize courses, but you can do it manually:
```bash
# Windows
initialize_courses.bat

# Or via API
POST http://localhost:8080/api/courses/initialize
```

### Step 2: Seed Bots
```bash
# Via API
POST http://localhost:8080/api/admin/bots/seed?count=100&groups=25

# Or use the batch file
seed_bots.bat
```

### Step 3: (Optional) Add More Bots and Groups
```bash
# Add 150 more bots and create 25 CSCI groups
add_more_bots_and_groups.bat

# Or via API
POST http://localhost:8080/api/admin/bots/add-more?count=150
POST http://localhost:8080/api/admin/bots/create-csci-groups?count=25
```

## Troubleshooting

### "Bots already exist" Error
If you see this error, delete existing bots first:
```bash
# Via API
DELETE http://localhost:8080/api/admin/bots/delete-all
```

### "No courses found" Error
Make sure courses are initialized:
```bash
initialize_courses.bat
```

### Backend Not Running
Make sure the backend is running on port 8080:
```bash
start_backend.bat
```

### Groups Don't Appear
1. Check that bots were created: `GET http://localhost:8080/api/admin/bots/count`
2. Check that groups exist in the database
3. Make sure you're logged in as a real user (not a bot)
4. Check group visibility settings (should be PUBLIC)

## API Endpoints Reference

- `POST /api/admin/bots/seed?count=100&groups=25` - Create bots and groups
- `GET /api/admin/bots` - List all bots
- `GET /api/admin/bots/count` - Get bot count
- `POST /api/admin/bots/add-more?count=150` - Add more bots
- `POST /api/admin/bots/create-csci-groups?count=25` - Create CSCI groups
- `DELETE /api/admin/bots/delete-all` - Delete all bots and their groups
- `POST /api/courses/initialize` - Initialize courses

## Notes

- All bots use the password: `botpassword123`
- Bots are marked with `isBot: true` flag
- Bot groups are PUBLIC and open for joining
- The bot seeder automatically initializes courses if they don't exist
- Bot groups are created across multiple departments (CSCI, ITEC, ENGR, PHYS, MATH)

