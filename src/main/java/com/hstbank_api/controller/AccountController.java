package com.hstbank_api.controller;

import com.hstbank_api.dto.AccountResponse;
import com.hstbank_api.dto.CreateAccountRequest;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.User;
import com.hstbank_api.service.AccountService;
import com.hstbank_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreateAccountRequest request) {
        // <?> = can return any type
        try {
            User user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Account account = accountService.createAccount(
                    user,
                    request.getAccountType(),
                    request.getInitialBalance(),
                    request.getCurrency()
            );

            AccountResponse response = new AccountResponse(
                    account.getId(),
                    account.getUser().getId(),
                    account.getAccountNumber(),
                    account.getCurrency(),
                    account.getAccountType(),
                    account.getBalance()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}