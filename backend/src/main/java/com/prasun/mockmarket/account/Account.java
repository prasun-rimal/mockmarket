package com.prasun.mockmarket.account;

import com.prasun.mockmarket.common.MoneyUtil;
import com.prasun.mockmarket.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "starting_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal startingBalance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Account() {
    }

    public Account(User user) {
        this.user = user;
        this.cashBalance = MoneyUtil.STARTING_BALANCE;
        this.startingBalance = MoneyUtil.STARTING_BALANCE;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public BigDecimal getStartingBalance() { return startingBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = MoneyUtil.money(cashBalance); }
}
