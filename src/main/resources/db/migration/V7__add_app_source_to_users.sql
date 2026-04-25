-- Add app_source column to users table
ALTER TABLE users ADD COLUMN app_source VARCHAR(20) NOT NULL DEFAULT 'ADMINPANEL';

-- Create index on app_source for faster queries
CREATE INDEX idx_app_source ON users(app_source);
