package finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

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
    private Long shares;

    public Holding() {
    }

    public Holding(User user, String symbol, String companyName, Long shares) {
        this();
        this.user = user;
        this.id = new HoldingId(user.getId(), symbol);
        this.companyName = companyName;
        this.shares = shares;
    }

    public void remove(Long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transaction must be a positive number of shares.");
        if (quantity > shares)
            throw new IllegalArgumentException("Insufficient shares to sell.");
        this.shares -= quantity;
    }

    public void add(Long quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Transaction must be a positive number of shares.");
        this.shares += quantity;
    }

    public Long getShares() {
        return this.shares;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
