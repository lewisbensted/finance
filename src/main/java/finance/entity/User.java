package finance.entity;

import finance.exceptions.InsufficientFundsException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private Double balance;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holding> holdings = new ArrayList<>();

    protected User() {
    }

    public User(String username, String email, String firstName, String lastName, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = (double) 0;
    }

    public Double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be a positive number.");
        this.balance += amount;
    }

    public void withdraw(double amount){
        if (amount <= 0) throw new IllegalArgumentException("Amount must be a positive number.");
        if (amount > this.getBalance())
            throw new InsufficientFundsException("Insufficient funds.");
        this.balance -= amount;
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

    public Transaction addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        return transaction;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(this.transactions);
    }

    public Holding addHolding(Holding holding) {
        this.holdings.add(holding);
        return holding;
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
}
