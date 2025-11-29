package com.hstbank_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.hstbank_api.model.AccountType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private Long userId;
    private String accountNumber;
    private String currency;
    private AccountType accountType;
    private BigDecimal balance;
}