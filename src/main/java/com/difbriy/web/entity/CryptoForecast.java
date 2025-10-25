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
    
    @Column(nullable = false)
    private String forecastPeriod; // "1_MONTH", "6_MONTHS", "1_YEAR"
    
    @Column(precision = 30, scale = 8)
    private BigDecimal predictedPrice;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal priceChangePercent;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal confidence;
    
    @Column(columnDefinition = "TEXT")
    private String forecastReasoning;
    
    @Column(columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(columnDefinition = "TEXT")
    private String marketConditions;
    
    @Column(columnDefinition = "TEXT")
    private String technicalIndicators;
    
    @Column(nullable = false)
    private LocalDateTime forecastDate;
    
    @Column(nullable = false)
    private LocalDateTime targetDate;
    
    @PrePersist
    protected void onCreate() {
        forecastDate = LocalDateTime.now();
    }
}
