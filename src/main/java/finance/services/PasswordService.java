package finance.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordService {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String hash(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean compare(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }
}
