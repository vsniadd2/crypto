-- V5__fix_decimal_precision.sql
-- Исправление точности DECIMAL полей для предотвращения переполнения

-- Увеличиваем точность для больших значений криптовалют
-- DECIMAL(30,8) позволяет хранить числа до 9999999999999999999999999999.99999999

-- Исправляем таблицу crypto_data
ALTER TABLE crypto_data 
    ALTER COLUMN market_cap TYPE DECIMAL(30,8),
    ALTER COLUMN volume_24h TYPE DECIMAL(30,8),
    ALTER COLUMN circulating_supply TYPE DECIMAL(30,8),
    ALTER COLUMN total_supply TYPE DECIMAL(30,8),
    ALTER COLUMN max_supply TYPE DECIMAL(30,8);

-- Исправляем таблицу crypto_analysis
ALTER TABLE crypto_analysis 
    ALTER COLUMN current_price TYPE DECIMAL(30,8),
    ALTER COLUMN market_cap TYPE DECIMAL(30,8),
    ALTER COLUMN volume_24h TYPE DECIMAL(30,8);

-- Исправляем таблицу crypto_forecast
ALTER TABLE crypto_forecast 
    ALTER COLUMN predicted_price TYPE DECIMAL(30,8);

-- Исправляем таблицу crypto_predictions
ALTER TABLE crypto_predictions 
    ALTER COLUMN current_price TYPE DECIMAL(30,8),
    ALTER COLUMN predicted_price_1h TYPE DECIMAL(30,8),
    ALTER COLUMN predicted_price_24h TYPE DECIMAL(30,8),
    ALTER COLUMN predicted_price_7d TYPE DECIMAL(30,8);

-- Добавляем комментарии для понимания изменений
COMMENT ON COLUMN crypto_data.market_cap IS 'Рыночная капитализация - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_data.volume_24h IS 'Объем торгов за 24ч - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_analysis.current_price IS 'Текущая цена - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_analysis.market_cap IS 'Рыночная капитализация - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_analysis.volume_24h IS 'Объем торгов за 24ч - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_forecast.predicted_price IS 'Прогнозируемая цена - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_predictions.current_price IS 'Текущая цена - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_predictions.predicted_price_1h IS 'Прогноз на 1 час - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_predictions.predicted_price_24h IS 'Прогноз на 24 часа - увеличена точность до DECIMAL(30,8)';
COMMENT ON COLUMN crypto_predictions.predicted_price_7d IS 'Прогноз на 7 дней - увеличена точность до DECIMAL(30,8)';
