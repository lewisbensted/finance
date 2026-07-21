package finance.controllers;

import finance.dtos.*;
import finance.services.TradingService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static finance.controllers.AuthUtils.authenticateUser;
import static finance.dtos.ErrorCode.OPERATION_FAILED;
import static finance.dtos.ErrorCode.OPERATION_PARTIALLY_FAILED;
import static finance.entities.TransactionType.BUY;
import static finance.entities.TransactionType.SELL;

@RestController
public class TradingController {
        TradingService transactionService;

        TradingController(TradingService transactionService) {
                this.transactionService = transactionService;
        }

        @PostMapping(value = "/api/buy")
        ResponseEntity<ApiResponse<TransactionResponseDTO>> buyStocks(HttpSession session,
                        @RequestBody List<TransactionRequestDTO> transactions) {
                SessionUser sessionUser = authenticateUser(session);

                Map<String, ItemErrorDTO> errorFields = new HashMap<>();
                Map<String, TransactionRequestDTO> successful = new HashMap<>();
                TransactionExecutionResult transactionResults = transactionService.executeTransactions(sessionUser.id(),
                                BUY,
                                transactions);

                for (TransactionResultDTO transactionResult : transactionResults.transactions()) {
                        String symbol = transactionResult.transaction().symbol();
                        if (transactionResult.error() != null)
                                errorFields.put(symbol, transactionResult.error());
                        else
                                successful.put(symbol, transactionResult.transaction());
                }

                boolean allFailed = successful.isEmpty();
                return ResponseEntity.status(allFailed ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK)
                                .body(new ApiResponse<>(new TransactionResponseDTO(successful, transactionResults.balance()), errorFields.isEmpty()
                                                ? null
                                                : new ErrorDTO(allFailed ? OPERATION_FAILED
                                                                : OPERATION_PARTIALLY_FAILED,
                                                                String.format("Failed to execute %s transactions",
                                                                                allFailed ? "all" : "some"),
                                                                errorFields)));

        }

        @PostMapping(value = "/api/sell")
        ResponseEntity<ApiResponse<TransactionResponseDTO>> sellStocks(HttpSession session,
                        @RequestBody List<TransactionRequestDTO> transactions) {
                SessionUser sessionUser = authenticateUser(session);

                Map<String, ItemErrorDTO> errorFields = new HashMap<>();
                Map<String, TransactionRequestDTO> successful = new HashMap<>();
                TransactionExecutionResult transactionResults = transactionService.executeTransactions(sessionUser.id(),
                                SELL,
                                transactions);

                for (TransactionResultDTO transactionResult : transactionResults.transactions()) {
                        String symbol = transactionResult.transaction().symbol();
                        if (transactionResult.error() != null)
                                errorFields.put(transactionResult.transaction().symbol(), transactionResult.error());
                        else
                                successful.put(symbol, transactionResult.transaction());
                }

                boolean allFailed = successful.isEmpty();
                return ResponseEntity.status(allFailed ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.OK)
                                .body(new ApiResponse<>(new TransactionResponseDTO(successful, transactionResults.balance()),
                                                errorFields.isEmpty() ? null
                                                                : new ErrorDTO(allFailed ? OPERATION_FAILED
                                                                                : OPERATION_PARTIALLY_FAILED,
                                                                                String.format("Failed to execute %s transactions",
                                                                                                allFailed ? "all"
                                                                                                                : "some"),
                                                                                errorFields)));
        }

        @GetMapping(value = "/api/transactions")
        ResponseEntity<PageResponse<TransactionDTO>> fetchTransactions(HttpSession session,
                        @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                        @PageableDefault(size = 10) Pageable pageable) {
                SessionUser sessionUser = authenticateUser(session);

                Pageable safePageable = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                Sort.by(direction, "id"));

                Page<TransactionDTO> transactions = transactionService.fetchTransactions(sessionUser.id(),
                                safePageable);
                return ResponseEntity.ok().body(PageResponse.from(transactions));
        }
}
