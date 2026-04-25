# Spring Boot JWT Authentication with MS SQL Server

A complete REST API application built with Spring Boot, JWT authentication, MS SQL Server database, and Flyway migrations.

## 🚀 Quick Start

**New to this project?** Check out the [Quick Start Guide](QUICK_START.md) to get up and running in minutes!

---

## 🔧 Tech Stack

- Java 11
- Spring Boot 2.7.18
- Spring Security
- JWT (JSON Web Token)
- Spring Data JPA
- MS SQL Server
- Flyway Migration
- Swagger/OpenAPI (SpringDoc)
- Maven
- Lombok

## 📋 Prerequisites

- Java 11 or higher
- MS SQL Server installed and running
- Maven 3.6+
- SQL Server Authentication enabled

## 🗄️ Database Setup

1. Create a MS SQL Server database:
```sql
CREATE DATABASE TEST_DB;
```

2. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
```

3. Flyway will automatically create tables and insert default roles on first run.

## 🚀 Running the Application

1. Clone the repository and navigate to the project directory

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📊 Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `password` (BCrypt encrypted)
- `role_id` (Foreign Key to Role)

### Roles Table
- `id` (Primary Key)
- `name` (e.g., ROLE_USER, ROLE_ADMIN)

**Relationship**: Many-to-One (User → Role)

## 🔐 API Endpoints

### Authentication Endpoints (Public)

#### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "role": "ROLE_USER"
}
```

**Response (Success):**
```json
{
  "message": "User registered successfully!"
}
```

**Response (Error):**
```json
{
  "message": "Error: Username is already taken!"
}
```

---

#### 2. Login User
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (Success):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

**Response (Error):**
```json
{
  "message": "Invalid username or password"
}
```

---

### User Endpoints (Authenticated)

#### 3. Get Current User
**GET** `/api/users/me`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (Success):**
```json
{
  "id": 1,
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

**Response (Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users/me"
}
```

---

### Admin Endpoints (ROLE_ADMIN Only)

#### 4. Get All Users
**GET** `/api/admin/users`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (Success):**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "role": "ROLE_USER"
  },
  {
    "id": 2,
    "username": "admin_user",
    "role": "ROLE_ADMIN"
  }
]
```

**Response (Forbidden - Non-Admin):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## 📖 Interactive API Documentation (Swagger UI)

This application includes **Swagger UI** for interactive API documentation and testing!

### 🔗 Access Swagger UI

Once the application is running, open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

### ✨ Features

- 🎯 **Try it out** - Execute real API calls directly from the browser
- 📝 **Request/Response schemas** - See expected data structures
- 🔐 **JWT Authentication** - Authorize with JWT tokens
- 📋 **All endpoints documented** - Complete API reference
- 🎨 **Interactive testing** - No need for Postman or cURL

### 🚀 Quick Test Flow

1. **Open Swagger UI**: `http://localhost:8080/swagger-ui.html`
2. **Register**: Use `POST /api/auth/register` to create a user
3. **Login**: Use `POST /api/auth/login` to get JWT token
4. **Authorize**: Click 🔓 button and paste token
5. **Test**: Try authenticated endpoints like `/api/users/me`

**For detailed Swagger usage guide, see [SWAGGER_GUIDE.md](SWAGGER_GUIDE.md)**

---

## 🧪 Testing the API

### Using cURL

1. **Register a User:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123","role":"ROLE_USER"}'
```

2. **Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123"}'
```

3. **Get Current User (use token from login):**
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <your_jwt_token>"
```

4. **Register Admin:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","role":"ROLE_ADMIN"}'
```

5. **Get All Users (Admin only):**
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin_jwt_token>"
```

### Using Postman

1. Import the provided collection or create requests manually
2. For authenticated endpoints, add header: `Authorization: Bearer <token>`
3. Use the token received from login response

---

## 🏗️ Project Structure

```
src/main/java/com/example/auth/
│
├── config/
│   └── SecurityConfig.java          # Security configuration
│
├── controller/
│   ├── AuthController.java          # Authentication endpoints
│   ├── UserController.java          # User endpoints
│   └── AdminController.java         # Admin endpoints
│
├── dto/
│   ├── LoginRequest.java            # Login request DTO
│   ├── RegisterRequest.java         # Registration request DTO
│   ├── JwtResponse.java             # JWT response DTO
│   ├── UserResponse.java            # User response DTO
│   └── MessageResponse.java         # Message response DTO
│
├── entity/
│   ├── User.java                    # User entity
│   └── Role.java                    # Role entity
│
├── exception/
│   └── GlobalExceptionHandler.java  # Global exception handling
│
├── repository/
│   ├── UserRepository.java          # User repository
│   └── RoleRepository.java          # Role repository
│
├── security/
│   ├── JwtUtil.java                 # JWT utility class
│   ├── JwtAuthenticationFilter.java # JWT filter
│   ├── UserDetailsServiceImpl.java  # User details service
│   └── JwtAuthenticationEntryPoint.java
│
├── service/
│   ├── AuthService.java             # Authentication service
│   └── UserService.java             # User service
│
└── SpringJwtAuthApplication.java    # Main application class

src/main/resources/
│
├── db/migration/                     # Flyway migration scripts
│   ├── V1__create_roles_table.sql
│   ├── V2__create_users_table.sql
│   └── V3__insert_default_roles.sql
│
└── application.properties            # Application configuration
```

---

## 🔒 Security Features

- **JWT Authentication**: Stateless authentication using JWT tokens
- **BCrypt Password Encoding**: Passwords are encrypted using BCrypt
- **Role-Based Access Control**: Endpoints protected by roles (USER, ADMIN)
- **Stateless Session Management**: No server-side sessions
- **Exception Handling**: Global exception handling for proper error responses

---

## 🔄 Database Migration with Flyway

This project uses **Flyway** for database version control and migrations.

### Migration Files

All migrations are in `src/main/resources/db/migration/`:

- **V1__create_roles_table.sql** - Creates roles table
- **V2__create_users_table.sql** - Creates users table with indexes
- **V3__insert_default_roles.sql** - Inserts ROLE_USER and ROLE_ADMIN

### Adding New Migrations

Create a new SQL file following the naming convention:
```
V{version}__{description}.sql
```

Example: `V4__add_email_to_users.sql`
```sql
ALTER TABLE users ADD email NVARCHAR(100);
GO
```

Flyway automatically runs new migrations on application startup.

### Migration Commands

```bash
# View migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Run migrations manually
mvn flyway:migrate
```

For detailed Flyway usage, see [FLYWAY_GUIDE.md](FLYWAY_GUIDE.md)

---

## ⚙️ Configuration

### JWT Settings (application.properties)

```properties
# JWT secret key (minimum 256 bits for HS512)
jwt.secret=MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS512Algorithm

# JWT expiration time in milliseconds (1 hour = 3600000)
jwt.expiration=3600000
```

### Database Settings

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
spring.jpa.hibernate.ddl-auto=validate
```

### Flyway Settings

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

---

## 📝 Available Roles

The application initializes with two default roles:
- `ROLE_USER` - Regular user access
- `ROLE_ADMIN` - Administrative access

---

## 🛡️ Error Handling

The application includes comprehensive error handling:

- **400 Bad Request** - Validation errors, duplicate username
- **401 Unauthorized** - Invalid credentials, missing/invalid token
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - User not found
- **500 Internal Server Error** - Server errors

---

## 📦 Dependencies

Key dependencies used in this project:

- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-security` - Security features
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-validation` - Input validation
- `mssql-jdbc` - MS SQL Server driver
- `flyway-core` - Database migration tool
- `flyway-sqlserver` - Flyway MS SQL Server support
- `jjwt` - JWT implementation
- `lombok` - Reduce boilerplate code

---

## 🔄 Workflow

1. User registers with username, password, and role
2. Flyway creates database schema and inserts default roles on first startup
3. User logs in and receives JWT token
4. User includes JWT token in Authorization header for protected endpoints
5. Server validates token and grants access based on role
6. Token expires after configured time (default: 1 hour)

---

## 📄 License

This project is open-source and available under the MIT License.

---

## 📚 Additional Documentation

- [Quick Start Guide](QUICK_START.md) - Get running in minutes!
- [Swagger/OpenAPI Guide](SWAGGER_GUIDE.md) - Interactive API documentation
- [API Examples and Testing](API_EXAMPLES.md) - Sample requests and responses
- [Database Setup Guide](DATABASE_SETUP.md) - MS SQL Server configuration
- [Flyway Migration Guide](FLYWAY_GUIDE.md) - Database version control and migrations

---

## 👨‍💻 Author

Created as a complete example of Spring Boot JWT authentication with PostgreSQL.
