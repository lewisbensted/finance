package finance.unit.entities;

import finance.entities.Holding;
import finance.entities.User;
import finance.exceptions.InsufficientSharesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static finance.fixtures.HoldingFixtures.appleHolding;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HoldingTests {
    User testUser = new User("testuser", "testuser@test.com", "test", "user", "test_hash", BigDecimal.valueOf(100));
    Holding testHolding;

    @BeforeEach
    void setUp() {
        testHolding = appleHolding(testUser, 10L);
    }

    @Nested
    class TestAdd {
        @Test
        void testSuccess() {
            testHolding.add(10L);
            assertEquals(20L, testHolding.getShares());
        }

        @Test
        void testNullQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testHolding.add(null));
            assertEquals("Quantity must be positive", exception.getMessage());
            assertEquals(10L, testHolding.getShares());
        }

        @Test
        void testZeroQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testHolding.add(0L));
            assertEquals("Quantity must be positive", exception.getMessage());
            assertEquals(10L, testHolding.getShares());
        }
    }

    @Nested
    class TestRemove {
        @Test
        void testSuccess() {
            testHolding.remove(10L);
            assertEquals(0, testHolding.getShares());
        }

        @Test
        void testNullQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testHolding.remove(null));
            assertEquals("Quantity must be positive", exception.getMessage());
            assertEquals(10L, testHolding.getShares());
        }

        @Test
        void testNegativeQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> testHolding.remove(-1L));
            assertEquals("Quantity must be positive", exception.getMessage());
            assertEquals(10L, testHolding.getShares());
        }

        @Test
        void testInsufficientShares() {
            InsufficientSharesException exception = assertThrows(InsufficientSharesException.class, () -> testHolding.remove(11L));
            assertEquals("Insufficient shares", exception.getMessage());
            assertEquals(10L, testHolding.getShares());
        }
    }
}
