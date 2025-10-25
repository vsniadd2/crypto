CREATE TABLE news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    content TEXT,
    category VARCHAR(100),
    author VARCHAR(100),
    published_at TIMESTAMP,
    image_path VARCHAR(500)
);