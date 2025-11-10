package com.difbriy.web.dto.crypto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoAnalysisRequest {
    private String symbol;
    private String analysisType;
    private List<String> forecastPeriods;
    private boolean includeTechnicalAnalysis = true;
    private boolean includeFundamentalAnalysis = true;
    private boolean includeMarketSentiment = true;
    private int historicalDataPoints = 100;
}
