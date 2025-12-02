-- V13: Fix crypto_analysis table schema - add missing columns

-- Добавляем недостающие колонки в таблицу crypto_analysis если они отсутствуют
DO $$ 
BEGIN
    -- Проверяем и добавляем price_change_24h
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'price_change_24h'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN price_change_24h DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем price_change_percent_24h
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'price_change_percent_24h'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN price_change_percent_24h DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем current_price
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'current_price'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN current_price DECIMAL(30,8);
    END IF;
    
    -- Проверяем и добавляем market_cap
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'market_cap'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN market_cap DECIMAL(30,8);
    END IF;
    
    -- Проверяем и добавляем volume_24h
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'volume_24h'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN volume_24h DECIMAL(30,8);
    END IF;
    
    -- Проверяем и добавляем rsi
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'rsi'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN rsi DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем sma_20
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'sma_20'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN sma_20 DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем sma_50
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'sma_50'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN sma_50 DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем sma_200
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'sma_200'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN sma_200 DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем volatility
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'volatility'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN volatility DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем support_level
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'support_level'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN support_level DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем resistance_level
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'resistance_level'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN resistance_level DECIMAL(10,4);
    END IF;
    
    -- Проверяем и добавляем technical_analysis
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'technical_analysis'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN technical_analysis TEXT;
    END IF;
    
    -- Проверяем и добавляем fundamental_analysis
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'fundamental_analysis'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN fundamental_analysis TEXT;
    END IF;
    
    -- Проверяем и добавляем market_sentiment
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'market_sentiment'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN market_sentiment TEXT;
    END IF;
    
    -- Проверяем и добавляем llm_analysis
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'llm_analysis'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN llm_analysis TEXT;
    END IF;
    
    -- Проверяем и добавляем analysis_date
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'analysis_date'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN analysis_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Проверяем и добавляем analysis_type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'crypto_analysis' 
        AND column_name = 'analysis_type'
    ) THEN
        ALTER TABLE crypto_analysis ADD COLUMN analysis_type VARCHAR(20) NOT NULL DEFAULT 'ANALYSIS';
        -- Добавляем ограничение CHECK
        ALTER TABLE crypto_analysis ADD CONSTRAINT chk_analysis_type 
            CHECK (analysis_type IN ('ANALYSIS', 'FORECAST'));
    END IF;
    
END $$;
