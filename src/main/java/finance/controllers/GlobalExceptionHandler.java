package finance.controllers;

import finance.dto.ErrorDTO;
import finance.exceptions.AuthenticationException;
import finance.exceptions.ForbiddenException;
import finance.exceptions.MissingRequestBodyException;
import finance.exceptions.UnauthorisedException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingRequestBodyException.class)
    public ResponseEntity<ErrorDTO> handleMissingBody(MissingRequestBodyException ex) {
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", "Empty Request Body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMissingBody(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.putIfAbsent(error.getField(), new ArrayList<>());
            fieldErrors.get(error.getField()).add(error.getDefaultMessage());
        });
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", "Validation Error(s)", fieldErrors));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDTO> ForbiddenException(ForbiddenException ex) {
        return ResponseEntity.status(403).body(new ErrorDTO("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<ErrorDTO> UnauthorisedException(UnauthorisedException ex) {
        return ResponseEntity.status(401).body(new ErrorDTO("UNAUTHORISED", ex.getMessage()));
    }
}
