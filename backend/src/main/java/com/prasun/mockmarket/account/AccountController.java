package com.prasun.mockmarket.account;

import com.prasun.mockmarket.auth.CurrentUser;
import com.prasun.mockmarket.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public AccountResponse get(@CurrentUser User user) {
        return accountService.responseFor(user);
    }
}
