package com.prasun.mockmarket.market;

import java.util.List;

public interface MarketDataClient {
    StockQuote quote(String symbol);
    List<StockSearchResult> search(String query);
}
