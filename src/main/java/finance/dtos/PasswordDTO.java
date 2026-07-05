package finance.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordDTO(
        @NotBlank(message = "Password is required")
        String password,
                          @NotBlank(message = "New password is required")
                          @Pattern(
                                  regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[$&+,:;=?@#|'<>.^*()%!\\-/])\\S+$",
                                  message = "New password must contain a number, letter and special character, without spaces"
                          )
                          @Size(min = 8, max = 30, message = "New password must be between 8 and 30 characters")
                          String newPassword) {
}
