package com.prasun.mockmarket.watchlist;

import java.math.BigDecimal;

public record WatchlistResponse(String symbol, BigDecimal price, BigDecimal change, BigDecimal percentChange) {
}
