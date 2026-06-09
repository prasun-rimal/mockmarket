package com.prasun.mockmarket.portfolio;

import com.prasun.mockmarket.auth.CurrentUser;
import com.prasun.mockmarket.user.User;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/holdings")
    public List<HoldingResponse> holdings(@CurrentUser User user) {
        return portfolioService.holdingsFor(user);
    }

    @GetMapping("/summary")
    public PortfolioSummary summary(@CurrentUser User user) {
        return portfolioService.summaryFor(user);
    }
}
