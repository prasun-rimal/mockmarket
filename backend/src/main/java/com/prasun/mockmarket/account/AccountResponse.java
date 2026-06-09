package com.prasun.mockmarket.account;

import java.math.BigDecimal;

public record AccountResponse(String username, BigDecimal cashBalance, BigDecimal startingBalance) {
}
