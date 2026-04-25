# Migration Summary: MS SQL Server → PostgreSQL

## Changes Completed

### 1. Database Driver — `pom.xml`

```diff
- <groupId>com.microsoft.sqlserver</groupId>
- <artifactId>mssql-jdbc</artifactId>

+ <groupId>org.postgresql</groupId>
+ <artifactId>postgresql</artifactId>
```

```diff
- <artifactId>flyway-sqlserver</artifactId>   ← removed (PostgreSQL needs only flyway-core)
```

---

### 2. Application Configuration — `application.properties`

```diff
- spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=CKA_DB;encrypt=true;trustServerCertificate=true
- spring.datasource.username=sa
- spring.datasource.password=Avanza123
- spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect

+ spring.datasource.url=jdbc:postgresql://localhost:5432/cka_db
+ spring.datasource.username=postgres
+ spring.datasource.password=postgres
+ spring.datasource.driver-class-name=org.postgresql.Driver
+ spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

### 3. Flyway Migration Scripts

All six migration files were updated. Key SQL syntax changes:

| MS SQL Server | PostgreSQL |
|---|---|
| `BIGINT IDENTITY(1,1) PRIMARY KEY` | `BIGSERIAL PRIMARY KEY` |
| `NVARCHAR(n)` | `VARCHAR(n)` |
| `ALTER TABLE t ADD col TYPE` | `ALTER TABLE t ADD COLUMN col TYPE` |
| `CREATE INDEX … WHERE …` (filtered) | `CREATE INDEX …` (plain index) |

#### V1 — roles table
```sql
-- PostgreSQL
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

#### V2 — users table
```sql
-- PostgreSQL
CREATE TABLE users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role_id  BIGINT       NOT NULL,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

#### V4 — extend users + user_role
```sql
-- PostgreSQL (ADD COLUMN, VARCHAR, no filtered index)
ALTER TABLE users ADD COLUMN fullname  VARCHAR(100);
ALTER TABLE users ADD COLUMN job_title VARCHAR(100);
ALTER TABLE users ADD COLUMN email     VARCHAR(150);
ALTER TABLE users ADD COLUMN status    VARCHAR(20) DEFAULT 'Active';
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role       PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

#### V5 — projects + user_project
```sql
CREATE TABLE projects (
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    path                VARCHAR(255),
    database_table_name VARCHAR(100),
    status              VARCHAR(20)  NOT NULL DEFAULT 'Active'
);
CREATE TABLE user_project ( … );
```

#### V6 — datasources + project_datasource
```sql
CREATE TABLE datasources (
    id     BIGSERIAL    PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'Active'
);
CREATE TABLE project_datasource ( … );
```

---

### 4. JPA Entity Classes

All `columnDefinition = "NVARCHAR(n)"` attributes were replaced with standard JPA `length = n`:

```diff
- @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(50)")
+ @Column(nullable = false, unique = true, length = 50)
  private String username;

- @Column(columnDefinition = "NVARCHAR(100)")
+ @Column(length = 100)
  private String fullname;
```

Affected entities: `User`, `Role`, `Project`, `Datasource`, `ProjectDatasource`

---

## Why These Changes Were Necessary

| Feature | MS SQL Server | PostgreSQL |
|---|---|---|
| Auto-increment PK | `BIGINT IDENTITY(1,1)` | `BIGSERIAL` |
| Unicode string | `NVARCHAR(n)` | `VARCHAR(n)` (UTF-8 by default) |
| Add column DDL | `ADD col TYPE` | `ADD COLUMN col TYPE` |
| Flyway extension | `flyway-sqlserver` required | `flyway-core` sufficient |
| JDBC driver | `mssql-jdbc` | `postgresql` |
| Hibernate dialect | `SQLServerDialect` | `PostgreSQLDialect` |
| Default port | 1433 | 5432 |

---

## Important Notes

1. **Never modify executed migrations** — always create a new version file instead
2. **Flyway validates checksums** — prevents accidental modification of applied migrations
3. **`ddl-auto=validate`** — Hibernate validates but never alters schema; Flyway owns all DDL
4. Commit all migration SQL files to version control alongside application code

---

## References

- [QUICK_START.md](QUICK_START.md) — Getting started with PostgreSQL
- [DATABASE_SETUP.md](DATABASE_SETUP.md) — Full PostgreSQL setup guide
- [FLYWAY_GUIDE.md](FLYWAY_GUIDE.md) — Migration patterns and best practices
- [API_EXAMPLES.md](API_EXAMPLES.md) — API testing examples
