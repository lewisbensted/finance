package finance.services;

import finance.entity.User;
import finance.exceptions.NotFoundException;
import finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import static finance.entity.TransferType.DEPOSIT;
import static finance.entity.TransferType.WITHDRAW;

@Service
public class AccountService {
    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deposit(Long userId, double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.deposit(amount);
        userRepository.save(user);
    }

    @Transactional
    public void withdraw(Long userId, double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.withdraw(amount);
        userRepository.save(user);
    }
}
