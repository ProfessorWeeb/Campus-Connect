# Campus Connect

A digital platform designed for student collaboration, academic engagement, and peer networking within an academic community.

## Project Structure

```
CampusConnect/
├── backend/          # Spring Boot backend
├── frontend/         # React frontend
└── README.md
```

## Technologies

- **Backend**: Spring Boot (Spring Web, Spring Data JPA, Spring Security)
- **Frontend**: React.js
- **Database**: PostgreSQL (H2 for development)

## Features

- User authentication and profile management (JWT-based)
- Group creation and management with join requests
- Direct and group messaging
- **Group Posts** - Create discussion posts within groups (NEW)
- **Notifications** - System notifications for users (NEW)
- Search and matching algorithm
- Resource sharing
- Meeting scheduling

## Getting Started

### Backend Setup

(You might need to run start_postgres before going to the backend folder.)

1. Navigate to `backend/` directory
2. Configure database in `src/main/resources/application.properties`
3. Run: `./mvnw spring-boot:run` (or `start_backend.bat` on Windows)

### Frontend Setup

1. Navigate to `frontend/` directory
2. Install dependencies: `npm install`
3. Run: `npm start`

### Setting Up Bot Groups (Test Data)

After pulling from GitHub, you need to seed bot users and groups:

1. **Start the backend** (if not already running)
2. **Run the bot seeder**: `seed_bots.bat` (Windows) or use the API endpoint
3. This creates 100 bot users and 25 groups for testing

See [SETUP_BOTS.md](SETUP_BOTS.md) for detailed instructions.

## Development

The application uses:
- Spring Boot for RESTful API
- React for responsive UI
- PostgreSQL for data persistence
- JWT for authentication

## Changes from GitHub Repository

This implementation includes features from [S-Kaiserr/Campus-Connect](https://github.com/S-Kaiserr/Campus-Connect):
- Post model for group discussions
- Notification system for user alerts
- UserGroup model for many-to-many relationships
- Maven wrapper for consistent builds

See [CHANGES_FROM_REPO.md](CHANGES_FROM_REPO.md) for detailed information.

