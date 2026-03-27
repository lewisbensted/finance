package finance.dto;

import jakarta.validation.constraints.*;

public record RegisterDTO(@NotBlank(message = "Username is required.")
                          @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
                          String username,

                          @NotBlank(message = "Email is required.")
                          @Email(message = "Invalid email address.")
                          String email,

                          @NotBlank(message = "First name is required.")
                          @Pattern(
                                  regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ' -]+$",
                                  message = "Invalid first name format."
                          )
                          String firstName,

                          @NotBlank(message = "Last name is required")
                          @Pattern(
                                  regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ' -]+$",
                                  message = "Invalid last name format."
                          )
                          String lastName,

                          @NotBlank(message = "Password is required.")
                          @Pattern(
                                  regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[$&+,:;=?@#|'<>.^*()%!\\-/])\\S+$",
                                  message = "Password must contain a number, letter and special character, without spaces."
                          )
                          @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters.")
                          String password,

                          @NotBlank(message = "Password confirmation is required.")
                          String confirmPassword) {
}
