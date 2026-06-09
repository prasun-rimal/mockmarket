package com.prasun.mockmarket.watchlist;

import com.prasun.mockmarket.common.ApiException;
import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.market.MarketService;
import com.prasun.mockmarket.user.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchlistService {
    private final WatchlistRepository watchlist;
    private final MarketService marketService;

    public WatchlistService(WatchlistRepository watchlist, MarketService marketService) {
        this.watchlist = watchlist;
        this.marketService = marketService;
    }

    public List<WatchlistResponse> get(User user) {
        return watchlist.findByUserOrderByCreatedAtDesc(user).stream()
                .map(item -> {
                    var quote = marketService.quote(item.getSymbol());
                    return new WatchlistResponse(item.getSymbol(), MoneyUtil.money(quote.price()), MoneyUtil.money(quote.change()), MoneyUtil.percent(quote.percentChange()));
                })
                .toList();
    }

    @Transactional
    public WatchlistResponse add(User user, String symbol) {
        var clean = clean(symbol);
        if (!watchlist.existsByUserAndSymbol(user, clean)) {
            marketService.quote(clean);
            watchlist.save(new WatchlistItem(user, clean));
        }
        var quote = marketService.quote(clean);
        return new WatchlistResponse(clean, MoneyUtil.money(quote.price()), MoneyUtil.money(quote.change()), MoneyUtil.percent(quote.percentChange()));
    }

    @Transactional
    public void delete(User user, String symbol) {
        var clean = clean(symbol);
        var item = watchlist.findByUserAndSymbol(user, clean)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Watchlist item not found."));
        watchlist.delete(item);
    }

    private String clean(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Symbol is required.");
        }
        return symbol.trim().toUpperCase();
    }
}
