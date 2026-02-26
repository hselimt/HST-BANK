package com.hstbank_api.controller;

import com.hstbank_api.dto.CardRequest;
import com.hstbank_api.dto.CardResponse;
import com.hstbank_api.model.Card;
import com.hstbank_api.model.User;
import com.hstbank_api.service.CardService;
import com.hstbank_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardRequest request) {
        try {
            User user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Card card = cardService.createCard(request.getUserId(), request);

            CardResponse response = new CardResponse(
                    card.getId(),
                    card.getUser().getId(),
                    card.getCardNumber(),
                    card.getCardHolderName(),
                    card.getCardType(),
                    card.getCardBrand(),
                    card.getCardBalance(),
                    card.getCreditLimit(),
                    card.getAccount() != null ? card.getAccount().getId() : null
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{cardId}/payment")
    public ResponseEntity<?> makePayment(@PathVariable Long cardId, @RequestBody BigDecimal amount) {
        try {
            cardService.makeCardPayment(cardId, amount, "Card Payment");
            return ResponseEntity.ok("Payment processed");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}