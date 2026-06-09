package com.prasun.mockmarket.portfolio;

import com.prasun.mockmarket.account.AccountService;
import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.market.MarketService;
import com.prasun.mockmarket.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {
    private final HoldingRepository holdings;
    private final MarketService marketService;
    private final AccountService accountService;

    public PortfolioService(HoldingRepository holdings, MarketService marketService, AccountService accountService) {
        this.holdings = holdings;
        this.marketService = marketService;
        this.accountService = accountService;
    }

    public List<HoldingResponse> holdingsFor(User user) {
        return holdings.findByUserOrderBySymbolAsc(user).stream().map(this::toResponse).toList();
    }

    public PortfolioSummary summaryFor(User user) {
        var account = accountService.getFor(user);
        var holdingRows = holdingsFor(user);
        var holdingsValue = holdingRows.stream().map(HoldingResponse::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        var total = account.getCashBalance().add(holdingsValue);
        var gainLoss = total.subtract(account.getStartingBalance());
        var gainLossPercent = account.getStartingBalance().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gainLoss.divide(account.getStartingBalance(), 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        var top = holdingRows.stream()
                .sorted(Comparator.comparing(HoldingResponse::marketValue).reversed())
                .limit(5)
                .toList();
        return new PortfolioSummary(
                MoneyUtil.money(account.getCashBalance()),
                MoneyUtil.money(holdingsValue),
                MoneyUtil.money(total),
                MoneyUtil.money(gainLoss),
                MoneyUtil.percent(gainLossPercent),
                top
        );
    }

    private HoldingResponse toResponse(Holding holding) {
        var quote = marketService.quote(holding.getSymbol());
        var current = MoneyUtil.money(quote.price());
        var marketValue = MoneyUtil.money(holding.getQuantity().multiply(current));
        var costBasis = holding.getQuantity().multiply(holding.getAveragePrice());
        var gainLoss = MoneyUtil.money(marketValue.subtract(costBasis));
        var gainPercent = costBasis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gainLoss.divide(costBasis, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return new HoldingResponse(
                holding.getSymbol(),
                MoneyUtil.quantity(holding.getQuantity()),
                MoneyUtil.money(holding.getAveragePrice()),
                current,
                marketValue,
                gainLoss,
                MoneyUtil.percent(gainPercent)
        );
    }
}
