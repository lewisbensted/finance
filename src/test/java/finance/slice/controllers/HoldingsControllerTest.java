package finance.slice.controllers;

import finance.controllers.HoldingController;
import finance.services.HoldingService;
import finance.session.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static finance.fixtures.HoldingFixtures.appleHolding;
import static finance.fixtures.HoldingFixtures.microsoftHolding;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
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

    SessionUser testSessionUser = new SessionUser(1L, "testuser");

    @Test
    void test200Success() throws Exception {
        when(holdingService.fetchHoldings(any(), any())).thenReturn(new PageImpl<>(List.of(appleHolding(40L), microsoftHolding(50L))));

        mockMvc.perform(get("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .sessionAttr("USER_SESSION", testSessionUser))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[*].symbol")
                        .value(containsInAnyOrder("AAPL", "MSFT")));
    }

    @Test
    void test401Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/holdings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("Not logged in"));
    }
}
