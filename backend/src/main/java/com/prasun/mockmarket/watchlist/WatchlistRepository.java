package com.prasun.mockmarket.watchlist;

import com.prasun.mockmarket.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndSymbol(User user, String symbol);
    Optional<WatchlistItem> findByUserAndSymbol(User user, String symbol);
}
