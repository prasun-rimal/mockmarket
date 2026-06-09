package com.prasun.mockmarket.portfolio;

import java.math.BigDecimal;

public record HoldingResponse(
        String symbol,
        BigDecimal quantity,
        BigDecimal averagePrice,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal gainLoss,
        BigDecimal gainLossPercent
) {
}
