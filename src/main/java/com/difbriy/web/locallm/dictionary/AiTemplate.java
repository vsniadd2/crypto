package com.difbriy.web.locallm.dictionary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiTemplate {
    CRYPTO_PREDICTION_TEMPLATE("""
            Ты - эксперт по анализу криптовалют и прогнозированию цен. 
            
            Данные для анализа:
            Символ: {symbol}
            Текущая цена: {currentPrice} USD
            Изменение за 24ч: {priceChange24h}%
            Объем торгов за 24ч: {volume24h} USD
            Рыночная капитализация: {marketCap} USD
            Волатильность: {volatility}%
            RSI: {rsi}
            SMA 20: {movingAverage20} USD
            SMA 50: {movingAverage50} USD
            
            Исторические данные за последние {historicalDataPoints} точек:
            {historicalData}
            
            Технический анализ:
            {technicalAnalysis}
            
            Последние новости:
            {recentNews}
            
            Задача: Проанализируй данные и дай прогноз цены на {timeframe}.
            
            Формат ответа (строго JSON):
            {{
                "predictedPrice": число,
                "confidenceScore": число от 0 до 100,
                "predictionReasoning": "подробное обоснование прогноза",
                "marketSentiment": "bullish/bearish/neutral",
                "technicalIndicators": "анализ технических индикаторов",
                "newsSentiment": "positive/negative/neutral",
                "riskFactors": ["фактор1", "фактор2"],
                "supportingFactors": ["фактор1", "фактор2"]
            }}
            
            Учти:
            1. Криптовалюты крайне волатильны - будь осторожен с прогнозами
            2. Анализируй технические индикаторы и паттерны
            3. Учитывай настроения рынка и новости
            4. Оценивай уверенность в прогнозе реалистично
            5. Укажи основные риски и поддерживающие факторы
            """);


    private final String template;
}
