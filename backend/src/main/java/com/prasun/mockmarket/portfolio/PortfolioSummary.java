package com.prasun.mockmarket.portfolio;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummary(
        BigDecimal cashBalance,
        BigDecimal holdingsValue,
        BigDecimal totalPortfolioValue,
        BigDecimal totalGainLoss,
        BigDecimal totalGainLossPercent,
        List<HoldingResponse> topHoldings
) {
}
