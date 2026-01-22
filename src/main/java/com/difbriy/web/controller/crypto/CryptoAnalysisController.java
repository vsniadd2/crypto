package com.difbriy.web.controller.crypto;

import com.difbriy.web.dto.crypto.CryptoAnalysisRequest;
import com.difbriy.web.dto.crypto.CryptoAnalysisResponse;
import com.difbriy.web.service.crypto.CryptoAnalysisService;
import com.difbriy.web.service.llm.LocalLLMService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "*")
public class CryptoAnalysisController {

    CryptoAnalysisService cryptoAnalysisService;
    LocalLLMService llmService;

    @PostMapping("/analyze")
    public ResponseEntity<CryptoAnalysisResponse> analyzeCrypto(@RequestBody CryptoAnalysisRequest request) {
        try {
            log.info("Received analysis request for symbol: {}, type: {}", request.getSymbol(), request.getAnalysisType());

            // Валидация запроса
            if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            if (request.getAnalysisType() == null ||
                    (!request.getAnalysisType().equals("ANALYSIS") && !request.getAnalysisType().equals("FORECAST"))) {
                return ResponseEntity.badRequest().build();
            }

            // Выполняем анализ
            CryptoAnalysisResponse response = cryptoAnalysisService.performAnalysis(request);

            log.info("Analysis completed for symbol: {}", request.getSymbol());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing analysis request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analysis/{symbol}")
    public ResponseEntity<List<CryptoAnalysisResponse>> getAnalysisHistory(@PathVariable String symbol) {
        try {
            log.info("Fetching analysis history for symbol: {}", symbol);

            List<CryptoAnalysisResponse> history = cryptoAnalysisService.getAnalysisHistory(symbol);

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching analysis history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analysis/{symbol}/latest")
    public ResponseEntity<CryptoAnalysisResponse> getLatestAnalysis(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "ANALYSIS") String type) {
        try {
            log.info("Fetching latest analysis for symbol: {}, type: {}", symbol, type);

            Optional<CryptoAnalysisResponse> analysis = cryptoAnalysisService.getLatestAnalysis(symbol, type);

            return analysis.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching latest analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Object> checkHealth() {
        try {
            boolean isHealthy = llmService.isHealthy();

            if (isHealthy) {
                return ResponseEntity.ok().body("LLM service is healthy");
            } else {
                return ResponseEntity.status(503).body("LLM service is not available");
            }

        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(503).body("Health check failed: " + e.getMessage());
        }
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getAvailableSymbols() {
        try {
            // Возвращаем список популярных криптовалют
            List<String> symbols = List.of(
                    "BTC", "ETH", "BNB", "XRP", "ADA", "SOL", "DOGE", "DOT", "AVAX", "MATIC",
                    "LTC", "LINK", "UNI", "ATOM", "FIL", "TRX", "ETC", "XLM", "BCH", "ALGO"
            );

            return ResponseEntity.ok(symbols);

        } catch (Exception e) {
            log.error("Error fetching available symbols: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/test-llm")
    public ResponseEntity<String> testLLM(@RequestBody String prompt) {
        try {
            log.info("Testing LLM with prompt: {}", prompt);

            String response = llmService.generateAnalysis(prompt);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error testing LLM: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
