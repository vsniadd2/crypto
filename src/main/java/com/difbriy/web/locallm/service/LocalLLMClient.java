package com.difbriy.web.locallm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalLLMClient {

    @Value("${local.llm.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${local.llm.model:gemma3:4b}")
    private String model;

    @Value("${local.llm.temperature:0.3}")
    private double temperature;

    @Value("${local.llm.max-tokens:1000}")
    private int maxTokens;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Генерирует прогноз через локальную LLM
     */
    public String generatePrediction(String prompt) {
        try {
            log.info("Sending request to local LLM: {}", model);
            
            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("prompt", prompt);
            request.put("stream", false);
            request.put("options", Map.of(
                "temperature", temperature,
                "num_predict", maxTokens
            ));

            String response = restTemplate.postForObject(
                baseUrl + "/api/generate",
                request,
                String.class
            );

            if (response != null) {
                JsonNode jsonResponse = objectMapper.readTree(response);
                String generatedText = jsonResponse.get("response").asText();
                log.info("Received response from LLM, length: {}", generatedText.length());
                return generatedText;
            }

            return "Ошибка: LLM не вернул ответ";

        } catch (Exception e) {
            log.error("Error calling local LLM: {}", e.getMessage(), e);
            return "Ошибка при обращении к локальной LLM: " + e.getMessage();
        }
    }

    /**
     * Проверяет доступность локальной LLM
     */
    public boolean isAvailable() {
        try {
            String response = restTemplate.getForObject(baseUrl + "/api/tags", String.class);
            return response != null && response.contains(model);
        } catch (Exception e) {
            log.warn("Local LLM not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получает список доступных моделей
     */
    public String getAvailableModels() {
        try {
            return restTemplate.getForObject(baseUrl + "/api/tags", String.class);
        } catch (Exception e) {
            log.error("Error getting available models: {}", e.getMessage());
            return "Ошибка при получении списка моделей";
        }
    }
}
