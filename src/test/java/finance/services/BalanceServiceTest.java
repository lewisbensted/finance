package finance.services;


import finance.entities.User;
import finance.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BalanceServiceTest {

    private BalanceService balanceService;
    private UserRepository mockUserRepo;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        balanceService = new BalanceService(mockUserRepo);
        testUser = new User(BigDecimal.valueOf(100));
    }

    @Nested
    class DepositTests {
        @Test
        void testSuccess() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            balanceService.deposit(1L, BigDecimal.valueOf(50));
            assertEquals(BigDecimal.valueOf(150), testUser.getBalance());
            verify(mockUserRepo).save(testUser);
        }

        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> balanceService.deposit(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo).findById(any());
            verify(mockUserRepo, never()).save(any());
            assertEquals("User not found.", exception.getMessage());
        }

        @Test
        void testDepositFails() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> balanceService.deposit(1L, BigDecimal.valueOf(-50)));
            verify(mockUserRepo, never()).save(any());
            assertEquals(testUser.getBalance(), BigDecimal.valueOf(100));
            assertEquals("Amount must be positive", exception.getMessage());
        }
    }

    @Nested
    class WithdrawTests {
        @Test
        void testSuccess() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            balanceService.withdraw(1L, BigDecimal.valueOf(50));
            assertEquals(BigDecimal.valueOf(50), testUser.getBalance());
            verify(mockUserRepo).save(testUser);
        }

        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> balanceService.withdraw(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo).findById(any());
            verify(mockUserRepo, never()).save(any());
            assertEquals("User not found.", exception.getMessage());
        }

        @Test
        void testWithdrawFails() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> balanceService.withdraw(1L, BigDecimal.valueOf(-50)));
            verify(mockUserRepo, never()).save(any());
            assertEquals(testUser.getBalance(), BigDecimal.valueOf(100));
            assertEquals("Amount must be positive", exception.getMessage());
        }
    }
}
