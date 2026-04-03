package finance.services;

import finance.dto.*;
import finance.entity.Holding;
import finance.entity.Transaction;
import finance.entity.TransactionType;
import finance.entity.User;
import finance.repository.HoldingRepository;
import finance.repository.TransactionRepository;
import finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;

@Service
public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;
    private final StockService stockService;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, HoldingRepository holdingRepository, StockService stockService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.holdingRepository = holdingRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void buy(User user, TransactionDTO transactionDTO, Double price) {
        String symbol = transactionDTO.symbol();
        String companyName = transactionDTO.companyName();
        Integer quantity = transactionDTO.quantity();

        user.withdraw(quantity * price);

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, BUY);
        transactionRepository.save(transaction);
        user.addTransaction(transaction);

        Holding holding = holdingRepository.findByIdUserIdAndIdSymbol(user.getId(), symbol)
                .orElseGet(() -> new Holding(user, symbol, companyName, 0));

        holding.add(quantity);
        if (!user.getHoldings().contains(holding)) {
            user.addHolding(holding);
        }
    }

    @Transactional
    public void sell(User user, TransactionDTO transactionDTO, Double price) {
        String symbol = transactionDTO.symbol();
        String companyName = transactionDTO.companyName();
        Integer quantity = transactionDTO.quantity();

        user.deposit(quantity * price);

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, SELL);
        transactionRepository.save(transaction);
        user.addTransaction(transaction);

        Holding holding = holdingRepository.findByIdUserIdAndIdSymbol(user.getId(), symbol)
                .orElseThrow(() -> new IllegalArgumentException("Holding does not exist"));

        holding.remove(quantity);
        if (holding.getShares() <= 0) {
            user.removeHolding(holding);
        }
    }

    public List<TransactionResultDTO> executeTransactions(Long userId, TransactionType type, List<TransactionDTO> transactions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Map<String, StockFetchDTO> prices = stockService.fetchPrices(transactions.stream().map(TransactionDTO::symbol).toArray(String[]::new));

        List<TransactionResultDTO> results = new ArrayList<>();

        for (TransactionDTO transaction : transactions) {
            StockFetchDTO fetch = prices.get(transaction.symbol());
            if (fetch == null) {
                results.add(new TransactionResultDTO(transaction, "Unexpected Error."));
                continue;
            }
            if (fetch.error() != null) {
                results.add(new TransactionResultDTO(transaction, fetch.error()));
                continue;
            }
            try {
                if (type == BUY)
                    buy(user, transaction, fetch.stock().latestPrice());
                else
                    sell(user, transaction, fetch.stock().latestPrice());
                results.add(new TransactionResultDTO(transaction, null));
            } catch (IllegalArgumentException e) {
                results.add(new TransactionResultDTO(transaction, e.getMessage()));
            } catch (Exception e) {
                results.add(new TransactionResultDTO(transaction, "Unexpected Error."));
            }
        }

        return results;
    }


}
