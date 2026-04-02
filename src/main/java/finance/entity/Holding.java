package finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;

@Entity
@Table(name = "holdings")
public class Holding {

    @EmbeddedId
    private HoldingId id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @MapsId("userId")
    private User user;

    @NotNull
    @Column(name = "company_name", nullable = false, updatable = false)
    private String companyName;

    @NotNull
    @Column(nullable = false)
    private Integer shares;

    public Holding() {
    }

    public Holding(User user, String symbol, String companyName, Integer shares) {
        this();
        this.user = user;
        this.id = new HoldingId(user.getId(), symbol);
        this.companyName = companyName;
        this.shares = shares;
    }

    public void remove(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transaction must be a positive number of shares.");
        if (quantity > shares)
            throw new IllegalArgumentException("Insufficient shares to sell.");
        this.shares -= quantity;
    }

    public void add(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transaction must be a positive number of shares.");
        this.shares += quantity;
    }

    public Integer getShares() {
        return this.shares;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
