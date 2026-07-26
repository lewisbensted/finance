package finance.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class PageExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleInvalidUrl() {
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralError() {
        return "error";
    }
}