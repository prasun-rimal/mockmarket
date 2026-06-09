package com.prasun.mockmarket.market;

import com.prasun.mockmarket.common.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FinnhubClient implements MarketDataClient {
    private final RestClient restClient = RestClient.create("https://finnhub.io/api/v1");
    private final String apiKey;
    private final boolean demoMode;

    public FinnhubClient(
            @Value("${app.market.finnhub-api-key}") String apiKey,
            @Value("${app.market.demo-mode}") boolean demoMode
    ) {
        this.apiKey = apiKey;
        this.demoMode = demoMode;
    }

    @Override
    public StockQuote quote(String symbol) {
        var clean = cleanSymbol(symbol);
        if (demoMode || apiKey == null || apiKey.isBlank()) {
            return demoQuote(clean);
        }
        try {
            var response = restClient.get()
                    .uri(uri -> uri.path("/quote").queryParam("symbol", clean).queryParam("token", apiKey).build())
                    .retrieve()
                    .body(FinnhubQuote.class);
            if (response == null || response.c() == null || response.c().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Symbol not found.");
            }
            return response.toStockQuote(clean);
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Market data is temporarily unavailable.");
        }
    }

    @Override
    public List<StockSearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Search query is required.");
        }
        if (demoMode || apiKey == null || apiKey.isBlank()) {
            return demoSearch(query);
        }
        try {
            var response = restClient.get()
                    .uri(uri -> uri.path("/search").queryParam("q", query).queryParam("token", apiKey).build())
                    .retrieve()
                    .body(FinnhubSearchResponse.class);
            if (response == null || response.result() == null) {
                return List.of();
            }
            return response.result().stream()
                    .filter(item -> item.symbol() != null && !item.symbol().contains("."))
                    .limit(12)
                    .map(FinnhubSearchItem::toResult)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Market search is temporarily unavailable.");
        }
    }

    private String cleanSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Symbol is required.");
        }
        return symbol.trim().toUpperCase();
    }

    private StockQuote demoQuote(String symbol) {
        return switch (symbol) {
            case "MSFT" -> quoteOf("MSFT", "476.18", "4.91", "1.04", "478.62", "469.40", "471.11", "471.27");
            case "TSLA" -> quoteOf("TSLA", "184.25", "-2.10", "-1.13", "189.80", "181.22", "188.40", "186.35");
            case "NVDA" -> quoteOf("NVDA", "142.88", "3.44", "2.47", "144.10", "138.20", "139.22", "139.44");
            case "AMZN" -> quoteOf("AMZN", "214.37", "1.86", "0.88", "216.00", "211.72", "212.10", "212.51");
            default -> quoteOf(symbol, "291.13", "2.35", "0.81", "292.00", "287.50", "288.00", "288.78");
        };
    }

    private StockQuote quoteOf(String symbol, String price, String change, String percentChange, String high, String low, String open, String previousClose) {
        return new StockQuote(symbol, new BigDecimal(price), new BigDecimal(change), new BigDecimal(percentChange), new BigDecimal(high), new BigDecimal(low), new BigDecimal(open), new BigDecimal(previousClose));
    }

    private List<StockSearchResult> demoSearch(String query) {
        var symbols = List.of(
                new StockSearchResult("AAPL", "Apple Inc", "Common Stock"),
                new StockSearchResult("MSFT", "Microsoft Corp", "Common Stock"),
                new StockSearchResult("NVDA", "NVIDIA Corp", "Common Stock"),
                new StockSearchResult("TSLA", "Tesla Inc", "Common Stock"),
                new StockSearchResult("AMZN", "Amazon.com Inc", "Common Stock")
        );
        var lower = query.toLowerCase();
        return symbols.stream()
                .filter(item -> item.symbol().toLowerCase().contains(lower) || item.description().toLowerCase().contains(lower))
                .toList();
    }
}
