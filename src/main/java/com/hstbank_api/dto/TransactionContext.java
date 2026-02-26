package com.hstbank_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionContext {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String description;
    private String targetCurrency;
}
