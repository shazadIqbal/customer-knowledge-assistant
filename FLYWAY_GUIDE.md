# Flyway Migration Guide

## Overview

Flyway is a database migration tool that helps you version control your database schema. This project uses Flyway to manage all database changes in a structured, repeatable way.

## Why Flyway?

- **Version Control**: Track all database changes in SQL files
- **Repeatable**: Same migrations produce same results
- **Automatic**: Runs migrations on application startup
- **Safe**: Validates checksums to prevent accidental changes
- **Team-Friendly**: Easy to collaborate on database changes

## Migration File Structure

Migrations are located in: `src/main/resources/db/migration/`

### Naming Convention

```
V{version}__{description}.sql
```

- **V**: Prefix for versioned migrations (required)
- **{version}**: Version number (e.g., 1, 2, 3, or 1.1, 1.2)
- **__**: Double underscore separator (required)
- **{description}**: Brief description using underscores
- **.sql**: File extension

### Examples

```
V1__create_roles_table.sql
V2__create_users_table.sql
V3__insert_default_roles.sql
V4__add_email_to_users.sql
V5__create_audit_log.sql
V1.1__add_index_to_username.sql
```

## Current Migrations

### V1__create_roles_table.sql
```sql
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);
```

**Purpose**: Creates the roles table to store user roles (ROLE_USER, ROLE_ADMIN)

---

### V2__create_users_table.sql
```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IDX_users_username ON users(username);
CREATE INDEX IDX_users_role_id ON users(role_id);
```

**Purpose**: Creates users table with foreign key to roles and performance indexes

---

### V3__insert_default_roles.sql
```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```

**Purpose**: Inserts the default roles required by the application

---

## How Flyway Works

1. **Application Startup**: Flyway scans `db/migration` folder
2. **Check History**: Looks at `flyway_schema_history` table
3. **Find Pending**: Identifies migrations not yet applied
4. **Execute**: Runs pending migrations in order
5. **Record**: Saves migration details to history table

### Migration States

- **Pending**: Not yet executed
- **Success**: Executed successfully
- **Failed**: Execution failed (needs manual fix)

## Flyway Schema History Table

Flyway automatically creates a `flyway_schema_history` table to track migrations:

```sql
SELECT * FROM flyway_schema_history;
```

Columns:
- `installed_rank`: Execution order
- `version`: Migration version
- `description`: Migration description
- `type`: Migration type (SQL, Java)
- `script`: Migration filename
- `checksum`: File checksum for validation
- `installed_by`: User who ran migration
- `installed_on`: Timestamp
- `execution_time`: Duration in milliseconds
- `success`: Success/failure flag

## Creating New Migrations

### Step 1: Create Migration File

Create a new file in `src/main/resources/db/migration/`:

```
V4__add_email_to_users.sql
```

### Step 2: Write SQL

```sql
-- Add email column
ALTER TABLE users 
ADD email NVARCHAR(100);
GO

-- Add unique constraint
CREATE UNIQUE INDEX IDX_users_email 
ON users(email) 
WHERE email IS NOT NULL;
GO
```

### Step 3: Run Application

Flyway will automatically execute the migration on startup.

### Step 4: Verify

```sql
-- Check if migration ran
SELECT * FROM flyway_schema_history WHERE version = '4';

-- Check table structure
EXEC sp_help 'users';
```

## Common Migration Patterns

### Adding a Column

```sql
-- V4__add_email_to_users.sql
ALTER TABLE users 
ADD email NVARCHAR(100);
GO
```

### Adding a Column with Default Value

```sql
-- V5__add_created_at_to_users.sql
ALTER TABLE users 
ADD created_at DATETIME2 NOT NULL DEFAULT GETDATE();
GO
```

### Creating an Index

```sql
-- V6__add_index_to_email.sql
CREATE INDEX IDX_users_email ON users(email);
GO
```

### Creating a New Table

```sql
-- V7__create_refresh_tokens.sql
CREATE TABLE refresh_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token NVARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME2 NOT NULL,
    CONSTRAINT FK_tokens_users FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

CREATE INDEX IDX_tokens_user_id ON refresh_tokens(user_id);
GO
```

### Adding Foreign Key

```sql
-- V8__add_created_by_to_audit.sql
ALTER TABLE audit_log
ADD created_by_user_id BIGINT;
GO

ALTER TABLE audit_log
ADD CONSTRAINT FK_audit_created_by 
FOREIGN KEY (created_by_user_id) REFERENCES users(id);
GO
```

### Inserting Data

```sql
-- V9__insert_default_admin.sql
-- Insert default admin user (password: admin123)
DECLARE @role_id BIGINT;

SELECT @role_id = id FROM roles WHERE name = 'ROLE_ADMIN';

INSERT INTO users (username, password, role_id)
VALUES ('admin', '$2a$10$slYQm7V7hCpTa2hUKQLKkOJCkpDuqlRa7iduPvWZpLMQrJhH8Q8J6', @role_id);
GO
```

### Modifying Column

```sql
-- V10__increase_password_length.sql
ALTER TABLE users 
ALTER COLUMN password NVARCHAR(255) NOT NULL;
GO
```

### Dropping Column

```sql
-- V11__remove_legacy_field.sql
ALTER TABLE users 
DROP COLUMN legacy_field;
GO
```

### Renaming Column (SQL Server)

```sql
-- V12__rename_column.sql
EXEC sp_rename 'users.old_name', 'new_name', 'COLUMN';
GO
```

## Best Practices

### DO ✅

1. **Version Control**: Commit migration files to Git
2. **Descriptive Names**: Use clear, descriptive filenames
3. **One Purpose**: Each migration should do one logical thing
4. **Test Locally**: Test migrations on local database first
5. **Incremental**: Make small, incremental changes
6. **Use GO**: Separate batches in SQL Server with `GO`
7. **Comments**: Add comments explaining complex migrations
8. **Indexes**: Create indexes for foreign keys and frequently queried columns

### DON'T ❌

1. **Never Modify**: Don't modify executed migrations
2. **Don't Skip Versions**: Keep version numbers sequential
3. **Avoid Deleting**: Don't delete migration files
4. **No Manual Changes**: Don't manually edit production database
5. **Check Dependencies**: Ensure tables exist before adding foreign keys

## Configuration

### application.properties

```properties
# Enable Flyway
spring.flyway.enabled=true

# Create baseline for existing databases
spring.flyway.baseline-on-migrate=true

# Migration files location
spring.flyway.locations=classpath:db/migration

# File naming
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql

# Validation
spring.flyway.validate-on-migrate=true

# Disable for production!
# spring.flyway.clean-on-validation-error=false
```

### Hibernate Settings

```properties
# Use validate with Flyway (don't auto-generate schema)
spring.jpa.hibernate.ddl-auto=validate
```

## Maven Commands

```bash
# Show migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Run migrations manually
mvn flyway:migrate

# Repair failed migrations
mvn flyway:repair

# Clean database (DANGEROUS!)
mvn flyway:clean
```

## Troubleshooting

### Problem: Migration Failed

**Error**: Migration failed, database rolled back

**Solution**:
1. Check error in console
2. Fix the SQL in migration file
3. Run repair command:
```bash
mvn flyway:repair
```
4. Restart application

### Problem: Checksum Mismatch

**Error**: Migration checksum mismatch

**Cause**: Migration file was modified after execution

**Solution**:
```bash
# Option 1: Repair (updates checksum)
mvn flyway:repair

# Option 2: Create new migration
# Create V{next}__fix_previous_migration.sql
```

### Problem: Out of Order

**Error**: Detected resolved migration not applied to database

**Cause**: New migration added with lower version number

**Solution**:
```properties
# Allow out-of-order migrations (not recommended)
spring.flyway.out-of-order=true
```

Better: Renumber new migration to next available version

### Problem: Baseline Existing Database

**Scenario**: Adding Flyway to existing database

**Solution**:
```properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

This marks current state as baseline, future migrations apply normally.

## Rollback Strategy

Flyway Community Edition doesn't support automatic rollback.

### Manual Rollback Process

1. **Create Undo Migration**:
```sql
-- V13__undo_add_email.sql
ALTER TABLE users DROP COLUMN email;
GO
```

2. **Or Use Transactions**:
```sql
BEGIN TRANSACTION;

-- Your migration here
ALTER TABLE users ADD email NVARCHAR(100);

-- Test
SELECT * FROM users;

-- If error, rollback
ROLLBACK;
-- If success, commit
COMMIT;
```

## Testing Migrations

### Local Testing

1. Create test database:
```sql
CREATE DATABASE TEST_DB_MIGRATION;
GO
```

2. Point application to test DB
3. Run application
4. Verify migrations
5. Drop test DB if satisfied

### Integration Test

```java
@SpringBootTest
@Sql(scripts = "/db/migration/V1__create_roles_table.sql")
class MigrationTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    void testRolesTableCreated() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'roles'",
            Integer.class
        );
        assertEquals(1, count);
    }
}
```

## Production Deployment

### Pre-Deployment Checklist

- [ ] All migrations tested locally
- [ ] Migrations reviewed by team
- [ ] Backup database
- [ ] Migration files committed to Git
- [ ] Version numbers sequential
- [ ] No syntax errors

### Deployment Process

1. **Backup Database**
```sql
BACKUP DATABASE TEST_DB TO DISK = 'C:\Backups\TEST_DB_backup.bak';
GO
```

2. **Deploy Application**
   - Flyway runs migrations automatically on startup

3. **Verify Migrations**
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
GO
```

4. **Monitor Application**
   - Check logs for errors
   - Verify functionality

### Rollback Plan

If deployment fails:

1. Stop application
2. Restore database from backup
3. Fix migration issue
4. Redeploy

## Advanced Topics

### Repeatable Migrations

For views, procedures, functions that can be re-run:

```
R__create_user_view.sql
```

```sql
-- Repeatable migrations use CREATE OR ALTER
CREATE OR ALTER VIEW vw_users_with_roles AS
SELECT u.id, u.username, r.name as role_name
FROM users u
INNER JOIN roles r ON u.role_id = r.id;
GO
```

### Callbacks

Execute scripts at specific points:

- `beforeMigrate.sql` - Before all migrations
- `afterMigrate.sql` - After all migrations
- `beforeEachMigrate.sql` - Before each migration

### Environment-Specific Migrations

```properties
# Development
spring.flyway.locations=classpath:db/migration,classpath:db/dev

# Production
spring.flyway.locations=classpath:db/migration,classpath:db/prod
```

## Summary

- ✅ All schema changes go through Flyway migrations
- ✅ Never modify executed migrations
- ✅ Test migrations locally first
- ✅ Keep migrations small and focused
- ✅ Always backup before production deployment
- ✅ Use `validate` mode with Hibernate
- ✅ Version control all migration files

Flyway ensures your database schema is always in sync with your application code!
