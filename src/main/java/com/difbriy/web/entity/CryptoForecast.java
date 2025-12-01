package com.difbriy.web.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_forecast")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoForecast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private CryptoAnalysis analysis;
    
    @Column(name = "forecast_period", nullable = false)
    private String forecastPeriod; // "1_MONTH", "6_MONTHS", "1_YEAR"
    
    @Column(name = "predicted_price", precision = 30, scale = 8)
    private BigDecimal predictedPrice;
    
    @Column(name = "price_change_percent", precision = 10, scale = 4)
    private BigDecimal priceChangePercent;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal confidence;
    
    @Column(name = "forecast_reasoning", columnDefinition = "TEXT")
    private String forecastReasoning;
    
    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(name = "market_conditions", columnDefinition = "TEXT")
    private String marketConditions;
    
    @Column(name = "technical_indicators", columnDefinition = "TEXT")
    private String technicalIndicators;
    
    @Column(name = "forecast_date", nullable = false)
    private LocalDateTime forecastDate;
    
    @Column(name = "target_date", nullable = false)
    private LocalDateTime targetDate;
    
    @PrePersist
    protected void onCreate() {
        forecastDate = LocalDateTime.now();
    }
}
