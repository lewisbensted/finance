package finance.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance.dtos.ErrorDTO;
import finance.dtos.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FundingTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders headers;

    private TestUtils testUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("DELETE FROM users");

        testUtils = new TestUtils(restTemplate);
        testUtils.register(testUtils.newUser);
        headers = testUtils.authenticateHeaders(testUtils.login(testUtils.loginUser));

    }

    @Test
    void userCanDeposit() {
        testUtils.deposit(BigDecimal.valueOf(50), headers);
        ResponseEntity<UserDTO> depositResponse = testUtils.deposit(BigDecimal.valueOf(30), headers);
        assertEquals(HttpStatus.OK, depositResponse.getStatusCode());

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(80)));
    }

    @Test
    void userCanWithdraw() {
        testUtils.deposit(BigDecimal.valueOf(50), headers);
        ResponseEntity<String> withdrawResponse = testUtils.withdraw(BigDecimal.valueOf(30), headers);
        assertEquals(HttpStatus.OK, withdrawResponse.getStatusCode());

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(20)));
    }

    @Test
    void userCannotWithdrawMoreThanBalance() throws JsonProcessingException {

        testUtils.deposit(BigDecimal.valueOf(50), headers);
        ResponseEntity<String> withdrawResponse = testUtils.withdraw(BigDecimal.valueOf(60), headers);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, withdrawResponse.getStatusCode());
        ErrorDTO error = objectMapper.readValue(withdrawResponse.getBody(), ErrorDTO.class);
        assertEquals("Insufficient funds", error.message());

        ResponseEntity<BigDecimal> balanceResponse = testUtils.getBalance(headers);
        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().compareTo(BigDecimal.valueOf(50)));
    }
}
