package com.prasun.mockmarket.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prasun.mockmarket.account.AccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accounts;

    @Test
    void registerCreatesUserTokenAndStartingCash() {
        var response = authService.register(new RegisterRequest("Prasun", "prasun@example.com", "Market!123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("prasun@example.com");

        var account = accounts.findAll().getFirst();
        assertThat(account.getCashBalance()).isEqualByComparingTo(new BigDecimal("100000.00"));
    }

    @Test
    void duplicateEmailIsRejected() {
        authService.register(new RegisterRequest("One", "dupe@example.com", "Market!123"));

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Two", "dupe@example.com", "Market!123")))
                .hasMessageContaining("already exists");
    }
}
