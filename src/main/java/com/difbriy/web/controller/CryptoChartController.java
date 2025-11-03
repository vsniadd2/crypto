package com.difbriy.web.controller;

import com.difbriy.web.dto.crypto.CryptoChartData;
import com.difbriy.web.entity.CryptoData;
import com.difbriy.web.service.crypto.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crypto/chart")
@RequiredArgsConstructor
@Slf4j
public class CryptoChartController {
    
    private final CryptoService cryptoService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Получение исторических данных для графика конкретной криптовалюты
     * Поддерживаемые периоды: 24h, 7d, 30d, 6m
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<CryptoChartData> getCryptoChartData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "7d") String period) {
        
        try {
            List<CryptoData> historyData = cryptoService.getCryptoHistory(symbol, period);
            
            if (historyData.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Получаем последние данные для текущих значений
            CryptoData latestData = cryptoService.getLatestCryptoData(symbol);
            
            // Преобразуем данные в формат для графика
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

            return ResponseEntity.ok(chartData);
            
        } catch (Exception e) {
            log.error("Error getting chart data for symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение всех доступных символов криптовалют
     */
    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getAllCryptoSymbols() {
        try {
            List<String> symbols = cryptoService.getAllCryptoSymbols();
            return ResponseEntity.ok(symbols);
        } catch (Exception e) {
            log.error("Error getting crypto symbols", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение последних данных для всех криптовалют
     */
    @GetMapping("/latest")
    public ResponseEntity<List<CryptoData>> getLatestCryptoData() {
        try {
            List<CryptoData> latestData = cryptoService.getLatestCryptoData();
            return ResponseEntity.ok(latestData);
        } catch (Exception e) {
            log.error("Error getting latest crypto data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение последних данных для конкретной криптовалюты
     */
    @GetMapping("/latest/{symbol}")
    public ResponseEntity<CryptoData> getLatestCryptoData(@PathVariable String symbol) {
        try {
            CryptoData latestData = cryptoService.getLatestCryptoData(symbol);
            if (latestData == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(latestData);
        } catch (Exception e) {
            log.error("Error getting latest crypto data for symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Отправка обновленных данных через WebSocket
     */
    @PostMapping("/update")
    public ResponseEntity<String> triggerChartUpdate(@RequestParam String symbol) {
        try {
            // Получаем последние данные
            CryptoData latestData = cryptoService.getLatestCryptoData(symbol);
            if (latestData != null) {
                // Отправляем через WebSocket
                messagingTemplate.convertAndSend("/topic/crypto/chart/" + symbol, latestData);
                log.info("Chart update sent for symbol: {}", symbol);
                return ResponseEntity.ok("Chart update triggered for " + symbol);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error triggering chart update for symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение статистики по криптовалюте
     */
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<Object> getCryptoStats(@PathVariable String symbol) {
        try {
            CryptoData latestData = cryptoService.getLatestCryptoData(symbol);
            if (latestData == null) {
                return ResponseEntity.notFound().build();
            }

            // Получаем данные за разные периоды для статистики
            List<CryptoData> last24h = cryptoService.getCryptoHistory(symbol, "24h");
            List<CryptoData> last7d = cryptoService.getCryptoHistory(symbol, "7d");

            // Вычисляем статистику
            BigDecimal minPrice24h = last24h.stream()
                    .map(CryptoData::getPrice)
                    .filter(price -> price != null)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxPrice24h = last24h.stream()
                    .map(CryptoData::getPrice)
                    .filter(price -> price != null)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            BigDecimal avgPrice24h = last24h.stream()
                    .map(CryptoData::getPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(last24h.size()), 8, RoundingMode.HALF_UP);

            Map<String, Object> stats = new HashMap<>();
            stats.put("symbol", symbol);
            stats.put("currentPrice", latestData.getPrice());
            stats.put("marketCap", latestData.getMarketCap());
            stats.put("volume24h", latestData.getVolume24h());
            stats.put("percentChange24h", latestData.getPercentChange24h());
            stats.put("minPrice24h", minPrice24h);
            stats.put("maxPrice24h", maxPrice24h);
            stats.put("avgPrice24h", avgPrice24h);
            stats.put("dataPoints24h", last24h.size());
            stats.put("dataPoints7d", last7d.size());
            stats.put("lastUpdated", latestData.getTimestamp());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting crypto stats for symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 