package com.organization.query_engine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("Validation Exception", ex);
        ProblemDetail problemDetail =
                buildProblemDetail(
                        "Validation Error",
                        HttpStatus.BAD_REQUEST,
                        "Validation failed");

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> {
                            String fieldName =
                                    error instanceof FieldError
                                            ? ((FieldError) error).getField()
                                            : error.getObjectName();
                            String errorMessage = error.getDefaultMessage();
                            validationErrors.put(fieldName, errorMessage);
                        });
        problemDetail.setProperty("errors", validationErrors);
        return ResponseEntity.badRequest().body(problemDetail);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ProblemDetail> handleAllUncaughtException(Exception ex) {
        log.error("Internal Server Error", ex);
        ProblemDetail problemDetail =
                buildProblemDetail(
                        "Internal Server Error",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(NotFoundException ex) {
        log.error("User not found exception", ex);
        ProblemDetail problemDetail =
                buildProblemDetail(
                        "User not found exception",
                        HttpStatus.NOT_FOUND,
                        ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(ClientException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ProblemDetail> handleClientException(ClientException ex) {
        log.error("Unexpected client exception", ex);
        ProblemDetail problemDetail =
                buildProblemDetail(
                        "Unexpected client exception",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ProblemDetail buildProblemDetail(
            String title, HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
