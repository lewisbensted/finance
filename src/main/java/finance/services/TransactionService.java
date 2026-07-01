package finance.services;

import finance.dtos.*;
import finance.entities.Holding;
import finance.entities.Transaction;
import finance.entities.TransactionType;
import finance.entities.User;
import finance.exceptions.InsufficientFundsException;
import finance.exceptions.InsufficientSharesException;
import finance.repositories.TransactionRepository;
import finance.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;

@Service
public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final StockService stockService;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, StockService stockService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void buy(User user, StockDTO stock, Long quantity) {
        if (stock == null)
            throw new IllegalArgumentException("Stock cannot be null");
        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Transaction must be a positive number of shares");

        String symbol = stock.symbol();
        String companyName = stock.companyName();
        BigDecimal price = stock.latestPrice();

        Holding holding = user.getHoldings()
                .stream()
                .filter(h -> h.getSymbol().equals(symbol))
                .findFirst()
                .orElseGet(() -> {
                    Holding h = new Holding(user, symbol, companyName, 0L);
                    user.addHolding(h);
                    return h;
                });

        holding.add(quantity);
        user.withdraw(BigDecimal.valueOf(quantity).multiply(price));

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, BUY);
        transactionRepository.save(transaction);
        userRepository.save(user);
        user.addTransaction(transaction);
    }

    @Transactional
    public void sell(User user, StockDTO stock, Long quantity) {
        if (stock == null)
            throw new IllegalArgumentException("Stock cannot be null");
        if (quantity == null || quantity <= 0)
            throw new IllegalArgumentException("Transaction must be a positive number of shares");
        String symbol = stock.symbol();
        String companyName = stock.companyName();
        BigDecimal price = stock.latestPrice();

        Holding holding = user.getHoldings()
                .stream()
                .filter(h -> h.getSymbol().equals(symbol))
                .findFirst()
                .orElseThrow(() -> new InsufficientSharesException("Holding does not exist"));
        holding.remove(quantity);
        if (holding.getShares() <= 0) {
            user.removeHolding(holding);
        }

        user.deposit(BigDecimal.valueOf(quantity).multiply(price));

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, SELL);
        transactionRepository.save(transaction);
        userRepository.save(user);
        user.addTransaction(transaction);
    }

    @Transactional(rollbackFor = InsufficientFundsException.class)
    public List<TransactionResultDTO> executeTransactions(Long userId, TransactionType type, List<TransactionRequestDTO> transactions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (transactions.isEmpty()) {
            return List.of();
        }

        Map<String, StockResultDTO> prices = stockService.fetchPrices(transactions.stream().map(TransactionRequestDTO::symbol).toArray(String[]::new));

        List<TransactionResultDTO> results = new ArrayList<>();

        BigDecimal runningCost = BigDecimal.ZERO;

        for (TransactionRequestDTO transaction : transactions) {
            StockResultDTO fetch = prices.get(transaction.symbol());
            if (fetch == null || fetch.error() != null) continue;
            if (type == BUY) {
                BigDecimal cost = BigDecimal.valueOf(transaction.quantity())
                        .multiply(fetch.stock().latestPrice());
                runningCost = runningCost.add(cost);
            }
        }

        if (type == BUY && runningCost.compareTo(user.getBalance()) > 0)
            throw new InsufficientFundsException("Insufficient funds for all transactions");


        for (TransactionRequestDTO transaction : transactions) {
            StockResultDTO fetch = prices.get(transaction.symbol());
            if (fetch == null) {
                results.add(new TransactionResultDTO(
                        transaction,
                        new ItemErrorDTO("INTERNAL_ERROR", "No price returned")
                ));
                continue;
            }
            if (fetch.error() != null) {
                results.add(new TransactionResultDTO(transaction, fetch.error()));
                continue;
            }
            StockDTO stock = fetch.stock();
            try {
                if (type == BUY) {
                    buy(user, stock, transaction.quantity());
                } else
                    sell(user, stock, transaction.quantity());
                results.add(new TransactionResultDTO(transaction, null));

            } catch (InsufficientSharesException e) {
                results.add(new TransactionResultDTO(transaction, new ItemErrorDTO("UNPROCESSABLE", e.getMessage())));
            } catch (IllegalArgumentException e) {
                results.add(new TransactionResultDTO(transaction, new ItemErrorDTO("BAD_REQUEST", e.getMessage())));
            } catch (Exception e) {
                results.add(new TransactionResultDTO(transaction, new ItemErrorDTO("INTERNAL_ERROR", "Unexpected error")));
            }
        }
        return results;
    }

    public Page<TransactionDTO> fetchTransactions(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);
        return transactions.map(Transaction::toDTO);
    }
}
