package finance.controllers;

import finance.dto.StockResponseDTO;
import finance.entity.User;
import finance.services.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static finance.controllers.AuthUtils.authenticateUser;

@RestController
public class TransactionController {
    TransactionService transactionService;

    TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping
    ResponseEntity<StockResponseDTO> buyStocks(HttpSession session){
        User activeUser = authenticateUser(session);
        return null;
    }
}
