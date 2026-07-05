package finance.e2e;

import finance.dtos.LoginDTO;
import finance.dtos.PasswordDTO;
import finance.dtos.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserFlowTest {
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
    void registerAndLogin() {
        assertEquals(HttpStatus.CREATED, testUtils.register(testUtils.newUser).getStatusCode());
        ResponseEntity<UserDTO> loginResponse = testUtils.login(testUtils.loginUser);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        HttpHeaders headers = testUtils.authenticateHeaders(loginResponse);
        restTemplate.exchange(
                "/api/password",
                HttpMethod.PUT,
                new HttpEntity<>(new PasswordDTO("password123!", "password321!"), headers),
                String.class
        );
        restTemplate.exchange(
                "/api/logout",
                HttpMethod.POST,
                null,
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, testUtils.login(testUtils.loginUser).getStatusCode());
        assertEquals(HttpStatus.OK, testUtils.login(new LoginDTO("testuser", "password321!")).getStatusCode());
    }
}
