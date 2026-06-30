package finance.controllers;

import finance.entities.User;
import finance.exceptions.AuthorisationException;
import jakarta.servlet.http.HttpSession;

public class AuthUtils {
    public static Long authenticateUser(HttpSession session) {
        Long activeUser = (Long) session.getAttribute("USER_SESSION");
        if (activeUser == null) {
            throw new AuthorisationException("Not logged in");
        }
        return activeUser;
    }

    public static User getAuthenticatedUser(HttpSession session) {
        return (User) session.getAttribute("USER_SESSION");
    }
}
