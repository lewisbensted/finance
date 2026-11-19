package finance.configuration;

import finance.controllers.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiter loginRateLimiter(Environment environment) {
        return new RateLimiter(5, Duration.ofMinutes(1), environment);
    }
}
