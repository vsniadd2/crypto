package com.difbriy.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "crypto_predictions")
public class CryptoPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "symbol", nullable = false)
    String symbol;

    @Column(name = "current_price", precision = 30, scale = 8)
    BigDecimal currentPrice;

    @Column(name = "predicted_price_1h", precision = 30, scale = 8)
    BigDecimal predictedPrice1h;

    @Column(name = "predicted_price_24h", precision = 30, scale = 8)
    BigDecimal predictedPrice24h;

    @Column(name = "predicted_price_7d", precision = 30, scale = 8)
    BigDecimal predictedPrice7d;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    BigDecimal confidenceScore;

    @Column(name = "prediction_reasoning", columnDefinition = "TEXT")
    String predictionReasoning;

    @Column(name = "market_sentiment")
    String marketSentiment;

    @Column(name = "technical_indicators", columnDefinition = "TEXT")
    String technicalIndicators;

    @Column(name = "news_sentiment")
    String newsSentiment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "prediction_timestamp", nullable = false)
    LocalDateTime predictionTimestamp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    private void init() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (predictionTimestamp == null) {
            predictionTimestamp = LocalDateTime.now();
        }
    }
}
