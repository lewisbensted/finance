package finance.services;

import finance.entity.User;
import finance.exceptions.AuthenticationException;
import finance.exceptions.NotFoundException;
import finance.exceptions.RegistrationException;
import finance.repository.UserRepository;
import org.springframework.stereotype.Service;

import static finance.services.PasswordService.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String email, String firstName, String lastName, String password, String passwordRepeat) {
        userRepository.findByEmail(email).ifPresent(e -> {
            throw new RegistrationException("Email address already taken.");
        });
        userRepository.findByUsername(username).ifPresent(e -> {
            throw new RegistrationException("Username already taken.");
        });
        if (!password.equals(passwordRepeat)) throw new RegistrationException("Passwords do not match.");
        validatePassword(password);
        User newUser = new User(username, email, firstName, lastName, hash(password));
        return userRepository.save(newUser);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AuthenticationException("Invalid username or password."));
        if (!compare(user.getPasswordHash(), password))
            throw new AuthenticationException("Invalid username or password.");
        return user;
    }

    public User topUpBalance(long id, double topUp) {
        if (topUp <= 0) throw new IllegalArgumentException("Top-up amount must be positive");
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found."));
        user.setBalance(user.getBalance() + topUp);
        return user;
    }
}
