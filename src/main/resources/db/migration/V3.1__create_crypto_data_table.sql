CREATE TABLE crypto_data (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(255),
    price DECIMAL(20,8),
    market_cap DECIMAL(20,2),
    volume_24h DECIMAL(20,2),
    circulating_supply DECIMAL(20,2),
    total_supply DECIMAL(20,2),
    max_supply DECIMAL(20,2),
    percent_change_1h DECIMAL(10,4),
    percent_change_24h DECIMAL(10,4),
    percent_change_7d DECIMAL(10,4),
    rank INT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_symbol_timestamp ON crypto_data(symbol, timestamp);
CREATE INDEX idx_timestamp ON crypto_data(timestamp);
CREATE INDEX idx_symbol ON crypto_data(symbol); 