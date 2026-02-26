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
public class TransferProcessor extends AbstractTransactionProcessor {

    // Constructor calls super()
    public TransferProcessor(AccountService accountService, TransactionRepository transactionRepository) {
        super(accountService, transactionRepository);
    }

    @Override
    protected void validate(TransactionContext context) {
        super.validate(context); // parent validation first

        if (context.getToAccountId() == null) {
            throw new RuntimeException("Destination account is required");
        }

        if (context.getFromAccountId().equals(context.getToAccountId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        Account fromAccount = accountService.getAccountById(context.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        Account toAccount = accountService.getAccountById(context.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (fromAccount.getBalance().compareTo(context.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new RuntimeException("Currency mismatch - use exchange for different currencies");
        }
    }

    @Override
    protected Transaction execute(TransactionContext context) {
        Account fromAccount = accountService.getAccountById(context.getFromAccountId()).get();
        Account toAccount = accountService.getAccountById(context.getToAccountId()).get();

        accountService.withdraw(fromAccount, context.getAmount());
        accountService.deposit(toAccount, context.getAmount());

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(context.getAmount());
        transaction.setCurrency(fromAccount.getCurrency());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription(context.getDescription() != null ? context.getDescription() : "Transfer");
        transaction.setCreatedAt(LocalDateTime.now());

        return transaction; // parent's process() calls save()
    }

    @Override
    public TransactionType getSupportedType() {
        return TransactionType.TRANSFER;
    }
}