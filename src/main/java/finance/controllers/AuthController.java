package finance.controllers;

import finance.dtos.*;
import finance.entities.User;
import finance.exceptions.*;
import finance.services.UserService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static finance.controllers.ControllerUtils.authenticateUser;
import static finance.controllers.ControllerUtils.checkRequestAllowed;

@Controller
public class AuthController {
    public static final String USER_SESSION = "USER_SESSION";

    private final UserService userService;
    private final RateLimiter rateLimiter;

    public AuthController(
            UserService userService,
            @Qualifier("authRateLimiter") RateLimiter rateLimiter) {
        this.userService = userService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute(USER_SESSION) != null) {
            return "redirect:/search";
        }
        return "register";
    }

    @PostMapping(value = "/api/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterDTO body,
                                             HttpSession session, HttpServletRequest request) {
        checkRequestAllowed(rateLimiter, request.getRemoteAddr());
        if (session.getAttribute(USER_SESSION) != null)
            throw new ForbiddenException("Cannot register a new user while logged in");
        userService.register(body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(USER_SESSION) != null) {
            return "redirect:/search";
        }
        return "login";
    }

    @PostMapping(value = "/api/login")
    @ResponseBody
    public ResponseEntity<ErrorDTO> login(@RequestBody @Valid LoginDTO body, HttpSession session, HttpServletRequest request) {
        checkRequestAllowed(rateLimiter, request.getRemoteAddr());

        if (session.getAttribute(USER_SESSION) != null)
            throw new ForbiddenException("Already logged in");

        User user = userService.login(body);
        session.setAttribute(USER_SESSION, new SessionUser(
                user.getId(),
                user.getUsername()));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping(value = "/api/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping(value = "/api/password")
    public ResponseEntity<Void> changePassword(HttpSession session,
                                               @RequestBody @Valid PasswordDTO body, HttpServletRequest request) {
        checkRequestAllowed(rateLimiter, request.getRemoteAddr());
        SessionUser sessionUser = authenticateUser(session);
        userService.changePassword(sessionUser.id(), body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
