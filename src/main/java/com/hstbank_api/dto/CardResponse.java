package com.hstbank_api.dto;

import com.hstbank_api.model.CardBrand;
import com.hstbank_api.model.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private Long userId;
    private String cardNumber;
    private String cardHolderName;
    private CardType cardType;
    private CardBrand cardBrand;
    private BigDecimal cardBalance;
    private BigDecimal creditLimit;
    private Long linkedAccountId;

    public CardResponse(Long id, Long userId, CardType cardType, BigDecimal cardBalance) {
        this.id = id;
        this.userId = userId;
        this.cardType = cardType;
        this.cardBalance = cardBalance;
    }
}