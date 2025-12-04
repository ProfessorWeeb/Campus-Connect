# Updating Frontend API Calls for AWS Deployment

To prepare the frontend for AWS deployment, you need to replace all hardcoded `http://localhost:8080` API calls with the centralized `api` instance from `axiosConfig.js`.

## Step 1: Import the API Config

In each file that makes API calls, replace:
```javascript
import axios from 'axios';
```

With:
```javascript
import api from '../utils/axiosConfig';  // Adjust path as needed
```

## Step 2: Replace API Calls

Replace all instances of:
```javascript
axios.get('http://localhost:8080/api/...')
axios.post('http://localhost:8080/api/...')
axios.put('http://localhost:8080/api/...')
axios.delete('http://localhost:8080/api/...')
```

With:
```javascript
api.get('/...')      // Note: '/api' is already in baseURL
api.post('/...')
api.put('/...')
api.delete('/...')
```

## Files That Need Updates

1. **frontend/src/context/AuthContext.js**
   - Line 22: `axios.get('http://localhost:8080/api/users/me')` → `api.get('/users/me')`

2. **frontend/src/pages/Login.js**
   - Line 19: `axios.post('http://localhost:8080/api/auth/login', ...)` → `api.post('/auth/login', ...)`

3. **frontend/src/pages/Register.js**
   - Line 32: `axios.post('http://localhost:8080/api/auth/register', ...)` → `api.post('/auth/register', ...)`

4. **frontend/src/pages/Dashboard.js**
   - Lines 28-30: Replace all `axios.get('http://localhost:8080/api/...')` → `api.get('/...')`
   - Line 44: `axios.get('http://localhost:8080/api/groups/recommended')` → `api.get('/groups/recommended')`
   - Line 69: `axios.post(...)` → `api.post(...)`

5. **frontend/src/pages/Groups.js**
   - Multiple instances: Replace all `axios.get/post('http://localhost:8080/api/...')` → `api.get/post('/...')`

6. **frontend/src/pages/GroupDetail.js**
   - Multiple instances: Replace all `axios.get/post/put/delete('http://localhost:8080/api/...')` → `api.get/post/put/delete('/...')`

7. **frontend/src/pages/Messages.js**
   - Multiple instances: Replace all `axios.get/post('http://localhost:8080/api/...')` → `api.get/post('/...')`

8. **frontend/src/pages/Profile.js**
   - Line 59: `axios.put('http://localhost:8080/api/users/me?...')` → `api.put('/users/me?...')`

## Example Transformation

**Before:**
```javascript
import axios from 'axios';

const fetchGroups = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/groups');
    setGroups(response.data);
  } catch (error) {
    console.error('Error fetching groups:', error);
  }
};
```

**After:**
```javascript
import api from '../utils/axiosConfig';

const fetchGroups = async () => {
  try {
    const response = await api.get('/groups');
    setGroups(response.data);
  } catch (error) {
    console.error('Error fetching groups:', error);
  }
};
```

## Important Notes

1. **Remove `/api` prefix**: The base URL already includes `/api`, so use `/groups` not `/api/groups`
2. **Query parameters**: Keep query parameters as-is: `api.get('/users/me?param=value')`
3. **Request body**: Keep request body as-is: `api.post('/endpoint', data)`
4. **Error handling**: The axiosConfig already handles 401 errors, but you can keep custom error handling

## Verification

After updating, test locally:
1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm start`
3. Verify all API calls work correctly
4. Check browser console for any errors

## Automated Update (Optional)

You can use find-and-replace in your IDE:
- Find: `axios.get('http://localhost:8080/api`
- Replace: `api.get('`
- Find: `axios.post('http://localhost:8080/api`
- Replace: `api.post('`
- Find: `axios.put('http://localhost:8080/api`
- Replace: `api.put('`
- Find: `axios.delete('http://localhost:8080/api`
- Replace: `api.delete('`

Then manually fix the paths (remove `/api` prefix).

