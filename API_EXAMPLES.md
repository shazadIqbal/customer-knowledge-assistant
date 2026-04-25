# Sample API Requests and Responses

## 1. Register a New User (ROLE_USER)

### Request
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123",
  "role": "ROLE_USER"
}
```

### Success Response (200 OK)
```json
{
  "message": "User registered successfully!"
}
```

### Error Response - Username Taken (400 Bad Request)
```json
{
  "message": "Error: Username is already taken!"
}
```

### Error Response - Validation Error (400 Bad Request)
```json
{
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": "Username is required",
    "password": "size must be between 6 and 100"
  }
}
```

---

## 2. Register an Admin User

### Request
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

### Success Response (200 OK)
```json
{
  "message": "User registered successfully!"
}
```

---

## 3. Login User

### Request
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

### Success Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3MTQwNjQ0MDAsImV4cCI6MTcxNDA2ODAwMH0.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "type": "Bearer",
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

### Error Response - Invalid Credentials (400 Bad Request)
```json
{
  "message": "Invalid username or password"
}
```

---

## 4. Get Current User Details (Authenticated)

### Request
```http
GET http://localhost:8080/api/users/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJST0xFX1VTRVIiLCJpYXQiOjE3MTQwNjQ0MDAsImV4cCI6MTcxNDA2ODAwMH0.xxx
```

### Success Response (200 OK)
```json
{
  "id": 1,
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

### Error Response - No Token (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users/me"
}
```

### Error Response - Invalid/Expired Token (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/users/me"
}
```

---

## 5. Get All Users (Admin Only)

### Request
```http
GET http://localhost:8080/api/admin/users
Authorization: Bearer <admin_jwt_token>
```

### Success Response (200 OK)
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "role": "ROLE_USER"
  },
  {
    "id": 2,
    "username": "jane_smith",
    "role": "ROLE_USER"
  },
  {
    "id": 3,
    "username": "admin",
    "role": "ROLE_ADMIN"
  }
]
```

### Error Response - Non-Admin User (403 Forbidden)
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/admin/users"
}
```

### Error Response - No Token (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/admin/users"
}
```

---

## Testing Sequence

### Step 1: Register Regular User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "role": "ROLE_USER"
  }'
```

### Step 2: Register Admin User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "role": "ROLE_ADMIN"
  }'
```

### Step 3: Login as Regular User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```
**Save the token from response!**

### Step 4: Get Current User Info
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <your_token_here>"
```

### Step 5: Try Admin Endpoint (Should Fail)
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <user_token_here>"
```
**Expected: 403 Forbidden**

### Step 6: Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```
**Save the admin token!**

### Step 7: Access Admin Endpoint (Should Succeed)
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin_token_here>"
```
**Expected: 200 OK with list of all users**

---

## Postman Collection

### Setup
1. Create a new collection in Postman
2. Add the following requests:

#### Environment Variables
- `base_url`: `http://localhost:8080`
- `user_token`: (will be set after login)
- `admin_token`: (will be set after admin login)

#### Requests

1. **Register User**
   - Method: POST
   - URL: `{{base_url}}/api/auth/register`
   - Body: See example above

2. **Login User**
   - Method: POST
   - URL: `{{base_url}}/api/auth/login`
   - Body: See example above
   - Test Script:
   ```javascript
   var jsonData = pm.response.json();
   pm.environment.set("user_token", jsonData.token);
   ```

3. **Get Current User**
   - Method: GET
   - URL: `{{base_url}}/api/users/me`
   - Headers: `Authorization: Bearer {{user_token}}`

4. **Get All Users (Admin)**
   - Method: GET
   - URL: `{{base_url}}/api/admin/users`
   - Headers: `Authorization: Bearer {{admin_token}}`

---

## JWT Token Structure

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "john_doe",
  "role": "ROLE_USER",
  "iat": 1714064400,
  "exp": 1714068000
}
```

### Claims Explanation
- `sub`: Subject (username)
- `role`: User's role
- `iat`: Issued at (timestamp)
- `exp`: Expiration (timestamp)

---

## Common HTTP Status Codes

- **200 OK**: Request successful
- **400 Bad Request**: Validation error or invalid data
- **401 Unauthorized**: Authentication required or token invalid
- **403 Forbidden**: Authenticated but insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error
