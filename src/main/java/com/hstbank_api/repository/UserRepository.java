package com.hstbank_api.repository;

import com.hstbank_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user using the email
    // Spring reads the method names and generates SQL automatically
    Optional<User> findByEmail(String email);
}