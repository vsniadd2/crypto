package com.difbriy.web.service.crypto;

import com.difbriy.web.entity.CryptoData;
import com.difbriy.web.repository.CryptoDataRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final CryptoDataRepository cryptoDataRepository;

    private static final String ALL_COINS_CACHE_KEY = "all_coins";
    private static final String SINGLE_COIN_CACHE_KEY_PREFIX = "coin:";
    private static final long CACHE_DURATION = 1;

    @Value("${api.crypto.url}")
    private String apiUrl;

    @Value("${api.crypto.key}")
    private String apiKey;

    @Scheduled(fixedRate = 30000)
    public void scheduledUpdate() {
        try {
            List<Map<String, Object>> data = fetchDataFromCoinGeckoInternal();
            saveCryptoDataToDatabase(data);
            messagingTemplate.convertAndSend("/topic/crypto", data);
            log.info("Data updated, saved to database and sent through WebSocket");
        } catch (Exception e) {
            log.error("Error in scheduled update", e);
        }
    }

    public void sendCryptoDataOnDemand() {
        try {
            List<Map<String, Object>> data = fetchDataFromCoinGeckoInternal();
            messagingTemplate.convertAndSend("/topic/crypto/response", data);
            log.info("Crypto data sent on demand through WebSocket");
        } catch (Exception e) {
            log.error("Error sending crypto data on demand", e);
        }
    }


    public void sendSingleCryptoData(Long coinId) {
        try {
            ResponseEntity<?> response = fetchSingleCoin(coinId);
            if (response.getBody() != null) {
                messagingTemplate.convertAndSend("/topic/crypto/single", response.getBody());
                log.info("Single crypto data sent for coin ID: {}", coinId);
            }
        } catch (Exception e) {
            log.error("Error sending single crypto data for coin ID: {}", coinId, e);
        }
    }

    public ResponseEntity<?> fetchDataFromCoinGecko() throws IOException {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cachedData = (List<Map<String, Object>>) redisTemplate.opsForValue().get(ALL_COINS_CACHE_KEY);
        if (cachedData == null) {
            cachedData = fetchDataFromCoinGeckoInternal();
        }
        return ResponseEntity.ok(cachedData);
    }

    private List<Map<String, Object>> fetchDataFromCoinGeckoInternal() throws IOException {
        log.info("Fetching new data from CoinMarketCap API");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("X-CMC_PRO_API_KEY", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to fetch data from API: {}", response.code());
                return new ArrayList<>();
            }

            Map<String, Object> jsonResponse = objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) jsonResponse.get("data");

            List<Map<String, Object>> processedData = data.stream()
                    .map(coin -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> quote = (Map<String, Object>) ((Map<String, Object>) coin.get("quote")).get("USD");
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", coin.get("id"));
                        result.put("name", coin.get("name"));
                        result.put("symbol", coin.get("symbol"));
                        result.put("slug", coin.get("slug"));
                        result.put("circulating_supply", coin.get("circulating_supply"));
                        result.put("total_supply", coin.get("total_supply"));
                        result.put("price", quote.get("price"));
                        result.put("volume_24h", quote.get("volume_24h"));
                        result.put("volume_change_24h", quote.get("volume_change_24h"));
                        result.put("percent_change_1h", quote.get("percent_change_1h"));
                        result.put("percent_change_24h", quote.get("percent_change_24h"));
                        result.put("percent_change_7d", quote.get("percent_change_7d"));
                        result.put("percent_change_30d", quote.get("percent_change_30d"));
                        result.put("percent_change_60d", quote.get("percent_change_60d"));
                        result.put("percent_change_90d", quote.get("percent_change_90d"));
                        result.put("market_cap", quote.get("market_cap"));
                        result.put("market_cap_dominance", quote.get("market_cap_dominance"));
                        result.put("fully_diluted_market_cap", quote.get("fully_diluted_market_cap"));
                        result.put("last_updated", quote.get("last_updated"));
                        return result;
                    }).collect(Collectors.toList());

            redisTemplate.opsForValue().set(ALL_COINS_CACHE_KEY, processedData, CACHE_DURATION, TimeUnit.MINUTES);
            log.info("New data fetched from API and stored in Redis cache");

            return processedData;
        }
    }

    public ResponseEntity<?> fetchSingleCoin(Long id) {
        String cacheKey = SINGLE_COIN_CACHE_KEY_PREFIX + id;

        @SuppressWarnings("unchecked")
        Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.info("Returning single coin data from Redis cache for id: {}", id);
            return ResponseEntity.ok(cachedData);
        }

        log.info("Cache miss - fetching single coin data from CoinMarketCap API for id: {}", id);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl + "/" + id)
                .addHeader("X-CMC_PRO_API_KEY", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to fetch single coin data from API: {}", response.code());
                return ResponseEntity.ok(new ArrayList<>());
            }

            Map<String, Object> jsonResponse = objectMapper.readValue(
                    response.body().string(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) jsonResponse.get("data");
            @SuppressWarnings("unchecked")
            Map<String, Object> quote = (Map<String, Object>) ((Map<String, Object>) data.get("quote")).get("USD");

            Map<String, Object> result = new HashMap<>();
            result.put("id", data.get("id"));
            result.put("name", data.get("name"));
            result.put("symbol", data.get("symbol"));
            result.put("slug", data.get("slug"));
            result.put("circulating_supply", data.get("circulating_supply"));
            result.put("total_supply", data.get("total_supply"));
            result.put("price", quote.get("price"));
            result.put("volume_24h", quote.get("volume_24h"));
            result.put("volume_change_24h", quote.get("volume_change_24h"));
            result.put("percent_change_1h", quote.get("percent_change_1h"));
            result.put("percent_change_24h", quote.get("percent_change_24h"));
            result.put("percent_change_7d", quote.get("percent_change_7d"));
            result.put("percent_change_30d", quote.get("percent_change_30d"));
            result.put("percent_change_60d", quote.get("percent_change_60d"));
            result.put("percent_change_90d", quote.get("percent_change_90d"));
            result.put("market_cap", quote.get("market_cap"));
            result.put("market_cap_dominance", quote.get("market_cap_dominance"));
            result.put("fully_diluted_market_cap", quote.get("fully_diluted_market_cap"));
            result.put("last_updated", quote.get("last_updated"));

            redisTemplate.opsForValue().set(cacheKey, result, CACHE_DURATION, TimeUnit.MINUTES);
            log.info("Single coin data fetched from API and stored in Redis cache for id: {}", id);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error fetching single coin data from API", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }


    private void saveCryptoDataToDatabase(List<Map<String, Object>> data) {
        try {
            List<CryptoData> cryptoDataList = new ArrayList<>();

            for (Map<String, Object> coin : data) {
                CryptoData cryptoData = new CryptoData();
                cryptoData.setSymbol((String) coin.get("symbol"));
                cryptoData.setName((String) coin.get("name"));

                if (coin.get("price") != null) {
                    cryptoData.setPrice(new BigDecimal(coin.get("price").toString()));
                }

                if (coin.get("market_cap") != null) {
                    cryptoData.setMarketCap(new BigDecimal(coin.get("market_cap").toString()));
                }

                if (coin.get("volume_24h") != null) {
                    cryptoData.setVolume24h(new BigDecimal(coin.get("volume_24h").toString()));
                }

                if (coin.get("circulating_supply") != null) {
                    cryptoData.setCirculatingSupply(new BigDecimal(coin.get("circulating_supply").toString()));
                }

                if (coin.get("total_supply") != null) {
                    cryptoData.setTotalSupply(new BigDecimal(coin.get("total_supply").toString()));
                }

                if (coin.get("max_supply") != null) {
                    cryptoData.setMaxSupply(new BigDecimal(coin.get("max_supply").toString()));
                }

                if (coin.get("percent_change_1h") != null) {
                    cryptoData.setPercentChange1h(new BigDecimal(coin.get("percent_change_1h").toString()));
                }

                if (coin.get("percent_change_24h") != null) {
                    cryptoData.setPercentChange24h(new BigDecimal(coin.get("percent_change_24h").toString()));
                }

                if (coin.get("percent_change_7d") != null) {
                    cryptoData.setPercentChange7d(new BigDecimal(coin.get("percent_change_7d").toString()));
                }

                if (coin.get("rank") != null) {
                    cryptoData.setRank((Integer) coin.get("rank"));
                }

                cryptoData.setTimestamp(LocalDateTime.now());
                cryptoDataList.add(cryptoData);
            }

            cryptoDataRepository.saveAll(cryptoDataList);
            log.info("Saved {} crypto data records to database", cryptoDataList.size());

        } catch (Exception e) {
            log.error("Error saving crypto data to database", e);
        }
    }

    public List<CryptoData> getCryptoHistory(String symbol, String period) {
        LocalDateTime startTime;

        switch (period.toLowerCase()) {
            case "24h":
                startTime = LocalDateTime.now().minusHours(24);
                return cryptoDataRepository.findLast24HoursBySymbol(symbol, startTime);
            case "7d":
                startTime = LocalDateTime.now().minusDays(7);
                return cryptoDataRepository.findLast7DaysBySymbol(symbol, startTime);
            case "30d":
                startTime = LocalDateTime.now().minusDays(30);
                return cryptoDataRepository.findLast30DaysBySymbol(symbol, startTime);
            case "6m":
                startTime = LocalDateTime.now().minusDays(180);
                return cryptoDataRepository.findLast6MonthsBySymbol(symbol, startTime);
            default:
                startTime = LocalDateTime.now().minusDays(7);
                return cryptoDataRepository.findLast7DaysBySymbol(symbol, startTime);
        }
    }

    public List<CryptoData> getLatestCryptoData() {
        return cryptoDataRepository.findLatestDataForAllCryptos();
    }

    public CryptoData getLatestCryptoData(String symbol) {
        return cryptoDataRepository.findLatestBySymbol(symbol);
    }

    public List<String> getAllCryptoSymbols() {
        return cryptoDataRepository.findAllSymbols();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldData() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(180); // 6 месяцев = 180 дней
            cryptoDataRepository.deleteOldData(cutoffDate);
            log.info("Cleaned up old crypto data older than 6 months (180 days)");
        } catch (Exception e) {
            log.error("Error cleaning up old crypto data", e);
        }
    }
}