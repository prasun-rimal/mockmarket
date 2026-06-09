package com.prasun.mockmarket.trade;

import com.prasun.mockmarket.auth.CurrentUser;
import com.prasun.mockmarket.user.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    public TradeResponse buy(@CurrentUser User user, @Valid @RequestBody TradeRequest request) {
        return tradeService.buy(user, request);
    }

    @PostMapping("/sell")
    public TradeResponse sell(@CurrentUser User user, @Valid @RequestBody TradeRequest request) {
        return tradeService.sell(user, request);
    }

    @GetMapping("/history")
    public List<TransactionResponse> history(@CurrentUser User user) {
        return tradeService.history(user);
    }
}
