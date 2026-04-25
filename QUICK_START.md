# Quick Start Guide

Get the Spring Boot JWT Authentication application running in minutes!

## Prerequisites

- Java 11 or higher installed
- Maven 3.6+ installed
- PostgreSQL 13+ installed and running
- Git (optional)

---

## Step 1: Setup PostgreSQL

### Option A: Local Installation

**Windows** — Download the installer from https://www.postgresql.org/download/windows/ and run it.  
**macOS** — `brew install postgresql@15 && brew services start postgresql@15`  
**Ubuntu/Debian** — `sudo apt install postgresql postgresql-contrib && sudo systemctl start postgresql`

### Option B: Docker (Cross-platform)

```bash
docker run --name postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_USER=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### Create the database

```bash
# Using psql CLI
psql -U postgres -c "CREATE DATABASE cka_db;"

# Or inside Docker
docker exec -it postgres psql -U postgres -c "CREATE DATABASE cka_db;"
```

### Verify

```bash
psql -U postgres -c "\l" | grep cka_db
```

---

## Step 2: Configure Application

Open `src/main/resources/application.properties` and update if needed:

```properties
# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/cka_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Step 3: Build the Application

```bash
cd customer-knowledge-assistant
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: xx.xxx s
```

---

## Step 4: Run the Application

```bash
mvn spring-boot:run
```

### What Happens on First Start?

1. Application starts on port 8080
2. Flyway runs migrations automatically:
   - V1: Creates `roles` table
   - V2: Creates `users` table
   - V3: Inserts default roles (`ROLE_USER`, `ROLE_ADMIN`)
   - V4: Extends `users` with business fields + creates `user_role` table
   - V5: Creates `projects` and `user_project` tables
   - V6: Creates `datasources` and `project_datasource` tables
3. Application ready to accept requests

### Expected Console Output

```
Flyway Community Edition x.x.x by Redgate
Database: jdbc:postgresql://localhost:5432/cka_db (PostgreSQL x.x)
Successfully validated x migrations
Successfully applied x migrations
Started SpringJwtAuthApplication in x.xxx seconds
```

---

## Step 5: Test the API

### Swagger UI

Open your browser at: **http://localhost:8080/swagger-ui.html**

### Test 1: Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123","role":"ROLE_USER"}'
```

Expected Response:
```json
{ "message": "User registered successfully!" }
```

### Test 2: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123"}'
```

Expected Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

Copy the token for subsequent requests.

### Test 3: Get Current User

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Test 4: Create a Datasource

```bash
curl -X POST http://localhost:8080/api/management/datasources \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"name":"Primary Storage","status":"Active"}'
```

### Test 5: Create a Project

```bash
curl -X POST http://localhost:8080/api/management/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"name":"Alpha","path":"/projects/alpha","databaseTableName":"alpha_data","datasourceId":1,"apiKey":"key-abc","folderUrl":"s3://bucket/alpha"}'
```

### Test 6: Create a Managed User

```bash
curl -X POST http://localhost:8080/api/management/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"username":"jane","password":"pass123","fullname":"Jane Doe","jobTitle":"Analyst","email":"jane@example.com","roleId":1,"projectId":1}'
```

---

## Step 6: Verify Database

```bash
# Connect to the database
psql -U postgres -d cka_db

# List tables
\dt

# Check roles
SELECT * FROM roles;

# Check Flyway history
SELECT installed_rank, version, description, success FROM flyway_schema_history;
```

Expected tables: `flyway_schema_history`, `roles`, `users`, `user_role`, `projects`, `user_project`, `datasources`, `project_datasource`

---

## Troubleshooting

### Problem: Cannot connect to PostgreSQL

```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# Start service (Linux/macOS)
sudo systemctl start postgresql
brew services start postgresql   # macOS with Homebrew

# Windows
Start-Service postgresql-x64-15
```

### Problem: Authentication failed

Ensure the credentials in `application.properties` match your PostgreSQL user.  
To reset the `postgres` user password:

```sql
psql -U postgres
ALTER USER postgres WITH PASSWORD 'postgres';
```

### Problem: Database does not exist

```bash
psql -U postgres -c "CREATE DATABASE cka_db;"
```

### Problem: Flyway migration failed

```bash
# Repair Flyway
mvn flyway:repair

# Or manually clear the failed entry
psql -U postgres -d cka_db -c "DELETE FROM flyway_schema_history WHERE success = false;"
```

### Problem: Port 5432 already in use

```bash
# Find the process
netstat -ano | findstr :5432       # Windows
lsof -i :5432                      # Linux/macOS
```

---

## Project Structure

```
customer-knowledge-assistant/
├── src/main/
│   ├── java/com/example/auth/
│   │   ├── config/           # Security + OpenAPI configuration
│   │   ├── controller/       # REST controllers (Auth, Admin, Management)
│   │   ├── dto/              # Request/Response DTOs + PagedResponse
│   │   ├── entity/           # JPA entities (User, Role, Project, Datasource, …)
│   │   ├── exception/        # GlobalExceptionHandler, ResourceNotFoundException
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter, util, entry point
│   │   └── service/          # Business logic services
│   └── resources/
│       ├── db/migration/     # Flyway SQL migrations V1–V6
│       └── application.properties
└── pom.xml
```

---

**Happy Coding!**
