package finance.services;

import finance.dto.StockDTO;
import finance.entity.Holding;
import finance.entity.User;
import finance.repository.HoldingRepository;
import finance.repository.TransactionRepository;
import finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class TransactionServiceTest {

    private UserRepository mockUserRepo;
    private TransactionRepository mockTransactionRepo;
    private HoldingRepository mockHoldingRepo;
    private StockService mockStockService;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockTransactionRepo = mock(TransactionRepository.class);
        mockHoldingRepo = mock(HoldingRepository.class);
        mockStockService = mock(StockService.class);
        transactionService = new TransactionService(mockUserRepo, mockTransactionRepo, mockHoldingRepo, mockStockService);
    }

    @Nested
    class BuyTests {
        @Test
        void testSuccessNewHolding() {
            User testUser = new User();
            testUser.deposit(BigDecimal.valueOf(100));
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(5));
            Holding newHolding = new Holding(testUser, "Test", "TST", 0L);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(newHolding));
            transactionService.buy(testUser, testStock, 5L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(testStock.latestPrice().multiply(BigDecimal.valueOf(5)));
            assertTrue(testUser.getHoldings().contains(newHolding));
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
        }

        @Test
        void testSuccessExistingHolding() {
            User testUser = new User();
            testUser.deposit(BigDecimal.valueOf(100));
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(5));
            Holding existingHolding = new Holding(testUser, "Test", "TST", 5L);
            testUser.addHolding(existingHolding);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(existingHolding));
            transactionService.buy(testUser, testStock, 3L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(testStock.latestPrice().multiply(BigDecimal.valueOf(3)));
            assertEquals(8, existingHolding.getShares());
            assertTrue(testUser.getHoldings().contains(existingHolding));
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
        }

        @Test
        void testWithdrawFails() {
            User mockUser = mock(User.class);
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
            doThrow(new RuntimeException("Invalid withdrawal"))
                    .when(mockUser)
                    .withdraw(any(BigDecimal.class));
            assertThrows(RuntimeException.class,
                    () -> transactionService.buy(mockUser, testStock, 5L));
            verify(mockTransactionRepo, never()).save(any());
        }
    }

    @Nested
    class SellTests {
        @Test
        void testSuccessPartial() {
            User testUser = new User();
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(5));
            Holding testHolding = new Holding(testUser, "Test", "TST", 3L);
            testUser.addHolding(testHolding);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(testHolding));
            transactionService.sell(testUser, testStock, 2L);

            verify(mockTransactionRepo).save(any());
            BigDecimal expected = testStock.latestPrice().multiply(BigDecimal.valueOf(2));
            assertEquals(expected, testUser.getBalance());
            assertEquals(1, testUser.getHoldings().size());
            assertEquals(1L, testHolding.getShares());
        }

        @Test
        void testSuccessFull() {
            User testUser = new User();
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(5));
            Holding testHolding = new Holding(testUser, "Test", "TST", 2L);
            testUser.addHolding(testHolding);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(testHolding));
            transactionService.sell(testUser, testStock, 2L);

            verify(mockTransactionRepo).save(any());
            BigDecimal expected = testStock.latestPrice().multiply(BigDecimal.valueOf(2));
            assertEquals(expected, testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
        }

        @Test
        void testDepositFails() {
            User mockUser = mock(User.class);
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
            doThrow(new RuntimeException("Invalid deposit"))
                    .when(mockUser)
                    .deposit(any(BigDecimal.class));
            assertThrows(RuntimeException.class,
                    () -> transactionService.sell(mockUser, testStock, 5L));
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testFindHoldingFails() {
            User mockUser = mock(User.class);
            StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
            doThrow(new RuntimeException(""))
                    .when(mockHoldingRepo)
                    .findByIdUserIdAndIdSymbol(any(), any());
            assertThrows(RuntimeException.class,
                    () -> transactionService.sell(mockUser, testStock, 5L));
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUser, never()).deposit(any());
        }

    }
}
