package com.hstbank_api.dto;

import com.hstbank_api.model.CardBrand;
import com.hstbank_api.model.CardType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardRequest {
    private Long userId;
    private String cardNumber;
    private String cardHolderName;
    private CardType cardType;
    private CardBrand cardBrand;
    private BigDecimal creditLimit;
    private Long accountId;
}