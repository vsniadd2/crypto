package com.difbriy.web.dto.crypto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CryptoAnalysisDataDto {
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal priceChange24h;
    private BigDecimal volume24h;
    private BigDecimal marketCap;
    private List<PriceDataPoint> historicalPrices;
    private List<String> recentNews;
    private String technicalAnalysis;
    private BigDecimal volatility;
    private BigDecimal rsi;
    private BigDecimal movingAverage20;
    private BigDecimal movingAverage50;
}
