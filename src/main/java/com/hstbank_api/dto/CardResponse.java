package com.hstbank_api.dto;

import com.hstbank_api.model.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private Long userId;
    private CardType cardType;
    private BigDecimal cardBalance;
}