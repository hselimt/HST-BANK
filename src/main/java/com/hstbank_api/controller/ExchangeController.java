package com.hstbank_api.controller;

import com.hstbank_api.dto.ExchangeRateResponse;
import com.hstbank_api.dto.ExchangeRequest;
import com.hstbank_api.dto.TransactionResponse;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/rates")
    public ResponseEntity<ExchangeRateResponse> getRates(@RequestParam(defaultValue = "TRY") String base) {
        try {
            Map<String, BigDecimal> rates = exchangeService.getRates(base.toUpperCase());
            return ResponseEntity.ok(new ExchangeRateResponse(base.toUpperCase(), rates));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rate")
    public ResponseEntity<?> getRate(@RequestParam String from, @RequestParam String to) {
        try {
            BigDecimal rate = exchangeService.getRate(from.toUpperCase(), to.toUpperCase());
            return ResponseEntity.ok(Map.of(
                    "from", from.toUpperCase(),
                    "to", to.toUpperCase(),
                    "rate", rate
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> exchange(@RequestBody ExchangeRequest request) {
        try {
            Transaction transaction = exchangeService.exchange(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount()
            );

            TransactionResponse response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getFromAccount().getId(),
                    transaction.getToAccount().getId(),
                    transaction.getCurrency(),
                    transaction.getTransactionType(),
                    transaction.getExchangeRate(),
                    transaction.getTargetCurrency(),
                    transaction.getTargetAmount()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
