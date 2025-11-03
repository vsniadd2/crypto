package com.difbriy.web.dto.crypto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PredictionResponseDto {
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal predictedPrice;
    private BigDecimal confidenceScore;
    private String predictionReasoning;
    private String marketSentiment;
    private String technicalIndicators;
    private String newsSentiment;
    private String timeframe;
    private List<String> riskFactors;
    private List<String> supportingFactors;
}
