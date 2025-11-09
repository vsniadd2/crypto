CREATE TABLE crypto_predictions (
    id BIGSERIAL PRIMARY KEY,
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_symbol_created_at ON crypto_predictions(symbol, created_at);
CREATE INDEX idx_symbol_prediction_timestamp ON crypto_predictions(symbol, prediction_timestamp);
CREATE INDEX idx_confidence_score ON crypto_predictions(confidence_score);
CREATE INDEX idx_created_at ON crypto_predictions(created_at);
