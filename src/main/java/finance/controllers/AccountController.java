package finance.controllers;

import finance.dto.AmountDTO;
import finance.dto.UserDTO;
import finance.entity.User;
import finance.services.AccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static finance.controllers.AuthUtils.authenticateUser;

@RestController
public class AccountController {
    AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(value = "/api/deposit")
    ResponseEntity<UserDTO> deposit(@Valid @RequestBody AmountDTO request, HttpSession session) {
        User activeUser = authenticateUser(session);
        accountService.deposit(activeUser, request.amount());
        return ResponseEntity.status(200).body(new UserDTO(activeUser));
    }

    @PostMapping(value = "/api/withdraw")
    ResponseEntity<UserDTO> withdraw(@Valid @RequestBody AmountDTO request, HttpSession session) {
        User activeUser = authenticateUser(session);
        accountService.withdraw(activeUser, request.amount());
        return ResponseEntity.status(200).body(new UserDTO(activeUser));
    }

    @GetMapping(value = "/api/balance")
    ResponseEntity<UserDTO> getBalance(HttpSession session) {
        User activeUser = authenticateUser(session);
        return ResponseEntity.status(200).body(new UserDTO(activeUser));
    }
}
