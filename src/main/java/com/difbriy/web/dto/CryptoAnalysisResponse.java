package com.difbriy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoAnalysisResponse {
    private Long id;
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercent24h;
    private BigDecimal rsi;
    private BigDecimal sma20;
    private BigDecimal sma50;
    private BigDecimal sma200;
    private BigDecimal volatility;
    private BigDecimal supportLevel;
    private BigDecimal resistanceLevel;
    private String technicalAnalysis;
    private String fundamentalAnalysis;
    private String marketSentiment;
    private String llmAnalysis;
    private LocalDateTime analysisDate;
    private String analysisType;
    private List<CryptoForecastResponse> forecasts;
}
