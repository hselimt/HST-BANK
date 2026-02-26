package com.hstbank_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class ExchangeRateResponse {
    private String baseCurrency;
    private Map<String, BigDecimal> rates;
}
