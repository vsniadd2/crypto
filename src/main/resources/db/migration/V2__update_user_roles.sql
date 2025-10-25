-- First, drop the existing foreign key constraint
ALTER TABLE IF EXISTS user_roles DROP CONSTRAINT IF EXISTS fkck7bkebbxnx8ilo4lcmemi8gf;

-- Drop the existing user_roles table
DROP TABLE IF EXISTS user_roles;

-- Drop the existing users table if it exists
DROP TABLE IF EXISTS users;

-- Create the users table first
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,
    dateTimeOfCreated TIMESTAMP,
    isActive BOOLEAN DEFAULT TRUE
);

-- Create the user_roles table with proper foreign key constraint
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
); 