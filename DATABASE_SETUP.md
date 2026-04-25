# Database Setup Instructions

## MS SQL Server Installation

### Windows
1. Download SQL Server from: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
2. Download SQL Server Express (free) or Developer Edition
3. Run the installer and choose "Basic" installation
4. Download and install SQL Server Management Studio (SSMS): https://aka.ms/ssmsfullsetup
5. Enable SQL Server Authentication:
   - Open SSMS and connect to your SQL Server instance
   - Right-click on the server → Properties → Security
   - Select "SQL Server and Windows Authentication mode"
   - Restart SQL Server service

### Setting Up SQL Server Authentication

1. Open SQL Server Configuration Manager
2. Enable TCP/IP protocol:
   - SQL Server Network Configuration → Protocols for MSSQLSERVER
   - Right-click TCP/IP → Enable
   - Restart SQL Server service

3. Set SA password:
```sql
ALTER LOGIN sa ENABLE;
GO
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Passw0rd';
GO
```

### Docker (Cross-platform)
```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" \
   -p 1433:1433 --name sql-server \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

---

## Database Creation

### Option 1: Using SQL Server Management Studio (SSMS)

1. Open SSMS
2. Connect to your SQL Server instance
   - Server name: localhost
   - Authentication: SQL Server Authentication
   - Login: sa
   - Password: YourStrong@Passw0rd
3. Right-click on "Databases" → "New Database"
4. Enter database name: `TEST_DB`
5. Click "OK"

### Option 2: Using sqlcmd Command Line

1. Open Command Prompt or PowerShell
2. Connect to SQL Server:
```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd
```

3. Create database:
```sql
CREATE DATABASE TEST_DB;
GO
```

4. Verify database creation:
```sql
SELECT name FROM sys.databases WHERE name = 'TEST_DB';
GO
```

5. Exit sqlcmd:
```sql
EXIT
```

### Option 3: Using SQL File

Create a file `setup.sql`:
```sql
-- Create database
CREATE DATABASE TEST_DB;
GO

-- Use database
USE TEST_DB;
GO

-- Verify
SELECT DB_NAME() AS CurrentDatabase;
GO
```

Run the file:
```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -i setup.sql
```

---

## Configure Application

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrong@Passw0rd
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Flyway Migrations

Flyway will automatically create the database schema when you run the application for the first time.

### Migration Files

Located in `src/main/resources/db/migration/`:

1. **V1__create_roles_table.sql** - Creates roles table
2. **V2__create_users_table.sql** - Creates users table with foreign key
3. **V3__insert_default_roles.sql** - Inserts ROLE_USER and ROLE_ADMIN

### Adding New Migrations

Create new migration files following the naming convention:
```
V{version}__{description}.sql
```

Examples:
- `V4__add_email_to_users.sql`
- `V5__create_audit_log_table.sql`

Flyway executes migrations in order and tracks them in the `flyway_schema_history` table.

---

## Database Schema (Created by Flyway)

### roles table
```sql
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);
```

### users table
```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Initial Data (Inserted by V3 migration)
```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```

---

## Verify Database Setup

1. Connect to the database using SSMS or sqlcmd:
```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -d TEST_DB
```

2. Check tables (after running the application):
```sql
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE';
GO
```

Expected output:
```
TABLE_NAME
-----------------
flyway_schema_history
roles
users
```

3. Check roles:
```sql
SELECT * FROM roles;
GO
```

Expected output:
```
id    name
----  -----------
1     ROLE_USER
2     ROLE_ADMIN
```

4. Check Flyway migration history:
```sql
SELECT * FROM flyway_schema_history;
GO
```

---

## Troubleshooting

### Problem: Cannot connect to database

**Solution 1**: Check SQL Server is running
```powershell
# Windows PowerShell
Get-Service | Where-Object {$_.Name -like '*SQL*'}

# Or check in Services app (services.msc)
```

**Solution 2**: Enable TCP/IP protocol
1. Open SQL Server Configuration Manager
2. SQL Server Network Configuration → Protocols
3. Enable TCP/IP
4. Restart SQL Server service

**Solution 3**: Check firewall
```powershell
# Allow SQL Server through firewall
New-NetFirewallRule -DisplayName "SQL Server" -Direction Inbound -Protocol TCP -LocalPort 1433 -Action Allow
```

### Problem: Login failed for user 'sa'

**Solution**: Enable SQL Server Authentication
```sql
-- Enable sa login and set password
ALTER LOGIN sa ENABLE;
GO
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Passw0rd';
GO
```

### Problem: Database already exists

```sql
-- Drop and recreate
USE master;
GO
DROP DATABASE IF EXISTS TEST_DB;
GO
CREATE DATABASE TEST_DB;
GO
```

### Problem: Flyway migration fails

**Solution 1**: Check migration file syntax
- Ensure SQL syntax is correct for MS SQL Server
- Check file naming: `V{version}__{description}.sql`

**Solution 2**: Repair Flyway baseline
```sql
-- Delete flyway history (use with caution!)
DELETE FROM flyway_schema_history;
```

Or add to application.properties:
```properties
spring.flyway.clean-on-validation-error=true
```

---

## Reset Database

To start fresh:

### Using SSMS
1. Right-click on TEST_DB → Delete
2. Create new database

### Using sqlcmd
```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd
```

```sql
USE master;
GO
DROP DATABASE IF EXISTS TEST_DB;
GO
CREATE DATABASE TEST_DB;
GO
EXIT
```

---

## Database Connection URL Formats

### Local SQL Server (Default instance)
```
jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
```

### Named Instance
```
jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
```

### Remote SQL Server
```
jdbc:sqlserver://hostname:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=false
```

### With Windows Authentication
```
jdbc:sqlserver://localhost:1433;databaseName=TEST_DB;integratedSecurity=true
```

### Custom Port
```
jdbc:sqlserver://localhost:1435;databaseName=TEST_DB;encrypt=true;trustServerCertificate=true
```

---

## Testing Database Connection

### Using Java Code
```java
@SpringBootTest
class DatabaseConnectionTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    void testConnection() throws SQLException {
        assertNotNull(dataSource);
        Connection conn = dataSource.getConnection();
        assertTrue(conn.isValid(1));
        conn.close();
    }
}
```

### Using sqlcmd
```bash
sqlcmd -S localhost -U sa -P YourStrong@Passw0rd -d TEST_DB -Q "SELECT 1 AS test;"
```

Expected output:
```
test
----
1
```

---

## Flyway Commands

### Check migration status
Flyway tracks migrations automatically. View history:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
GO
```

### Manual Flyway commands (using Maven)

```bash
# Show migration info
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Migrate database
mvn flyway:migrate

# Clean database (DANGEROUS - deletes all objects!)
mvn flyway:clean
```

---

## Production Considerations

1. **Use environment variables**:
```properties
spring.datasource.url=${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=TEST_DB}
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD}
```

2. **Use connection pooling** (already included via HikariCP):
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

3. **Use validate for production**:
```properties
# Development (with Flyway)
spring.jpa.hibernate.ddl-auto=validate

# Flyway handles all schema changes
spring.flyway.enabled=true
```

4. **Enable SSL for production**:
```properties
spring.datasource.url=jdbc:sqlserver://hostname:1433;databaseName=TEST_DB;encrypt=true;trustServerCertificate=false
```

5. **Flyway baseline for existing databases**:
```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

---

## Creating Custom Migrations

### Example: Add email column to users

Create `V4__add_email_to_users.sql`:
```sql
-- Add email column to users table
ALTER TABLE users 
ADD email NVARCHAR(100);
GO

-- Create unique index on email
CREATE UNIQUE INDEX IDX_users_email 
ON users(email) 
WHERE email IS NOT NULL;
GO
```

### Example: Create audit log table

Create `V5__create_audit_log.sql`:
```sql
-- Create audit log table
CREATE TABLE audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action NVARCHAR(50) NOT NULL,
    timestamp DATETIME2 DEFAULT GETDATE(),
    details NVARCHAR(MAX),
    CONSTRAINT FK_audit_users FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- Create index on timestamp
CREATE INDEX IDX_audit_timestamp ON audit_log(timestamp);
GO
```

---

## Useful SQL Server Queries

### List all tables
```sql
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE';
GO
```

### Show table structure
```sql
EXEC sp_help 'users';
GO
```

### List all databases
```sql
SELECT name FROM sys.databases;
GO
```

### Get database size
```sql
EXEC sp_spaceused;
GO
```

### List all foreign keys
```sql
SELECT 
    fk.name AS FK_Name,
    tp.name AS Parent_Table,
    cp.name AS Parent_Column,
    tr.name AS Referenced_Table,
    cr.name AS Referenced_Column
FROM sys.foreign_keys AS fk
INNER JOIN sys.foreign_key_columns AS fkc ON fk.object_id = fkc.constraint_object_id
INNER JOIN sys.tables AS tp ON fkc.parent_object_id = tp.object_id
INNER JOIN sys.columns AS cp ON fkc.parent_object_id = cp.object_id AND fkc.parent_column_id = cp.column_id
INNER JOIN sys.tables AS tr ON fkc.referenced_object_id = tr.object_id
INNER JOIN sys.columns AS cr ON fkc.referenced_object_id = cr.object_id AND fkc.referenced_column_id = cr.column_id;
GO
```
