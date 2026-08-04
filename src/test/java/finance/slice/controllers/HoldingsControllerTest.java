package finance.slice.controllers;

import finance.controllers.HoldingController;
import finance.exceptions.InsufficientFundsException;
import finance.exceptions.NotFoundException;
import finance.services.HoldingService;
import finance.session.SessionUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static finance.fixtures.HoldingFixtures.appleHoldingDTO;
import static finance.fixtures.HoldingFixtures.microsoftHoldingDTO;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HoldingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HoldingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldingService holdingService;

    SessionUser testSessionUser = new SessionUser(1L, "testuser");

    @Nested
    class FetchHoldingsController {
        @Test
        void test200Success() throws Exception {
            when(holdingService.fetchHoldings(any(), any())).thenReturn(new PageImpl<>(List.of(appleHoldingDTO(40L), microsoftHoldingDTO(50L))));

            mockMvc.perform(get("/api/holdings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].symbol")
                            .value(containsInAnyOrder("AAPL", "MSFT")));
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(get("/api/holdings")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }
    }

    @Nested
    class FetchHoldingController {
        @Test
        void test200Success() throws Exception {
            when(holdingService.fetchHolding(any(), any())).thenReturn(appleHoldingDTO(40L));

            mockMvc.perform(get("/api/holding?symbol=AAPL")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.symbol").value("AAPL"))
                    .andExpect(jsonPath("$.shares").value("40"));
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(get("/api/holding?symbol=AAPL")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400NoSymbolProvided() throws Exception {

            mockMvc.perform(get("/api/holding")
                    .contentType(MediaType.APPLICATION_JSON)
                    .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Required request parameter 'symbol' for method parameter type String is not present"));
        }

        @Test
        void test400BlankSymbolProvided() throws Exception {
            mockMvc.perform(get("/api/holding")
                            .param("symbol","")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("fetchHolding.symbol: must not be blank"));
        }

        @Test
        void test404HoldingNotFound() throws Exception {
            doThrow(new NotFoundException("Holding not found"))
                    .when(holdingService)
                    .fetchHolding(any(), any());
            mockMvc.perform(get("/api/holding?symbol=AAPL")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Holding not found"));
        }
    }

    @Nested
    class PortfolioController {
        @Test
        void test401Redirect() throws Exception{
            mockMvc.perform(get("/portfolio"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/search"));
        }

        @Test
        void test200Success() throws Exception{
            when(holdingService.fetchHoldings(any(), any())).thenReturn(new PageImpl<>(List.of(appleHoldingDTO(40L), microsoftHoldingDTO(50L))));

            mockMvc.perform(get("/portfolio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isOk())
                    .andExpect(view().name("portfolio"))
                    .andExpect(model().attributeExists("holdings"))
                    .andExpect(model().attributeExists("holdingsList"));
        }
    }
}
