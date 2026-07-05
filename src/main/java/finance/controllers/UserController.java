package finance.controllers;

import finance.dtos.*;
import finance.entities.User;
import finance.exceptions.*;
import finance.services.UserService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static finance.controllers.AuthUtils.authenticateUser;


@RestController
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/api/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid RegisterDTO user, HttpSession session) {
        if (session.getAttribute("USER_SESSION") != null)
            throw new ForbiddenException("Cannot register while logged in");
        User newUser = userService.register(user.username(), user.email(), user.firstName(), user.lastName(), user.password(), user.confirmPassword());
        return ResponseEntity.status(201).body(new UserDTO(newUser));
    }

    @PostMapping(value = "/api/login")
    public ResponseEntity<UserDTO> login(@RequestBody @Valid LoginDTO loginBody, HttpSession session) {
        if (session.getAttribute("USER_SESSION") != null) throw new ForbiddenException("Already logged in");

        User user = userService.login(loginBody.username(), loginBody.password());
        session.setAttribute("USER_SESSION", new SessionUser(
                user.getId(),
                user.getUsername()
        ));
        return ResponseEntity.status(200).body(new UserDTO(user));
    }

    @PostMapping(value = "/api/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.removeAttribute("USER_SESSION");
        return ResponseEntity.status(200).body("Logged out");
    }

    @PutMapping(value = "/api/password")
    public ResponseEntity<String> changePassword(HttpSession session, @RequestBody @Valid PasswordDTO body) {
        SessionUser sessionUser = authenticateUser(session);
        userService.changePassword(sessionUser.id(), body.password(), body.newPassword());
        return ResponseEntity.status(200).body("Password updated");
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorDTO> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(409).body(new ErrorDTO("CONFLICT", ex.getMessage()));
    }
}
