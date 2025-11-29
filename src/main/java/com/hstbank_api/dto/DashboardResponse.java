package com.hstbank_api.dto;

import com.hstbank_api.model.Account;
import com.hstbank_api.model.Card;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResponse {
    private String userName;
    private int totalAccounts;
    private int totalCards;
    private BigDecimal totalBalance;
    private BigDecimal totalCreditAvailable;
    private List<Account> accounts;
    private List<Card> cards;
}