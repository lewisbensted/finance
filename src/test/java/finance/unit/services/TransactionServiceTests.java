package finance.unit.services;

import finance.dtos.*;
import finance.entities.Holding;
import finance.entities.Transaction;
import finance.entities.User;
import finance.exceptions.InsufficientFundsException;
import finance.exceptions.InsufficientSharesException;
import finance.repositories.TransactionRepository;
import finance.repositories.UserRepository;
import finance.services.StockService;
import finance.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceTests {

    private UserRepository mockUserRepo;
    private TransactionRepository mockTransactionRepo;
    private StockService mockStockService;
    private TransactionService transactionService;
    private User testUser;

    StockDTO testStock = new StockDTO("Test", "TST", BigDecimal.valueOf(2));
    StockDTO appleStock = new StockDTO("Apple", "AAPL", BigDecimal.valueOf(4));
    StockDTO microsoftStock = new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(5));

    TransactionRequestDTO appleTransactionRequest = new TransactionRequestDTO("AAPL", 4L);
    TransactionRequestDTO bananaTransactionRequest = new TransactionRequestDTO("BANANA", 5L);
    TransactionRequestDTO microsoftTransaction = new TransactionRequestDTO("MSFT", 2L);
    TransactionRequestDTO invalidTransactionRequest = new TransactionRequestDTO("MSFT", -5L);
    TransactionRequestDTO appleTransactionSecondRequest = new TransactionRequestDTO("AAPL", 3L);
    TransactionRequestDTO microsoftTransactionSecond = new TransactionRequestDTO("MSFT", 8L);
    TransactionRequestDTO insufficientFundsTransaction = new TransactionRequestDTO("MSFT", 50L);
    TransactionRequestDTO googleTransactionRequest = new TransactionRequestDTO("GOOG", 10L);

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockTransactionRepo = mock(TransactionRepository.class);
        mockStockService = mock(StockService.class);
        transactionService = new TransactionService(mockUserRepo, mockTransactionRepo, mockStockService);
        testUser = new User("testuser", "testuser@test.com", "test", "user", "test_hash", BigDecimal.valueOf(100));
        when(mockStockService.fetchPrices(any())).thenReturn(
                Map.of("AAPL", new StockResultDTO(appleStock, null),
                        "BANANA", new StockResultDTO(null, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")),
                        "MSFT", new StockResultDTO(microsoftStock, null)));
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
            assertEquals("Transaction must be a positive number of shares", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testInsufficientFunds() {
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
            assertEquals("Transaction must be a positive number of shares", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }


        @Test
        void testInvalidHolding() {
            InsufficientSharesException exception = assertThrows(InsufficientSharesException.class, () -> transactionService.sell(testUser, testStock, 5L));
            assertEquals("Holding does not exist", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testInsufficientShares() {
            testUser.addHolding(new Holding(testUser, "TST", "Test", 5L));
            InsufficientSharesException exception = assertThrows(InsufficientSharesException.class, () -> transactionService.sell(testUser, testStock, 100L));
            assertEquals("Insufficient shares", exception.getMessage());
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
            assertEquals("User not found", exception.getMessage());
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
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, bananaTransactionRequest, invalidTransactionRequest, googleTransactionRequest);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(4, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransactionRequest);
                assertEquals("NOT_FOUND", transactions.get(1).error().code());
                assertEquals("Stock symbol not found", transactions.get(1).error().message());
                assertEquals(transactions.get(2).transaction(), invalidTransactionRequest);
                assertEquals("BAD_REQUEST", transactions.get(2).error().code());
                assertEquals("Transaction must be a positive number of shares", transactions.get(2).error().message());
                assertEquals(transactions.get(3).transaction(), googleTransactionRequest);
                assertEquals("INTERNAL_ERROR", transactions.get(3).error().code());
                assertEquals("No price returned", transactions.get(3).error().message());

                assertEquals(BigDecimal.valueOf(84), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(1, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(4L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNull(microsoftHolding);
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
            }

            @Test
            void testSuccessAll() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, microsoftTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(74), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(4L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(2L, microsoftHolding.getShares());
            }

            @Test
            void testInsufficientFunds() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, insufficientFundsTransaction);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
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
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, microsoftTransaction, appleTransactionSecondRequest);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, BUY, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), appleTransactionSecondRequest);
                assertNull(transactions.get(2).error());
                assertEquals(BigDecimal.valueOf(62), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(7L, appleHolding.getShares());
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
                List<TransactionRequestDTO> transactionRequests = List.of(
                        appleTransactionRequest, microsoftTransaction
                );

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransaction);
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(126), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(6L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(8L, microsoftHolding.getShares());
            }

            @Test
            void testSuccessPartial() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, bananaTransactionRequest, invalidTransactionRequest);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransactionRequest);
                assertEquals("NOT_FOUND", transactions.get(1).error().code());
                assertEquals("Stock symbol not found", transactions.get(1).error().message());
                assertEquals(transactions.get(2).transaction(), invalidTransactionRequest);
                assertEquals("BAD_REQUEST", transactions.get(2).error().code());
                assertEquals("Transaction must be a positive number of shares", transactions.get(2).error().message());

                assertEquals(BigDecimal.valueOf(116), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(6L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(10L, microsoftHolding.getShares());
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
            }

            @Test
            void testCumulative() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, appleTransactionRequest, microsoftTransaction, microsoftTransactionSecond);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(4, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), appleTransactionRequest);
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), microsoftTransaction);
                assertNull(transactions.get(2).error());
                assertEquals(transactions.get(3).transaction(), microsoftTransactionSecond);
                assertNull(transactions.get(3).error());
                assertEquals(BigDecimal.valueOf(182), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(2L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNull(microsoftHolding);
            }

            @Test
            void testCumulativeInsufficientShares() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest, appleTransactionRequest, appleTransactionRequest);

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));

                List<TransactionResultDTO> transactions = transactionService.executeTransactions(1L, SELL, transactionRequests);
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest);
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), appleTransactionRequest);
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), appleTransactionRequest);
                assertEquals("UNPROCESSABLE", transactions.get(2).error().code());
                assertEquals("Insufficient shares", transactions.get(2).error().message());
                assertEquals(BigDecimal.valueOf(132), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(2L, appleHolding.getShares());
            }
        }
    }

    @Nested
    class FetchTransactionsTests {
        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any())).thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.fetchTransactions(1L, PageRequest.of(0, 10)));
            assertEquals("User not found.", exception.getMessage());
            verifyNoInteractions(mockTransactionRepo);
        }

        @Test
        void testSuccess() {
            Transaction appleTransaction = new Transaction(testUser, "AAPL", "Apple", 5L, BigDecimal.valueOf(20), BUY, LocalDateTime.of(2024, 1, 1, 12, 0));
            Transaction microsoftTransaction = new Transaction(testUser, "MSFT", "Microsoft", 5L, BigDecimal.valueOf(20), BUY, LocalDateTime.of(2024, 1, 1, 12, 1));

            when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
            when(mockTransactionRepo.findByUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(appleTransaction, microsoftTransaction)));
            List<TransactionDTO> result = transactionService.fetchTransactions(1L, PageRequest.of(0, 10)).getContent();

            assertEquals(2, result.size());
            assertEquals("AAPL", result.get(0).symbol());
            assertEquals("MSFT", result.get(1).symbol());
        }
    }
}
