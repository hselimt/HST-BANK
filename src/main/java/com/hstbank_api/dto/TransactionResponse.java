package com.hstbank_api.dto;

import com.hstbank_api.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private Long fromAccountId;
    private Long toAccountId;
    private String currency;
    private TransactionType transactionType;
    private BigDecimal exchangeRate;
    private String targetCurrency;
    private BigDecimal targetAmount;
}
