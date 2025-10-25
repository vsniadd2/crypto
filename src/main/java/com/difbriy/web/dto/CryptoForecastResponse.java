package com.difbriy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoForecastResponse {
    private Long id;
    private String forecastPeriod;
    private BigDecimal predictedPrice;
    private BigDecimal priceChangePercent;
    private BigDecimal confidence;
    private String forecastReasoning;
    private String riskFactors;
    private String marketConditions;
    private String technicalIndicators;
    private LocalDateTime forecastDate;
    private LocalDateTime targetDate;
}
