package finance.controllers;

import finance.entities.User;
import finance.exceptions.AuthorisationException;
import jakarta.servlet.http.HttpSession;

public class AuthUtils {
    public static User authenticateUser(HttpSession session) {
        User activeUser = (User) session.getAttribute("USER_SESSION");
        if (activeUser == null) {
            throw new AuthorisationException("Not logged in");
        }
        return activeUser;
    }
}
