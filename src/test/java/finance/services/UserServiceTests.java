package finance.services;

import finance.entities.User;
import finance.exceptions.AuthorisationException;
import finance.exceptions.RegistrationException;
import finance.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static finance.services.PasswordService.hash;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    private UserRepository mockUserRepo;
    private UserService userService;
    User existingUser;
    BCryptPasswordEncoder mockPasswordEncoder;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockPasswordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserService(mockUserRepo);
        existingUser = new User("existinguser", "existinguser@test.com", "existing", "user", hash("password"));
    }


    @Nested
    class RegisterTests {
        @Test
        void testSuccess() {
            when(mockUserRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.empty());
            when(mockUserRepo.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            User newUser = userService.register("testuser", "testuser@test.com", "test", "user", "testpassword", "testpassword");
            assertNotNull(newUser);
            assertEquals("testuser", newUser.getUsername());
            assertEquals("testuser@test.com", newUser.getEmail());
            assertEquals("test", newUser.getFirstName());
            assertEquals("user", newUser.getLastName());
            assertEquals(BigDecimal.ZERO, newUser.getBalance());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testEmailTaken() {
            when(mockUserRepo.findByEmail(anyString()))
                    .thenReturn(Optional.of(existingUser));
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.empty());
            RegistrationException exception = assertThrows(RegistrationException.class,
                    () -> userService.register("testuser", "existinguser@test.com", "test", "user", "testpassword", "testpassword"));
            assertEquals("Email address already taken", exception.getMessage());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testUsernameTaken() {
            when(mockUserRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.of(existingUser));

            RegistrationException exception = assertThrows(RegistrationException.class,
                    () -> userService.register("existinguser", "testuser@test.com", "test", "user", "testpassword", "testpassword"));
            assertEquals("Username already taken", exception.getMessage());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testPasswordMatch() {
            when(mockUserRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());
            when(mockUserRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());
            RegistrationException exception = assertThrows(RegistrationException.class,
                    () -> userService.register("existinguser", "testuser@test.com", "test", "user", "testpassword", "testpasword"));
            assertEquals("Passwords do not match", exception.getMessage());
            verify(mockUserRepo, never()).save(any());
        }
    }

    @Nested
    class LoginTests {
        @Test
        void testSuccess() {
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.of(existingUser));
            User loggedInUser = userService.login("existinguser", "password");
            assertEquals(loggedInUser, existingUser);
        }

        @Test
        void testInvalidUsername() {
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.empty());
            AuthorisationException exception = assertThrows(AuthorisationException.class,
                    () -> userService.login("testuser", "password"));
            assertEquals("Invalid username or password", exception.getMessage());
        }

        @Test
        void testIncorrectPassword() {
            when(mockUserRepo.findByUsername(anyString()))
                    .thenReturn(Optional.of(existingUser));
            AuthorisationException exception = assertThrows(AuthorisationException.class,
                    () -> userService.login("existinguser", "pasword"));
            assertEquals("Invalid username or password", exception.getMessage());
        }
    }
}
