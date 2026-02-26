package com.hstbank_api.controller;

import com.hstbank_api.dto.ExternalTransferRequest;
import com.hstbank_api.dto.TransferRequest;
import com.hstbank_api.dto.TransactionResponse;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Transaction;
import com.hstbank_api.model.TransactionType;
import com.hstbank_api.service.AccountService;
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
    private final AccountService accountService;

    // Internal transfer
    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            Transaction transaction = transactionService.transfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    request.getDescription()
            );

            return ResponseEntity.ok(mapToResponse(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // External transfer by IBAN
    @PostMapping("/external")
    public ResponseEntity<?> externalTransfer(@RequestBody ExternalTransferRequest request) {
        try {
            // Resolve IBAN to account
            Account toAccount = accountService.getAccountByAccountNumber(request.getToAccountNumber())
                    .orElseThrow(() -> new RuntimeException("Recipient account not found: " + request.getToAccountNumber()));

            Transaction transaction = transactionService.transfer(
                    request.getFromAccountId(),
                    toAccount.getId(),
                    request.getAmount(),
                    request.getDescription()
            );

            return ResponseEntity.ok(mapToResponse(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable Long accountId) {
        try {
            List<Transaction> transactions = transactionService.getTransactionHistory(accountId);
            List<TransactionResponse> responses = transactions.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(@PathVariable Long userId) {
        try {
            List<Transaction> transactions = transactionService.getAllUserTransactions(userId);
            List<TransactionResponse> responses = transactions.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/history/{accountId}/type/{type}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByType(
            @PathVariable Long accountId,
            @PathVariable TransactionType type) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByType(accountId, type);
            List<TransactionResponse> responses = transactions.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt(),
                tx.getFromAccount().getId(),
                tx.getToAccount().getId(),
                tx.getCurrency(),
                tx.getTransactionType(),
                tx.getExchangeRate(),
                tx.getTargetCurrency(),
                tx.getTargetAmount()
        );
    }
}
