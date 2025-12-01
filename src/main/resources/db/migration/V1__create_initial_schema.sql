-- Create initial schema with users and token tables

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

-- Create token table
CREATE TABLE token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(1024),
    token_type VARCHAR(255),
    expired BOOLEAN DEFAULT FALSE,
    revoked BOOLEAN DEFAULT FALSE,
    user_id BIGINT,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_token ON token(token);
