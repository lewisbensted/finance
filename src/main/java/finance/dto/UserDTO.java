package finance.dto;

import finance.entity.User;

public record UserDTO(String username, String email, String firstName, String lastName, Double balance) {
    public UserDTO(User user) {
        this(user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBalance());
    }
}
