package com.hstbank_api.repository;

import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find all transactions belonging to an account
    // Spring reads the method names and generates SQL automatically
    List<Transaction> findByFromAccount(Account account);
    List<Transaction> findByToAccount(Account account);
}
