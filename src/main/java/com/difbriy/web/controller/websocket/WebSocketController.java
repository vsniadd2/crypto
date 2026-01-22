package com.difbriy.web.controller.websocket;

import com.difbriy.web.dto.crypto.CryptoChartData;
import com.difbriy.web.dto.crypto.PredictionRequestDto;
import com.difbriy.web.dto.crypto.PredictionResponseDto;
import com.difbriy.web.entity.CryptoData;
import com.difbriy.web.service.crypto.CryptoService;
import com.difbriy.web.locallm.service.CryptoPredictionLLMService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketController {

    CryptoService cryptoService;
    CryptoPredictionLLMService cryptoPredictionService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/crypto/request")
    @SendToUser("/topic/crypto/response")
    public Map<String, Object> handleCryptoRequest(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Crypto data requested by session: {}", sessionId);

        cryptoService.sendCryptoDataOnDemand();

        return Map.of(
                "type", "crypto_request",
                "message", "Crypto data sent",
                "sessionId", sessionId
        );
    }

    @MessageMapping("/crypto/single")
    public void handleSingleCryptoRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Long coinId = Long.valueOf(request.get("coinId").toString());
        log.info("Single crypto data requested for coin ID: {} by session: {}", coinId, sessionId);

        cryptoService.sendSingleCryptoData(coinId);
    }

    @MessageMapping("/chart/request")
    public void handleChartRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String symbol = (String) request.get("symbol");
        String period = (String) request.getOrDefault("period", "7d");

        log.info("Chart data requested for symbol: {} period: {} by session: {}", symbol, period, sessionId);

        try {
            List<CryptoData> historyData = cryptoService.getCryptoHistory(symbol, period);

            if (!historyData.isEmpty()) {
                CryptoData latestData = cryptoService.getLatestCryptoData(symbol);

                List<CryptoChartData.ChartPoint> chartPoints = historyData.stream()
                        .map(data -> new CryptoChartData.ChartPoint(
                                data.getTimestamp(),
                                data.getPrice(),
                                data.getVolume24h(),
                                data.getMarketCap()
                        ))
                        .collect(Collectors.toList());

                CryptoChartData chartData = new CryptoChartData();
                chartData.setSymbol(symbol);
                chartData.setName(latestData != null ? latestData.getName() : symbol);
                chartData.setData(chartPoints);

                if (latestData != null) {
                    chartData.setCurrentPrice(latestData.getPrice());
                    chartData.setPriceChange24h(latestData.getPercentChange24h());
                    chartData.setPercentChange24h(latestData.getPercentChange24h());
                    chartData.setMarketCap(latestData.getMarketCap());
                    chartData.setVolume24h(latestData.getVolume24h());
                    chartData.setLastUpdated(latestData.getTimestamp());
                }

                messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/data", chartData);
                log.info("Chart data sent for symbol: {} to session: {}", symbol, sessionId);
            } else {
                messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/error",
                        Map.of("error", "No data available for symbol: " + symbol));
            }

        } catch (Exception e) {
            log.error("Error getting chart data for symbol: {}", symbol, e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/error",
                    Map.of("error", "Error retrieving chart data for symbol: " + symbol));
        }
    }

    @MessageMapping("/chart/symbols")
    public void handleSymbolsRequest(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Crypto symbols requested by session: {}", sessionId);

        try {
            List<String> symbols = cryptoService.getAllCryptoSymbols();
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/symbols", symbols);
            log.info("Crypto symbols sent to session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error getting crypto symbols", e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/error",
                    Map.of("error", "Error retrieving crypto symbols"));
        }
    }

    @MessageMapping("/chart/stats")
    public void handleStatsRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String symbol = (String) request.get("symbol");

        log.info("Stats requested for symbol: {} by session: {}", symbol, sessionId);

        try {
            CryptoData latestData = cryptoService.getLatestCryptoData(symbol);
            if (latestData != null) {
                List<CryptoData> last24h = cryptoService.getCryptoHistory(symbol, "24h");

                double minPrice24h = last24h.stream()
                        .mapToDouble(data -> data.getPrice() != null ? data.getPrice().doubleValue() : 0.0)
                        .min()
                        .orElse(0.0);

                double maxPrice24h = last24h.stream()
                        .mapToDouble(data -> data.getPrice() != null ? data.getPrice().doubleValue() : 0.0)
                        .max()
                        .orElse(0.0);

                double avgPrice24h = last24h.stream()
                        .mapToDouble(data -> data.getPrice() != null ? data.getPrice().doubleValue() : 0.0)
                        .average()
                        .orElse(0.0);

                Map<String, Object> stats = Map.of(
                        "symbol", symbol,
                        "currentPrice", latestData.getPrice(),
                        "marketCap", latestData.getMarketCap(),
                        "volume24h", latestData.getVolume24h(),
                        "percentChange24h", latestData.getPercentChange24h(),
                        "minPrice24h", minPrice24h,
                        "maxPrice24h", maxPrice24h,
                        "avgPrice24h", avgPrice24h,
                        "dataPoints24h", last24h.size(),
                        "lastUpdated", latestData.getTimestamp()
                );

                messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/stats", stats);
                log.info("Stats sent for symbol: {} to session: {}", symbol, sessionId);
            } else {
                messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/error",
                        Map.of("error", "No data available for symbol: " + symbol));
            }

        } catch (Exception e) {
            log.error("Error getting stats for symbol: {}", symbol, e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/chart/error",
                    Map.of("error", "Error retrieving stats for symbol: " + symbol));
        }
    }

    @MessageMapping("/prediction/request")
    public void handlePredictionRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String symbol = (String) request.get("symbol");
        String timeframe = (String) request.getOrDefault("timeframe", "24h");

        log.info("Prediction requested for symbol: {} timeframe: {} by session: {}", symbol, timeframe, sessionId);

        try {
            PredictionRequestDto predictionRequest = PredictionRequestDto.builder()
                    .symbol(symbol.toUpperCase())
                    .timeframe(timeframe)
                    .includeNewsAnalysis(true)
                    .includeTechnicalAnalysis(true)
                    .historicalDataPoints(50)
                    .build();

            PredictionResponseDto prediction = cryptoPredictionService.generatePrediction(predictionRequest);

            messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/data", prediction);
            log.info("Prediction sent for symbol: {} to session: {}", symbol, sessionId);

        } catch (Exception e) {
            log.error("Error generating prediction for symbol: {}", symbol, e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/error",
                    Map.of("error", "Error generating prediction for symbol: " + symbol));
        }
    }

    @MessageMapping("/prediction/latest")
    public void handleLatestPredictionRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String symbol = (String) request.get("symbol");

        log.info("Latest prediction requested for symbol: {} by session: {}", symbol, sessionId);

        try {
            var prediction = cryptoPredictionService.getLatestPrediction(symbol.toUpperCase());

            if (prediction.isPresent()) {
                messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/latest", prediction.get());
                log.info("Latest prediction sent for symbol: {} to session: {}", symbol, sessionId);
            } else {
                messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/error",
                        Map.of("error", "No prediction available for symbol: " + symbol));
            }

        } catch (Exception e) {
            log.error("Error getting latest prediction for symbol: {}", symbol, e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/error",
                    Map.of("error", "Error retrieving latest prediction for symbol: " + symbol));
        }
    }

    @MessageMapping("/prediction/history")
    public void handlePredictionHistoryRequest(Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String symbol = (String) request.get("symbol");

        log.info("Prediction history requested for symbol: {} by session: {}", symbol, sessionId);

        try {
            var predictions = cryptoPredictionService.getPredictionsHistory(symbol.toUpperCase());

            messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/history", predictions);
            log.info("Prediction history sent for symbol: {} to session: {}", symbol, sessionId);

        } catch (Exception e) {
            log.error("Error getting prediction history for symbol: {}", symbol, e);
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/prediction/error",
                    Map.of("error", "Error retrieving prediction history for symbol: " + symbol));
        }
    }
}