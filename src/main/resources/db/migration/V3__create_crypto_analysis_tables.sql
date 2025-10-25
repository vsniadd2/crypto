-- V3__create_crypto_analysis_tables.sql

-- Создание таблицы для анализа криптовалют
CREATE TABLE crypto_analysis (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    current_price DECIMAL(20,8),
    market_cap DECIMAL(20,8),
    volume_24h DECIMAL(20,8),
    price_change_24h DECIMAL(10,4),
    price_change_percent_24h DECIMAL(10,4),
    rsi DECIMAL(10,4),
    sma_20 DECIMAL(10,4),
    sma_50 DECIMAL(10,4),
    sma_200 DECIMAL(10,4),
    volatility DECIMAL(10,4),
    support_level DECIMAL(10,4),
    resistance_level DECIMAL(10,4),
    technical_analysis TEXT,
    fundamental_analysis TEXT,
    market_sentiment TEXT,
    llm_analysis TEXT,
    analysis_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    analysis_type VARCHAR(20) NOT NULL CHECK (analysis_type IN ('ANALYSIS', 'FORECAST'))
);

-- Создание таблицы для прогнозов криптовалют
CREATE TABLE crypto_forecast (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL REFERENCES crypto_analysis(id) ON DELETE CASCADE,
    forecast_period VARCHAR(20) NOT NULL CHECK (forecast_period IN ('1_MONTH', '6_MONTHS', '1_YEAR')),
    predicted_price DECIMAL(20,8),
    price_change_percent DECIMAL(10,4),
    confidence DECIMAL(10,4),
    forecast_reasoning TEXT,
    risk_factors TEXT,
    market_conditions TEXT,
    technical_indicators TEXT,
    forecast_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    target_date TIMESTAMP NOT NULL
);

-- Создание индексов для оптимизации запросов
CREATE INDEX idx_crypto_analysis_symbol ON crypto_analysis(symbol);
CREATE INDEX idx_crypto_analysis_date ON crypto_analysis(analysis_date);
CREATE INDEX idx_crypto_analysis_type ON crypto_analysis(analysis_type);
CREATE INDEX idx_crypto_analysis_symbol_type_date ON crypto_analysis(symbol, analysis_type, analysis_date);

CREATE INDEX idx_crypto_forecast_analysis_id ON crypto_forecast(analysis_id);
CREATE INDEX idx_crypto_forecast_period ON crypto_forecast(forecast_period);
CREATE INDEX idx_crypto_forecast_date ON crypto_forecast(forecast_date);

-- Комментарии к таблицам
COMMENT ON TABLE crypto_analysis IS 'Таблица для хранения анализа криптовалют';
COMMENT ON TABLE crypto_forecast IS 'Таблица для хранения прогнозов цен криптовалют';

-- Комментарии к ключевым полям
COMMENT ON COLUMN crypto_analysis.symbol IS 'Символ криптовалюты (BTC, ETH, etc.)';
COMMENT ON COLUMN crypto_analysis.analysis_type IS 'Тип анализа: ANALYSIS или FORECAST';
COMMENT ON COLUMN crypto_analysis.rsi IS 'Индекс относительной силы (RSI)';
COMMENT ON COLUMN crypto_analysis.sma_20 IS 'Простая скользящая средняя за 20 периодов';
COMMENT ON COLUMN crypto_analysis.sma_50 IS 'Простая скользящая средняя за 50 периодов';
COMMENT ON COLUMN crypto_analysis.sma_200 IS 'Простая скользящая средняя за 200 периодов';
COMMENT ON COLUMN crypto_analysis.volatility IS 'Волатильность в процентах';
COMMENT ON COLUMN crypto_analysis.support_level IS 'Уровень поддержки';
COMMENT ON COLUMN crypto_analysis.resistance_level IS 'Уровень сопротивления';

COMMENT ON COLUMN crypto_forecast.forecast_period IS 'Период прогноза: 1_MONTH, 6_MONTHS, 1_YEAR';
COMMENT ON COLUMN crypto_forecast.predicted_price IS 'Прогнозируемая цена';
COMMENT ON COLUMN crypto_forecast.price_change_percent IS 'Прогнозируемое изменение цены в процентах';
COMMENT ON COLUMN crypto_forecast.confidence IS 'Уровень уверенности в прогнозе (0-100%)';
COMMENT ON COLUMN crypto_forecast.target_date IS 'Целевая дата прогноза';
