package finance.entities;

import finance.dtos.UserDTO;
import finance.exceptions.InsufficientFundsException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static finance.services.PasswordService.hash;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, updatable = false)
    private String username;

    @NotNull
    @Column(nullable = false, updatable = false)
    private String email;

    @NotNull
    @Column(name = "first_name", nullable = false, updatable = false)
    private String firstName;

    @NotNull
    @Column(name = "last_name", nullable = false, updatable = false)
    private String lastName;

    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotNull
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private final List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Holding> holdings = new ArrayList<>();

    protected User() {
    }

    public User(String username,
                String email,
                String firstName,
                String lastName,
                String passwordHash,
                BigDecimal balance) {

        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = balance;
    }

    public User(String username, String email, String firstName, String lastName, String passwordHash) {
        this(username, email, firstName, lastName, passwordHash, BigDecimal.ZERO);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.compareTo(this.getBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(this.transactions);
    }

    public void addHolding(Holding holding) {
        this.holdings.add(holding);
    }

    public void removeHolding(Holding holding) {
        this.holdings.remove(holding);
        holding.setUser(null);
    }

    public List<Holding> getHoldings() {
        return this.holdings;
    }

    public Long getId() {
        return this.id;
    }

    public void changePassword(String newPassword) {
        this.passwordHash = hash(newPassword);
    }
}
