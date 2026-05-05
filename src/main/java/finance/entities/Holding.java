package finance.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

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

    protected Holding() {
    }

    public Holding(User user, String symbol, String companyName, Long shares) {
        this();
        this.user = user;
        this.id = new HoldingId(user.getId(), symbol);
        this.companyName = companyName;
        this.shares = shares;
    }

    public void remove(Long quantity) {
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (quantity > shares)
            throw new IllegalArgumentException("Insufficient shares");
        this.shares -= quantity;
    }

    public void add(Long quantity) {
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.shares += quantity;
    }

    public Long getShares() {
        return this.shares;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSymbol() {
        return id.getSymbol();
    }
}
