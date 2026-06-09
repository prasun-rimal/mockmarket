package com.prasun.mockmarket.market;

import java.math.BigDecimal;

public record StockQuote(
        String symbol,
        BigDecimal price,
        BigDecimal change,
        BigDecimal percentChange,
        BigDecimal high,
        BigDecimal low,
        BigDecimal open,
        BigDecimal previousClose
) {
}
