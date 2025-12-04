package com.difbriy.web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_forecast")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CryptoForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    CryptoAnalysis analysis;

    @Column(name = "forecast_period", nullable = false)
    String forecastPeriod; // "1 MONTH", "6 MONTHS", "1 YEAR"

    @Column(name = "predicted_price", precision = 30, scale = 8)
    BigDecimal predictedPrice;

    @Column(name = "price_change_percent", precision = 10, scale = 4)
    BigDecimal priceChangePercent;

    @Column(precision = 10, scale = 4)
    BigDecimal confidence;

    @Column(name = "forecast_reasoning", columnDefinition = "TEXT")
    String forecastReasoning;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    String riskFactors;

    @Column(name = "market_conditions", columnDefinition = "TEXT")
    String marketConditions;

    @Column(name = "technical_indicators", columnDefinition = "TEXT")
    String technicalIndicators;

    @Column(name = "forecast_date", nullable = false)
    @CreationTimestamp
    LocalDateTime forecastDate;

    @Column(name = "target_date", nullable = false)
    LocalDateTime targetDate;

}
