package finance.e2e;

import finance.dtos.LoginDTO;
import finance.dtos.RegisterDTO;
import finance.dtos.UserDTO;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.List;

public class TestUtils {
    private final TestRestTemplate restTemplate;

    public TestUtils(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<UserDTO> register() {
        RegisterDTO registerBody = new RegisterDTO("testuser", "testuser@test.com", "test", "user", "password123!", "password123!");
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

    protected ResponseEntity<UserDTO> login() {
        LoginDTO loginBody = new LoginDTO("testuser", "password123!");
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
}
