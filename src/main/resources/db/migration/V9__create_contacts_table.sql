-- V9: Create contacts table (with IF NOT EXISTS check)
CREATE TABLE IF NOT EXISTS contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации запросов (с проверкой существования)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_contacts_email') THEN
        CREATE INDEX idx_contacts_email ON contacts(email);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_contacts_timestamp') THEN
        CREATE INDEX idx_contacts_timestamp ON contacts(timestamp);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_contacts_email_timestamp') THEN
        CREATE INDEX idx_contacts_email_timestamp ON contacts(email, timestamp);
    END IF;
END $$;

-- Комментарии к таблице
COMMENT ON TABLE contacts IS 'Таблица для хранения контактных сообщений от пользователей';
COMMENT ON COLUMN contacts.name IS 'Имя отправителя';
COMMENT ON COLUMN contacts.email IS 'Email отправителя';
COMMENT ON COLUMN contacts.subject IS 'Тема сообщения';
COMMENT ON COLUMN contacts.message IS 'Текст сообщения';
COMMENT ON COLUMN contacts.timestamp IS 'Время создания сообщения';
