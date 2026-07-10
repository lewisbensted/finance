package finance.services;

import finance.dtos.LoginDTO;
import finance.dtos.PasswordDTO;
import finance.dtos.RegisterDTO;
import finance.entities.User;
import finance.exceptions.NotFoundException;
import finance.exceptions.RegistrationException;
import finance.exceptions.AuthenticationException;
import finance.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import static finance.dtos.ErrorCode.INVALID_CREDENTIALS;
import static finance.services.PasswordService.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(RegisterDTO dto) {
        userRepository.findByEmail(dto.email()).ifPresent(e -> {
            throw new RegistrationException("Email address already taken");
        });
        userRepository.findByUsername(dto.username()).ifPresent(e -> {
            throw new RegistrationException("Username already taken");
        });
        if (!dto.password().equals(dto.passwordRepeat())) throw new RegistrationException("Passwords do not match");
        User newUser = new User(dto.username(), dto.email(), dto.firstName(), dto.lastName(), hash(dto.password()));
        return userRepository.save(newUser);
    }

    @Transactional
    public User login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.username()).orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS, "Invalid username or password"));
        if (!compare(dto.password(), user.getPasswordHash()))
            throw new AuthenticationException(INVALID_CREDENTIALS, "Invalid username or password");
        return user;
    }

    @Transactional
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!compare(dto.password(), user.getPasswordHash()))
            throw new AuthenticationException(INVALID_CREDENTIALS, "Incorrect password");
        user.changePassword(dto.newPassword());
        userRepository.save(user);
    }
}
