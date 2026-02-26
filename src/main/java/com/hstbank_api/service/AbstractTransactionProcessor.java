package com.hstbank_api.service;

import com.hstbank_api.dto.TransactionContext;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionType;
import com.hstbank_api.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public abstract class AbstractTransactionProcessor {

    protected final AccountService accountService;
    protected final TransactionRepository transactionRepository;

    // Constructor injection - subclasses must call super()
    public AbstractTransactionProcessor(AccountService accountService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction process(TransactionContext context) {
        validate(context);
        Transaction transaction = execute(context);
        return transactionRepository.save(transaction);
    }

    // Common validation - subclasses can add more via override + super call
    protected void validate(TransactionContext context) {
        if (context.getAmount() == null || context.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transaction amount must be positive");
        }

        if (context.getFromAccountId() == null) {
            throw new RuntimeException("Source account is required");
        }
    }

    // Each subclass implements its own transaction logic
    protected abstract Transaction execute(TransactionContext context);

    // Used for processor registry (Map<TransactionType, Processor>)
    public abstract TransactionType getSupportedType();
}