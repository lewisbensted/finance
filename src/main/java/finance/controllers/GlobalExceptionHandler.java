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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleMissingBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", "Empty Request Body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.putIfAbsent(error.getField(), new ArrayList<>());
            fieldErrors.get(error.getField()).add(error.getDefaultMessage());
        });
        return ResponseEntity.status(400).body(new ValidationErrorDTO("BAD_REQUEST", "Validation Error(s)", fieldErrors));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDTO> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(new ErrorDTO("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(AuthorisationException.class)
    public ResponseEntity<ErrorDTO> handleUnauthorised(AuthorisationException ex) {
        return ResponseEntity.status(401).body(new ErrorDTO("UNAUTHORISED", ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorDTO("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", ex.getMessage()));
    }
}
