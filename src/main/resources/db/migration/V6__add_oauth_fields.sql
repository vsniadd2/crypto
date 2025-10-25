ALTER TABLE users

ADD COLUMN oauth_provider VARCHAR(50),
ADD COLUMN oauth_id VARCHAR(255),
ADD COLUMN avatar_url VARCHAR(500);

CREATE INDEX idx_oauth_provider_id ON users(oauth_provider, oauth_id);