package finance.slice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance.controllers.FundingController;
import finance.dtos.AmountDTO;
import finance.entities.User;
import finance.services.AccountService;
import finance.session.SessionUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(FundingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FundingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    SessionUser testSessionUser = new SessionUser(1L, "testuser");

    User testUser = new User(
            "testuser",
            "testuser@test.com",
            "test",
            "user",
            "password_hash"
    );

    AmountDTO testAmount = new AmountDTO(BigDecimal.valueOf(10));

    @Nested
    class DepositTests {
        @Test
        void test400BadJSON() throws Exception {
            String badJson = """
                    {
                        "amount": "ten"
                    }
                    """;
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(badJson))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            when(accountService.deposit(anyLong(), any()))
                    .thenReturn(testUser);
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400InvalidRequest() throws Exception {
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(new AmountDTO(null))))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.amount").value("Amount must be positive"));
        }

        @Test
        void test200Success() throws Exception {
            when(accountService.deposit(anyLong(), any()))
                    .thenReturn(testUser);
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.username").value("testuser"));
        }
    }

    @Nested
    class WithdrawTests {
        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            when(accountService.withdraw(anyLong(), any()))
                    .thenReturn(testUser);
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400WithdrawFails() throws Exception {
            doThrow(new IllegalArgumentException("Insufficient funds"))
                    .when(accountService)
                    .withdraw(any(), any());
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Insufficient funds"));
        }

        @Test
        void test400InvalidRequest() throws Exception {
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser)
                            .content(objectMapper.writeValueAsString(new AmountDTO(BigDecimal.valueOf(-1)))))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.amount").value("Amount must be positive"));
        }

        @Test
        void test200Success() throws Exception {
            when(accountService.withdraw(anyLong(), any()))
                    .thenReturn(testUser);
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.username").value("testuser"));
        }
    }

    @Nested
    class BalanceTests {
        @Test
        void test401Unauthorised() throws Exception {
            mockMvc.perform(get("/api/balance")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test200Success() throws Exception {
            when(accountService.getBalance(anyLong()))
                    .thenReturn(testUser);
            mockMvc.perform(get("/api/balance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.username").value("testuser"));
        }
    }
}
