package com.hstbank_api;

import com.hstbank_api.model.AccountType;
import com.hstbank_api.model.User;
import com.hstbank_api.service.AccountService;
import com.hstbank_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AddTestValues implements CommandLineRunner {
    // CommandLineRunner = Spring calls run() on startup

    private final UserService userService;
    private final AccountService accountService;

    /*
     * @Override is a safety check
     * - Tells compiler: "I'm implementing run() from CommandLineRunner interface"
     * - If I typo the method name or change parameters, compiler throws error
     * - Without it typos create silent bugs (method never gets called, no warning)
    */
    @Override
    public void run(String... args) { // String... = accepts any number of arguments
        if (userService.findByEmail("admin@hstbank.com").isEmpty()) {
            User admin = userService.createUser(
                    "admin@hstbank.com",
                    "admin123",
                    "Admin",
                    "User"
            );
            accountService.createAccount(
                    admin,
                    AccountType.CHECKING,
                    new BigDecimal("10000.00"),
                    "TRY"
            );
            System.out.println("Test user created: admin@hstbank.com / admin123");
        }
    }
}