package finance.controllers;

import finance.dtos.AmountDTO;
import finance.dtos.UserDTO;
import finance.entities.User;
import finance.services.FundingService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static finance.controllers.ControllerUtils.authenticateUser;
import static finance.controllers.ControllerUtils.checkRequestAllowed;

@RestController
public class FundingController {
    private final FundingService accountService;
    private final RateLimiter rateLimiter;

    public FundingController(FundingService accountService, @Qualifier("tradeRateLimiter") RateLimiter rateLimiter) {
        this.accountService = accountService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping(value = "/api/deposit")
    ResponseEntity<UserDTO> deposit(@Valid @RequestBody AmountDTO request, HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        checkRequestAllowed(rateLimiter, String.valueOf(sessionUser.id()));
        User user = accountService.deposit(sessionUser.id(), request.amount());
        return ResponseEntity.ok().body(new UserDTO(user));
    }

    @PostMapping(value = "/api/withdraw")
    ResponseEntity<UserDTO> withdraw(@Valid @RequestBody AmountDTO request, HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        checkRequestAllowed(rateLimiter, String.valueOf(sessionUser.id()));
        User user = accountService.withdraw(sessionUser.id(), request.amount());
        return ResponseEntity.ok().body(new UserDTO(user));
    }

    @GetMapping(value = "/api/balance")
    ResponseEntity<BigDecimal> getBalance(HttpSession session) {
        SessionUser sessionUser = authenticateUser(session);
        checkRequestAllowed(rateLimiter, String.valueOf(sessionUser.id()));
        BigDecimal balance = accountService.getBalance(sessionUser.id());
        return ResponseEntity.ok().body(balance);
    }
}
