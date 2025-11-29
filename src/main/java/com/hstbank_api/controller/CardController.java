package com.hstbank_api.controller;

import com.hstbank_api.dto.CardRequest;
import com.hstbank_api.dto.CardResponse;
import com.hstbank_api.model.Card;
import com.hstbank_api.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardRequest request) {
        try {
            Card card = cardService.createCard(request.getUserId(), request);

            CardResponse response = new CardResponse(
                    card.getId(),
                    card.getUser().getId(),
                    card.getCardType(),
                    card.getCardBalance()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}