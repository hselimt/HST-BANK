package com.hstbank_api.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRateProvider {

    // Get exchange rate from one currency to another
    BigDecimal getRate(String fromCurrency, String toCurrency);

    // Get all available rates for a base currency
    Map<String, BigDecimal> getAllRates(String baseCurrency);
}
