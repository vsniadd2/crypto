package com.difbriy.web.dto.crypto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionRequestDto {
    private String symbol;
    private String timeframe;
    private boolean includeNewsAnalysis;
    private boolean includeTechnicalAnalysis;
    private int historicalDataPoints;
}
