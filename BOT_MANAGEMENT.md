# Bot User Management Guide

This guide explains how to create, manage, and delete bot users for testing the Campus Connect API.

## Overview

Bot users are test accounts that can be easily created, managed, and deleted without affecting real user accounts. They are marked with an `isBot` flag in the database.

## Features

- ✅ Create 100 bot users with realistic data
- ✅ 20 bots automatically create groups
- ✅ Easy deletion of all bots and their groups
- ✅ Hide/show bots without deleting them
- ✅ Bots are automatically excluded from user searches
- ✅ All bots use the same password: `botpassword123`

## API Endpoints

### 1. Seed Bot Users

Create bot users and groups for testing.

**Endpoint:** `POST /api/admin/bots/seed`

**Parameters:**
- `count` (optional, default: 100) - Number of bot users to create
- `groups` (optional, default: 20) - Number of groups to create

**Example:**
```bash
# Create 100 bots with 20 groups
curl -X POST "http://localhost:8080/api/admin/bots/seed?count=100&groups=20"

# Create 50 bots with 10 groups
curl -X POST "http://localhost:8080/api/admin/bots/seed?count=50&groups=10"
```

**Response:**
```json
{
  "success": true,
  "message": "Bots seeded successfully",
  "botsCreated": 100,
  "groupsCreated": 20,
  "botIds": [1, 2, 3, ...],
  "groupIds": [1, 2, 3, ...]
}
```

### 2. List All Bots

Get information about all bot users.

**Endpoint:** `GET /api/admin/bots`

**Example:**
```bash
curl "http://localhost:8080/api/admin/bots"
```

**Response:**
```json
{
  "count": 100,
  "bots": [
    {
      "id": 1,
      "username": "bot_alex_1",
      "email": "bot1@test.mga.edu",
      "firstName": "Alex",
      "lastName": "Smith",
      "major": "Computer Science"
    },
    ...
  ]
}
```

### 3. Get Bot Count

Get the total number of bot users.

**Endpoint:** `GET /api/admin/bots/count`

**Example:**
```bash
curl "http://localhost:8080/api/admin/bots/count"
```

**Response:**
```json
{
  "count": 100
}
```

### 4. Delete All Bots

Delete all bot users and their associated groups.

**Endpoint:** `DELETE /api/admin/bots/delete-all`

**Example:**
```bash
curl -X DELETE "http://localhost:8080/api/admin/bots/delete-all"
```

**Response:**
```json
{
  "success": true,
  "message": "All bots and their groups deleted successfully",
  "botsDeleted": 100
}
```

### 5. Hide All Bots

Set all bot users' visibility to private (they won't appear in public searches).

**Endpoint:** `POST /api/admin/bots/hide`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/admin/bots/hide"
```

**Response:**
```json
{
  "success": true,
  "message": "All bots hidden successfully",
  "botsHidden": 100
}
```

### 6. Show All Bots

Set all bot users' visibility to public.

**Endpoint:** `POST /api/admin/bots/show`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/admin/bots/show"
```

**Response:**
```json
{
  "success": true,
  "message": "All bots shown successfully",
  "botsShown": 100
}
```

## Bot User Details

### Default Password
All bot users have the same password: **`botpassword123`**

### Bot Characteristics
- Random first and last names from predefined lists
- Usernames: `bot_<firstname>_<number>`
- Emails: `bot<number>@test.mga.edu`
- Random majors from common college majors
- Random course enrollments
- Interests include: Programming, Study Groups, Mathematics, Science
- All marked as `isBot = true`

### Groups Created by Bots
- Random course codes and names
- Study group topics (Study Group, Homework Help, Exam Prep, etc.)
- Group sizes between 5-10 members
- Creator automatically added as a member

## Usage Examples

### Complete Testing Workflow

1. **Create bots:**
   ```bash
   curl -X POST "http://localhost:8080/api/admin/bots/seed?count=100&groups=20"
   ```

2. **Test your API** with the bot users and groups

3. **Check bot count:**
   ```bash
   curl "http://localhost:8080/api/admin/bots/count"
   ```

4. **Hide bots during real user testing:**
   ```bash
   curl -X POST "http://localhost:8080/api/admin/bots/hide"
   ```

5. **Delete all bots when done:**
   ```bash
   curl -X DELETE "http://localhost:8080/api/admin/bots/delete-all"
   ```

### Using PowerShell

```powershell
# Create bots
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bots/seed?count=100&groups=20" -Method POST

# Get bot count
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bots/count" -Method GET

# Delete all bots
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bots/delete-all" -Method DELETE
```

## Important Notes

⚠️ **Bots are automatically excluded from user searches** - The `searchUsers`, `findUsersByCourse`, and `findUsersBySkill` methods automatically filter out bot accounts.

⚠️ **Deleting bots also deletes their groups** - When you delete all bots, all groups created by bots are also deleted.

⚠️ **Bots cannot be created if bots already exist** - You must delete existing bots before creating new ones. This prevents accidental duplication.

⚠️ **Bot accounts are for testing only** - They should not be used in production. Consider removing the bot management endpoints before deploying to production.

## Database Schema

The `User` model includes:
- `isBot` (Boolean, default: false) - Marks accounts as bot accounts
- Bots are stored in the same `users` table as real users
- Bots can be easily identified and filtered using the `isBot` flag

## Troubleshooting

### "Bots already exist" Error
If you get this error, delete existing bots first:
```bash
curl -X DELETE "http://localhost:8080/api/admin/bots/delete-all"
```

### Bots Not Appearing in Searches
This is expected behavior! Bots are automatically excluded from user searches to keep test data separate from real users.

### Can't Login as Bot
All bots use the password: `botpassword123`
- Email format: `bot<number>@test.mga.edu`
- Example: `bot1@test.mga.edu` / `botpassword123`

