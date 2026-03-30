package finance.controllers;

import finance.entity.User;
import finance.exceptions.UnauthorisedException;
import jakarta.servlet.http.HttpSession;

public class AuthUtils {
    public static User authenticateUser(HttpSession session) {
        User activeUser = (User) session.getAttribute("USER_SESSION");
        if (session ==null ||  activeUser == null) {
            throw new UnauthorisedException("Not logged in.");
        }
        return activeUser;
    }
}
