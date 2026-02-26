package com.hstbank_api.service;

import com.hstbank_api.dto.TransactionContext;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionType;
import com.hstbank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final Map<TransactionType, AbstractTransactionProcessor> processors;

    // Spring injects List of all AbstractTransactionProcessor implementations automatically
    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService,
                              List<AbstractTransactionProcessor> processorList) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        // Build registry: TransactionType -> Processor
        this.processors = processorList.stream()
                .collect(Collectors.toMap(
                        AbstractTransactionProcessor::getSupportedType, // key = type
                        processor -> processor // value = processor instance
                ));
    }

    public Transaction processTransaction(TransactionType type, TransactionContext context) {
        AbstractTransactionProcessor processor = processors.get(type);
        if (processor == null) {
            throw new RuntimeException("Unsupported transaction type: " + type);
        }
        return processor.process(context); // dynamic binding - calls correct subclass
    }

    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        TransactionContext context = TransactionContext.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .description(description)
                .build();

        return processTransaction(TransactionType.TRANSFER, context);
    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Get transactions where user sent or received money
        List<Transaction> sentTransactions = transactionRepository.findByFromAccount(account);
        List<Transaction> receivedTransactions = transactionRepository.findByToAccount(account);
        sentTransactions.addAll(receivedTransactions);

        // Remove duplicates (card payments have same from/to)
        return sentTransactions.stream().distinct().collect(Collectors.toList());
    }

    // Get transactions filtered by type
    public List<Transaction> getTransactionsByType(Long accountId, TransactionType type) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Transaction> all = getTransactionHistory(accountId);
        return all.stream()
                .filter(tx -> tx.getTransactionType() == type)
                .collect(Collectors.toList());
    }

    // Get all transactions for a user across all accounts
    public List<Transaction> getAllUserTransactions(Long userId) {
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return accounts.stream()
                .flatMap(account -> getTransactionHistory(account.getId()).stream())
                .distinct()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // newest first
                .collect(Collectors.toList());
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
}
