package com.hstbank_api.service;

import com.hstbank_api.dto.AccountResponse;
import com.hstbank_api.dto.CardResponse;
import com.hstbank_api.dto.DashboardResponse;
import com.hstbank_api.dto.TransactionResponse;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Card;
import com.hstbank_api.model.User;
import com.hstbank_api.repository.AccountRepository;
import com.hstbank_api.repository.CardRepository;
import com.hstbank_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final TransactionService transactionService;
    private final ExchangeRateProvider exchangeRateProvider;

    public DashboardResponse getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountRepository.findByUserId(userId);
        List<Card> cards = cardRepository.findByUserId(userId);

        // Balance grouped by currency: {"TRY": 10000, "USD": 500}
        Map<String, BigDecimal> balanceByCurrency = accounts.stream()
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, Account::getBalance, BigDecimal::add)
                ));

        BigDecimal totalCreditAvailable = cards.stream()
                .filter(card -> card.getCreditLimit() != null)
                .map(Card::getCardBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert entities to DTOs (no lazy loading issues)
        List<AccountResponse> accountDtos = accounts.stream()
                .map(acc -> new AccountResponse(
                        acc.getId(),
                        acc.getUser().getId(),
                        acc.getAccountNumber(),
                        acc.getCurrency(),
                        acc.getAccountType(),
                        acc.getBalance()
                ))
                .collect(Collectors.toList());

        List<CardResponse> cardDtos = cards.stream()
                .map(card -> new CardResponse(
                        card.getId(),
                        card.getUser().getId(),
                        card.getCardNumber(),
                        card.getCardHolderName(),
                        card.getCardType(),
                        card.getCardBrand(),
                        card.getCardBalance(),
                        card.getCreditLimit(),
                        card.getAccount() != null ? card.getAccount().getId() : null
                ))
                .collect(Collectors.toList());

        // Recent transactions (last 10 across all accounts)
        List<TransactionResponse> recentTransactions;
        try {
            recentTransactions = transactionService.getAllUserTransactions(userId).stream()
                    .limit(10)
                    .map(tx -> new TransactionResponse(
                            tx.getId(),
                            tx.getAmount(),
                            tx.getDescription(),
                            tx.getCreatedAt(),
                            tx.getFromAccount().getId(),
                            tx.getToAccount().getId(),
                            tx.getCurrency(),
                            tx.getTransactionType(),
                            tx.getExchangeRate(),
                            tx.getTargetCurrency(),
                            tx.getTargetAmount()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            recentTransactions = Collections.emptyList();
        }

        // Exchange rates
        Map<String, BigDecimal> exchangeRates;
        try {
            exchangeRates = exchangeRateProvider.getAllRates("TRY");
        } catch (Exception e) {
            exchangeRates = Collections.emptyMap();
        }

        DashboardResponse response = new DashboardResponse();
        response.setUserName(user.getFirstName() + " " + user.getLastName());
        response.setTotalAccounts(accounts.size());
        response.setTotalCards(cards.size());
        response.setBalanceByCurrency(balanceByCurrency);
        response.setTotalCreditAvailable(totalCreditAvailable);
        response.setAccounts(accountDtos);
        response.setCards(cardDtos);
        response.setRecentTransactions(recentTransactions);
        response.setExchangeRates(exchangeRates);

        return response;
    }
}