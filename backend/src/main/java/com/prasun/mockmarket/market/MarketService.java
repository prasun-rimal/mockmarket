package com.prasun.mockmarket.market;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketService {
    private final MarketDataClient marketDataClient;

    public MarketService(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    public StockQuote quote(String symbol) {
        return marketDataClient.quote(symbol);
    }

    public List<StockSearchResult> search(String query) {
        return marketDataClient.search(query);
    }
}
