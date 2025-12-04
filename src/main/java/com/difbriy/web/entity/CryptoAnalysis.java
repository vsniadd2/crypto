package com.difbriy.web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "crypto_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CryptoAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String symbol;

    @Column(nullable = false)
    String name;

    @Column(name = "current_price", precision = 30, scale = 8)
    BigDecimal currentPrice;

    @Column(name = "market_cap", precision = 30, scale = 8)
    BigDecimal marketCap;

    @Column(name = "volume_24h", precision = 30, scale = 8)
    BigDecimal volume24h;

    @Column(name = "price_change_24h", precision = 10, scale = 4)
    BigDecimal priceChange24h;

    @Column(name = "price_change_percent_24h", precision = 10, scale = 4)
    BigDecimal priceChangePercent24h;

    @Column(precision = 10, scale = 4)
    BigDecimal rsi;

    @Column(name = "sma_20", precision = 10, scale = 4)
    BigDecimal sma20;

    @Column(name = "sma_50", precision = 10, scale = 4)
    BigDecimal sma50;

    @Column(name = "sma_200", precision = 10, scale = 4)
    BigDecimal sma200;

    @Column(precision = 10, scale = 4)
    BigDecimal volatility;

    @Column(name = "support_level", precision = 10, scale = 4)
    BigDecimal supportLevel;

    @Column(name = "resistance_level", precision = 10, scale = 4)
    BigDecimal resistanceLevel;

    @Column(name = "technical_analysis", columnDefinition = "TEXT")
    String technicalAnalysis;

    @Column(name = "fundamental_analysis", columnDefinition = "TEXT")
    String fundamentalAnalysis;

    @Column(name = "market_sentiment", columnDefinition = "TEXT")
    String marketSentiment;

    @Column(name = "llm_analysis", columnDefinition = "TEXT")
    String llmAnalysis;

    @Column(name = "analysis_date", nullable = false)
    @CreationTimestamp
    LocalDateTime analysisDate;

    @Column(name = "analysis_type", nullable = false)
    String analysisType; // "ANALYSIS" or "FORECAST"

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<CryptoForecast> forecasts;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "symbol = " + symbol + ", " +
                "name = " + name + ", " +
                "currentPrice = " + currentPrice + ", " +
                "marketCap = " + marketCap + ", " +
                "volume24h = " + volume24h + ", " +
                "priceChange24h = " + priceChange24h + ", " +
                "priceChangePercent24h = " + priceChangePercent24h + ", " +
                "rsi = " + rsi + ", " +
                "sma20 = " + sma20 + ", " +
                "sma50 = " + sma50 + ", " +
                "sma200 = " + sma200 + ", " +
                "volatility = " + volatility + ", " +
                "supportLevel = " + supportLevel + ", " +
                "resistanceLevel = " + resistanceLevel + ", " +
                "technicalAnalysis = " + technicalAnalysis + ", " +
                "fundamentalAnalysis = " + fundamentalAnalysis + ", " +
                "marketSentiment = " + marketSentiment + ", " +
                "llmAnalysis = " + llmAnalysis + ", " +
                "analysisDate = " + analysisDate + ", " +
                "analysisType = " + analysisType + ")";
    }
}
