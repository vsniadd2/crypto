package com.difbriy.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoAnalysisRequest {
    private String symbol;
    private String analysisType; // "ANALYSIS" or "FORECAST"
    private List<String> forecastPeriods; // ["1_MONTH", "6_MONTHS", "1_YEAR"]
    private boolean includeTechnicalAnalysis = true;
    private boolean includeFundamentalAnalysis = true;
    private boolean includeMarketSentiment = true;
    private int historicalDataPoints = 100; // Количество точек для анализа
}
