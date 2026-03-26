package finance.controllers;

import finance.dto.ErrorDTO;
import finance.dto.RegisterDTO;
import finance.dto.UserDTO;
import finance.entity.User;
import finance.exceptions.MissingRequestBodyException;
import finance.exceptions.PasswordException;
import finance.exceptions.RegistrationException;
import finance.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "api/register")
    public ResponseEntity<UserDTO> registerUser (@RequestBody(required = false) @Valid RegisterDTO user){
        if (user == null) throw new MissingRequestBodyException("No requests body provided");
        User newUser = userService.register(user.username(), user.email(), user.firstName(), user.lastName(), user.password(), user.confirmPassword());
        return ResponseEntity.status(201).body(new UserDTO(newUser));
    }


    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorDTO> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", ex.getMessage()));
    }
    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<ErrorDTO> handlePasswordException(PasswordException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", ex.getMessage()));
    }

}
