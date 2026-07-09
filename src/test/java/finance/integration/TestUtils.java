package finance.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import finance.dtos.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class TestUtils {
    protected RegisterDTO newUser = new RegisterDTO("testuser", "testuser@test.com", "test", "user", "password123!", "password123!");
    protected LoginDTO loginUser = new LoginDTO("testuser", "password123!");

    private final TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String STOCK_URL = "https://finance.cs50.io/quote?symbol=";

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
        String setCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        if (setCookie == null) {
            throw new IllegalStateException("Login response did not contain a session cookie");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, setCookie.split(";", 2)[0]);
        return headers;
    }

    public ResponseEntity<UserDTO> deposit(BigDecimal amount, HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/deposit",
                HttpMethod.POST,
                new HttpEntity<>(new AmountDTO(amount), headers),
                UserDTO.class
        );
    }

    public ResponseEntity<String> withdraw(BigDecimal amount, HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/withdraw",
                HttpMethod.POST,
                new HttpEntity<>(new AmountDTO(amount), headers),
                String.class
        );
    }

    public ResponseEntity<UserDTO> getBalance(HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/balance",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDTO.class
        );
    }

    public ResponseEntity<TransactionResponseDTO> buyShares(List<TransactionRequestDTO> transactions, HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/buy",
                HttpMethod.POST,
                new HttpEntity<>(transactions, headers),
                TransactionResponseDTO.class
        );
    }

    public ResponseEntity<TransactionResponseDTO> sellShares(List<TransactionRequestDTO> transactions, HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/sell",
                HttpMethod.POST,
                new HttpEntity<>(transactions, headers),
                TransactionResponseDTO.class
        );
    }

    public ResponseEntity<PageResponse<TransactionDTO>> getTransactions(HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/transactions",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public ResponseEntity<PageResponse<HoldingDTO>> getHoldings(HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/holdings",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public void mockStockPrice(MockRestServiceServer mockServer, StockDTO stock, ExpectedCount count) {
        try {
            mockServer.expect(count, requestTo(STOCK_URL + stock.symbol()))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(stock),
                            MediaType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void mockStockPrice(MockRestServiceServer mockServer, StockDTO stock) {
        mockStockPrice(mockServer, stock, ExpectedCount.once());
    }

    public void mockStockError(MockRestServiceServer mockServer, String symbol, HttpStatus status) {
        mockServer.expect(requestTo(STOCK_URL + symbol))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(status));
    }
}
