package finance.controllers;

import finance.dtos.AmountDTO;
import finance.dtos.UserDTO;
import finance.entities.User;
import finance.services.FundingService;
import finance.session.SessionUser;
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
    FundingService accountService;

    public FundingController(FundingService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(value = "/api/deposit")
    ResponseEntity<UserDTO> deposit(@Valid @RequestBody AmountDTO request, HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        User user = accountService.deposit(sessionUser.id(), request.amount());
        return ResponseEntity.ok().body(new UserDTO(user));
    }

    @PostMapping(value = "/api/withdraw")
    ResponseEntity<UserDTO> withdraw(@Valid @RequestBody AmountDTO request, HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        User user = accountService.withdraw(sessionUser.id(), request.amount());
        return ResponseEntity.ok().body(new UserDTO(user));
    }

    @GetMapping(value = "/api/balance")
    ResponseEntity<UserDTO> getBalance(HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        User user = accountService.getBalance(sessionUser.id());
        return ResponseEntity.ok().body(new UserDTO(user));
    }
}
