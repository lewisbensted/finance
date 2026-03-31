package finance.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HoldingId implements Serializable {
    private Long userId;
    private String symbol;

    public HoldingId() {
    }

    public HoldingId(Long userId, String symbol) {
        this.userId = userId;
        this.symbol = symbol;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoldingId compare)) return false;
        return Objects.equals(compare.symbol, this.symbol) &&
                Objects.equals(compare.userId, this.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, symbol);
    }
}