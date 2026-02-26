package com.hstbank_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    private String userName;
    private int totalAccounts;
    private int totalCards;
    private Map<String, BigDecimal> balanceByCurrency; // {"TRY": 10000, "USD": 500}
    private BigDecimal totalCreditAvailable;
    private List<AccountResponse> accounts;
    private List<CardResponse> cards;
    private List<TransactionResponse> recentTransactions;
    private Map<String, BigDecimal> exchangeRates;
}