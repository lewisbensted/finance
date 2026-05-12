package finance.entities;

import finance.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTests {
    User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "testuser@test.com", "test", "user", "test_hash", BigDecimal.valueOf(100));
    }

    @Nested
    class TestDeposit {
        @Test
        void testSuccess() {
            testUser.deposit(BigDecimal.valueOf(10));
            assertEquals(BigDecimal.valueOf(110), testUser.getBalance());
        }

        @Test
        void testZeroAmount() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testUser.deposit(BigDecimal.ZERO));
            assertEquals("Amount must be positive", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }

        @Test
        void testNullAmount() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testUser.deposit(null));
            assertEquals("Amount must be positive", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }
    }

    @Nested
    class TestWithdraw {
        @Test
        void testSuccess() {
            testUser.withdraw(BigDecimal.valueOf(100));
            assertEquals(BigDecimal.ZERO, testUser.getBalance());
        }

        @Test
        void testNegativeAmount() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testUser.withdraw(BigDecimal.valueOf(-1)));
            assertEquals("Amount must be positive", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }

        @Test
        void testNullAmount() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testUser.withdraw(BigDecimal.valueOf(-1)));
            assertEquals("Amount must be positive", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }

        @Test
        void testInsufficientBalance() {
            InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> testUser.withdraw(BigDecimal.valueOf(101)));
            assertEquals("Insufficient funds", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }
    }
}
