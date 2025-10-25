package com.difbriy.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceDataPoint {
    private String timestamp;
    private BigDecimal price;
    private BigDecimal volume;
}
