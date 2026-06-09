package com.prasun.mockmarket.account;

import com.prasun.mockmarket.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
}
