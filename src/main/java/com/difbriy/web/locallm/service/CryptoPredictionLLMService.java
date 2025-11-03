package com.difbriy.web.locallm.service;

import com.difbriy.web.dto.crypto.CryptoAnalysisDataDto;
import com.difbriy.web.dto.crypto.PredictionRequestDto;
import com.difbriy.web.dto.crypto.PredictionResponseDto;
import com.difbriy.web.dto.crypto.PriceDataPoint;
import com.difbriy.web.entity.CryptoData;
import com.difbriy.web.entity.CryptoPrediction;
import com.difbriy.web.repository.CryptoDataRepository;
import com.difbriy.web.repository.CryptoPredictionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoPredictionLLMService {

    private final CryptoDataRepository cryptoDataRepository;
    private final CryptoPredictionRepository cryptoPredictionRepository;
    private final LocalLLMClient localLLMClient;
    private final ObjectMapper objectMapper;

    /**
     * Генерирует прогноз цены для указанной криптовалюты
     */
    public PredictionResponseDto generatePrediction(PredictionRequestDto request) {
        try {
            log.info("Generating prediction for {} with timeframe {}", request.getSymbol(), request.getTimeframe());
            
            // Получаем данные для анализа
            CryptoAnalysisDataDto analysisData = prepareAnalysisData(request);
            
            // Генерируем прогноз через локальную LLM или простой алгоритм
            PredictionResponseDto prediction;
            if (localLLMClient.isAvailable()) {
                prediction = generatePredictionWithLLM(analysisData, request);
            } else {
                log.warn("Local LLM not available, using simple prediction algorithm");
                prediction = generateSimplePrediction(analysisData, request);
            }
            
            // Сохраняем прогноз в БД
            savePrediction(prediction);
            
            return prediction;
            
        } catch (Exception e) {
            log.error("Error generating prediction for {}: {}", request.getSymbol(), e.getMessage(), e);
            return createFallbackPrediction(request);
        }
    }

    /**
     * Подготавливает данные для анализа
     */
    private CryptoAnalysisDataDto prepareAnalysisData(PredictionRequestDto request) {
        // Получаем последние данные криптовалюты
        CryptoData latestData = cryptoDataRepository.findLatestBySymbol(request.getSymbol());
        
        if (latestData == null) {
            throw new RuntimeException("No data found for symbol: " + request.getSymbol());
        }

        // Получаем исторические данные за последние 6 месяцев для прогноза
        LocalDateTime startTime = LocalDateTime.now().minusMonths(6); // Последние 6 месяцев
        List<CryptoData> historicalData = cryptoDataRepository.findLast6MonthsBySymbol(
                request.getSymbol(), startTime);

        // Конвертируем в DTO
        List<PriceDataPoint> pricePoints = historicalData.stream()
                .map(data -> PriceDataPoint.builder()
                        .timestamp(data.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .price(data.getPrice())
                        .volume(data.getVolume24h())
                        .build())
                .collect(Collectors.toList());

        // Вычисляем технические индикаторы
        String technicalAnalysis = calculateTechnicalIndicators(historicalData);
        
        // Получаем новости (заглушка)
        List<String> recentNews = getRecentNews(request.getSymbol());

        return CryptoAnalysisDataDto.builder()
                .symbol(request.getSymbol())
                .currentPrice(latestData.getPrice())
                .priceChange24h(latestData.getPercentChange24h())
                .volume24h(latestData.getVolume24h())
                .marketCap(latestData.getMarketCap())
                .historicalPrices(pricePoints)
                .recentNews(recentNews)
                .technicalAnalysis(technicalAnalysis)
                .volatility(calculateVolatility(historicalData))
                .rsi(calculateRSI(historicalData))
                .movingAverage20(calculateMovingAverage(historicalData, 20))
                .movingAverage50(calculateMovingAverage(historicalData, 50))
                .build();
    }

    /**
     * Генерирует прогноз через локальную LLM
     */
    private PredictionResponseDto generatePredictionWithLLM(CryptoAnalysisDataDto analysisData, PredictionRequestDto request) {
        try {
            // Формируем промпт для LLM
            String prompt = buildPromptForLLM(analysisData, request);
            
            // Получаем ответ от LLM
            String llmResponse = localLLMClient.generatePrediction(prompt);
            
            // Парсим ответ LLM
            return parseLLMResponse(llmResponse, analysisData, request);
            
        } catch (Exception e) {
            log.error("Error generating prediction with LLM: {}", e.getMessage(), e);
            // Fallback на простой алгоритм
            return generateSimplePrediction(analysisData, request);
        }
    }

    /**
     * Формирует промпт для локальной LLM
     */
    private String buildPromptForLLM(CryptoAnalysisDataDto analysisData, PredictionRequestDto request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Ты - эксперт по анализу криптовалют и прогнозированию цен.\n\n");
        prompt.append("Данные для анализа:\n");
        prompt.append("Символ: ").append(analysisData.getSymbol()).append("\n");
        prompt.append("Текущая цена: $").append(analysisData.getCurrentPrice()).append("\n");
        prompt.append("Изменение за 24ч: ").append(analysisData.getPriceChange24h()).append("%\n");
        prompt.append("Объем торгов за 24ч: $").append(analysisData.getVolume24h()).append("\n");
        prompt.append("Рыночная капитализация: $").append(analysisData.getMarketCap()).append("\n");
        prompt.append("Волатильность: ").append(analysisData.getVolatility()).append("%\n");
        prompt.append("RSI: ").append(analysisData.getRsi()).append("\n");
        prompt.append("SMA 20: $").append(analysisData.getMovingAverage20()).append("\n");
        prompt.append("SMA 50: $").append(analysisData.getMovingAverage50()).append("\n\n");
        
        prompt.append("Технический анализ:\n").append(analysisData.getTechnicalAnalysis()).append("\n\n");
        
        prompt.append("Задача: Проанализируй данные и дай прогноз цены на ").append(request.getTimeframe()).append(".\n\n");
        
        prompt.append("Формат ответа (строго JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"predictedPrice\": число,\n");
        prompt.append("  \"confidenceScore\": число от 0 до 100,\n");
        prompt.append("  \"predictionReasoning\": \"подробное обоснование прогноза\",\n");
        prompt.append("  \"marketSentiment\": \"bullish/bearish/neutral\",\n");
        prompt.append("  \"technicalIndicators\": \"анализ технических индикаторов\",\n");
        prompt.append("  \"newsSentiment\": \"positive/negative/neutral\",\n");
        prompt.append("  \"riskFactors\": [\"фактор1\", \"фактор2\"],\n");
        prompt.append("  \"supportingFactors\": [\"фактор1\", \"фактор2\"]\n");
        prompt.append("}\n\n");
        
        prompt.append("Учти:\n");
        prompt.append("1. Криптовалюты крайне волатильны - будь осторожен с прогнозами\n");
        prompt.append("2. Анализируй технические индикаторы и паттерны\n");
        prompt.append("3. Учитывай настроения рынка\n");
        prompt.append("4. Оценивай уверенность в прогнозе реалистично\n");
        prompt.append("5. Укажи основные риски и поддерживающие факторы\n");
        
        return prompt.toString();
    }

    /**
     * Парсит ответ от локальной LLM
     */
    private PredictionResponseDto parseLLMResponse(String llmResponse, CryptoAnalysisDataDto analysisData, PredictionRequestDto request) {
        try {
            // Извлекаем JSON из ответа LLM
            String jsonPart = extractJsonFromResponse(llmResponse);
            
            // Парсим JSON
            JsonNode jsonNode = objectMapper.readTree(jsonPart);
            
            // Валидируем и ограничиваем значения
            BigDecimal predictedPrice = validateAndLimitBigDecimal(
                new BigDecimal(jsonNode.get("predictedPrice").asText()), 
                30, 8, "predictedPrice"
            );
            
            BigDecimal confidenceScore = validateAndLimitBigDecimal(
                new BigDecimal(jsonNode.get("confidenceScore").asText()), 
                5, 2, "confidenceScore"
            );
            
            return PredictionResponseDto.builder()
                    .symbol(request.getSymbol())
                    .currentPrice(analysisData.getCurrentPrice())
                    .predictedPrice(predictedPrice)
                    .confidenceScore(confidenceScore)
                    .predictionReasoning(jsonNode.get("predictionReasoning").asText())
                    .marketSentiment(jsonNode.get("marketSentiment").asText())
                    .technicalIndicators(jsonNode.get("technicalIndicators").asText())
                    .newsSentiment(jsonNode.get("newsSentiment").asText())
                    .timeframe(request.getTimeframe())
                    .riskFactors(parseStringArray(jsonNode.get("riskFactors")))
                    .supportingFactors(parseStringArray(jsonNode.get("supportingFactors")))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing LLM response: {}", e.getMessage());
            log.debug("LLM Response: {}", llmResponse);
            // Fallback на простой алгоритм
            return generateSimplePrediction(analysisData, request);
        }
    }

    /**
     * Извлекает JSON из ответа LLM
     */
    private String extractJsonFromResponse(String response) {
        // Ищем JSON в ответе
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        // Если JSON не найден, возвращаем весь ответ
        return response;
    }

    /**
     * Генерирует простой прогноз на основе технического анализа
     */
    private PredictionResponseDto generateSimplePrediction(CryptoAnalysisDataDto analysisData, PredictionRequestDto request) {
        BigDecimal currentPrice = analysisData.getCurrentPrice();
        BigDecimal predictedPrice = currentPrice;
        BigDecimal confidenceScore = BigDecimal.valueOf(50); // Базовая уверенность
        String marketSentiment = "neutral";
        String predictionReasoning = "";
        List<String> riskFactors = new ArrayList<>();
        List<String> supportingFactors = new ArrayList<>();

        // Анализ тренда
        if (analysisData.getPriceChange24h() != null) {
            if (analysisData.getPriceChange24h().compareTo(BigDecimal.valueOf(5)) > 0) {
                marketSentiment = "bullish";
                confidenceScore = confidenceScore.add(BigDecimal.valueOf(10));
                supportingFactors.add("Сильный рост за 24 часа");
            } else if (analysisData.getPriceChange24h().compareTo(BigDecimal.valueOf(-5)) < 0) {
                marketSentiment = "bearish";
                confidenceScore = confidenceScore.add(BigDecimal.valueOf(10));
                riskFactors.add("Сильное падение за 24 часа");
            }
        }

        // Анализ RSI
        if (analysisData.getRsi() != null) {
            if (analysisData.getRsi().compareTo(BigDecimal.valueOf(70)) > 0) {
                riskFactors.add("RSI указывает на перекупленность");
                confidenceScore = confidenceScore.subtract(BigDecimal.valueOf(5));
            } else if (analysisData.getRsi().compareTo(BigDecimal.valueOf(30)) < 0) {
                supportingFactors.add("RSI указывает на перепроданность");
                confidenceScore = confidenceScore.add(BigDecimal.valueOf(5));
            }
        }

        // Анализ скользящих средних
        if (analysisData.getMovingAverage20() != null && analysisData.getMovingAverage50() != null) {
            if (analysisData.getMovingAverage20().compareTo(analysisData.getMovingAverage50()) > 0) {
                supportingFactors.add("Краткосрочная MA выше долгосрочной");
                confidenceScore = confidenceScore.add(BigDecimal.valueOf(5));
            } else {
                riskFactors.add("Краткосрочная MA ниже долгосрочной");
                confidenceScore = confidenceScore.subtract(BigDecimal.valueOf(5));
            }
        }

        // Расчет прогнозируемой цены на основе тренда
        BigDecimal trendMultiplier = BigDecimal.ONE;
        if (marketSentiment.equals("bullish")) {
            trendMultiplier = BigDecimal.valueOf(1.05); // +5%
        } else if (marketSentiment.equals("bearish")) {
            trendMultiplier = BigDecimal.valueOf(0.95); // -5%
        }

        // Применяем временной фактор
        switch (request.getTimeframe()) {
            case "1h":
                trendMultiplier = trendMultiplier.add(BigDecimal.valueOf(0.01)); // +1%
                break;
            case "24h":
                // Без изменений
                break;
            case "7d":
                trendMultiplier = trendMultiplier.multiply(BigDecimal.valueOf(1.1)); // +10%
                break;
        }

        predictedPrice = currentPrice.multiply(trendMultiplier);

        // Формируем обоснование
        predictionReasoning = String.format(
                "Прогноз основан на техническом анализе. Текущая цена: %s USD. " +
                "Анализ тренда показывает %s настроения. " +
                "RSI: %s, SMA20: %s, SMA50: %s. " +
                "Прогнозируемая цена: %s USD.",
                currentPrice,
                marketSentiment.equals("bullish") ? "позитивные" : 
                marketSentiment.equals("bearish") ? "негативные" : "нейтральные",
                analysisData.getRsi(),
                analysisData.getMovingAverage20(),
                analysisData.getMovingAverage50(),
                predictedPrice
        );

        return PredictionResponseDto.builder()
                .symbol(request.getSymbol())
                .currentPrice(currentPrice)
                .predictedPrice(predictedPrice)
                .confidenceScore(confidenceScore.min(BigDecimal.valueOf(100)).max(BigDecimal.valueOf(0)))
                .predictionReasoning(predictionReasoning)
                .marketSentiment(marketSentiment)
                .technicalIndicators(analysisData.getTechnicalAnalysis())
                .newsSentiment("neutral")
                .timeframe(request.getTimeframe())
                .riskFactors(riskFactors)
                .supportingFactors(supportingFactors)
                .build();
    }

    /**
     * Сохраняет прогноз в базу данных
     */
    private void savePrediction(PredictionResponseDto prediction) {
        try {
            CryptoPrediction entity = new CryptoPrediction();
            entity.setSymbol(prediction.getSymbol());
            
            // Валидируем все BigDecimal поля перед сохранением
            entity.setCurrentPrice(validateAndLimitBigDecimal(prediction.getCurrentPrice(), 30, 8, "currentPrice"));
            
            BigDecimal predictedPrice = validateAndLimitBigDecimal(prediction.getPredictedPrice(), 30, 8, "predictedPrice");
            entity.setPredictedPrice1h(prediction.getTimeframe().equals("1h") ? predictedPrice : null);
            entity.setPredictedPrice24h(prediction.getTimeframe().equals("24h") ? predictedPrice : null);
            entity.setPredictedPrice7d(prediction.getTimeframe().equals("7d") ? predictedPrice : null);
            
            entity.setConfidenceScore(validateAndLimitBigDecimal(prediction.getConfidenceScore(), 5, 2, "confidenceScore"));
            entity.setPredictionReasoning(prediction.getPredictionReasoning());
            entity.setMarketSentiment(prediction.getMarketSentiment());
            entity.setTechnicalIndicators(prediction.getTechnicalIndicators());
            entity.setNewsSentiment(prediction.getNewsSentiment());
            entity.setPredictionTimestamp(LocalDateTime.now());

            cryptoPredictionRepository.save(entity);
            log.info("Prediction saved for {} with confidence {}", prediction.getSymbol(), prediction.getConfidenceScore());
            
        } catch (Exception e) {
            log.error("Error saving prediction for {}: {}", prediction.getSymbol(), e.getMessage(), e);
            throw new RuntimeException("Failed to save prediction", e);
        }
    }

    /**
     * Создает резервный прогноз в случае ошибки
     */
    private PredictionResponseDto createFallbackPrediction(PredictionRequestDto request) {
        return PredictionResponseDto.builder()
                .symbol(request.getSymbol())
                .currentPrice(BigDecimal.ZERO)
                .predictedPrice(BigDecimal.ZERO)
                .confidenceScore(BigDecimal.ZERO)
                .predictionReasoning("Не удалось сгенерировать прогноз из-за технических проблем")
                .marketSentiment("neutral")
                .technicalIndicators("Данные недоступны")
                .newsSentiment("neutral")
                .timeframe(request.getTimeframe())
                .riskFactors(List.of("Техническая ошибка"))
                .supportingFactors(List.of())
                .build();
    }

    // Вспомогательные методы для технического анализа

    private String calculateTechnicalIndicators(List<CryptoData> historicalData) {
        if (historicalData.size() < 20) {
            return "Недостаточно данных для технического анализа";
        }

        StringBuilder analysis = new StringBuilder();
        
        // Анализ тренда
        CryptoData latest = historicalData.get(historicalData.size() - 1);
        CryptoData weekAgo = historicalData.get(Math.max(0, historicalData.size() - 7));
        
        if (latest.getPrice().compareTo(weekAgo.getPrice()) > 0) {
            analysis.append("Восходящий тренд за неделю. ");
        } else {
            analysis.append("Нисходящий тренд за неделю. ");
        }

        // Анализ объема
        BigDecimal avgVolume = historicalData.stream()
                .map(CryptoData::getVolume24h)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicalData.size()), 2, RoundingMode.HALF_UP);

        if (latest.getVolume24h().compareTo(avgVolume.multiply(BigDecimal.valueOf(1.5))) > 0) {
            analysis.append("Высокий объем торгов. ");
        }

        return analysis.toString();
    }

    private BigDecimal calculateVolatility(List<CryptoData> historicalData) {
        if (historicalData.size() < 2) return BigDecimal.ZERO;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < historicalData.size(); i++) {
            BigDecimal prevPrice = historicalData.get(i - 1).getPrice();
            BigDecimal currPrice = historicalData.get(i).getPrice();
            BigDecimal returnRate = currPrice.subtract(prevPrice).divide(prevPrice, 4, RoundingMode.HALF_UP);
            returns.add(returnRate);
        }

        BigDecimal mean = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);

        return variance.sqrt(new java.math.MathContext(4)).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateRSI(List<CryptoData> historicalData) {
        if (historicalData.size() < 14) return BigDecimal.valueOf(50);

        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();

        for (int i = 1; i < Math.min(15, historicalData.size()); i++) {
            BigDecimal change = historicalData.get(i).getPrice().subtract(historicalData.get(i - 1).getPrice());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }

        BigDecimal avgGain = gains.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(gains.size()), 4, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(losses.size()), 4, RoundingMode.HALF_UP);

        if (avgLoss.equals(BigDecimal.ZERO)) return BigDecimal.valueOf(100);

        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateMovingAverage(List<CryptoData> historicalData, int period) {
        if (historicalData.size() < period) return BigDecimal.ZERO;

        return historicalData.stream()
                .skip(Math.max(0, historicalData.size() - period))
                .map(CryptoData::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    }

    private List<String> getRecentNews(String symbol) {
        // Заглушка - в реальном проекте можно интегрировать с новостными API
        return List.of(
                "Рыночные настроения остаются неопределенными",
                "Технические индикаторы показывают смешанные сигналы",
                "Объем торгов в пределах нормы"
        );
    }

    /**
     * Получает последний прогноз для криптовалюты
     */
    public Optional<CryptoPrediction> getLatestPrediction(String symbol) {
        return cryptoPredictionRepository.findLatestBySymbol(symbol);
    }

    /**
     * Получает все прогнозы для криптовалюты
     */
    public List<CryptoPrediction> getPredictionsHistory(String symbol) {
        return cryptoPredictionRepository.findBySymbolOrderByCreatedAtDesc(symbol);
    }

    private List<String> parseStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return new ArrayList<>();
        }
        
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            result.add(item.asText());
        }
        return result;
    }
    
    /**
     * Валидирует и ограничивает BigDecimal значения в соответствии с точностью DECIMAL полей
     */
    private BigDecimal validateAndLimitBigDecimal(BigDecimal value, int precision, int scale, String fieldName) {
        if (value == null) {
            log.warn("Null value for field: {}", fieldName);
            return BigDecimal.ZERO;
        }
        
        // Проверяем, что значение не превышает максимально допустимое для DECIMAL(precision, scale)
        BigDecimal maxValue = BigDecimal.TEN.pow(precision - scale).subtract(BigDecimal.ONE);
        BigDecimal minValue = maxValue.negate();
        
        if (value.compareTo(maxValue) > 0) {
            log.warn("Value {} exceeds maximum for field {} (max: {}), limiting to maximum", 
                    value, fieldName, maxValue);
            return maxValue;
        }
        
        if (value.compareTo(minValue) < 0) {
            log.warn("Value {} below minimum for field {} (min: {}), limiting to minimum", 
                    value, fieldName, minValue);
            return minValue;
        }
        
        // Округляем до нужного количества знаков после запятой
        BigDecimal roundedValue = value.setScale(scale, RoundingMode.HALF_UP);
        
        if (!roundedValue.equals(value)) {
            log.debug("Rounded value {} to {} for field {}", value, roundedValue, fieldName);
        }
        
        return roundedValue;
    }
}
