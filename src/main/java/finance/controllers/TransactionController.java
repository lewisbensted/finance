package finance.controllers;

import finance.dto.*;
import finance.entity.User;
import finance.exceptions.InsufficientFundsException;
import finance.services.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static finance.controllers.AuthUtils.authenticateUser;
import static finance.entity.TransactionType.BUY;
import static finance.entity.TransactionType.SELL;

@RestController
public class TransactionController {
    TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value ="/api/buy")
    ResponseEntity<TransactionResponseDTO> buyStocks(HttpSession session, @RequestBody List<TransactionDTO> transactions) {
        User activeUser = authenticateUser(session);

        Map<String, List<String>> errorFields = new HashMap<>();
        List<TransactionDTO> successful = new ArrayList<>();
        List<TransactionResultDTO> transactionResults = transactionService.executeTransactions(activeUser.getId(), BUY, transactions);

        for (TransactionResultDTO transactionResult : transactionResults) {
            if (transactionResult.error() != null)
                errorFields.put(transactionResult.transaction().symbol(), List.of(transactionResult.error()));
            else successful.add(transactionResult.transaction());
        }

        boolean allFailed = successful.isEmpty();
        return ResponseEntity.status(allFailed ? 422 : 200).body(new TransactionResponseDTO(successful, errorFields.isEmpty() ? null : new ErrorDTO("PARTIAL_FAILURE", String.format("Failed to execute %s transactions", allFailed ? "all" : "some"), errorFields)));
    }

    @PostMapping(value ="/api/sell")
    ResponseEntity<TransactionResponseDTO> sellStocks(HttpSession session, @RequestBody List<TransactionDTO> transactions) {
        User activeUser = authenticateUser(session);

        Map<String, List<String>> errorFields = new HashMap<>();
        List<TransactionDTO> successful = new ArrayList<>();
        List<TransactionResultDTO> transactionResults = transactionService.executeTransactions(activeUser.getId(), SELL, transactions);

        for (TransactionResultDTO transactionResult : transactionResults) {
            if (transactionResult.error() != null)
                errorFields.put(transactionResult.transaction().symbol(), List.of(transactionResult.error()));
            else successful.add(transactionResult.transaction());
        }

        boolean allFailed = successful.isEmpty();
        return ResponseEntity.status(allFailed ? 422 : 200).body(new TransactionResponseDTO(successful, errorFields.isEmpty() ? null : new ErrorDTO("PARTIAL_FAILURE", String.format("Failed to execute %s transactions", allFailed ? "all" : "some"), errorFields)));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorDTO> handleInsufficientFundsException(InsufficientFundsException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", ex.getMessage()));
    }
}
