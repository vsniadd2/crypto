package com.difbriy.web.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "crypto_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(nullable = false)
    private String name;
    
    @Column(precision = 30, scale = 8)
    private BigDecimal currentPrice;
    
    @Column(precision = 30, scale = 8)
    private BigDecimal marketCap;
    
    @Column(precision = 30, scale = 8)
    private BigDecimal volume24h;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal priceChange24h;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal priceChangePercent24h;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal rsi;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal sma20;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal sma50;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal sma200;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal volatility;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal supportLevel;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal resistanceLevel;
    
    @Column(columnDefinition = "TEXT")
    private String technicalAnalysis;
    
    @Column(columnDefinition = "TEXT")
    private String fundamentalAnalysis;
    
    @Column(columnDefinition = "TEXT")
    private String marketSentiment;
    
    @Column(columnDefinition = "TEXT")
    private String llmAnalysis;
    
    @Column(nullable = false)
    private LocalDateTime analysisDate;
    
    @Column(nullable = false)
    private String analysisType; // "ANALYSIS" or "FORECAST"
    
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CryptoForecast> forecasts;
    
    @PrePersist
    protected void onCreate() {
        analysisDate = LocalDateTime.now();
    }
}
