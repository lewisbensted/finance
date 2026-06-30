package finance.controllers;

import finance.dtos.AmountDTO;
import finance.dtos.UserDTO;
import finance.entities.User;
import finance.services.AccountService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static finance.controllers.AuthUtils.authenticateUser;

@RestController
public class FundingController {
    AccountService accountService;

    public FundingController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(value = "/api/deposit")
    ResponseEntity<UserDTO> deposit(@Valid @RequestBody AmountDTO request, HttpSession session) {
        Long activeUserId = authenticateUser(session);
        User user = accountService.deposit(activeUserId, request.amount());
        return ResponseEntity.status(200).body(new UserDTO(user));
    }

    @PostMapping(value = "/api/withdraw")
    ResponseEntity<UserDTO> withdraw(@Valid @RequestBody AmountDTO request, HttpSession session) {
        Long activeUserId = authenticateUser(session);
        User user = accountService.withdraw(activeUserId, request.amount());
        return ResponseEntity.status(200).body(new UserDTO(user));
    }

    @GetMapping(value = "/api/balance")
    ResponseEntity<UserDTO> getBalance(HttpSession session) {
        Long activeUserId = authenticateUser(session);
        User user = accountService.getBalance(activeUserId);
        return ResponseEntity.status(200).body(new UserDTO(user));
    }
}
