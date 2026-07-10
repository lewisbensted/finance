package finance.controllers;

import finance.exceptions.AuthenticationException;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;

import static finance.dtos.ErrorCode.UNAUTHENTICATED;

public class AuthUtils {
    public static SessionUser authenticateUser(HttpSession session) {
        SessionUser activeUser = (SessionUser) session.getAttribute("USER_SESSION");
        if (activeUser == null) {
            throw new AuthenticationException(UNAUTHENTICATED, "Not logged in");
        }
        return activeUser;
    }
}
