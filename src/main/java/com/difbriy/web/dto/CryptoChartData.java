package com.difbriy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CryptoChartData {
    private String symbol;
    private String name;
    private List<ChartPoint> data;
    private BigDecimal currentPrice;
    private BigDecimal priceChange24h;
    private BigDecimal percentChange24h;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private LocalDateTime lastUpdated;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartPoint {
        private LocalDateTime timestamp;
        private BigDecimal price;
        private BigDecimal volume;
        private BigDecimal marketCap;
    }
} 