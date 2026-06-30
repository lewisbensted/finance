package finance.services;

import finance.entities.User;
import finance.exceptions.NotFoundException;
import finance.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User deposit(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        user.deposit(amount);
        return userRepository.save(user);
    }

    @Transactional
    public User withdraw(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        user.withdraw(amount);
        return userRepository.save(user);
    }


    public User getBalance(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }
}
