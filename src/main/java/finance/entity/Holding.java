package finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;

@Entity
@IdClass(HoldingId.class)
@Table(name = "holdings")
public class Holding {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @NotNull
    @Id
    private User user;

    @Column(nullable = false, updatable = false)
    @NotNull
    @Id
    private String symbol;

    @NotNull
    @Column(name = "company_name", nullable = false, updatable = false)
    private String companyName;

    @NotNull
    @Column(nullable = false)
    private int shares;

    public Holding(User user, String symbol, String companyName, Integer shares) {
        this.user = user;
        this.symbol = symbol;
        this.companyName = companyName;
        this.shares = shares;
    }

    public void updateShares(TransactionType transactionType, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Transaction must be a positive number of shares.");
        if (transactionType == SELL && amount>shares) throw new IllegalArgumentException("Insufficient shares to sell.");
        this.shares += transactionType==BUY ? amount : -amount;
    }
}
