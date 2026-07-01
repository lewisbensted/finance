package finance.e2e;

import finance.dtos.AmountDTO;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FundingFlowTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils = new TestUtils(restTemplate);
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void depositAndWithdraw() {

        testUtils.register();
        ResponseEntity<UserDTO> login = testUtils.login();

        String sessionCookie = Objects.requireNonNull(login.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE))
                .split(";", 2)[0];

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        headers.setContentType(MediaType.APPLICATION_JSON);

        AmountDTO depositBody = new AmountDTO(BigDecimal.valueOf(50));
        ResponseEntity<UserDTO> depositResponse = restTemplate.exchange(
                "/api/deposit",
                HttpMethod.POST,
                new HttpEntity<>(depositBody, headers),
                UserDTO.class
        );
        assertEquals(HttpStatus.OK, depositResponse.getStatusCode());

        AmountDTO withdrawBody = new AmountDTO(BigDecimal.valueOf(20));
        ResponseEntity<UserDTO> withdrawResponse = restTemplate.exchange(
                "/api/withdraw",
                HttpMethod.POST,
                new HttpEntity<>(withdrawBody, headers),
                UserDTO.class
        );
        assertEquals(HttpStatus.OK, withdrawResponse.getStatusCode());

        ResponseEntity<UserDTO> balanceResponse = restTemplate.exchange(
                "/api/balance",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDTO.class
        );

        assertNotNull(balanceResponse.getBody());
        assertEquals(0, balanceResponse.getBody().balance().compareTo(BigDecimal.valueOf(30)));
    }
}
