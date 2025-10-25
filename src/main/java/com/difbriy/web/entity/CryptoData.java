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
@Table(name = "crypto_data")
public class CryptoData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "symbol", nullable = false)
    String symbol;

    @Column(name = "name")
    String name;

    @Column(name = "price", precision = 20, scale = 8)
    BigDecimal price;

    @Column(name = "market_cap", precision = 30, scale = 8)
    BigDecimal marketCap;

    @Column(name = "volume_24h", precision = 30, scale = 8)
    BigDecimal volume24h;

    @Column(name = "circulating_supply", precision = 30, scale = 8)
    BigDecimal circulatingSupply;

    @Column(name = "total_supply", precision = 30, scale = 8)
    BigDecimal totalSupply;

    @Column(name = "max_supply", precision = 30, scale = 8)
    BigDecimal maxSupply;

    @Column(name = "percent_change_1h", precision = 10, scale = 4)
    BigDecimal percentChange1h;

    @Column(name = "percent_change_24h", precision = 10, scale = 4)
    BigDecimal percentChange24h;

    @Column(name = "percent_change_7d", precision = 10, scale = 4)
    BigDecimal percentChange7d;

    @Column(name = "rank")
    Integer rank;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp;

    @PrePersist
    private void init() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
} 