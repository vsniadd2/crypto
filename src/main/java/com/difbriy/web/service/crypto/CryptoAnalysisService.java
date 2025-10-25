package com.difbriy.web.service.crypto;

import com.difbriy.web.dto.CryptoAnalysisRequest;
import com.difbriy.web.dto.CryptoAnalysisResponse;
import com.difbriy.web.dto.CryptoForecastResponse;
import com.difbriy.web.entity.CryptoAnalysis;
import com.difbriy.web.entity.CryptoForecast;
import com.difbriy.web.repository.CryptoAnalysisRepository;
import com.difbriy.web.repository.CryptoForecastRepository;
import com.difbriy.web.service.llm.LocalLLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoAnalysisService {
    
    private final CryptoAnalysisRepository analysisRepository;
    private final CryptoForecastRepository forecastRepository;
    private final CryptoDataService cryptoDataService;
    private final LocalLLMService llmService;
    
    @Transactional
    public CryptoAnalysisResponse performAnalysis(CryptoAnalysisRequest request) {
        log.info("Starting crypto analysis for symbol: {}, type: {}", request.getSymbol(), request.getAnalysisType());
        
        try {
            Map<String, Object> marketData;
            
            if ("ANALYSIS".equals(request.getAnalysisType())) {
                marketData = cryptoDataService.getLatestCryptoDataFromDB(request.getSymbol());
            }
            else if ("FORECAST".equals(request.getAnalysisType())) {
                marketData = cryptoDataService.getCryptoDataForForecastFromDB(request.getSymbol());
            } else {
                throw new IllegalArgumentException("Invalid analysis type: " + request.getAnalysisType());
            }
            
            CryptoAnalysis analysis = createAnalysis(request, marketData);

            String llmAnalysis = llmService.generateCryptoAnalysis(
                request.getSymbol(), marketData, request.getAnalysisType());
            analysis.setLlmAnalysis(llmAnalysis);

            analysis = analysisRepository.save(analysis);

            List<CryptoForecast> forecasts = new ArrayList<>();
            if ("FORECAST".equals(request.getAnalysisType()) && request.getForecastPeriods() != null) {
                forecasts = createForecasts(analysis, marketData, request.getForecastPeriods());
                forecastRepository.saveAll(forecasts);
            }

            return convertToResponse(analysis, forecasts);
            
        } catch (Exception e) {
            log.error("Error performing crypto analysis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform crypto analysis: " + e.getMessage());
        }
    }
    
    private CryptoAnalysis createAnalysis(CryptoAnalysisRequest request, Map<String, Object> marketData) {
        CryptoAnalysis analysis = new CryptoAnalysis();
        
        analysis.setSymbol(request.getSymbol());
        analysis.setName((String) marketData.get("name"));
        analysis.setCurrentPrice((BigDecimal) marketData.get("currentPrice"));
        analysis.setMarketCap((BigDecimal) marketData.get("marketCap"));
        analysis.setVolume24h((BigDecimal) marketData.get("volume24h"));
        analysis.setPriceChange24h((BigDecimal) marketData.get("priceChange24h"));
        analysis.setPriceChangePercent24h((BigDecimal) marketData.get("priceChangePercent24h"));
        analysis.setRsi((BigDecimal) marketData.get("rsi"));
        analysis.setSma20((BigDecimal) marketData.get("sma20"));
        analysis.setSma50((BigDecimal) marketData.get("sma50"));
        analysis.setSma200((BigDecimal) marketData.get("sma200"));
        analysis.setVolatility((BigDecimal) marketData.get("volatility"));
        analysis.setSupportLevel((BigDecimal) marketData.get("supportLevel"));
        analysis.setResistanceLevel((BigDecimal) marketData.get("resistanceLevel"));
        analysis.setAnalysisType(request.getAnalysisType());

        analysis.setTechnicalAnalysis(generateTechnicalAnalysis(marketData));

        analysis.setFundamentalAnalysis(generateFundamentalAnalysis(marketData));

        analysis.setMarketSentiment(generateMarketSentiment(marketData));
        
        return analysis;
    }
    
    private String generateTechnicalAnalysis(Map<String, Object> marketData) {
        StringBuilder analysis = new StringBuilder();
        
        BigDecimal currentPrice = (BigDecimal) marketData.get("currentPrice");
        BigDecimal rsi = (BigDecimal) marketData.get("rsi");
        BigDecimal sma20 = (BigDecimal) marketData.get("sma20");
        BigDecimal sma50 = (BigDecimal) marketData.get("sma50");
        BigDecimal sma200 = (BigDecimal) marketData.get("sma200");
        BigDecimal volatility = (BigDecimal) marketData.get("volatility");
        
        analysis.append("=== ТЕХНИЧЕСКИЙ АНАЛИЗ ===\n\n");

        analysis.append("RSI (14): ").append(rsi).append("\n");
        if (rsi.compareTo(BigDecimal.valueOf(70)) > 0) {
            analysis.append("• Перекупленность - возможна коррекция вниз\n");
        } else if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            analysis.append("• Перепроданность - возможен отскок вверх\n");
        } else {
            analysis.append("• Нейтральная зона - тренд продолжается\n");
        }

        analysis.append("\nСкользящие средние:\n");
        analysis.append("• SMA 20: $").append(sma20).append("\n");
        analysis.append("• SMA 50: $").append(sma50).append("\n");
        analysis.append("• SMA 200: $").append(sma200).append("\n");
        
        if (currentPrice.compareTo(sma20) > 0 && sma20.compareTo(sma50) > 0 && sma50.compareTo(sma200) > 0) {
            analysis.append("• Бычий тренд - все MA расположены восходяще\n");
        } else if (currentPrice.compareTo(sma20) < 0 && sma20.compareTo(sma50) < 0 && sma50.compareTo(sma200) < 0) {
            analysis.append("• Медвежий тренд - все MA расположены нисходяще\n");
        } else {
            analysis.append("• Смешанные сигналы - тренд неопределенный\n");
        }

        analysis.append("\nВолатильность: ").append(volatility).append("%\n");
        if (volatility.compareTo(BigDecimal.valueOf(10)) > 0) {
            analysis.append("• Высокая волатильность - повышенный риск\n");
        } else if (volatility.compareTo(BigDecimal.valueOf(5)) < 0) {
            analysis.append("• Низкая волатильность - стабильный тренд\n");
        } else {
            analysis.append("• Умеренная волатильность - нормальные условия\n");
        }

        BigDecimal support = (BigDecimal) marketData.get("supportLevel");
        BigDecimal resistance = (BigDecimal) marketData.get("resistanceLevel");
        
        analysis.append("\nКлючевые уровни:\n");
        analysis.append("• Поддержка: $").append(support).append("\n");
        analysis.append("• Сопротивление: $").append(resistance).append("\n");
        
        return analysis.toString();
    }
    
    private String generateFundamentalAnalysis(Map<String, Object> marketData) {
        StringBuilder analysis = new StringBuilder();
        
        BigDecimal marketCap = (BigDecimal) marketData.get("marketCap");
        BigDecimal volume24h = (BigDecimal) marketData.get("volume24h");
        BigDecimal priceChangePercent24h = (BigDecimal) marketData.get("priceChangePercent24h");
        
        analysis.append("=== ФУНДАМЕНТАЛЬНЫЙ АНАЛИЗ ===\n\n");

        analysis.append("Рыночная капитализация: $").append(formatNumber(marketCap)).append("\n");
        if (marketCap.compareTo(BigDecimal.valueOf(10000000000L)) > 0) { // > $10B
            analysis.append("• Крупная капитализация - стабильный актив\n");
        } else if (marketCap.compareTo(BigDecimal.valueOf(1000000000L)) > 0) { // > $1B
            analysis.append("• Средняя капитализация - умеренный риск\n");
        } else {
            analysis.append("• Малая капитализация - высокий риск и потенциал\n");
        }

        analysis.append("\nОбъем торгов за 24ч: $").append(formatNumber(volume24h)).append("\n");
        BigDecimal volumeRatio = volume24h.divide(marketCap, 4, RoundingMode.HALF_UP);
        analysis.append("• Отношение объема к капитализации: ").append(volumeRatio.multiply(BigDecimal.valueOf(100))).append("%\n");
        
        if (volumeRatio.compareTo(BigDecimal.valueOf(0.1)) > 0) {
            analysis.append("• Высокая ликвидность - активные торги\n");
        } else {
            analysis.append("• Низкая ликвидность - ограниченные торги\n");
        }

        analysis.append("\nИзменение за 24ч: ").append(priceChangePercent24h).append("%\n");
        if (priceChangePercent24h.abs().compareTo(BigDecimal.valueOf(10)) > 0) {
            analysis.append("• Высокая волатильность - значительные движения\n");
        } else if (priceChangePercent24h.abs().compareTo(BigDecimal.valueOf(5)) > 0) {
            analysis.append("• Умеренная волатильность - нормальные движения\n");
        } else {
            analysis.append("• Низкая волатильность - стабильная цена\n");
        }
        
        return analysis.toString();
    }
    
    private String generateMarketSentiment(Map<String, Object> marketData) {
        StringBuilder analysis = new StringBuilder();
        
        BigDecimal priceChangePercent24h = (BigDecimal) marketData.get("priceChangePercent24h");
        BigDecimal rsi = (BigDecimal) marketData.get("rsi");
        BigDecimal volume24h = (BigDecimal) marketData.get("volume24h");
        
        analysis.append("=== АНАЛИЗ НАСТРОЕНИЙ РЫНКА ===\n\n");

        analysis.append("Общее настроение: ");
        if (priceChangePercent24h.compareTo(BigDecimal.valueOf(5)) > 0) {
            analysis.append("ОЧЕНЬ ПОЗИТИВНОЕ\n");
        } else if (priceChangePercent24h.compareTo(BigDecimal.valueOf(2)) > 0) {
            analysis.append("ПОЗИТИВНОЕ\n");
        } else if (priceChangePercent24h.compareTo(BigDecimal.valueOf(-2)) > 0) {
            analysis.append("НЕЙТРАЛЬНОЕ\n");
        } else if (priceChangePercent24h.compareTo(BigDecimal.valueOf(-5)) > 0) {
            analysis.append("НЕГАТИВНОЕ\n");
        } else {
            analysis.append("ОЧЕНЬ НЕГАТИВНОЕ\n");
        }

        analysis.append("\nТехнические индикаторы настроений:\n");
        if (rsi.compareTo(BigDecimal.valueOf(70)) > 0) {
            analysis.append("• RSI указывает на эйфорию рынка\n");
        } else if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            analysis.append("• RSI указывает на панику рынка\n");
        } else {
            analysis.append("• RSI указывает на сбалансированные настроения\n");
        }

        analysis.append("\nАнализ активности:\n");
        if (volume24h.compareTo(BigDecimal.valueOf(1000000000L)) > 0) { // > $1B
            analysis.append("• Высокая торговая активность\n");
        } else {
            analysis.append("• Умеренная торговая активность\n");
        }
        
        return analysis.toString();
    }
    
    private List<CryptoForecast> createForecasts(CryptoAnalysis analysis, Map<String, Object> marketData, List<String> periods) {
        List<CryptoForecast> forecasts = new ArrayList<>();
        
        for (String period : periods) {
            CryptoForecast forecast = createForecast(analysis, marketData, period);
            forecasts.add(forecast);
        }
        
        return forecasts;
    }
    
    private CryptoForecast createForecast(CryptoAnalysis analysis, Map<String, Object> marketData, String period) {
        CryptoForecast forecast = new CryptoForecast();
        forecast.setAnalysis(analysis);
        forecast.setForecastPeriod(period);
        
        BigDecimal currentPrice = analysis.getCurrentPrice();
        BigDecimal predictedPrice = calculatePredictedPrice(currentPrice, marketData, period);
        BigDecimal priceChangePercent = predictedPrice.subtract(currentPrice)
            .divide(currentPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        forecast.setPredictedPrice(predictedPrice);
        forecast.setPriceChangePercent(priceChangePercent);
        forecast.setConfidence(calculateConfidence(marketData, period));
        forecast.setForecastReasoning(generateForecastReasoning(marketData, period, predictedPrice));
        forecast.setRiskFactors(generateRiskFactors(marketData, period));
        forecast.setMarketConditions(generateMarketConditions(marketData, period));
        forecast.setTechnicalIndicators(generateTechnicalIndicators(marketData));

        LocalDateTime targetDate = calculateTargetDate(period);
        forecast.setTargetDate(targetDate);
        
        return forecast;
    }
    
    private BigDecimal calculatePredictedPrice(BigDecimal currentPrice, Map<String, Object> marketData, String period) {

        BigDecimal trendFactor = calculateTrendFactor(marketData, period);
        
        BigDecimal volatility = (BigDecimal) marketData.get("volatility");
        BigDecimal volatilityFactor = volatility.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal technicalFactor = calculateTechnicalFactor(marketData, period);

        double randomFactor = 0.9 + Math.random() * 0.2;

        BigDecimal combinedFactor = trendFactor
            .multiply(BigDecimal.valueOf(1 + volatilityFactor.doubleValue()))
            .multiply(technicalFactor)
            .multiply(BigDecimal.valueOf(randomFactor));
        
        return currentPrice.multiply(combinedFactor);
    }
    
    private BigDecimal calculateTrendFactor(Map<String, Object> marketData, String period) {
        BigDecimal priceChangePercent24h = (BigDecimal) marketData.get("priceChangePercent24h");
        BigDecimal rsi = (BigDecimal) marketData.get("rsi");

        BigDecimal baseTrend = BigDecimal.ONE.add(priceChangePercent24h.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));

        double periodMultiplier = switch (period) {
            case "1_MONTH" -> 1.0;
            case "6_MONTHS" -> 1.5;
            case "1_YEAR" -> 2.0;
            default -> 1.0;
        };

        BigDecimal rsiAdjustment = BigDecimal.ONE;
        if (rsi.compareTo(BigDecimal.valueOf(70)) > 0) {
            rsiAdjustment = BigDecimal.valueOf(0.95);
        } else if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
            rsiAdjustment = BigDecimal.valueOf(1.05);
        }
        
        return baseTrend.multiply(BigDecimal.valueOf(periodMultiplier)).multiply(rsiAdjustment);
    }
    
    private BigDecimal calculateTechnicalFactor(Map<String, Object> marketData, String period) {
        BigDecimal currentPrice = (BigDecimal) marketData.get("currentPrice");
        BigDecimal sma20 = (BigDecimal) marketData.get("sma20");
        BigDecimal sma50 = (BigDecimal) marketData.get("sma50");
        BigDecimal sma200 = (BigDecimal) marketData.get("sma200");
        
        BigDecimal factor = BigDecimal.ONE;

        if (currentPrice.compareTo(sma20) > 0 && sma20.compareTo(sma50) > 0) {
            factor = factor.multiply(BigDecimal.valueOf(1.02));
        } else if (currentPrice.compareTo(sma20) < 0 && sma20.compareTo(sma50) < 0) {
            factor = factor.multiply(BigDecimal.valueOf(0.98));
        }

        if (sma50.compareTo(sma200) > 0) {
            factor = factor.multiply(BigDecimal.valueOf(1.01));
        } else {
            factor = factor.multiply(BigDecimal.valueOf(0.99));
        }
        
        return factor;
    }
    
    private BigDecimal calculateConfidence(Map<String, Object> marketData, String period) {
        BigDecimal volatility = (BigDecimal) marketData.get("volatility");
        BigDecimal volume24h = (BigDecimal) marketData.get("volume24h");
        
        BigDecimal confidence = BigDecimal.valueOf(70);


        if (volatility.compareTo(BigDecimal.valueOf(5)) < 0) {
            confidence = confidence.add(BigDecimal.valueOf(10));
        } else if (volatility.compareTo(BigDecimal.valueOf(15)) > 0) {
            confidence = confidence.subtract(BigDecimal.valueOf(15));
        }

        switch (period) {
            case "1_MONTH" -> confidence = confidence.add(BigDecimal.valueOf(10));
            case "6_MONTHS" -> confidence = confidence.subtract(BigDecimal.valueOf(5));
            case "1_YEAR" -> confidence = confidence.subtract(BigDecimal.valueOf(15));
        }

        if (volume24h.compareTo(BigDecimal.valueOf(1000000000L)) > 0) {
            confidence = confidence.add(BigDecimal.valueOf(5));
        }
        
        return confidence.max(BigDecimal.valueOf(30)).min(BigDecimal.valueOf(95));
    }
    
    private String generateForecastReasoning(Map<String, Object> marketData, String period, BigDecimal predictedPrice) {
        StringBuilder reasoning = new StringBuilder();
        
        BigDecimal currentPrice = (BigDecimal) marketData.get("currentPrice");
        BigDecimal priceChangePercent = predictedPrice.subtract(currentPrice)
            .divide(currentPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        reasoning.append("Прогноз основан на:\n");
        reasoning.append("• Техническом анализе трендов\n");
        reasoning.append("• Анализе волатильности (").append(marketData.get("volatility")).append("%)\n");
        reasoning.append("• Положении относительно скользящих средних\n");
        reasoning.append("• Исторических паттернах движения цены\n\n");
        
        reasoning.append("Ожидаемое изменение: ").append(priceChangePercent).append("%\n");
        
        if (priceChangePercent.compareTo(BigDecimal.ZERO) > 0) {
            reasoning.append("Прогнозируется рост цены на основе текущих технических индикаторов.\n");
        } else {
            reasoning.append("Прогнозируется снижение цены на основе текущих технических индикаторов.\n");
        }
        
        return reasoning.toString();
    }
    
    private String generateRiskFactors(Map<String, Object> marketData, String period) {
        StringBuilder risks = new StringBuilder();
        
        BigDecimal volatility = (BigDecimal) marketData.get("volatility");
        
        risks.append("Основные факторы риска:\n");
        risks.append("• Высокая волатильность криптовалютного рынка\n");
        risks.append("• Регуляторные изменения\n");
        risks.append("• Технологические риски\n");
        risks.append("• Макроэкономические факторы\n");
        
        if (volatility.compareTo(BigDecimal.valueOf(10)) > 0) {
            risks.append("• Особенно высокий риск из-за волатильности ").append(volatility).append("%\n");
        }
        
        switch (period) {
            case "6_MONTHS", "1_YEAR" -> risks.append("• Долгосрочные прогнозы менее точны\n");
        }
        
        return risks.toString();
    }
    
    private String generateMarketConditions(Map<String, Object> marketData, String period) {
        StringBuilder conditions = new StringBuilder();
        
        BigDecimal volume24h = (BigDecimal) marketData.get("volume24h");
        BigDecimal marketCap = (BigDecimal) marketData.get("marketCap");
        
        conditions.append("Условия рынка:\n");
        conditions.append("• Объем торгов: $").append(formatNumber(volume24h)).append("\n");
        conditions.append("• Рыночная капитализация: $").append(formatNumber(marketCap)).append("\n");
        
        if (volume24h.divide(marketCap, 4, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.1)) > 0) {
            conditions.append("• Высокая ликвидность\n");
        } else {
            conditions.append("• Умеренная ликвидность\n");
        }
        
        return conditions.toString();
    }
    
    private String generateTechnicalIndicators(Map<String, Object> marketData) {
        StringBuilder indicators = new StringBuilder();
        
        indicators.append("Технические индикаторы:\n");
        indicators.append("• RSI: ").append(marketData.get("rsi")).append("\n");
        indicators.append("• SMA 20: $").append(marketData.get("sma20")).append("\n");
        indicators.append("• SMA 50: $").append(marketData.get("sma50")).append("\n");
        indicators.append("• SMA 200: $").append(marketData.get("sma200")).append("\n");
        indicators.append("• Волатильность: ").append(marketData.get("volatility")).append("%\n");
        
        return indicators.toString();
    }
    
    private LocalDateTime calculateTargetDate(String period) {
        return switch (period) {
            case "1_MONTH" -> LocalDateTime.now().plusMonths(1);
            case "6_MONTHS" -> LocalDateTime.now().plusMonths(6);
            case "1_YEAR" -> LocalDateTime.now().plusYears(1);
            default -> LocalDateTime.now().plusMonths(1);
        };
    }
    
    private String formatNumber(BigDecimal number) {
        if (number.compareTo(BigDecimal.valueOf(1000000000L)) >= 0) {
            return number.divide(BigDecimal.valueOf(1000000000L), 2, RoundingMode.HALF_UP) + "B";
        } else if (number.compareTo(BigDecimal.valueOf(1000000L)) >= 0) {
            return number.divide(BigDecimal.valueOf(1000000L), 2, RoundingMode.HALF_UP) + "M";
        } else if (number.compareTo(BigDecimal.valueOf(1000L)) >= 0) {
            return number.divide(BigDecimal.valueOf(1000L), 2, RoundingMode.HALF_UP) + "K";
        } else {
            return number.toString();
        }
    }
    
    private CryptoAnalysisResponse convertToResponse(CryptoAnalysis analysis, List<CryptoForecast> forecasts) {
        CryptoAnalysisResponse response = new CryptoAnalysisResponse();
        
        response.setId(analysis.getId());
        response.setSymbol(analysis.getSymbol());
        response.setName(analysis.getName());
        response.setCurrentPrice(analysis.getCurrentPrice());
        response.setMarketCap(analysis.getMarketCap());
        response.setVolume24h(analysis.getVolume24h());
        response.setPriceChange24h(analysis.getPriceChange24h());
        response.setPriceChangePercent24h(analysis.getPriceChangePercent24h());
        response.setRsi(analysis.getRsi());
        response.setSma20(analysis.getSma20());
        response.setSma50(analysis.getSma50());
        response.setSma200(analysis.getSma200());
        response.setVolatility(analysis.getVolatility());
        response.setSupportLevel(analysis.getSupportLevel());
        response.setResistanceLevel(analysis.getResistanceLevel());
        response.setTechnicalAnalysis(analysis.getTechnicalAnalysis());
        response.setFundamentalAnalysis(analysis.getFundamentalAnalysis());
        response.setMarketSentiment(analysis.getMarketSentiment());
        response.setLlmAnalysis(analysis.getLlmAnalysis());
        response.setAnalysisDate(analysis.getAnalysisDate());
        response.setAnalysisType(analysis.getAnalysisType());
        
        List<CryptoForecastResponse> forecastResponses = forecasts.stream()
            .map(this::convertForecastToResponse)
            .collect(Collectors.toList());
        response.setForecasts(forecastResponses);
        
        return response;
    }
    
    private CryptoForecastResponse convertForecastToResponse(CryptoForecast forecast) {
        CryptoForecastResponse response = new CryptoForecastResponse();
        
        response.setId(forecast.getId());
        response.setForecastPeriod(forecast.getForecastPeriod());
        response.setPredictedPrice(forecast.getPredictedPrice());
        response.setPriceChangePercent(forecast.getPriceChangePercent());
        response.setConfidence(forecast.getConfidence());
        response.setForecastReasoning(forecast.getForecastReasoning());
        response.setRiskFactors(forecast.getRiskFactors());
        response.setMarketConditions(forecast.getMarketConditions());
        response.setTechnicalIndicators(forecast.getTechnicalIndicators());
        response.setForecastDate(forecast.getForecastDate());
        response.setTargetDate(forecast.getTargetDate());
        
        return response;
    }
    
    public List<CryptoAnalysisResponse> getAnalysisHistory(String symbol) {
        List<CryptoAnalysis> analyses = analysisRepository.findBySymbolOrderByAnalysisDateDesc(symbol);
        return analyses.stream()
            .map(analysis -> {
                List<CryptoForecast> forecasts = forecastRepository.findByAnalysisIdOrderByForecastPeriod(analysis.getId());
                return convertToResponse(analysis, forecasts);
            })
            .collect(Collectors.toList());
    }
    
    public Optional<CryptoAnalysisResponse> getLatestAnalysis(String symbol, String analysisType) {
        Optional<CryptoAnalysis> analysis = analysisRepository.findFirstBySymbolAndAnalysisTypeOrderByAnalysisDateDesc(symbol, analysisType);
        return analysis.map(a -> {
            List<CryptoForecast> forecasts = forecastRepository.findByAnalysisIdOrderByForecastPeriod(a.getId());
            return convertToResponse(a, forecasts);
        });
    }
}
