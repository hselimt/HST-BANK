package com.hstbank_api.service;

import com.hstbank_api.dto.CardRequest;
import com.hstbank_api.model.*;
import com.hstbank_api.repository.CardRepository;
import com.hstbank_api.repository.UserRepository;
import com.hstbank_api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public Card createCard(Long userId, CardRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (cardRepository.existsByCardNumber(request.getCardNumber())) {
            throw new RuntimeException("Card number already exists");
        }

        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setCardHolderName(request.getCardHolderName());
        card.setCardType(request.getCardType());
        card.setCardBrand(request.getCardBrand());
        card.setUser(user);

        // Credit cards have their own limit, debit cards link to account balance
        if (request.getCardType() == CardType.CREDIT) {
            card.setCreditLimit(request.getCreditLimit());
            card.setCardBalance(request.getCreditLimit());
        } else {
            if (request.getAccountId() == null) {
                throw new RuntimeException("Debit cards must be linked to an account");
            }
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            card.setAccount(account);
            card.setCardBalance(account.getBalance()); // Debit card mirrors account balance
        }

        return cardRepository.save(card);
    }

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }
}