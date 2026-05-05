package finance.services;

import finance.dtos.StockDTO;
import finance.dtos.StockResultDTO;
import finance.dtos.TransactionDTO;
import finance.dtos.TransactionResultDTO;
import finance.entities.Holding;
import finance.entities.User;
import finance.exceptions.InsufficientFundsException;
import finance.repositories.HoldingRepository;
import finance.repositories.TransactionRepository;
import finance.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class TransactionServiceTests {

    private UserRepository mockUserRepo;
    private TransactionRepository mockTransactionRepo;
    private HoldingRepository mockHoldingRepo;
    private StockService mockStockService;
    private TransactionService transactionService;
    private User testUser;

    StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
    StockDTO appleStock = new StockDTO("Apple", "AAPL", BigDecimal.valueOf(4));
    StockDTO microsoftStock = new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(5));

    TransactionDTO appleTransaction = new TransactionDTO("AAPL", 5L);
    TransactionDTO bananaTransaction = new TransactionDTO("BANANA", 5L);
    TransactionDTO microsoftTransaction = new TransactionDTO("MSFT", 2L);
    TransactionDTO invalidTransaction = new TransactionDTO("MSFT", -5L);
    TransactionDTO appleTransactionSecond = new TransactionDTO("AAPL", 3L);
    TransactionDTO microsoftTransactionSecond = new TransactionDTO("MSFT", 5L);
    TransactionDTO insufficientFundsTransaction = new TransactionDTO("MSFT", 50L);

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockTransactionRepo = mock(TransactionRepository.class);
        mockHoldingRepo = mock(HoldingRepository.class);
        mockStockService = mock(StockService.class);
        transactionService = new TransactionService(mockUserRepo, mockTransactionRepo, mockHoldingRepo, mockStockService);
        testUser = new User(BigDecimal.valueOf(100));
    }

    @Nested
    class BuyTests {
        @Test
        void testSuccessNewHolding() {
            transactionService.buy(testUser, appleStock, 5L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(appleStock.latestPrice().multiply(BigDecimal.valueOf(5)));
            assertEquals(1, testUser.getHoldings().size());
            Holding holding = testUser.getHoldings().get(0);
            assertEquals("AAPL", holding.getSymbol());
            assertEquals(5L, holding.getShares());
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testSuccessExistingHolding() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            transactionService.buy(testUser, testStock, 3L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(testStock.latestPrice().multiply(BigDecimal.valueOf(3)));
            Holding holding = testUser.getHoldings().get(0);
            assertEquals("TST", holding.getSymbol());
            assertEquals(8L, holding.getShares());
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testInvalidQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.buy(testUser, testStock, -5L));
            assertEquals("Transaction must be a positive number of shares.", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testWithdrawFails() {
            InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> transactionService.buy(testUser, testStock, 100L));
            assertEquals("Insufficient funds", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testNullStock() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.buy(testUser, null, 5L));
            assertEquals("Stock cannot be null", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUserRepo, never()).save(any());
        }
    }

    @Nested
    class SellTests {
        @Test
        void testSellSome() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            transactionService.sell(testUser, testStock, 2L);
            BigDecimal expected = BigDecimal.valueOf(100).add(testStock.latestPrice().multiply(BigDecimal.valueOf(2)));
            assertEquals(expected, testUser.getBalance());
            assertEquals(1, testUser.getHoldings().size());
            assertEquals("TST", testUser.getHoldings().get(0).getSymbol());
            assertEquals(3L, testUser.getHoldings().get(0).getShares());
            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testSellAll() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            transactionService.sell(testUser, testStock, 5L);
            BigDecimal expected = BigDecimal.valueOf(100).add(testStock.latestPrice().multiply(BigDecimal.valueOf(5)));
            assertEquals(expected, testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
            verify(mockUserRepo).save(any());
            verify(mockTransactionRepo).save(any());
        }

        @Test
        void testInvalidQuantity() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.sell(testUser, testStock, -5L));
            assertEquals("Transaction must be a positive number of shares.", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }


        @Test
        void testInvalidHolding() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.sell(testUser, testStock, 5L));
            assertEquals("Holding does not exist", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testRemoveFails() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.sell(testUser, testStock, 100L));
            assertEquals("Insufficient shares to sell.", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testNullStock() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.sell(testUser, null, 5L));
            assertEquals("Stock cannot be null", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }
    }

    @Nested
    class ExecuteTransactionsTests {
        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any())).thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.executeTransactions(1L, BUY, List.of()));
            assertEquals("User not found.", exception.getMessage());
            verifyNoInteractions(mockStockService);
        }

        @Test
        void testEmptyList() {
            when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
            List<TransactionResultDTO> results = transactionService.executeTransactions(1L, BUY, List.of());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            assertTrue(results.isEmpty());
            List<Holding> holdings = testUser.getHoldings();
            assertTrue(holdings.isEmpty());
            verifyNoInteractions(mockStockService);
        }

        @Nested
        class testBuy {

            @Test
            void testSuccessPartial() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, bananaTransaction, invalidTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "BANANA", new StockResultDTO(null, "Invalid symbol"), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransaction);
                assertEquals("Invalid symbol", transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), invalidTransaction);
                assertEquals("Transaction must be a positive number of shares.", transactions.get(2).error());

                assertEquals(BigDecimal.valueOf(80), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(1, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(5L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNull(microsoftHolding);
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
            }

            @Test
            void testSuccessAll() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, microsoftTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(70), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(5L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(2L, microsoftHolding.getShares());
            }

            @Test
            void testInsufficientFunds() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, insufficientFundsTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "MSFT", new StockResultDTO(appleStock, null)));
                InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> transactionService.executeTransactions(1L, BUY, transactionRequests));
                assertEquals("Insufficient funds for all transactions", exception.getMessage());
                verifyNoInteractions(mockTransactionRepo);
                verify(mockUserRepo, never()).save(any());
                assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertTrue(holdings.isEmpty());
            }

            @Test
            void testCumulative() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, microsoftTransaction, appleTransactionSecond);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), appleTransactionSecond);
                assertNull(transactions.get(2).error());
                assertEquals(BigDecimal.valueOf(58), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(8L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(2L, microsoftHolding.getShares());
            }
        }

        @Nested
        class testSell {

            @BeforeEach
            void setUpSell() {
                Holding appleHolding = new Holding(testUser, "AAPL", "Apple", 10L);
                Holding microsoftHolding = new Holding(testUser, "MSFT", "Microsoft", 10L);
                testUser.addHolding(appleHolding);
                testUser.addHolding(microsoftHolding);
            }

            @Test
            void testSuccessAll() {
                List<TransactionDTO> transactionRequests = List.of(
                        appleTransaction, microsoftTransaction
                );

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(130), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(5L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(8L, microsoftHolding.getShares());
            }

            @Test
            void testSuccessPartial() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, bananaTransaction, invalidTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "BANANA", new StockResultDTO(null, "Invalid symbol"), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransaction);
                assertEquals("Invalid symbol", transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), invalidTransaction);
                assertEquals("Transaction must be a positive number of shares.", transactions.get(2).error());

                assertEquals(BigDecimal.valueOf(120), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(5L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(10L, microsoftHolding.getShares());
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
            }

            @Test
            void testCumulative() {
                List<TransactionDTO> transactionRequests = List.of(appleTransaction, appleTransaction, microsoftTransaction, microsoftTransactionSecond);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                when(mockStockService.fetchPrices(any())).thenReturn(Map.of("AAPL", new StockResultDTO(appleStock, null), "MSFT", new StockResultDTO(microsoftStock, null)));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(4, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransaction);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), appleTransaction);
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), microsoftTransaction);
                assertNull(transactions.get(2).error());
                assertEquals(transactions.get(3).transaction(), microsoftTransactionSecond);
                assertNull(transactions.get(3).error());
                assertEquals(BigDecimal.valueOf(175), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNull(appleHolding);
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(3L, microsoftHolding.getShares());
            }
        }
    }
}
