package com.prasun.mockmarket.trade;

import com.prasun.mockmarket.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}
