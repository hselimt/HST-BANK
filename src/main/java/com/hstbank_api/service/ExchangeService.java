package com.hstbank_api.service;

import com.hstbank_api.dto.TransactionContext;
import com.hstbank_api.model.*;
import com.hstbank_api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    // Interface reference
    private final ExchangeRateProvider exchangeRateProvider;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    // Get all exchange rates for a base currency
    public Map<String, BigDecimal> getRates(String baseCurrency) {
        return exchangeRateProvider.getAllRates(baseCurrency);
    }

    // Get specific rate
    public BigDecimal getRate(String from, String to) {
        return exchangeRateProvider.getRate(from, to);
    }

    // Execute exchange
    public Transaction exchange(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        TransactionContext context = TransactionContext.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        return transactionService.processTransaction(TransactionType.EXCHANGE, context);
    }

    // Get user's accounts grouped by currency
    public List<Account> getUserAccountsByCurrency(Long userId, String currency) {
        return accountRepository.findByUserIdAndCurrency(userId, currency);
    }
}
