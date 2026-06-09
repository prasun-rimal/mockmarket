package com.prasun.mockmarket.trade;

import java.math.BigDecimal;

public record TradeResponse(
        String type,
        String symbol,
        BigDecimal quantity,
        BigDecimal executionPrice,
        BigDecimal totalAmount,
        BigDecimal cashBalance,
        String message
) {
}
