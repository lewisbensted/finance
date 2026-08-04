package finance.unit.services;

import finance.dtos.*;
import finance.entities.Holding;
import finance.entities.Transaction;
import finance.entities.User;
import finance.exceptions.InsufficientFundsException;
import finance.exceptions.InsufficientSharesException;
import finance.repositories.TradingRepository;
import finance.repositories.UserRepository;
import finance.services.StockService;
import finance.services.TradingService;
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


import static finance.dtos.ErrorCode.*;
import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;
import static finance.fixtures.HoldingFixtures.appleHolding;
import static finance.fixtures.HoldingFixtures.microsoftHolding;
import static finance.fixtures.StockFixtures.*;
import static finance.fixtures.TradingFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TradingServiceTests {

    private UserRepository mockUserRepo;
    private TradingRepository mockTransactionRepo;
    private StockService mockStockService;
    private TradingService transactionService;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockTransactionRepo = mock(TradingRepository.class);
        mockStockService = mock(StockService.class);
        transactionService = new TradingService(mockUserRepo, mockTransactionRepo, mockStockService);

        testUser = new User("testuser", "testuser@test.com", "test", "user", "test_hash", BigDecimal.valueOf(100));
        testUser.addHolding(appleHolding(testUser, 5L));

        when(mockStockService.fetchPrices(any())).thenReturn(
                Map.of("AAPL", APPLE_STOCK_RESULT,
                        "BANANA", BANANA_STOCK_RESULT,
                        "MSFT", MICROSOFT_STOCK_RESULT));
    }

    @Nested
    class BuyTests {
        @Test
        void testSuccessNewHolding() {
            transactionService.buy(testUser, MICROSOFT_STOCK, 5L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(MICROSOFT_STOCK.latestPrice().multiply(BigDecimal.valueOf(5)));
            assertEquals(2, testUser.getHoldings().size());
            Holding holding = testUser.getHoldings().get(0);
            assertEquals("AAPL", holding.getSymbol());
            assertEquals(5L, holding.getShares());
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testSuccessExistingHolding() {
            transactionService.buy(testUser, APPLE_STOCK, 3L);

            BigDecimal expected = BigDecimal.valueOf(100).subtract(APPLE_STOCK.latestPrice().multiply(BigDecimal.valueOf(3)));
            Holding holding = testUser.getHoldings().get(0);
            assertEquals("AAPL", holding.getSymbol());
            assertEquals(8L, holding.getShares());
            assertEquals(expected, testUser.getBalance());

            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testInvalidQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.buy(testUser, APPLE_STOCK, -5L));
            assertEquals("Transaction must be a positive number of shares", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
            verify(mockUserRepo, never()).save(any());
        }

        @Test
        void testInsufficientFunds() {
            InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> transactionService.buy(testUser, APPLE_STOCK, 100L));
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
            transactionService.sell(testUser, APPLE_STOCK, 2L);
            BigDecimal expected = BigDecimal.valueOf(100).add(APPLE_STOCK.latestPrice().multiply(BigDecimal.valueOf(2)));
            assertEquals(expected, testUser.getBalance());
            assertEquals(1, testUser.getHoldings().size());
            assertEquals("AAPL", testUser.getHoldings().get(0).getSymbol());
            assertEquals(3L, testUser.getHoldings().get(0).getShares());
            verify(mockTransactionRepo).save(any());
            verify(mockUserRepo).save(any());
        }

        @Test
        void testSellAll() {
            transactionService.sell(testUser, APPLE_STOCK, 5L);
            BigDecimal expected = BigDecimal.valueOf(100).add(APPLE_STOCK.latestPrice().multiply(BigDecimal.valueOf(5)));
            assertEquals(expected, testUser.getBalance());
            assertTrue(testUser.getHoldings().isEmpty());
            verify(mockUserRepo).save(any());
            verify(mockTransactionRepo).save(any());
        }

        @Test
        void testInvalidQuantity() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.sell(testUser, APPLE_STOCK, -5L));
            assertEquals("Transaction must be a positive number of shares", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }


        @Test
        void testInvalidHolding() {
            InsufficientSharesException exception = assertThrows(InsufficientSharesException.class, () -> transactionService.sell(testUser, MICROSOFT_STOCK, 5L));
            assertEquals("Holding does not exist", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testInsufficientShares() {
            InsufficientSharesException exception = assertThrows(InsufficientSharesException.class, () -> transactionService.sell(testUser, APPLE_STOCK, 100L));
            assertEquals("Insufficient shares", exception.getMessage());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            verify(mockTransactionRepo, never()).save(any());
        }

        @Test
        void testNullStock() {
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
            TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, BUY, List.of());
            assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
            assertEquals(BigDecimal.valueOf(100), transactionResult.balance());
            assertTrue(transactionResult.transactions().isEmpty());
            List<Holding> holdings = testUser.getHoldings();
            assertEquals(1, holdings.size());
            verifyNoInteractions(mockStockService);
        }

        @Nested
        class testBuy {
            @Test
            void testSuccessPartial() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), bananaTransactionRequest(5L), microsoftTransactionRequest(-5L), new TransactionRequestDTO("GOOG", 10L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));

                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, BUY, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();

                assertEquals(4, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransactionRequest(5L));
                assertEquals(NOT_FOUND, transactions.get(1).error().code());
                assertEquals("Stock symbol not found", transactions.get(1).error().message());
                assertEquals(transactions.get(2).transaction(), microsoftTransactionRequest(-5L));
                assertEquals(INVALID_REQUEST, transactions.get(2).error().code());
                assertEquals("Transaction must be a positive number of shares", transactions.get(2).error().message());
                assertEquals(new TransactionRequestDTO("GOOG", 10L), transactions.get(3).transaction());
                assertEquals(INTERNAL_ERROR, transactions.get(3).error().code());
                assertEquals("No price returned", transactions.get(3).error().message());

                assertEquals(BigDecimal.valueOf(80), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(80), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(1, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(9L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNull(microsoftHolding);
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
                Holding googleHolding = holdings.stream().filter(h -> h.getSymbol().equals("GOOG")).findFirst().orElse(null);
                assertNull(googleHolding);
            }

            @Test
            void testSuccessAll() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), microsoftTransactionRequest(5L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));

                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, BUY, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();

                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransactionRequest(5L));
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(30), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(30), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(9L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(5L, microsoftHolding.getShares());
            }

            @Test
            void testInsufficientFunds() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), microsoftTransactionRequest(50L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> transactionService.executeTransactions(1L, BUY, transactionRequests));
                assertEquals("Insufficient funds for all transactions", exception.getMessage());
                verifyNoInteractions(mockTransactionRepo);
                verify(mockUserRepo, never()).save(any());
                assertEquals(BigDecimal.valueOf(100), testUser.getBalance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(1, holdings.size());
            }

            @Test
            void testCumulative() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), microsoftTransactionRequest(5L), appleTransactionRequest(4L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));

                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, BUY, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransactionRequest(5L));
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(2).error());
                assertEquals(BigDecimal.valueOf(10), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(10), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(13L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(5L, microsoftHolding.getShares());
            }
        }

        @Nested
        class testSell {

            @BeforeEach
            void setUpSell() {
                testUser.addHolding(microsoftHolding(testUser, 10L));
            }

            @Test
            void testSuccessAll() {
                List<TransactionRequestDTO> transactionRequests = List.of(
                        appleTransactionRequest(4L), microsoftTransactionRequest(5L)
                );

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, SELL, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();
                assertEquals(2, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransactionRequest(5L));
                assertNull(transactions.get(1).error());

                assertEquals(BigDecimal.valueOf(170), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(170), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(1L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(5L, microsoftHolding.getShares());
            }

            @Test
            void testSuccessPartial() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), bananaTransactionRequest(5L), microsoftTransactionRequest(-5L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, SELL, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), bananaTransactionRequest(5L));
                assertEquals(NOT_FOUND, transactions.get(1).error().code());
                assertEquals("Stock symbol not found", transactions.get(1).error().message());
                assertEquals(transactions.get(2).transaction(), microsoftTransactionRequest(-5L));
                assertEquals(INVALID_REQUEST, transactions.get(2).error().code());
                assertEquals("Transaction must be a positive number of shares", transactions.get(2).error().message());

                assertEquals(BigDecimal.valueOf(120), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(120), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                assertEquals(2, holdings.size());
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(1L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNotNull(microsoftHolding);
                assertEquals(10L, microsoftHolding.getShares());
                Holding bananaHolding = holdings.stream().filter(h -> h.getSymbol().equals("BANANA")).findFirst().orElse(null);
                assertNull(bananaHolding);
            }

            @Test
            void testCumulative() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), microsoftTransactionRequest(5L), microsoftTransactionRequest(5L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, SELL, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), microsoftTransactionRequest(5L));
                assertNull(transactions.get(1).error());
                assertEquals(transactions.get(2).transaction(), microsoftTransactionRequest(5L));
                assertNull(transactions.get(2).error());
                assertEquals(BigDecimal.valueOf(220), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(220), transactionResult.balance());
                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(1L, appleHolding.getShares());
                Holding microsoftHolding = holdings.stream().filter(h -> h.getSymbol().equals("MSFT")).findFirst().orElse(null);
                assertNull(microsoftHolding);
            }

            @Test
            void testCumulativeInsufficientShares() {
                List<TransactionRequestDTO> transactionRequests = List.of(appleTransactionRequest(4L), appleTransactionRequest(4L), appleTransactionRequest(4L));

                when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));

                TransactionExecutionResult transactionResult = transactionService.executeTransactions(1L, SELL, transactionRequests);
                List<TransactionResultDTO> transactions = transactionResult.transactions();
                assertEquals(3, transactions.size());
                assertEquals(transactions.get(0).transaction(), appleTransactionRequest(4L));
                assertNull(transactions.get(0).error());
                assertEquals(transactions.get(1).transaction(), appleTransactionRequest(4L));
                assertEquals(INSUFFICIENT_SHARES, transactions.get(1).error().code());
                assertEquals("Insufficient shares", transactions.get(1).error().message());
                assertEquals(transactions.get(2).transaction(), appleTransactionRequest(4L));
                assertEquals(INSUFFICIENT_SHARES, transactions.get(2).error().code());
                assertEquals("Insufficient shares", transactions.get(2).error().message());
                assertEquals(BigDecimal.valueOf(120), testUser.getBalance());
                assertEquals(BigDecimal.valueOf(120), transactionResult.balance());

                List<Holding> holdings = testUser.getHoldings();
                Holding appleHolding = holdings.stream().filter(h -> h.getSymbol().equals("AAPL")).findFirst().orElse(null);
                assertNotNull(appleHolding);
                assertEquals(1L, appleHolding.getShares());
            }
        }
    }

    @Nested
    class FetchTransactionsTests {
        @Test
        void testUserNotFound() {
            when(mockUserRepo.findById(any())).thenReturn(Optional.empty());
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.fetchTransactions(1L, PageRequest.of(0, 10)));
            assertEquals("User not found", exception.getMessage());
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
