CREATE TABLE crypto_predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    current_price DECIMAL(20,8),
    predicted_price_1h DECIMAL(20,8),
    predicted_price_24h DECIMAL(20,8),
    predicted_price_7d DECIMAL(20,8),
    confidence_score DECIMAL(5,2),
    prediction_reasoning TEXT,
    market_sentiment VARCHAR(20),
    technical_indicators TEXT,
    news_sentiment VARCHAR(20),
    prediction_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_symbol_created_at (symbol, created_at),
    INDEX idx_symbol_prediction_timestamp (symbol, prediction_timestamp),
    INDEX idx_confidence_score (confidence_score),
    INDEX idx_created_at (created_at)
);
