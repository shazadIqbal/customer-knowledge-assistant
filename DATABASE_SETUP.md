# Database Setup Instructions — PostgreSQL

## Installing PostgreSQL

### Windows
1. Download the installer from https://www.postgresql.org/download/windows/
2. Run the installer (includes pgAdmin and command-line tools)
3. During setup, set a password for the `postgres` superuser
4. Leave the default port as **5432**

### macOS
```bash
brew install postgresql@15
brew services start postgresql@15
echo 'export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"' >> ~/.zshrc
```

### Ubuntu / Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### Docker (Cross-platform)
```bash
docker run --name postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_USER=postgres \
  -p 5432:5432 \
  -d postgres:15
```

---

## Creating the Database

### Using psql
```bash
psql -U postgres -h localhost

# Inside psql shell:
CREATE DATABASE cka_db;
\l          -- list databases to verify
\q          -- exit
```

### Using a single command
```bash
psql -U postgres -c "CREATE DATABASE cka_db;"
```

### Using Docker
```bash
docker exec -it postgres psql -U postgres -c "CREATE DATABASE cka_db;"
```

---

## Configure the Application

Update `src/main/resources/application.properties`:

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/cka_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Flyway Migrations

Flyway creates the entire schema automatically on first startup.

### Migration Files (`src/main/resources/db/migration/`)

| File | What it does |
|------|-------------|
| `V1__create_roles_table.sql` | Creates `roles` table |
| `V2__create_users_table.sql` | Creates `users` table with FK to `roles` |
| `V3__insert_default_roles.sql` | Seeds `ROLE_USER` and `ROLE_ADMIN` |
| `V4__extend_users_and_user_role.sql` | Adds business columns to `users`; creates `user_role` junction table |
| `V5__create_projects_and_user_project.sql` | Creates `projects` and `user_project` junction table |
| `V6__create_datasources_and_project_datasource.sql` | Creates `datasources` and `project_datasource` junction table |

---

## Database Schema

### `roles`
```sql
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

### `users`
```sql
CREATE TABLE users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role_id  BIGINT       NOT NULL REFERENCES roles(id),
    fullname VARCHAR(100),
    job_title VARCHAR(100),
    email    VARCHAR(150),
    status   VARCHAR(20)  DEFAULT 'Active'
);
```

### `user_role` (junction)
```sql
CREATE TABLE user_role (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### `projects`
```sql
CREATE TABLE projects (
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    path                VARCHAR(255),
    database_table_name VARCHAR(100),
    status              VARCHAR(20)  NOT NULL DEFAULT 'Active'
);
```

### `user_project` (junction)
```sql
CREATE TABLE user_project (
    user_id    BIGINT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, project_id)
);
```

### `datasources`
```sql
CREATE TABLE datasources (
    id     BIGSERIAL    PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'Active'
);
```

### `project_datasource` (junction with payload)
```sql
CREATE TABLE project_datasource (
    project_id    BIGINT        NOT NULL REFERENCES projects(id)    ON DELETE CASCADE,
    datasource_id BIGINT        NOT NULL REFERENCES datasources(id) ON DELETE CASCADE,
    api_key       VARCHAR(500),
    folder_url    VARCHAR(1000),
    PRIMARY KEY (project_id, datasource_id)
);
```

---

## Verify Database Setup

```bash
psql -U postgres -d cka_db

-- List all tables
\dt

-- Check roles
SELECT * FROM roles;

-- Check Flyway migration history
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected tables: `flyway_schema_history`, `roles`, `users`, `user_role`, `projects`, `user_project`, `datasources`, `project_datasource`

---

## Troubleshooting

### Cannot connect to PostgreSQL

```bash
# Check if the service is up
pg_isready -h localhost -p 5432

# Linux
sudo systemctl status postgresql
sudo systemctl start postgresql

# macOS (Homebrew)
brew services restart postgresql@15

# Windows PowerShell
Start-Service postgresql-x64-15
```

### Authentication failed (`FATAL: password authentication failed`)

```bash
psql -U postgres
ALTER USER postgres WITH PASSWORD 'postgres';
```

Or update `application.properties` to use the correct credentials.

### Flyway migration failed

```bash
# Check current Flyway state
mvn flyway:info

# Repair (removes FAILED entries from flyway_schema_history)
mvn flyway:repair

# Manual cleanup in psql
psql -U postgres -d cka_db -c "DELETE FROM flyway_schema_history WHERE success = false;"
```

### Reset the database (start fresh)

```bash
psql -U postgres -c "DROP DATABASE IF EXISTS cka_db;"
psql -U postgres -c "CREATE DATABASE cka_db;"
```

---

## Connection URL Formats

### Local default
```
jdbc:postgresql://localhost:5432/cka_db
```

### Remote server
```
jdbc:postgresql://hostname:5432/cka_db
```

### Custom port
```
jdbc:postgresql://localhost:5433/cka_db
```

### With SSL (production)
```
jdbc:postgresql://hostname:5432/cka_db?ssl=true&sslmode=require
```

---

## Production Considerations

### 1. Use environment variables
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/cka_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
```

### 2. Connection pooling (HikariCP — included by default)
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

### 3. Enable SSL in production
```properties
spring.datasource.url=jdbc:postgresql://hostname:5432/cka_db?ssl=true&sslmode=require
```

---

## Useful PostgreSQL Queries

### List all tables
```sql
SELECT tablename FROM pg_tables WHERE schemaname = 'public';
```

### Describe a table
```sql
\d users
```

### List all foreign keys
```sql
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name  AS foreign_table,
    ccu.column_name AS foreign_column
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage  AS kcu USING (constraint_name, table_schema)
JOIN information_schema.constraint_column_usage AS ccu USING (constraint_name, table_schema)
WHERE tc.constraint_type = 'FOREIGN KEY';
```

### Database size
```sql
SELECT pg_size_pretty(pg_database_size('cka_db'));
```

### List all indexes
```sql
SELECT indexname, tablename, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename;
```
