package com.prasun.mockmarket.trade;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        TransactionType type,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        Instant createdAt
) {
    static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getSymbol(),
                transaction.getQuantity(),
                transaction.getPrice(),
                transaction.getTotalAmount(),
                transaction.getCreatedAt()
        );
    }
}
