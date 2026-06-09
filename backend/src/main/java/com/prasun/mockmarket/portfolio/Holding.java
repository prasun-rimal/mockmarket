package com.prasun.mockmarket.portfolio;

import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "average_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal averagePrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Holding() {
    }

    public Holding(User user, String symbol, BigDecimal quantity, BigDecimal averagePrice) {
        this.user = user;
        this.symbol = symbol.toUpperCase();
        this.quantity = MoneyUtil.quantity(quantity);
        this.averagePrice = MoneyUtil.money(averagePrice);
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSymbol() { return symbol; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setQuantity(BigDecimal quantity) { this.quantity = MoneyUtil.quantity(quantity); }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = MoneyUtil.money(averagePrice); }
}
