package com.difbriy.web.controller;

import com.difbriy.web.dto.PredictionRequestDto;
import com.difbriy.web.dto.PredictionResponseDto;
import com.difbriy.web.entity.CryptoPrediction;
import com.difbriy.web.locallm.service.CryptoPredictionLLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
public class CryptoPredictionController {

    private final CryptoPredictionLLMService cryptoPredictionService;

    /**
     * Генерирует новый прогноз для криптовалюты
     */
    @PostMapping("/generate")
    public ResponseEntity<PredictionResponseDto> generatePrediction(@RequestBody PredictionRequestDto request) {
        try {
            log.info("Received prediction request for {} with timeframe {}", request.getSymbol(), request.getTimeframe());
            
            PredictionResponseDto prediction = cryptoPredictionService.generatePrediction(request);
            
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("Error generating prediction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получает последний прогноз для криптовалюты
     */
    @GetMapping("/latest/{symbol}")
    public ResponseEntity<CryptoPrediction> getLatestPrediction(@PathVariable String symbol) {
        try {
            Optional<CryptoPrediction> prediction = cryptoPredictionService.getLatestPrediction(symbol);
            
            return prediction.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting latest prediction for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получает историю прогнозов для криптовалюты
     */
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<CryptoPrediction>> getPredictionHistory(@PathVariable String symbol) {
        try {
            List<CryptoPrediction> predictions = cryptoPredictionService.getPredictionsHistory(symbol);
            
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            log.error("Error getting prediction history for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Быстрый прогноз для популярных криптовалют
     */
    @GetMapping("/quick/{symbol}")
    public ResponseEntity<PredictionResponseDto> getQuickPrediction(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "24h") String timeframe) {
        try {
            PredictionRequestDto request = PredictionRequestDto.builder()
                    .symbol(symbol.toUpperCase())
                    .timeframe(timeframe)
                    .includeNewsAnalysis(true)
                    .includeTechnicalAnalysis(true)
                    .historicalDataPoints(50)
                    .build();

            PredictionResponseDto prediction = cryptoPredictionService.generatePrediction(request);
            
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("Error getting quick prediction for {}: {}", symbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
