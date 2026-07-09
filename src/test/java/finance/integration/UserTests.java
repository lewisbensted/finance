package finance.integration;

import finance.dtos.LoginDTO;
import finance.dtos.PasswordDTO;
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
public class UserTests {
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
    void protectedEndpointRequiresAuthentication(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> passwordResponse = restTemplate.exchange(
                "/api/password",
                HttpMethod.PUT,
                new HttpEntity<>(new PasswordDTO("old", "password321!"), headers),
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, passwordResponse.getStatusCode());
    }

    @Test
    void registerLoginAndChangePassword() {
        testUtils.register(testUtils.newUser);
        HttpHeaders headers = testUtils.authenticateHeaders(testUtils.login(testUtils.loginUser));
        ResponseEntity<String> passwordResponse = restTemplate.exchange(
                "/api/password",
                HttpMethod.PUT,
                new HttpEntity<>(new PasswordDTO("password123!", "password321!"), headers),
                String.class
        );
        assertEquals(HttpStatus.OK, passwordResponse.getStatusCode());
        restTemplate.exchange(
                "/api/logout",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, testUtils.login(testUtils.loginUser).getStatusCode());
        assertEquals(HttpStatus.OK, testUtils.login(new LoginDTO("testuser", "password321!")).getStatusCode());
    }
}
