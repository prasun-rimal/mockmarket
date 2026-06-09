package com.prasun.mockmarket.market;

public record FinnhubSearchItem(String symbol, String description, String type) {
    StockSearchResult toResult() {
        return new StockSearchResult(symbol, description, type);
    }
}
