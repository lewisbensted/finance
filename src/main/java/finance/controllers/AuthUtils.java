package finance.controllers;

import finance.exceptions.AuthorisationException;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;

public class AuthUtils {
    public static SessionUser authenticateUser(HttpSession session) {
        SessionUser activeUser = (SessionUser) session.getAttribute("USER_SESSION");
        if (activeUser == null) {
            throw new AuthorisationException("Not logged in");
        }
        return activeUser;
    }
}
