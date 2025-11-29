package com.hstbank_api.dto;

import lombok.Data;
import java.math.BigDecimal;
import com.hstbank_api.model.AccountType;

@Data
public class CreateAccountRequest {
    private Long userId;
    private AccountType accountType;
    private BigDecimal initialBalance;
    private String currency;
}