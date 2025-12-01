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
    
    @Column(name = "current_price", precision = 30, scale = 8)
    private BigDecimal currentPrice;
    
    @Column(name = "market_cap", precision = 30, scale = 8)
    private BigDecimal marketCap;
    
    @Column(name = "volume_24h", precision = 30, scale = 8)
    private BigDecimal volume24h;
    
    @Column(name = "price_change_24h", precision = 10, scale = 4)
    private BigDecimal priceChange24h;
    
    @Column(name = "price_change_percent_24h", precision = 10, scale = 4)
    private BigDecimal priceChangePercent24h;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal rsi;
    
    @Column(name = "sma_20", precision = 10, scale = 4)
    private BigDecimal sma20;
    
    @Column(name = "sma_50", precision = 10, scale = 4)
    private BigDecimal sma50;
    
    @Column(name = "sma_200", precision = 10, scale = 4)
    private BigDecimal sma200;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal volatility;
    
    @Column(name = "support_level", precision = 10, scale = 4)
    private BigDecimal supportLevel;
    
    @Column(name = "resistance_level", precision = 10, scale = 4)
    private BigDecimal resistanceLevel;
    
    @Column(name = "technical_analysis", columnDefinition = "TEXT")
    private String technicalAnalysis;
    
    @Column(name = "fundamental_analysis", columnDefinition = "TEXT")
    private String fundamentalAnalysis;
    
    @Column(name = "market_sentiment", columnDefinition = "TEXT")
    private String marketSentiment;
    
    @Column(name = "llm_analysis", columnDefinition = "TEXT")
    private String llmAnalysis;
    
    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;
    
    @Column(name = "analysis_type", nullable = false)
    private String analysisType; // "ANALYSIS" or "FORECAST"
    
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CryptoForecast> forecasts;
    
    @PrePersist
    protected void onCreate() {
        analysisDate = LocalDateTime.now();
    }
}
