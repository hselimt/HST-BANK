package com.hstbank_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExternalTransferRequest {
    private Long fromAccountId;
    private String toAccountNumber; // IBAN - for cross-user transfers
    private BigDecimal amount;
    private String description;
}
