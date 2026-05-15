package finance.controllers;

import finance.dtos.ErrorDTO;
import finance.dtos.LoginDTO;
import finance.dtos.RegisterDTO;
import finance.dtos.UserDTO;
import finance.entities.User;
import finance.exceptions.*;
import finance.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/api/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody @Valid RegisterDTO user, HttpSession session) {
        if (session.getAttribute("USER_SESSION") != null) throw new ForbiddenException("Already registered");
        User newUser = userService.register(user.username(), user.email(), user.firstName(), user.lastName(), user.password(), user.confirmPassword());
        return ResponseEntity.status(201).body(new UserDTO(newUser));
    }

    @PostMapping(value = "/api/login")
    public ResponseEntity<UserDTO> login(@RequestBody @Valid LoginDTO user, HttpSession session) {
        if (session.getAttribute("USER_SESSION") != null) throw new ForbiddenException("Already logged in");
        User activeUser = userService.login(user.username(), user.password());
        session.setAttribute("USER_SESSION", activeUser);
        return ResponseEntity.status(200).body(new UserDTO(activeUser));
    }

    @PostMapping(value = "/api/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.removeAttribute("USER_SESSION");
        return ResponseEntity.status(200).body("Logged out");
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorDTO> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(422).body(new ErrorDTO("UNPROCESSABLE_ENTITY", ex.getMessage()));
    }
}
