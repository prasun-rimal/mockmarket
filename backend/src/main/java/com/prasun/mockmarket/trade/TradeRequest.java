package com.prasun.mockmarket.trade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TradeRequest(
        @NotBlank String symbol,
        @NotNull @DecimalMin(value = "0.0001", message = "must be greater than 0") BigDecimal quantity
) {
}
