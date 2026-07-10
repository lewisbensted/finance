package finance.controllers;

import finance.dtos.ErrorDTO;
import finance.dtos.ValidationErrorDTO;
import finance.exceptions.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static finance.dtos.ErrorCode.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO(INVALID_REQUEST, "Empty or unreadable request body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.putIfAbsent(error.getField(), new ArrayList<>());
            fieldErrors.get(error.getField()).add(error.getDefaultMessage());
        });
        return ResponseEntity.status(400).body(new ValidationErrorDTO(INVALID_REQUEST, "Validation error(s)", fieldErrors));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDTO> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(new ErrorDTO(FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handleUnauthenticated(AuthenticationException ex) {
        return ResponseEntity.status(401).body(new ErrorDTO(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorDTO(NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO(INVALID_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorDTO> handleInsufficientFundsException(InsufficientFundsException ex) {
        return ResponseEntity.status(422).body(new ErrorDTO(INSUFFICIENT_FUNDS, ex.getMessage()));
    }

    @ExceptionHandler(InsufficientSharesException.class)
    public ResponseEntity<ErrorDTO> handleInsufficientSharesException(InsufficientSharesException ex) {
        return ResponseEntity.status(422).body(new ErrorDTO(INSUFFICIENT_SHARES, ex.getMessage()));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ErrorDTO> handleRegistrationException(RegistrationException ex) {
        return ResponseEntity.status(409).body(new ErrorDTO(REGISTRATION_FAILED, ex.getMessage()));
    }
}
