package com.hstbank_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// cache and fallback logic are private, only interface methods exposed
@Service
@RequiredArgsConstructor
public class LiveExchangeRateProvider implements ExchangeRateProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache: "TRY" -> { "USD" -> 0.028, "EUR" -> 0.025, ... }
    private final ConcurrentHashMap<String, Map<String, BigDecimal>> rateCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final String API_URL = "https://api.frankfurter.app/latest";

    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        Map<String, BigDecimal> rates = getAllRates(fromCurrency);
        BigDecimal rate = rates.get(toCurrency);

        if (rate == null) {
            throw new RuntimeException("Exchange rate not available for " + fromCurrency + " -> " + toCurrency);
        }

        return rate;
    }

    @Override
    public Map<String, BigDecimal> getAllRates(String baseCurrency) {
        // Return cached rates if still valid
        if (isCacheValid(baseCurrency)) {
            return rateCache.get(baseCurrency);
        }

        try {
            return fetchLiveRates(baseCurrency);
        } catch (Exception e) {
            System.err.println("Failed to fetch live rates: " + e.getMessage());
            // If cache exists but expired, still use it
            if (rateCache.containsKey(baseCurrency)) {
                return rateCache.get(baseCurrency);
            }
            return getFallbackRates(baseCurrency);
        }
    }

    private Map<String, BigDecimal> fetchLiveRates(String baseCurrency) {
        String url = API_URL + "?from=" + baseCurrency;
        String response = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode ratesNode = root.get("rates");

            Map<String, BigDecimal> rates = new HashMap<>();
            ratesNode.fields().forEachRemaining(entry ->
                    rates.put(entry.getKey(), new BigDecimal(entry.getValue().asText()).setScale(6, RoundingMode.HALF_UP))
            );

            // Update cache
            rateCache.put(baseCurrency, rates);
            cacheTimestamps.put(baseCurrency, System.currentTimeMillis());

            return rates;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse exchange rate response", e);
        }
    }

    private boolean isCacheValid(String baseCurrency) {
        Long timestamp = cacheTimestamps.get(baseCurrency);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS;
    }

    // Hardcoded fallback rates (approximate, used only when API is down)
    private Map<String, BigDecimal> getFallbackRates(String baseCurrency) {
        Map<String, BigDecimal> rates = new HashMap<>();

        if ("TRY".equals(baseCurrency)) {
            rates.put("USD", new BigDecimal("0.028000"));
            rates.put("EUR", new BigDecimal("0.025000"));
            rates.put("GBP", new BigDecimal("0.021000"));
        } else if ("USD".equals(baseCurrency)) {
            rates.put("TRY", new BigDecimal("35.700000"));
            rates.put("EUR", new BigDecimal("0.920000"));
            rates.put("GBP", new BigDecimal("0.790000"));
        } else if ("EUR".equals(baseCurrency)) {
            rates.put("TRY", new BigDecimal("38.800000"));
            rates.put("USD", new BigDecimal("1.087000"));
            rates.put("GBP", new BigDecimal("0.860000"));
        } else if ("GBP".equals(baseCurrency)) {
            rates.put("TRY", new BigDecimal("45.100000"));
            rates.put("USD", new BigDecimal("1.265000"));
            rates.put("EUR", new BigDecimal("1.163000"));
        }

        rateCache.put(baseCurrency, rates);
        cacheTimestamps.put(baseCurrency, System.currentTimeMillis());

        return rates;
    }
}
