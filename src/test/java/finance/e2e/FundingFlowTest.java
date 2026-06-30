package finance.e2e;

import finance.dtos.AmountDTO;
import finance.dtos.UserDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FundingFlowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils = new TestUtils(restTemplate);
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
        Assertions.assertNotNull(balanceResponse.getBody());
        assertEquals(BigDecimal.valueOf(30), balanceResponse.getBody().balance());
    }
}
