package finance.services;

import finance.entity.Holding;
import finance.entity.Transaction;
import finance.entity.TransactionType;
import finance.entity.User;
import finance.repository.HoldingRepository;
import finance.repository.TransactionRepository;
import finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;

@Service
public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, HoldingRepository holdingRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.holdingRepository = holdingRepository;
    }

    @Transactional
    public void buy(Long userId, String symbol, String companyName, Integer quantity, Double price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.withdraw(quantity * price);

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, BUY);
        transactionRepository.save(transaction);
        user.addTransaction(transaction);

        Holding holding = holdingRepository.findByIdUserIdAndIdSymbol(userId, symbol)
                .orElseGet(() -> new Holding(user, symbol, companyName, 0));

        holding.add(quantity);
        if (!user.getHoldings().contains(holding)) {
            user.addHolding(holding);
        }
    }

    @Transactional
    public void sell(Long userId, String symbol, String companyName, Integer quantity, Double price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.deposit(quantity * price);

        Transaction transaction = new Transaction(user, symbol, companyName, quantity, price, SELL);
        transactionRepository.save(transaction);
        user.addTransaction(transaction);

        Holding holding = holdingRepository.findByIdUserIdAndIdSymbol(userId, symbol)
                .orElseThrow(() -> new IllegalArgumentException("Holding does not exist"));

        holding.remove(quantity);
        if (holding.getShares() <=0) {
            user.removeHolding(holding);
        }
    }
}
