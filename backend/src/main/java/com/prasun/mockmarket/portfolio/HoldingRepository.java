package com.prasun.mockmarket.portfolio;

import com.prasun.mockmarket.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUserOrderBySymbolAsc(User user);
    Optional<Holding> findByUserAndSymbol(User user, String symbol);
}
