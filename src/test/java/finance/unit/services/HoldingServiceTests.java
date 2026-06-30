package finance.unit.services;

import finance.dtos.HoldingDTO;
import finance.entities.Holding;
import finance.entities.User;
import finance.repositories.HoldingRepository;
import finance.repositories.UserRepository;
import finance.services.HoldingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class HoldingServiceTests {
    private UserRepository mockUserRepo;
    private HoldingRepository mockHoldingRepo;
    private HoldingService holdingService;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockUserRepo = mock(UserRepository.class);
        mockHoldingRepo = mock(HoldingRepository.class);
        holdingService = new HoldingService(mockHoldingRepo, mockUserRepo);
        testUser = new User("testuser", "testuser@test.com", "test", "user", "test_hash", BigDecimal.valueOf(100));
    }

    @Test
    void testUserNotFound() {
        when(mockUserRepo.findById(any())).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> holdingService.fetchHoldings(1L, PageRequest.of(0, 10)));
        assertEquals("User not found.", exception.getMessage());
        verifyNoInteractions(mockHoldingRepo);
    }

    @Test
    void testSuccess() {
        Holding appleHolding = new Holding(testUser, "AAPL", "Apple", 40L);
        Holding microsoftHolding = new Holding(testUser, "MSFT", "Microsoft", 50L);


        when(mockUserRepo.findById(any())).thenReturn(Optional.of(testUser));
        when(mockHoldingRepo.findByIdUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(appleHolding, microsoftHolding)));
        List<HoldingDTO> result = holdingService.fetchHoldings(1L, PageRequest.of(0, 10)).getContent();

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals("MSFT", result.get(1).symbol());
    }
}
