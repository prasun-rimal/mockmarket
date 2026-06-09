package com.prasun.mockmarket.trade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prasun.mockmarket.auth.AuthService;
import com.prasun.mockmarket.auth.RegisterRequest;
import com.prasun.mockmarket.portfolio.HoldingRepository;
import com.prasun.mockmarket.user.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class TradeServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserRepository users;

    @Autowired
    private HoldingRepository holdings;

    @Test
    void buyAndSellUpdatesHoldingsAndHistory() {
        authService.register(new RegisterRequest("Trader", "trade@example.com", "Market!123"));
        var user = users.findByEmail("trade@example.com").orElseThrow();

        var buy = tradeService.buy(user, new TradeRequest("AAPL", new BigDecimal("2")));
        assertThat(buy.cashBalance()).isLessThan(new BigDecimal("100000.00"));
        assertThat(holdings.findByUserAndSymbol(user, "AAPL")).isPresent();

        tradeService.sell(user, new TradeRequest("AAPL", new BigDecimal("1")));
        assertThat(tradeService.history(user)).hasSize(2);
        assertThat(holdings.findByUserAndSymbol(user, "AAPL").orElseThrow().getQuantity()).isEqualByComparingTo(new BigDecimal("1.0000"));
    }

    @Test
    void cannotSellMoreThanOwned() {
        authService.register(new RegisterRequest("Risk", "risk@example.com", "Market!123"));
        var user = users.findByEmail("risk@example.com").orElseThrow();

        assertThatThrownBy(() -> tradeService.sell(user, new TradeRequest("AAPL", new BigDecimal("1"))))
                .hasMessageContaining("do not own");
    }
}
