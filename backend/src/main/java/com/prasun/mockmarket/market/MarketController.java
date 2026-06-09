package com.prasun.mockmarket.market;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {
    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/quote/{symbol}")
    public StockQuote quote(@PathVariable String symbol) {
        return marketService.quote(symbol);
    }

    @GetMapping("/search")
    public List<StockSearchResult> search(@RequestParam String query) {
        return marketService.search(query);
    }
}
