package com.hstbank_api.service;

import com.hstbank_api.model.Account;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionStatus;
import com.hstbank_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Transaction amount must be positive");
        }

        Account fromAccount = accountService.getAccountById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        Account toAccount = accountService.getAccountById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if(fromAccount.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient balance");
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())){
            throw new RuntimeException("Currency mismatch");
        }

        // These two must both succeed or both fail (@Transactional)
        accountService.withdraw(fromAccount, amount);
        accountService.deposit(toAccount, amount);

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setCurrency(fromAccount.getCurrency());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Get transactions where user sent or received money
        List<Transaction> sentTransactions = transactionRepository.findByFromAccount(account);
        List<Transaction> receivedTransactions = transactionRepository.findByToAccount(account);
        sentTransactions.addAll(receivedTransactions); // Merge both lists

        return sentTransactions;
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
}