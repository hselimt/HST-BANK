package com.hstbank_api.controller;

import com.hstbank_api.dto.TransferRequest;
import com.hstbank_api.dto.TransactionResponse;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransferController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            Transaction transaction = transactionService.transfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    request.getDescription()
            );

            // Entity to DTO conversion
            TransactionResponse response = new TransactionResponse(
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getDescription(),
                    transaction.getCreatedAt(),
                    transaction.getFromAccount().getId(),
                    transaction.getToAccount().getId()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable Long accountId) {
        try {
            List<Transaction> transactions = transactionService.getTransactionHistory(accountId);

            // .stream() = iterate list, .map() = transform each item to DTO format, .collect() = back to list
            List<TransactionResponse> responses = transactions.stream()
                    .map(tx -> new TransactionResponse(
                            tx.getId(),
                            tx.getAmount(),
                            tx.getDescription(),
                            tx.getCreatedAt(),
                            tx.getFromAccount().getId(),
                            tx.getToAccount().getId()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        // Chained Optional: find -> transform to DTO format -> wrap in ResponseEntity -> or return 404
        return transactionService.getTransactionById(transactionId)
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getDescription(),
                        tx.getCreatedAt(),
                        tx.getFromAccount().getId(),
                        tx.getToAccount().getId()
                ))
                .map(ResponseEntity::ok)  // ::ok is method reference, same as (r) -> ResponseEntity.ok(r)
                .orElse(ResponseEntity.notFound().build());
    }
}