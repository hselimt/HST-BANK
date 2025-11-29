package com.hstbank_api.service;

import com.hstbank_api.model.Account;
import com.hstbank_api.model.AccountType;
import com.hstbank_api.model.User;
import com.hstbank_api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(User user, AccountType accountType, BigDecimal initialBalance, String currency) {
        // .compareTo() for BigDecimal comparison
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial balance cannot be negative");
        }

        Account account = new Account();
        account.setUser(user);
        account.setAccountType(accountType);
        account.setBalance(initialBalance);
        account.setCurrency(currency);
        // UUID generates unique ID, format as Turkish IBAN
        account.setAccountNumber("TR" + UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        account.setCreatedAt(LocalDateTime.now());

        return accountRepository.save(account);
    }

    public Account deposit(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    public Account withdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdraw amount must be positive");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}