package finance.dto;

import jakarta.validation.constraints.NotEmpty;

public record RegisterDTO(@NotEmpty(message = "Username is required") String username,
                          @NotEmpty(message = "Email is required") String email,
                          @NotEmpty(message = "First name is required") String firstName,
                          @NotEmpty(message = "Last name is required") String lastName,
                          @NotEmpty(message = "Password is required") String password,
                          @NotEmpty(message = "Password confirmation is required") String confirmPassword) {
}
