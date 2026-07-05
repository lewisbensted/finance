package finance.services;

import finance.entities.User;
import finance.exceptions.NotFoundException;
import finance.exceptions.RegistrationException;
import finance.exceptions.AuthorisationException;
import finance.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import static finance.services.PasswordService.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(String username, String email, String firstName, String lastName, String password, String passwordRepeat) {
        userRepository.findByEmail(email).ifPresent(e -> {
            throw new RegistrationException("Email address already taken");
        });
        userRepository.findByUsername(username).ifPresent(e -> {
            throw new RegistrationException("Username already taken");
        });
        if (!password.equals(passwordRepeat)) throw new RegistrationException("Passwords do not match");
        User newUser = new User(username, email, firstName, lastName, hash(password));
        return userRepository.save(newUser);
    }

    @Transactional
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AuthorisationException("Invalid username or password"));
        if (!compare(password, user.getPasswordHash()))
            throw new AuthorisationException("Invalid username or password");
        return user;
    }

    @Transactional
    public void changePassword(Long userId, String password, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!compare(password, user.getPasswordHash()))
            throw new AuthorisationException("Incorrect password");
        user.changePassword(newPassword);
        userRepository.save(user);
    }
}
