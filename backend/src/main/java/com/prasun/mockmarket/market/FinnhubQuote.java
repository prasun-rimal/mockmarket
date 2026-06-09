package com.prasun.mockmarket.market;

import java.math.BigDecimal;

public record FinnhubQuote(BigDecimal c, BigDecimal d, BigDecimal dp, BigDecimal h, BigDecimal l, BigDecimal o, BigDecimal pc) {
    StockQuote toStockQuote(String symbol) {
        return new StockQuote(symbol.toUpperCase(), c, d, dp, h, l, o, pc);
    }
}
