-- ============================================
-- MS SQL Server Database Setup Script
-- Database: TEST_DB
-- ============================================

-- 1. Create Database
USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'TEST_DB')
BEGIN
    PRINT 'Database TEST_DB already exists. Dropping it...'
    ALTER DATABASE TEST_DB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE TEST_DB;
END
GO

PRINT 'Creating database TEST_DB...'
CREATE DATABASE TEST_DB;
GO

PRINT 'Database TEST_DB created successfully!'
GO

-- 2. Use the new database
USE TEST_DB;
GO

-- 3. Verify database
SELECT DB_NAME() AS CurrentDatabase;
GO

PRINT 'Setup complete! You can now start the Spring Boot application.'
PRINT 'Flyway will automatically create tables and insert default roles.'
GO

-- ============================================
-- Optional: View current databases
-- ============================================
-- SELECT name FROM sys.databases;
-- GO
