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

1. Navigate to `backend/` directory
2. Configure database in `src/main/resources/application.properties`
3. Run: `./mvnw spring-boot:run`

### Frontend Setup

1. Navigate to `frontend/` directory
2. Install dependencies: `npm install`
3. Run: `npm start`

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

