package finance.services;

import finance.entity.User;
import finance.exceptions.NotFoundException;
import finance.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void deposit(User user, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Top-up amount must be positive");
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
    }

    public void withdraw(User user, double amount) {
        if (amount > user.getBalance()) throw new IllegalArgumentException("Cannot withdraw more than current balance.");
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
    }
}
