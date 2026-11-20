package finance.configuration;

import finance.controllers.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimiter authRateLimiter(Environment environment) {
        return new RateLimiter(5, Duration.ofMinutes(1), environment);
    }

    @Bean
    public RateLimiter tradeRateLimiter(Environment environment) {
        return new RateLimiter(10, Duration.ofMinutes(1), environment);
    }

    @Bean
    public RateLimiter queryRateLimiter(Environment environment) {
        return new RateLimiter(60, Duration.ofMinutes(1), environment);
    }
}
