package com.hstbank_api.service;

import com.hstbank_api.dto.TransactionContext;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionStatus;
import com.hstbank_api.model.TransactionType;
import com.hstbank_api.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CardTransactionProcessor extends AbstractTransactionProcessor {

    public CardTransactionProcessor(AccountService accountService, TransactionRepository transactionRepository) {
        super(accountService, transactionRepository);
    }

    @Override
    protected void validate(TransactionContext context) {
        super.validate(context);

        Account fromAccount = accountService.getAccountById(context.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (fromAccount.getBalance().compareTo(context.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance for card payment");
        }
    }

    @Override
    protected Transaction execute(TransactionContext context) {
        Account fromAccount = accountService.getAccountById(context.getFromAccountId()).get();

        accountService.withdraw(fromAccount, context.getAmount());

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(fromAccount); // Card payments: same account for from/to
        transaction.setAmount(context.getAmount());
        transaction.setCurrency(fromAccount.getCurrency());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionType(TransactionType.CARD_PAYMENT);
        transaction.setDescription(context.getDescription() != null ? context.getDescription() : "Card Payment");
        transaction.setCreatedAt(LocalDateTime.now());

        return transaction;
    }

    @Override
    public TransactionType getSupportedType() {
        return TransactionType.CARD_PAYMENT;
    }
}