package com.hstbank_api.repository;

import com.hstbank_api.model.Card;
import com.hstbank_api.model.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    // Find all cards belonging to a user
    // Spring reads the method names and generates SQL automatically
    List<Card> findByUserId(Long userId);
    boolean existsByCardNumber(String cardNumber);
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByUserIdAndCardType(Long userId, CardType cardType);
    List<Card> findByUserIdAndIsActiveTrue(Long userId);
}