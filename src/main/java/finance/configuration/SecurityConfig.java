package finance.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/prices").permitAll()
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/logout").permitAll()
                        .requestMatchers("/api/deposit").permitAll()
                        .requestMatchers("/api/withdraw").permitAll()
                        .requestMatchers("/api/balance").permitAll()
                        .requestMatchers("/api/buy").permitAll()
                        .requestMatchers("/api/sell").permitAll()
                        .requestMatchers("/api/transactions").permitAll()
                        .requestMatchers("/api/holdings").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(withDefaults -> {});
        return http.build();
    }
}