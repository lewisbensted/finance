package finance.dto;

import finance.entity.User;

import java.math.BigDecimal;

public record UserDTO(String username, String email, String firstName, String lastName, BigDecimal balance) {
    public UserDTO(User user) {
        this(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBalance());
    }
}
