package com.hopital.accounting.api;

import com.hopital.accounting.application.dto.ApiError;
import com.hopital.accounting.application.exception.AccountingAccessDeniedException;
import com.hopital.accounting.application.exception.AccountingResourceNotFoundException;
import com.hopital.accounting.application.exception.AccountingValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class AccountingExceptionHandler {
    @ExceptionHandler(AccountingResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(AccountingResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ACCOUNTING_RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }
    @ExceptionHandler(AccountingAccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccountingAccessDeniedException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCOUNTING_SCOPE_FORBIDDEN", exception.getMessage(), request);
    }
    @ExceptionHandler({AccountingValidationException.class, IllegalStateException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "ACCOUNTING_VALIDATION_ERROR", exception.getMessage(), request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getDefaultMessage())
                .orElse("La requête comptable est invalide.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Le corps de la requête comptable contient une valeur invalide.", request);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> invalidParameter(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Un paramètre de la requête comptable contient une valeur invalide.", request);
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
