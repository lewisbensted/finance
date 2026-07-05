package finance.e2e;

import finance.dtos.LoginDTO;
import finance.dtos.RegisterDTO;
import finance.dtos.UserDTO;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Objects;

public class TestUtils {
    protected RegisterDTO newUser = new RegisterDTO("testuser", "testuser@test.com", "test", "user", "password123!", "password123!");
    protected LoginDTO loginUser = new LoginDTO("testuser", "password123!");

    private final TestRestTemplate restTemplate;

    public TestUtils(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<UserDTO> register(RegisterDTO registerBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterDTO> entity = new HttpEntity<>(registerBody, headers);

        return restTemplate.exchange(
                "/api/register",
                HttpMethod.POST,
                entity,
                UserDTO.class
        );
    }

    protected ResponseEntity<UserDTO> login(LoginDTO loginBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginBody, headers);

        return restTemplate.exchange(
                "/api/login",
                HttpMethod.POST,
                entity,
                UserDTO.class
        );
    }

    protected HttpHeaders authenticateHeaders(ResponseEntity<UserDTO> loginResponse) {
        String sessionCookie = Objects.requireNonNull(loginResponse.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE))
                .split(";", 2)[0];

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        return headers;
    }
}
