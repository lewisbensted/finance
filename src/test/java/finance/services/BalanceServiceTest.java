package finance.services;


import finance.entity.User;
import finance.repository.UserRepository;
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

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        balanceService = new BalanceService(mockUserRepo);
    }

    @Nested
    class DepositTests {
        @Test
        void testSuccess() {
            User testUser = new User();
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            balanceService.deposit(1L, BigDecimal.valueOf(50));
            assertEquals(BigDecimal.valueOf(50), testUser.getBalance());
            verify(mockUserRepo).save(testUser);
        }

        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> balanceService.deposit(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo).findById(1L);
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testDepositFails() {
            User mockUser = mock(User.class);
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(mockUser));
            doThrow(new RuntimeException("Invalid deposit"))
                    .when(mockUser)
                    .deposit(any(BigDecimal.class));
            assertThrows(RuntimeException.class,
                    () -> balanceService.deposit(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo, never()).save(any());
        }
    }

    @Nested
    class WithdrawTests {
        @Test
        void testSuccess() {
            User testUser = new User();
            testUser.deposit(BigDecimal.valueOf(100));
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
            assertThrows(IllegalArgumentException.class,
                    () -> balanceService.withdraw(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo).findById(1L);
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testWithdrawFails() {
            User mockUser = mock(User.class);
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(mockUser));
            doThrow(new RuntimeException("Invalid withdrawal"))
                    .when(mockUser)
                    .withdraw(any(BigDecimal.class));
            assertThrows(RuntimeException.class,
                    () -> balanceService.withdraw(1L, BigDecimal.valueOf(50)));
            verify(mockUserRepo, never()).save(any());
        }
    }
}
