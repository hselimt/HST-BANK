package com.hstbank_api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExchangeRequest {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
}
