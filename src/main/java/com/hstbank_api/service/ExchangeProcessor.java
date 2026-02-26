package com.hstbank_api.service;

import com.hstbank_api.dto.TransactionContext;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionStatus;
import com.hstbank_api.model.TransactionType;
import com.hstbank_api.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class ExchangeProcessor extends AbstractTransactionProcessor {

    // Interface reference - could be LiveExchangeRateProvider or any other implementation
    private final ExchangeRateProvider exchangeRateProvider;

    public ExchangeProcessor(AccountService accountService,
                             TransactionRepository transactionRepository,
                             ExchangeRateProvider exchangeRateProvider) {
        super(accountService, transactionRepository);
        this.exchangeRateProvider = exchangeRateProvider;
    }

    @Override
    protected void validate(TransactionContext context) {
        super.validate(context);

        if (context.getToAccountId() == null) {
            throw new RuntimeException("Target account is required for exchange");
        }

        Account fromAccount = accountService.getAccountById(context.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountService.getAccountById(context.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Exchange requires accounts with different currencies");
        }

        if (fromAccount.getBalance().compareTo(context.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance for exchange");
        }
    }

    @Override
    protected Transaction execute(TransactionContext context) {
        Account fromAccount = accountService.getAccountById(context.getFromAccountId()).get();
        Account toAccount = accountService.getAccountById(context.getToAccountId()).get();

        // Fetch live rate via interface
        BigDecimal rate = exchangeRateProvider.getRate(fromAccount.getCurrency(), toAccount.getCurrency());
        BigDecimal targetAmount = context.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);

        // Withdraw source currency, deposit target currency
        accountService.withdraw(fromAccount, context.getAmount());
        accountService.deposit(toAccount, targetAmount);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(context.getAmount());
        transaction.setCurrency(fromAccount.getCurrency());
        transaction.setTargetCurrency(toAccount.getCurrency());
        transaction.setTargetAmount(targetAmount);
        transaction.setExchangeRate(rate);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionType(TransactionType.EXCHANGE);
        transaction.setDescription(context.getDescription() != null ? context.getDescription() :
                fromAccount.getCurrency() + " → " + toAccount.getCurrency());
        transaction.setCreatedAt(LocalDateTime.now());

        return transaction;
    }

    @Override
    public TransactionType getSupportedType() {
        return TransactionType.EXCHANGE;
    }
}