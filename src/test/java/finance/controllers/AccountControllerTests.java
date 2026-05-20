package finance.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance.dtos.AmountDTO;
import finance.entities.User;
import finance.services.AccountService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

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
                            .sessionAttr("USER_SESSION", testUser)
                            .content(badJson))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            doNothing().when(accountService).deposit(anyLong(), any());
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400DepositFails() throws Exception {
            doThrow(new IllegalArgumentException("Amount must be positive"))
                    .when(accountService)
                    .deposit(any(), any());
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Amount must be positive"));
        }

        @Test
        void test200Success() throws Exception {
            doNothing().when(accountService).deposit(anyLong(), any());
            mockMvc.perform(post("/api/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser)
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
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthorised() throws Exception {
            doNothing().when(accountService).withdraw(anyLong(), any(BigDecimal.class));
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400WithdrawFails() throws Exception {
            doThrow(new IllegalArgumentException("Amount must be positive"))
                    .when(accountService)
                    .withdraw(any(), any());
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser)
                            .content(objectMapper.writeValueAsString(testAmount)))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Amount must be positive"));
        }

        @Test
        void test200Success() throws Exception {
            doNothing().when(accountService).withdraw(anyLong(), any());
            mockMvc.perform(post("/api/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser)
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
            mockMvc.perform(get("/api/balance")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testUser))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.username").value("testuser"));
        }
    }
}
