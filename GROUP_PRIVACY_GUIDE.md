# Group Privacy Settings Guide

This guide explains the four privacy combinations available when creating or managing groups in Campus Connect.

## Privacy Combinations

Groups have two settings that combine to create four privacy options:

1. **Visibility**: `PUBLIC` or `PRIVATE`
2. **Requires Invite**: `true` or `false`

### 1. Public - Open Join
- **Visibility**: `PUBLIC`
- **Requires Invite**: `false`
- **Description**: Group is visible in search/browse, anyone can join directly without approval
- **Use Case**: Open study groups, public discussion forums

### 2. Public - Invite Only
- **Visibility**: `PUBLIC`
- **Requires Invite**: `true`
- **Description**: Group is visible in search/browse, but requires approval to join
- **Use Case**: Public groups that want to control membership quality

### 3. Private - Direct Join
- **Visibility**: `PRIVATE`
- **Requires Invite**: `false`
- **Description**: Group is hidden from search, but allows direct join if user has the link
- **Use Case**: Semi-private groups shared via link

### 4. Private - Invite Only
- **Visibility**: `PRIVATE`
- **Requires Invite**: `true`
- **Description**: Group is hidden from search, requires invitation/approval to join
- **Use Case**: Exclusive groups, private project teams

## API Usage

### Create Group with Privacy Settings

**Endpoint:** `POST /api/groups`

**Parameters:**
- `visibility` (optional, default: "PUBLIC") - "PUBLIC" or "PRIVATE"
- `requiresInvite` (optional, default: false) - true or false

**Example 1: Public Open Join**
```bash
POST /api/groups
{
  "name": "CS 101 Study Group",
  "courseName": "Introduction to Computer Science",
  "visibility": "PUBLIC",
  "requiresInvite": false
}
```

**Example 2: Public Invite Only**
```bash
POST /api/groups
{
  "name": "Advanced Algorithms Study",
  "courseName": "CS 4750",
  "visibility": "PUBLIC",
  "requiresInvite": true
}
```

**Example 3: Private Direct Join**
```bash
POST /api/groups
{
  "name": "Project Team Alpha",
  "courseName": "Senior Capstone",
  "visibility": "PRIVATE",
  "requiresInvite": false
}
```

**Example 4: Private Invite Only**
```bash
POST /api/groups
{
  "name": "Exclusive Research Group",
  "courseName": "Graduate Research",
  "visibility": "PRIVATE",
  "requiresInvite": true
}
```

### Join a Group

**Endpoint:** `POST /api/groups/{groupId}/join`

The system automatically handles the join logic:
- If `requiresInvite = false`: User joins immediately
- If `requiresInvite = true`: A join request is created (needs approval)

**Example:**
```bash
POST /api/groups/1/join
# Optional message for join request
POST /api/groups/1/join?message="I'm interested in joining this study group"
```

**Response (Direct Join):**
```json
{
  "id": 1,
  "name": "CS 101 Study Group",
  "currentSize": 5,
  "visibility": "PUBLIC",
  "requiresInvite": false
}
```

**Response (Join Request):**
```json
{
  "id": 1,
  "groupId": 1,
  "userId": 5,
  "message": "I'm interested in joining",
  "status": "PENDING"
}
```

### Request to Join (Explicit)

**Endpoint:** `POST /api/groups/{groupId}/request-join`

Always creates a join request, even if the group allows direct join.

### Accept/Reject Join Requests

**Accept:** `POST /api/groups/join-requests/{requestId}/accept`
**Reject:** `POST /api/groups/join-requests/{requestId}/reject`

Only the group creator can accept/reject requests.

### Leave a Group

**Endpoint:** `POST /api/groups/{groupId}/leave`

**Note:** Group creators cannot leave their own groups. They must delete the group instead.

### Update Group Privacy

**Endpoint:** `POST /api/groups/{groupId}/privacy`

**Parameters:**
- `visibility` (optional) - "PUBLIC" or "PRIVATE"
- `requiresInvite` (optional) - true or false

**Example:**
```bash
POST /api/groups/1/privacy?visibility=PRIVATE&requiresInvite=true
```

## Behavior Summary

| Visibility | Requires Invite | Visible in Search | Direct Join | Join Request |
|------------|----------------|-------------------|-------------|--------------|
| PUBLIC     | false          | ✅ Yes            | ✅ Yes      | ❌ No        |
| PUBLIC     | true           | ✅ Yes            | ❌ No       | ✅ Yes       |
| PRIVATE    | false          | ❌ No             | ✅ Yes*     | ❌ No        |
| PRIVATE    | true           | ❌ No             | ❌ No       | ✅ Yes       |

*Direct join works if user has the direct link to the group

## Frontend Implementation Tips

1. **Group Creation Form:**
   - Radio buttons or dropdown for Visibility (Public/Private)
   - Checkbox for "Requires Invite/Approval"
   - Show description of selected combination

2. **Group Display:**
   - Show privacy badge: "Public - Open Join", "Private - Invite Only", etc.
   - Show appropriate join button:
     - "Join" for direct join groups
     - "Request to Join" for invite-only groups

3. **Join Flow:**
   - Try direct join first
   - If it returns a join request, show "Request Sent" message
   - If it returns the group, show "Joined Successfully"

## Examples Using PowerShell

```powershell
# Create a public open join group
$body = @{
    name = "CS 101 Study Group"
    courseName = "Introduction to Computer Science"
    visibility = "PUBLIC"
    requiresInvite = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/groups" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer YOUR_TOKEN"}

# Join a group (automatic handling)
Invoke-RestMethod -Uri "http://localhost:8080/api/groups/1/join" -Method POST -Headers @{Authorization="Bearer YOUR_TOKEN"}

# Leave a group
Invoke-RestMethod -Uri "http://localhost:8080/api/groups/1/leave" -Method POST -Headers @{Authorization="Bearer YOUR_TOKEN"}
```

