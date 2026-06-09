package com.prasun.mockmarket.account;

import com.prasun.mockmarket.common.ApiException;
import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accounts;

    public AccountService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public Account createFor(User user) {
        return accounts.save(new Account(user));
    }

    public Account getFor(User user) {
        return accounts.findByUser(user).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found."));
    }

    public AccountResponse responseFor(User user) {
        var account = getFor(user);
        return new AccountResponse(user.getName(), MoneyUtil.money(account.getCashBalance()), MoneyUtil.money(account.getStartingBalance()));
    }
}
