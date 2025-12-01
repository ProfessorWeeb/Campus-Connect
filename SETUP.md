# Campus Connect - Setup Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 16+ and npm
- PostgreSQL (optional, H2 is used by default for development)

## Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

4. (Optional) To use PostgreSQL instead of H2:
   - Update `application.properties` with your PostgreSQL credentials
   - Uncomment the PostgreSQL configuration lines
   - Comment out the H2 configuration lines

## Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The frontend will start on `http://localhost:3000`

## Default Configuration

- Backend API: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:campusconnect`
  - Username: `sa`
  - Password: (empty)

## Testing the Application

1. Start both backend and frontend servers
2. Navigate to `http://localhost:3000`
3. Register a new account
4. Create a study group
5. Search for groups
6. Send messages

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Users
- `GET /api/users/me` - Get current user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/me` - Update profile
- `GET /api/users/search?query=...` - Search users

### Groups
- `GET /api/groups` - Get all groups
- `GET /api/groups/search?query=...` - Search groups
- `GET /api/groups/{id}` - Get group by ID
- `POST /api/groups` - Create group
- `POST /api/groups/{id}/join` - Request to join group
- `GET /api/groups/my-groups` - Get user's groups

### Messages
- `GET /api/messages/inbox` - Get inbox
- `POST /api/messages/direct` - Send direct message
- `POST /api/messages/group` - Send group message
- `GET /api/messages/direct/{userId}` - Get conversation
- `GET /api/messages/group/{groupId}` - Get group messages

## Troubleshooting

### Backend Issues
- Ensure Java 17+ is installed: `java -version`
- Check Maven installation: `mvn -version`
- If port 8080 is in use, change it in `application.properties`

### Frontend Issues
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Check Node.js version: `node -version` (should be 16+)
- Ensure backend is running before starting frontend

### Database Issues
- H2 database is in-memory, so data is lost on restart
- For persistent data, switch to PostgreSQL
- Access H2 console at `/h2-console` when backend is running

## Production Deployment

1. Update `application.properties` for PostgreSQL
2. Set secure JWT secret in `application.properties`
3. Build frontend: `npm run build`
4. Configure CORS for production domain
5. Deploy backend to cloud (AWS, Heroku, etc.)
6. Serve frontend build files through a web server or CDN

