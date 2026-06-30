package finance.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserFlowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private TestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils = new TestUtils(restTemplate);
    }


    @Test
    void registerAndLogin() {
        assertEquals(HttpStatus.CREATED, testUtils.register().getStatusCode());
        assertEquals(HttpStatus.OK, testUtils.login().getStatusCode());
    }
}
