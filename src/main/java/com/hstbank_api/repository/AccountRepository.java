package com.hstbank_api.repository;

import com.hstbank_api.model.Account;
import com.hstbank_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Find all accounts belonging to a user
    // Spring reads the method names and generates SQL automatically
    List<Account> findByUser(User user);
    List<Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
}