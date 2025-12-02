-- V8: Increase token length (with existence check)
DO $$ 
BEGIN
    -- Проверяем, что таблица token существует и колонка token имеет тип меньше VARCHAR(1024)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'token' 
        AND column_name = 'token' 
        AND character_maximum_length < 1024
    ) THEN
        ALTER TABLE token ALTER COLUMN token TYPE VARCHAR(1024);
    END IF;
END $$;
