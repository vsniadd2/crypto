CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_timestamp ON contacts(timestamp);
CREATE INDEX idx_contacts_email_timestamp ON contacts(email, timestamp);

-- Комментарии к таблице
COMMENT ON TABLE contacts IS 'Таблица для хранения контактных сообщений от пользователей';
COMMENT ON COLUMN contacts.name IS 'Имя отправителя';
COMMENT ON COLUMN contacts.email IS 'Email отправителя';
COMMENT ON COLUMN contacts.subject IS 'Тема сообщения';
COMMENT ON COLUMN contacts.message IS 'Текст сообщения';
COMMENT ON COLUMN contacts.timestamp IS 'Время создания сообщения';

