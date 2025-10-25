package com.difbriy.web.service.crypto;

import com.difbriy.web.entity.CryptoData;
import com.difbriy.web.repository.CryptoDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDataService {
    
    @Value("${api.crypto}")
    private String cryptoApiUrl;
    
    @Value("${api.crypto.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CryptoDataRepository cryptoDataRepository;
    
    public Map<String, Object> getCryptoData(String symbol) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            Map<String, Object> currentData = getCurrentCryptoData(symbol);
            data.putAll(currentData);

            List<Map<String, Object>> historicalData = getHistoricalCryptoData(symbol, 30);
            data.put("historicalPrices", formatHistoricalData(historicalData));

            calculateTechnicalIndicators(data, historicalData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error fetching crypto data for {}: {}", symbol, e.getMessage(), e);
            return getDefaultCryptoData(symbol);
        }
    }
    
    private Map<String, Object> getCurrentCryptoData(String symbol) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-CMC_PRO_API_KEY", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol=" + symbol;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode dataNode = jsonResponse.get("data");
                
                if (dataNode != null && dataNode.has(symbol.toUpperCase())) {
                    JsonNode data = dataNode.get(symbol.toUpperCase());
                    JsonNode quote = data.get("quote");
                    JsonNode usdQuote = quote != null ? quote.get("USD") : null;
                    
                    if (data != null && usdQuote != null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("symbol", symbol);
                        result.put("name", data.get("name") != null ? data.get("name").asText() : symbol + " Coin");
                        result.put("currentPrice", usdQuote.get("price") != null ? new BigDecimal(usdQuote.get("price").asText()) : BigDecimal.ZERO);
                        result.put("marketCap", usdQuote.get("market_cap") != null ? new BigDecimal(usdQuote.get("market_cap").asText()) : BigDecimal.ZERO);
                        result.put("volume24h", usdQuote.get("volume_24h") != null ? new BigDecimal(usdQuote.get("volume_24h").asText()) : BigDecimal.ZERO);
                        result.put("priceChange24h", usdQuote.get("price_change_24h") != null ? new BigDecimal(usdQuote.get("price_change_24h").asText()) : BigDecimal.ZERO);
                        result.put("priceChangePercent24h", usdQuote.get("percent_change_24h") != null ? new BigDecimal(usdQuote.get("percent_change_24h").asText()) : BigDecimal.ZERO);
                        
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching current data for {}: {}", symbol, e.getMessage());
        }
        
        return getDefaultCryptoData(symbol);
    }
    
    private List<Map<String, Object>> getHistoricalCryptoData(String symbol, int days) {
        try {
            String coinId = getCoinGeckoId(symbol);
            if (coinId == null) {
                log.warn("Unknown symbol for CoinGecko API: {}", symbol);
                return new ArrayList<>();
            }
            
            String url = String.format("https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d",
                coinId, days);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                JsonNode prices = jsonResponse.get("prices");
                
                List<Map<String, Object>> historicalData = new ArrayList<>();
                for (JsonNode price : prices) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("timestamp", price.get(0).asLong());
                    dataPoint.put("price", new BigDecimal(price.get(1).asText()));
                    historicalData.add(dataPoint);
                }
                
                return historicalData;
            }
        } catch (Exception e) {
            log.error("Error fetching historical data for {}: {}", symbol, e.getMessage());
        }
        
        return generateMockHistoricalData(symbol, days);
    }
    
    private void calculateTechnicalIndicators(Map<String, Object> data, List<Map<String, Object>> historicalData) {
        if (historicalData.size() < 20) {
            data.put("rsi", BigDecimal.valueOf(50));
            data.put("sma20", data.get("currentPrice"));
            data.put("sma50", data.get("currentPrice"));
            data.put("sma200", data.get("currentPrice"));
            data.put("volatility", BigDecimal.valueOf(5));
            data.put("supportLevel", ((BigDecimal) data.get("currentPrice")).multiply(BigDecimal.valueOf(0.9)));
            data.put("resistanceLevel", ((BigDecimal) data.get("currentPrice")).multiply(BigDecimal.valueOf(1.1)));
            return;
        }
        
        List<BigDecimal> prices = historicalData.stream()
            .map(point -> (BigDecimal) point.get("price"))
            .toList();
        
        BigDecimal currentPrice = (BigDecimal) data.get("currentPrice");

        BigDecimal rsi = calculateRSI(prices);
        data.put("rsi", rsi);

        data.put("sma20", calculateSMA(prices, Math.min(20, prices.size())));
        data.put("sma50", calculateSMA(prices, Math.min(50, prices.size())));
        data.put("sma200", calculateSMA(prices, Math.min(200, prices.size())));

        BigDecimal volatility = calculateVolatility(prices);
        data.put("volatility", volatility);

        BigDecimal[] supportResistance = calculateSupportResistance(prices);
        data.put("supportLevel", supportResistance[0]);
        data.put("resistanceLevel", supportResistance[1]);
    }
    
    private BigDecimal calculateRSI(List<BigDecimal> prices) {
        if (prices.size() < 14) return BigDecimal.valueOf(50);
        
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }
        BigDecimal avgGain = gains.stream()
            .limit(14)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(14), 4, RoundingMode.HALF_UP);
        
        BigDecimal avgLoss = losses.stream()
            .limit(14)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(14), 4, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
            BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP)
        );
        
        return rsi;
    }
    
    private BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) {
            return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 8, RoundingMode.HALF_UP);
        }
        
        return prices.stream()
            .skip(prices.size() - period)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateVolatility(List<BigDecimal> prices) {
        if (prices.size() < 2) return BigDecimal.valueOf(5);
        
        BigDecimal mean = calculateSMA(prices, prices.size());
        
        BigDecimal variance = prices.stream()
            .map(price -> price.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), 8, RoundingMode.HALF_UP);
        
        BigDecimal stdDev = new BigDecimal(Math.sqrt(variance.doubleValue()));
        BigDecimal volatility = stdDev.divide(mean, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        return volatility;
    }
    
    private BigDecimal[] calculateSupportResistance(List<BigDecimal> prices) {
        if (prices.size() < 10) {
            BigDecimal currentPrice = prices.get(prices.size() - 1);
            return new BigDecimal[]{
                currentPrice.multiply(BigDecimal.valueOf(0.9)),
                currentPrice.multiply(BigDecimal.valueOf(1.1))
            };
        }
        
        BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal support = min.add(max.subtract(min).multiply(BigDecimal.valueOf(0.2)));
        BigDecimal resistance = min.add(max.subtract(min).multiply(BigDecimal.valueOf(0.8)));
        
        return new BigDecimal[]{support, resistance};
    }
    
    private String formatHistoricalData(List<Map<String, Object>> historicalData) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Map<String, Object> dataPoint : historicalData) {
            long timestamp = (Long) dataPoint.get("timestamp");
            BigDecimal price = (BigDecimal) dataPoint.get("price");
            
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, 
                java.time.ZoneOffset.UTC);
            
            sb.append(String.format("%s: $%.2f\n", 
                dateTime.format(formatter), price));
        }
        
        return sb.toString();
    }
    
    private Map<String, Object> getDefaultCryptoData(String symbol) {
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("name", symbol + " Coin");
        data.put("currentPrice", BigDecimal.valueOf(50000));
        data.put("marketCap", BigDecimal.valueOf(1000000000));
        data.put("volume24h", BigDecimal.valueOf(100000000));
        data.put("priceChange24h", BigDecimal.valueOf(1000));
        data.put("priceChangePercent24h", BigDecimal.valueOf(2.5));
        data.put("rsi", BigDecimal.valueOf(55));
        data.put("sma20", BigDecimal.valueOf(49000));
        data.put("sma50", BigDecimal.valueOf(48000));
        data.put("sma200", BigDecimal.valueOf(45000));
        data.put("volatility", BigDecimal.valueOf(8.5));
        data.put("supportLevel", BigDecimal.valueOf(45000));
        data.put("resistanceLevel", BigDecimal.valueOf(55000));
        data.put("historicalPrices", "Mock historical data");
        
        return data;
    }
    
    private List<Map<String, Object>> generateMockHistoricalData(String symbol, int days) {
        List<Map<String, Object>> data = new ArrayList<>();
        BigDecimal basePrice = BigDecimal.valueOf(50000);
        
        for (int i = days; i >= 0; i--) {
            Map<String, Object> dataPoint = new HashMap<>();
            long timestamp = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000L);

            double randomChange = (Math.random() - 0.5) * 0.1; // ±5%
            BigDecimal price = basePrice.multiply(BigDecimal.valueOf(1 + randomChange));
            
            dataPoint.put("timestamp", timestamp);
            dataPoint.put("price", price);
            data.add(dataPoint);
        }
        
        return data;
    }
    
    private String getCoinGeckoId(String symbol) {
        Map<String, String> symbolToId = new HashMap<>();
        symbolToId.put("BTC", "bitcoin");
        symbolToId.put("ETH", "ethereum");
        symbolToId.put("BNB", "binancecoin");
        symbolToId.put("XRP", "ripple");
        symbolToId.put("ADA", "cardano");
        symbolToId.put("SOL", "solana");
        symbolToId.put("DOGE", "dogecoin");
        symbolToId.put("DOT", "polkadot");
        symbolToId.put("AVAX", "avalanche-2");
        symbolToId.put("MATIC", "matic-network");
        symbolToId.put("LTC", "litecoin");
        symbolToId.put("LINK", "chainlink");
        symbolToId.put("UNI", "uniswap");
        symbolToId.put("ATOM", "cosmos");
        symbolToId.put("FIL", "filecoin");
        symbolToId.put("TRX", "tron");
        symbolToId.put("ETC", "ethereum-classic");
        symbolToId.put("XLM", "stellar");
        symbolToId.put("BCH", "bitcoin-cash");
        symbolToId.put("ALGO", "algorand");
        
        return symbolToId.get(symbol.toUpperCase());
    }

    public Map<String, Object> getLatestCryptoDataFromDB(String symbol) {
        log.info("Getting latest crypto data from DB for symbol: {}", symbol);
        
        try {
            CryptoData latestData = cryptoDataRepository.findLatestBySymbol(symbol);
            
            if (latestData == null) {
                log.warn("No data found in DB for symbol: {}", symbol);
                return getDefaultCryptoData(symbol);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("symbol", latestData.getSymbol());
            data.put("name", latestData.getName());
            data.put("currentPrice", latestData.getPrice());
            data.put("marketCap", latestData.getMarketCap());
            data.put("volume24h", latestData.getVolume24h());
            data.put("priceChange24h", latestData.getPercentChange24h());
            data.put("priceChangePercent24h", latestData.getPercentChange24h());
            
            data.put("historicalPrices", new ArrayList<>());
            
            calculateBasicTechnicalIndicators(data, latestData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error getting latest crypto data from DB for {}: {}", symbol, e.getMessage(), e);
            return getDefaultCryptoData(symbol);
        }
    }
    

    public Map<String, Object> getCryptoDataForForecastFromDB(String symbol) {
        log.info("Getting crypto data for forecast from DB for symbol: {}", symbol);
        
        try {
            CryptoData latestData = cryptoDataRepository.findLatestBySymbol(symbol);
            
            if (latestData == null) {
                log.warn("No data found in DB for symbol: {}", symbol);
                return getDefaultCryptoData(symbol);
            }
            
            LocalDateTime startTime = LocalDateTime.now().minusMonths(6);
            List<CryptoData> historicalData = cryptoDataRepository.findLast6MonthsBySymbol(symbol, startTime);
            
            Map<String, Object> data = new HashMap<>();
            data.put("symbol", latestData.getSymbol());
            data.put("name", latestData.getName());
            data.put("currentPrice", latestData.getPrice());
            data.put("marketCap", latestData.getMarketCap());
            data.put("volume24h", latestData.getVolume24h());
            data.put("priceChange24h", latestData.getPercentChange24h());
            data.put("priceChangePercent24h", latestData.getPercentChange24h());

            List<Map<String, Object>> historicalPrices = historicalData.stream()
                .map(cryptoData -> {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("timestamp", cryptoData.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    dataPoint.put("price", cryptoData.getPrice());
                    dataPoint.put("volume", cryptoData.getVolume24h());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            
            data.put("historicalPrices", historicalPrices);

            calculateTechnicalIndicatorsFromHistoricalData(data, historicalData);
            
            return data;
            
        } catch (Exception e) {
            log.error("Error getting crypto data for forecast from DB for {}: {}", symbol, e.getMessage(), e);
            return getDefaultCryptoData(symbol);
        }
    }

    private void calculateBasicTechnicalIndicators(Map<String, Object> data, CryptoData latestData) {
        data.put("rsi", BigDecimal.valueOf(50));
        data.put("sma20", latestData.getPrice());
        data.put("sma50", latestData.getPrice());
        data.put("sma200", latestData.getPrice());
        data.put("volatility", BigDecimal.valueOf(5));
        data.put("supportLevel", latestData.getPrice().multiply(BigDecimal.valueOf(0.95)));
        data.put("resistanceLevel", latestData.getPrice().multiply(BigDecimal.valueOf(1.05)));
    }

    private void calculateTechnicalIndicatorsFromHistoricalData(Map<String, Object> data, List<CryptoData> historicalData) {
        if (historicalData.isEmpty()) {
            data.put("rsi", BigDecimal.valueOf(50));
            data.put("sma20", data.get("currentPrice"));
            data.put("sma50", data.get("currentPrice"));
            data.put("sma200", data.get("currentPrice"));
            data.put("volatility", BigDecimal.valueOf(5));
            data.put("supportLevel", ((BigDecimal) data.get("currentPrice")).multiply(BigDecimal.valueOf(0.95)));
            data.put("resistanceLevel", ((BigDecimal) data.get("currentPrice")).multiply(BigDecimal.valueOf(1.05)));
            return;
        }

        BigDecimal rsi = calculateRSIFromDB(historicalData);
        data.put("rsi", rsi);
        
        data.put("sma20", calculateSMAFromDB(historicalData, 20));
        data.put("sma50", calculateSMAFromDB(historicalData, 50));
        data.put("sma200", calculateSMAFromDB(historicalData, 200));

        BigDecimal volatility = calculateVolatilityFromDB(historicalData);
        data.put("volatility", volatility);

        BigDecimal[] supportResistance = calculateSupportResistanceFromDB(historicalData);
        data.put("supportLevel", supportResistance[0]);
        data.put("resistanceLevel", supportResistance[1]);
    }

    private BigDecimal calculateRSIFromDB(List<CryptoData> historicalData) {
        if (historicalData.size() < 14) {
            return BigDecimal.valueOf(50);
        }
        
        List<BigDecimal> prices = historicalData.stream()
            .map(CryptoData::getPrice)
            .collect(Collectors.toList());
        
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }
        
        if (gains.size() < 14) {
            return BigDecimal.valueOf(50);
        }
        
        BigDecimal avgGain = gains.subList(gains.size() - 14, gains.size()).stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(14), 4, RoundingMode.HALF_UP);
        
        BigDecimal avgLoss = losses.subList(losses.size() - 14, losses.size()).stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(14), 4, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
            BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP)
        );
        
        return rsi;
    }

    private BigDecimal calculateSMAFromDB(List<CryptoData> historicalData, int period) {
        if (historicalData.size() < period) {
            return historicalData.get(historicalData.size() - 1).getPrice();
        }
        
        List<CryptoData> recentData = historicalData.subList(historicalData.size() - period, historicalData.size());
        BigDecimal sum = recentData.stream()
            .map(CryptoData::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolatilityFromDB(List<CryptoData> historicalData) {
        if (historicalData.size() < 2) {
            return BigDecimal.valueOf(5);
        }
        
        List<BigDecimal> prices = historicalData.stream()
            .map(CryptoData::getPrice)
            .collect(Collectors.toList());
        
        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal returnValue = prices.get(i).subtract(prices.get(i - 1))
                .divide(prices.get(i - 1), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            returns.add(returnValue);
        }

        BigDecimal mean = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);
        
        BigDecimal variance = returns.stream()
            .map(r -> r.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(returns.size()), 4, RoundingMode.HALF_UP);
        
        BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        return volatility;
    }

    private BigDecimal[] calculateSupportResistanceFromDB(List<CryptoData> historicalData) {
        if (historicalData.isEmpty()) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }
        
        List<BigDecimal> prices = historicalData.stream()
            .map(CryptoData::getPrice)
            .collect(Collectors.toList());
        
        BigDecimal minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal currentPrice = prices.get(prices.size() - 1);
        
        BigDecimal supportLevel = minPrice.min(currentPrice.multiply(BigDecimal.valueOf(0.95)));
        BigDecimal resistanceLevel = maxPrice.max(currentPrice.multiply(BigDecimal.valueOf(1.05)));
        
        return new BigDecimal[]{supportLevel, resistanceLevel};
    }
}
