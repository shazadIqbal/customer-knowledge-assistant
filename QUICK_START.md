# Quick Start Guide

Get the Spring Boot JWT Authentication application running in minutes!

## Prerequisites

- ✅ Java 11 or higher installed
- ✅ Maven 3.6+ installed
- ✅ MS SQL Server installed and running
- ✅ Git (optional)

## Step 1: Setup MS SQL Server

### Option A: Using SQL Server Management Studio (SSMS)

1. Open SSMS
2. Connect using:
   - Server: `localhost`
   - Authentication: `SQL Server Authentication`
   - Login: `sa`
   - Password: `YourStrong@Passw0rd`

3. Run the setup script:
   - Open `database-setup.sql`
   - Execute (or press F5)

### Option B: Using sqlcmd

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -i database-setup.sql
```

### Option C: Using Docker

```bash
# Start SQL Server in Docker
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" \
   -p 1433:1433 --name sql-server \
   -d mcr.microsoft.com/mssql/server:2022-latest

# Wait 30 seconds for SQL Server to start
# Create database
docker exec -it sql-server /opt/mssql-tools/bin/sqlcmd \
   -S localhost -U sa -P YourStrong@Passw0rd \
   -Q "CREATE DATABASE TEST_DB"
```

### Verify Database

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -Q "SELECT name FROM sys.databases WHERE name='TEST_DB'"
```

Expected output: `TEST_DB`

---

## Step 2: Configure Application

Open `src/main/resources/application.properties` and update if needed:

```properties
# Update these if your SQL Server credentials are different
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
```

**Note**: Default configuration should work if you used `sa` with password `YourStrong@Passw0rd`

---

## Step 3: Build the Application

```bash
# Navigate to project directory
cd base-project

# Clean and build
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

### What Happens?

1. ✅ Application starts on port 8080
2. ✅ Flyway runs migrations automatically:
   - V1: Creates `roles` table
   - V2: Creates `users` table
   - V3: Inserts default roles (ROLE_USER, ROLE_ADMIN)
3. ✅ Application ready to accept requests

### Expected Console Output

```
...
Flyway Community Edition x.x.x
Database: jdbc:sqlserver://localhost:1433 (Microsoft SQL Server x.x)
Successfully validated x migrations
...
Successfully applied x migrations
...
Started SpringJwtAuthApplication in x.xxx seconds
```

---

## Step 5: Test the API

### Test 1: Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"john_doe\",\"password\":\"password123\",\"role\":\"ROLE_USER\"}"
```

**Expected Response:**
```json
{
  "message": "User registered successfully!"
}
```

### Test 2: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"john_doe\",\"password\":\"password123\"}"
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

**📋 Copy the token for next step!**

### Test 3: Get Current User (Authenticated)

```bash
# Replace YOUR_TOKEN_HERE with the token from login
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "role": "ROLE_USER"
}
```

### Test 4: Register Admin User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"role\":\"ROLE_ADMIN\"}"
```

### Test 5: Login as Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

**📋 Copy admin token!**

### Test 6: Get All Users (Admin Only)

```bash
# Replace ADMIN_TOKEN_HERE with admin token
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer ADMIN_TOKEN_HERE"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "username": "john_doe",
    "role": "ROLE_USER"
  },
  {
    "id": 2,
    "username": "admin",
    "role": "ROLE_ADMIN"
  }
]
```

---

## Step 6: Verify Database

### Check Tables Created by Flyway

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -d TEST_DB -Q "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE'"
```

**Expected Tables:**
- `flyway_schema_history` - Flyway migration tracking
- `roles` - User roles
- `users` - User accounts

### Check Default Roles

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -d TEST_DB -Q "SELECT * FROM roles"
```

**Expected Output:**
```
id  name
--- -----------
1   ROLE_USER
2   ROLE_ADMIN
```

### Check Flyway Migration History

```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -d TEST_DB -Q "SELECT installed_rank, version, description, success FROM flyway_schema_history"
```

**Expected Output:**
```
installed_rank  version  description              success
--------------  -------  ----------------------   -------
1               1        create roles table       1
2               2        create users table       1
3               3        insert default roles     1
```

---

## 🎉 Success!

Your application is now running! You have:

- ✅ MS SQL Server database configured
- ✅ Flyway migrations executed
- ✅ Tables created (roles, users)
- ✅ Default roles inserted
- ✅ JWT authentication working
- ✅ User registration working
- ✅ Login working
- ✅ Protected endpoints working
- ✅ Role-based access control working

---

## 📁 Project Files Overview

```
base-project/
│
├── src/main/
│   ├── java/com/example/auth/
│   │   ├── config/           # Security configuration
│   │   ├── controller/       # REST API endpoints
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # JPA entities (User, Role)
│   │   ├── exception/        # Global error handling
│   │   ├── repository/       # Database repositories
│   │   ├── security/         # JWT utilities and filters
│   │   └── service/          # Business logic
│   │
│   └── resources/
│       ├── db/migration/     # Flyway SQL migration scripts
│       │   ├── V1__create_roles_table.sql
│       │   ├── V2__create_users_table.sql
│       │   └── V3__insert_default_roles.sql
│       └── application.properties
│
├── pom.xml                   # Maven dependencies
├── database-setup.sql        # Database creation script
│
└── Documentation/
    ├── README.md             # Main documentation
    ├── API_EXAMPLES.md       # API usage examples
    ├── DATABASE_SETUP.md     # Database configuration
    ├── FLYWAY_GUIDE.md       # Migration guide
    └── QUICK_START.md        # This file
```

---

## 🔧 Troubleshooting

### Problem: Cannot connect to SQL Server

**Check if SQL Server is running:**
```powershell
Get-Service | Where-Object {$_.Name -like '*SQL*'}
```

**Start SQL Server:**
```powershell
Start-Service MSSQLSERVER
```

### Problem: Login failed for user 'sa'

**Enable SA login:**
```sql
sqlcmd -S localhost -W  # Windows Authentication

ALTER LOGIN sa ENABLE;
GO
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Passw0rd';
GO
```

### Problem: TCP/IP not enabled

1. Open SQL Server Configuration Manager
2. SQL Server Network Configuration → Protocols for MSSQLSERVER
3. Right-click TCP/IP → Enable
4. Restart SQL Server service

### Problem: Port 1433 in use

**Check what's using port 1433:**
```powershell
netstat -ano | findstr :1433
```

### Problem: Application won't start

**Check Java version:**
```bash
java -version
```

Should be Java 11 or higher.

**Check Maven:**
```bash
mvn -version
```

**Clean and rebuild:**
```bash
mvn clean install -U
```

### Problem: Flyway migration failed

**Check migration history:**
```sql
SELECT * FROM flyway_schema_history;
```

**Repair Flyway:**
```bash
mvn flyway:repair
```

---

## 🚀 Next Steps

### 1. Customize Application

- Update JWT secret in `application.properties`
- Change JWT expiration time
- Add more roles
- Add more user fields (email, phone, etc.)

### 2. Add Features

- Email verification
- Password reset
- Refresh tokens
- User profile management
- Audit logging

### 3. Create New Migrations

Create `V4__add_email_to_users.sql`:
```sql
ALTER TABLE users ADD email NVARCHAR(100);
GO
CREATE UNIQUE INDEX IDX_users_email ON users(email) WHERE email IS NOT NULL;
GO
```

### 4. Deploy to Production

See [Database Setup Guide](DATABASE_SETUP.md) for production best practices.

---

## 📚 Learn More

- [Full README](README.md) - Complete project documentation
- [API Examples](API_EXAMPLES.md) - Detailed API examples with Postman
- [Database Setup](DATABASE_SETUP.md) - Advanced database configuration
- [Flyway Guide](FLYWAY_GUIDE.md) - Database migration patterns

---

## 🆘 Need Help?

1. Check the documentation in the `Documentation` folder
2. Review error logs in console
3. Verify database connection settings
4. Ensure SQL Server is running
5. Check Flyway migration history

---

## ✅ Quick Verification Checklist

Before asking for help, verify:

- [ ] Java 11+ installed and in PATH
- [ ] Maven installed and in PATH
- [ ] SQL Server running
- [ ] Database TEST_DB exists
- [ ] Can connect with sa credentials
- [ ] TCP/IP enabled for SQL Server
- [ ] Port 1433 accessible
- [ ] No firewall blocking connections
- [ ] application.properties configured correctly
- [ ] Maven build successful
- [ ] No compilation errors

---

**Happy Coding! 🎉**
