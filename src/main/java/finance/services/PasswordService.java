package finance.services;

import finance.exceptions.PasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordService {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    static void validatePassword(String password) {
        String reg1 = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[$&+,:;=?@#|'<>.^*()%!\\-/]).+$";
        String reg2 = "\\S*$";
        if (!password.matches(reg2)) throw new PasswordException("Password cannot contain spaces.");
        if (!password.matches(reg1))
            throw new PasswordException("Password must contain a number, letter and special character");
    }

    public static String hash(String password){
        return passwordEncoder.encode(password);
    }

    public static boolean compare(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }
}
