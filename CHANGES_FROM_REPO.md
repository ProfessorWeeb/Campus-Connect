# Changes Integrated from GitHub Repository

This document outlines the changes that have been prioritized and integrated from the [S-Kaiserr/Campus-Connect](https://github.com/S-Kaiserr/Campus-Connect) repository.

## New Models Added

### 1. Post Model
- **Purpose**: Allows users to create posts/discussions within groups
- **Location**: `backend/src/main/java/com/campusconnect/model/Post.java`
- **Features**:
  - Title and content fields
  - Author relationship (User)
  - Group relationship
  - Timestamp tracking

### 2. Notification Model
- **Purpose**: System notifications for users (join requests, messages, etc.)
- **Location**: `backend/src/main/java/com/campusconnect/model/Notification.java`
- **Features**:
  - Type and message fields
  - Read/unread status
  - User relationship
  - Timestamp tracking

### 3. UserGroup Model
- **Purpose**: Many-to-many relationship table for users and groups
- **Location**: `backend/src/main/java/com/campusconnect/model/UserGroup.java`
- **Features**:
  - Unique constraint on user-group pairs
  - Join timestamp
  - Proper JPA relationships

## New Repositories

- `PostRepository` - For managing group posts
- `NotificationRepository` - For managing user notifications
- `UserGroupRepository` - For managing user-group memberships

## New Services

### PostService
- Create posts in groups
- Retrieve posts by group or author
- Delete posts (author only)

### NotificationService
- Create notifications
- Get user notifications (all or unread only)
- Mark notifications as read
- Get unread count

## New Controllers

### PostController
- `POST /api/posts` - Create a post
- `GET /api/posts/group/{groupId}` - Get posts by group
- `GET /api/posts/author/{authorId}` - Get posts by author
- `GET /api/posts/{id}` - Get post by ID
- `DELETE /api/posts/{id}` - Delete a post

### NotificationController
- `GET /api/notifications` - Get all user notifications
- `GET /api/notifications/unread` - Get unread notifications
- `GET /api/notifications/unread-count` - Get unread count
- `POST /api/notifications/{id}/read` - Mark notification as read
- `POST /api/notifications/read-all` - Mark all as read

## Key Differences from GitHub Repo

### Maintained in This Implementation:
1. **JWT Authentication** - The GitHub repo uses basic password checking; this implementation includes full JWT authentication
2. **Long IDs** - This implementation uses Long IDs instead of UUIDs for simplicity
3. **Enhanced User Model** - Includes interests, skills, courses, roles, and visibility settings
4. **Enhanced Group Model** - Includes max size, status, and direct member relationships
5. **Join Request System** - Full join request workflow with approval/rejection
6. **Resource Sharing** - Resource model for sharing documents/links
7. **Meeting Scheduling** - Meeting model for scheduling group meetings

### Integrated from GitHub Repo:
1. **Post System** - Group discussion posts
2. **Notification System** - User notifications
3. **UserGroup Model** - Proper many-to-many relationship management

## Next Steps

To fully align with the GitHub repository structure, consider:
1. Adding Maven wrapper files (mvnw, mvnw.cmd) - Can be generated with `mvn wrapper:wrapper`
2. Updating package structure to `com.campusconnect.backend` (optional)
3. Migrating to UUID-based IDs (requires extensive refactoring)

## Usage

The new features can be used as follows:

### Creating a Post
```bash
POST /api/posts?groupId=1&title=Study Session&content=Let's meet tomorrow
```

### Getting Notifications
```bash
GET /api/notifications
GET /api/notifications/unread
```

### Managing Group Memberships
The UserGroup model is now available for more granular control over group memberships, though the existing Group.members relationship is still functional.

