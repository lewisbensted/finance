package finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @NotNull
    private User user;

    @Column(nullable = false, updatable = false)
    @NotNull
    private String symbol;

    @NotNull
    @Column(name = "company_name", nullable = false, updatable = false)
    private String companyName;

    @Positive
    @NotNull
    @Column(nullable = false, updatable = false, columnDefinition = "CHECK (shares>0)")
    private Integer shares;

    @PositiveOrZero
    @NotNull
    @Column(nullable = false, updatable = false, columnDefinition = "CHECK (price>=0)")
    private Double price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, updatable = false)
    private TransactionType transactionType;

    @NotNull
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public Transaction(User user, String symbol, String companyName, Integer shares, Double price, TransactionType transactionType, LocalDateTime createdAt) {
        this.user = user;
        this.symbol = symbol;
        this.companyName = companyName;
        this.shares = shares;
        this.price = price;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
    }
}
