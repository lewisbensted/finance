package finance.services;

import finance.dto.StockDTO;
import finance.dto.StockResultDTO;
import finance.dto.TransactionDTO;
import finance.dto.TransactionResultDTO;
import finance.entity.Holding;
import finance.entity.User;
import finance.exceptions.InsufficientFundsException;
import finance.repository.HoldingRepository;
import finance.repository.TransactionRepository;
import finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;
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
    private TransactionService spy;

    StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
    StockDTO appleStock = new StockDTO("Apple", "AAPL", BigDecimal.valueOf(4));
    StockDTO microsoftStock = new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(5));

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockTransactionRepo = mock(TransactionRepository.class);
        mockHoldingRepo = mock(HoldingRepository.class);
        mockStockService = mock(StockService.class);
        transactionService = new TransactionService(mockUserRepo, mockTransactionRepo, mockHoldingRepo, mockStockService);

        spy = Mockito.spy(transactionService);
    }

    @Nested
    class BuyTests {
        @Test
        void testSuccessNewHolding() {
            User testUser = new User(BigDecimal.valueOf(100));
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
            User testUser = new User(BigDecimal.valueOf(100));
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
            User testUser = new User(BigDecimal.valueOf(100));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.buy(testUser, testStock, -5L));
            assertEquals("Transaction must be a positive number of shares.", exception.getMessage());
            verify(mockTransactionRepo, never()).save(any());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
        }

        @Test
        void testNullStock() {
            User testUser = new User(BigDecimal.valueOf(100));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.buy(testUser, null, 5L));
            verify(mockTransactionRepo, never()).save(any());
            verify(mockHoldingRepo, never()).findByIdUserIdAndIdSymbol(any(), any());
            assertEquals("Stock cannot be null", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
        }
    }

    @Nested
    class SellTests {
        @Test
        void testSellSome() {
            User testUser = new User(BigDecimal.valueOf(100));
            Holding testHolding = new Holding(testUser, "Test", "TST", 3L);
            testUser.addHolding(testHolding);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(testHolding));
            transactionService.sell(testUser, testStock, 2L);

            verify(mockTransactionRepo).save(any());
            BigDecimal expected = BigDecimal.valueOf(100).add(testStock.latestPrice().multiply(BigDecimal.valueOf(2)));
            assertEquals(expected, testUser.getBalance());
            assertEquals(1, testUser.getHoldings().size());
            assertEquals(1L, testHolding.getShares());
        }

        @Test
        void testSellAll() {
            User testUser = new User(BigDecimal.valueOf(100));
            Holding testHolding = new Holding(testUser, "Test", "TST", 2L);
            testUser.addHolding(testHolding);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(testHolding));
            transactionService.sell(testUser, testStock, 2L);

            verify(mockTransactionRepo).save(any());
            BigDecimal expected = BigDecimal.valueOf(100).add(testStock.latestPrice().multiply(BigDecimal.valueOf(2)));
            assertEquals(expected, testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
        }

        @Test
        void testDepositFails() {
            User testUser = new User(BigDecimal.valueOf(100));
            Holding testHolding = new Holding(testUser, "Test", "TST", 2L);
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.of(testHolding));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.sell(testUser, testStock, -5L));
            assertEquals("Transaction must be a positive number of shares.", exception.getMessage());
            verify(mockTransactionRepo, never()).save(any());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }

        @Test
        void testFindHoldingFails() {
            User testUser = new User(BigDecimal.valueOf(100));
            when(mockHoldingRepo.findByIdUserIdAndIdSymbol(any(), any()))
                    .thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.sell(testUser, testStock, 5L));
            assertEquals("Holding does not exist", exception.getMessage());
            verify(mockTransactionRepo, never()).save(any());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }

        @Test
        void testNullStock() {
            User testUser = new User(BigDecimal.valueOf(100));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.sell(testUser, null, 5L));
            verify(mockTransactionRepo, never()).save(any());
            verify(mockHoldingRepo, never()).findByIdUserIdAndIdSymbol(any(), any());
            assertEquals("Stock cannot be null", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
        }
    }

    @Nested
    class ExecuteTransactionsTests {

        TransactionDTO appleTransaction = new TransactionDTO("AAPL", 5L);
        TransactionDTO bananaTransaction = new TransactionDTO("BANANA", 5L);
        TransactionDTO microsoftTransaction = new TransactionDTO("MSFT", 2L);
        TransactionDTO microsoftTransactionInvalid = new TransactionDTO("MSFT", -5L);

        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.executeTransactions(1L, BUY, List.of()));
            assertEquals("User not found.", exception.getMessage());
            verifyNoInteractions(mockStockService);
        }

        @Test
        void testEmptyList() {
            User testUser = new User(BigDecimal.valueOf(100));
            when(mockUserRepo.findById(any()))
                    .thenReturn(Optional.of(testUser));
            when(mockStockService.fetchPrices(any()))
                    .thenReturn(Map.of());
            List<TransactionResultDTO> results = transactionService.executeTransactions(1L, BUY, List.of());
            assertTrue(results.isEmpty());
        }

        @Nested
        class testBuy {

            @Test
            void testSuccessPartial() {
                User testUser = new User(BigDecimal.valueOf(100));
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, bananaTransaction, microsoftTransactionInvalid);

                doNothing()
                        .when(spy)
                        .buy(any(), eq(appleStock), any());
                doThrow(new IllegalArgumentException("Invalid quantity"))
                        .when(spy)
                        .buy(any(), eq(microsoftStock), any());
                when(mockUserRepo.findById(any()))
                        .thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any()))
                        .thenReturn(Map.of(
                                "AAPL", new StockResultDTO(appleStock, null),
                                "BANANA", new StockResultDTO(null, "Invalid symbol"),
                                "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = spy.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransaction);
                assertEquals("Invalid symbol", transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), microsoftTransactionInvalid);
                assertEquals("Invalid quantity", transactions.get(2).error());
                verify(mockUserRepo).findById(1L);
                verify(mockStockService).fetchPrices(
                        new String[]{"AAPL", "BANANA", "MSFT"}
                );
            }

            @Test
            void testSuccessFull() {
                User testUser = new User(BigDecimal.valueOf(100));

                doNothing()
                        .when(spy)
                        .buy(any(), eq(appleStock), any());
                doNothing()
                        .when(spy)
                        .buy(any(), eq(microsoftStock), any());

                List<TransactionDTO> transactionRequests = List.of(appleTransaction, microsoftTransaction);

                when(mockUserRepo.findById(any()))
                        .thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any()))
                        .thenReturn(Map.of(
                                "AAPL", new StockResultDTO(appleStock, null),
                                "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = spy.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());
                verify(mockUserRepo).findById(1L);
                verify(mockStockService).fetchPrices(
                        new String[]{"AAPL", "MSFT"}
                );
            }

            @Test
            void testInsufficientFunds() {
                User testUser = new User(BigDecimal.valueOf(10));
                doNothing()
                        .when(spy)
                        .buy(any(), eq(appleStock), any());
                doNothing()
                        .when(spy)
                        .buy(any(), eq(microsoftStock), any());

                List<TransactionDTO> transactionRequests = List.of(appleTransaction, microsoftTransaction);

                when(mockUserRepo.findById(any()))
                        .thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any()))
                        .thenReturn(Map.of(
                                "AAPL", new StockResultDTO(appleStock, null),
                                "MSFT", new StockResultDTO(appleStock, null)));
                assertThrows(InsufficientFundsException.class,
                        () -> transactionService.executeTransactions(1L, BUY, transactionRequests));

                assertEquals(BigDecimal.valueOf(10), testUser.getBalance());
            }

            @Test
            void testCumulativeBalance(){
                assertEquals(true, false);
            }

        }

        @Nested
        class testSell {
            @Test
            void testSuccessFull() {
                User testUser = new User(BigDecimal.valueOf(100));
                Holding appleHolding = new Holding(testUser, "AAPL", "Apple", 5L);
                Holding microsoftHolding = new Holding(testUser, "MSFT", "Microsoft", 10L);
                testUser.addHolding(appleHolding);
                testUser.addHolding(microsoftHolding);

                doNothing()
                        .when(spy)
                        .sell(any(), eq(appleStock), any());
                doNothing()
                        .when(spy)
                        .sell(any(), eq(microsoftStock), any());

                List<TransactionDTO> transactionRequests = List.of(appleTransaction, microsoftTransaction);

                when(mockUserRepo.findById(any()))
                        .thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any()))
                        .thenReturn(Map.of(
                                "AAPL", new StockResultDTO(appleStock, null),
                                "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = spy.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());
                verify(mockUserRepo).findById(1L);
                verify(mockStockService).fetchPrices(
                        new String[]{"AAPL", "MSFT"}
                );
            }

            @Test
            void testSuccessPartial() {
                User testUser = new User(BigDecimal.valueOf(100));

                Holding appleHolding = new Holding(testUser, "AAPL", "Apple", 10L);
                Holding microsoftHolding = new Holding(testUser, "MSFT", "Microsoft", 10L);
                testUser.addHolding(appleHolding);
                testUser.addHolding(microsoftHolding);

                doNothing()
                        .when(spy)
                        .sell(any(), eq(appleStock), any());
                doThrow(new IllegalArgumentException("Invalid quantity"))
                        .when(spy)
                        .sell(any(), eq(microsoftStock), any());

                List<TransactionDTO> transactionRequests = List.of(appleTransaction, bananaTransaction, microsoftTransactionInvalid);

                when(mockUserRepo.findById(any()))
                        .thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any()))
                        .thenReturn(Map.of(
                                "AAPL", new StockResultDTO(appleStock, null),
                                "BANANA", new StockResultDTO(null, "Invalid symbol"),
                                "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = spy.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransaction);
                assertEquals("Invalid symbol", transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), microsoftTransactionInvalid);
                assertEquals("Invalid quantity", transactions.get(2).error());
                verify(mockUserRepo).findById(1L);
                verify(mockStockService).fetchPrices(
                        new String[]{"AAPL", "BANANA", "MSFT"}
                );
            }

            @Test
            void testCumulativeBalance(){
                assertEquals(true, false);
            }
        }
    }
}
