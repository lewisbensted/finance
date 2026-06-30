package finance.controllers;

import finance.dtos.*;
import finance.entities.User;
import finance.exceptions.InsufficientFundsException;
import finance.exceptions.InsufficientSharesException;
import finance.services.TransactionService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static finance.controllers.AuthUtils.authenticateUser;
import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;

@RestController
public class TransactionController {
    TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/api/buy")
    ResponseEntity<TransactionResponseDTO> buyStocks(HttpSession session, @RequestBody List<TransactionRequestDTO> transactions) {
        SessionUser sessionUser = authenticateUser(session);

        Map<String, ItemErrorDTO> errorFields = new HashMap<>();
        List<TransactionRequestDTO> successful = new ArrayList<>();
        List<TransactionResultDTO> transactionResults = transactionService.executeTransactions(sessionUser.id(), BUY, transactions);

        for (TransactionResultDTO transactionResult : transactionResults) {
            if (transactionResult.error() != null)
                errorFields.put(transactionResult.transaction().symbol(), transactionResult.error());
            else successful.add(transactionResult.transaction());
        }

        boolean allFailed = successful.isEmpty();
        return ResponseEntity.status(allFailed ? 422 : 200).body(new TransactionResponseDTO(successful, errorFields.isEmpty() ? null : new ErrorDTO(allFailed ? "UNPROCESSABLE" : "PARTIAL_FAILURE", String.format("Failed to execute %s transactions", allFailed ? "all" : "some"), errorFields)));
    }

    @PostMapping(value = "/api/sell")
    ResponseEntity<TransactionResponseDTO> sellStocks(HttpSession session, @RequestBody List<TransactionRequestDTO> transactions) {
        SessionUser sessionUser = authenticateUser(session);

        Map<String, ItemErrorDTO> errorFields = new HashMap<>();
        List<TransactionRequestDTO> successful = new ArrayList<>();
        List<TransactionResultDTO> transactionResults = transactionService.executeTransactions(sessionUser.id(), SELL, transactions);

        for (TransactionResultDTO transactionResult : transactionResults) {
            if (transactionResult.error() != null)
                errorFields.put(transactionResult.transaction().symbol(), transactionResult.error());
            else successful.add(transactionResult.transaction());
        }

        boolean allFailed = successful.isEmpty();
        return ResponseEntity.status(allFailed ? 422 : 200).body(new TransactionResponseDTO(successful, errorFields.isEmpty() ? null : new ErrorDTO(allFailed ? "UNPROCESSABLE" : "PARTIAL_FAILURE", String.format("Failed to execute %s transactions", allFailed ? "all" : "some"), errorFields)));
    }

    @GetMapping(value = "/api/transactions")
    ResponseEntity<Page<TransactionDTO>> fetchTransactions(HttpSession session, @RequestParam(defaultValue = "DESC") Sort.Direction direction, @PageableDefault(
            size = 10
    ) Pageable pageable) {
        SessionUser sessionUser = authenticateUser(session);

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(direction, "createdAt")
        );

        Page<TransactionDTO> transactions = transactionService.fetchTransactions(sessionUser.id(), safePageable);
        return ResponseEntity.status(200).body(transactions);
    }


    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorDTO> handleInsufficientFundsException(InsufficientFundsException ex) {
        return ResponseEntity.status(422).body(new ErrorDTO("UNPROCESSABLE", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ErrorDTO> handleInsufficientSharesException(InsufficientSharesException ex) {
        return ResponseEntity.status(422).body(new ErrorDTO("UNPROCESSABLE", ex.getMessage()));
    }
}
