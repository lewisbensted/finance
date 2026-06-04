package finance.controllers;

import finance.dtos.HoldingDTO;
import finance.dtos.TransactionDTO;
import finance.entities.Holding;
import finance.entities.User;
import finance.services.HoldingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static finance.entities.TransactionType.BUY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HoldingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HoldingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldingService holdingService;

    User testUser = new User(
            "testuser",
            "testuser@test.com",
            "test",
            "user",
            "password_hash"
    );

    @Test
    void test200Success() throws Exception {
        HoldingDTO appleHolding = new HoldingDTO("AAPL", "Apple", 40L);
        HoldingDTO microsoftHolding = new HoldingDTO("MSFT", "Microsoft", 50L);
        when(holdingService.fetchHoldings(any(), any())).thenReturn(new PageImpl<>(List.of(appleHolding, microsoftHolding)));

        mockMvc.perform(get("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("USER_SESSION", testUser))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[*].symbol")
                        .value(containsInAnyOrder("AAPL", "MSFT")));
    }

    @Test
    void test401Unauthorised() throws Exception {
        mockMvc.perform(get("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }
}
