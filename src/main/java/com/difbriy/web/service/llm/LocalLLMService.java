package com.difbriy.web.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LocalLLMService {
    
    @Value("${local.llm.base-url}")
    private String baseUrl;
    
    @Value("${local.llm.model}")
    private String model;
    
    @Value("${local.llm.temperature}")
    private double temperature;
    
    @Value("${local.llm.max-tokens}")
    private int maxTokens;
    
    @Value("${local.llm.enabled}")
    private boolean enabled;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public LocalLLMService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateAnalysis(String prompt) {
        if (!enabled) {
            log.warn("Local LLM is disabled");
            return "LLM analysis is currently disabled.";
        }
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                "temperature", temperature,
                "num_predict", maxTokens
            ));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = baseUrl + "/api/generate";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.get("response").asText();
            } else {
                log.error("LLM API returned status: {}", response.getStatusCode());
                return "Error generating analysis: " + response.getStatusCode();
            }
            
        } catch (Exception e) {
            log.error("Error calling local LLM: {}", e.getMessage(), e);
            return "Error generating analysis: " + e.getMessage();
        }
    }
    
    public String generateCryptoAnalysis(String symbol, Map<String, Object> marketData, String analysisType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Ты - эксперт по криптовалютам и финансовому анализу. ");
        prompt.append("Проанализируй следующие данные для криптовалюты ").append(symbol).append(":\n\n");

        prompt.append("РЫНОЧНЫЕ ДАННЫЕ:\n");
        prompt.append("- Текущая цена: $").append(marketData.get("currentPrice")).append("\n");
        prompt.append("- Рыночная капитализация: $").append(marketData.get("marketCap")).append("\n");
        prompt.append("- Объем торгов за 24ч: $").append(marketData.get("volume24h")).append("\n");
        prompt.append("- Изменение за 24ч: ").append(marketData.get("priceChangePercent24h")).append("%\n");

        prompt.append("\nТЕХНИЧЕСКИЕ ИНДИКАТОРЫ:\n");
        prompt.append("- RSI: ").append(marketData.get("rsi")).append("\n");
        prompt.append("- SMA 20: $").append(marketData.get("sma20")).append("\n");
        prompt.append("- SMA 50: $").append(marketData.get("sma50")).append("\n");
        prompt.append("- SMA 200: $").append(marketData.get("sma200")).append("\n");
        prompt.append("- Волатильность: ").append(marketData.get("volatility")).append("%\n");
        prompt.append("- Уровень поддержки: $").append(marketData.get("supportLevel")).append("\n");
        prompt.append("- Уровень сопротивления: $").append(marketData.get("resistanceLevel")).append("\n");

        if (marketData.containsKey("historicalPrices")) {
            prompt.append("\nИСТОРИЧЕСКИЕ ДАННЫЕ (последние 30 дней):\n");
            prompt.append(marketData.get("historicalPrices"));
        }
        
        if ("ANALYSIS".equals(analysisType)) {
            prompt.append("\n\nЗАДАЧА: Проведи комплексный анализ этой криптовалюты. ");
            prompt.append("Включи:\n");
            prompt.append("1. Технический анализ (тренды, паттерны, индикаторы)\n");
            prompt.append("2. Фундаментальный анализ (использование, команда, технологии)\n");
            prompt.append("3. Анализ настроений рынка\n");
            prompt.append("4. Риски и возможности\n");
            prompt.append("5. Общую оценку и рекомендации\n\n");
            prompt.append("Ответ должен быть структурированным, профессиональным и содержать конкретные выводы.");
        } else if ("FORECAST".equals(analysisType)) {
            prompt.append("\n\nЗАДАЧА: Создай прогноз цены на 1 месяц, 6 месяцев и 1 год. ");
            prompt.append("Для каждого периода укажи:\n");
            prompt.append("1. Прогнозируемую цену\n");
            prompt.append("2. Процентное изменение\n");
            prompt.append("3. Уровень уверенности (0-100%)\n");
            prompt.append("4. Обоснование прогноза\n");
            prompt.append("5. Ключевые факторы риска\n");
            prompt.append("6. Условия рынка\n\n");
            prompt.append("Используй математические модели, анализ трендов и вероятностные методы.");
        }
        
        return generateAnalysis(prompt.toString());
    }
    
    public boolean isHealthy() {
        try {
            String url = baseUrl + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("LLM health check failed: {}", e.getMessage());
            return false;
        }
    }
}
