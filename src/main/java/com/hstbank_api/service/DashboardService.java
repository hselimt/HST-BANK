package com.hstbank_api.service;

import com.hstbank_api.dto.DashboardResponse;
import com.hstbank_api.model.Account;
import com.hstbank_api.model.Card;
import com.hstbank_api.model.User;
import com.hstbank_api.repository.AccountRepository;
import com.hstbank_api.repository.CardRepository;
import com.hstbank_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    public DashboardResponse getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Account> accounts = accountRepository.findByUserId(userId);
        List<Card> cards = cardRepository.findByUserId(userId);

        // .stream() = iterate list, .map() extracts balance from each account, .reduce() sums them all
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance) // Account::getBalance = (a) -> a.getBalance()
                .reduce(BigDecimal.ZERO, BigDecimal::add); // start at 0, add each

        // .stream() = iterate list, .filter() keeps only credit cards, then sum their available balance
        BigDecimal totalCreditAvailable = cards.stream()
                .filter(card -> card.getCreditLimit() != null) // only credit cards
                .map(Card::getCardBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardResponse response = new DashboardResponse();
        response.setUserName(user.getFirstName() + " " + user.getLastName());
        response.setTotalAccounts(accounts.size());
        response.setTotalCards(cards.size());
        response.setTotalBalance(totalBalance);
        response.setTotalCreditAvailable(totalCreditAvailable);
        response.setAccounts(accounts);
        response.setCards(cards);

        return response;
    }
}