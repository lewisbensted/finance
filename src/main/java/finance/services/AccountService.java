package finance.services;

import finance.entity.User;
import finance.exceptions.NotFoundException;
import finance.repository.UserRepository;
import org.springframework.stereotype.Service;

import static finance.entity.TransferType.DEPOSIT;
import static finance.entity.TransferType.WITHDRAW;

@Service
public class AccountService {
    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void deposit(User user, double amount) {
        user.updateBalance(DEPOSIT, amount);
        userRepository.save(user);
    }

    public void withdraw(User user, double amount) {
        user.updateBalance(WITHDRAW, amount);
        userRepository.save(user);
    }
}
