package finance.controllers;

import finance.dto.ErrorDTO;
import finance.exceptions.MissingRequestBodyException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        Map<String,String> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.status(400).body(new ErrorDTO("BAD_REQUEST", "Validation Error(s)", fieldErrors));
    }
}
