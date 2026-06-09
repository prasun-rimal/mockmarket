package com.prasun.mockmarket.watchlist;

import com.prasun.mockmarket.auth.CurrentUser;
import com.prasun.mockmarket.user.User;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public List<WatchlistResponse> get(@CurrentUser User user) {
        return watchlistService.get(user);
    }

    @PostMapping("/{symbol}")
    public WatchlistResponse add(@CurrentUser User user, @PathVariable String symbol) {
        return watchlistService.add(user, symbol);
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> delete(@CurrentUser User user, @PathVariable String symbol) {
        watchlistService.delete(user, symbol);
        return ResponseEntity.noContent().build();
    }
}
