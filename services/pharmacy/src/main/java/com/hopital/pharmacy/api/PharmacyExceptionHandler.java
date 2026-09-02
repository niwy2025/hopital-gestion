package com.hopital.pharmacy.api;

import com.hopital.pharmacy.application.dto.ApiError;
import com.hopital.pharmacy.application.exception.DataAccessDeniedException;
import com.hopital.pharmacy.application.exception.InvalidStockEntryException;
import com.hopital.pharmacy.application.exception.PharmacyResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PharmacyExceptionHandler {
    @ExceptionHandler(PharmacyResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(PharmacyResourceNotFoundException exception, HttpServletRequest request) { return error(HttpStatus.NOT_FOUND, "PHARMACY_RESOURCE_NOT_FOUND", exception.getMessage(), request); }
    @ExceptionHandler({InvalidStockEntryException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(RuntimeException exception, HttpServletRequest request) { return error(HttpStatus.UNPROCESSABLE_ENTITY, "STOCK_ENTRY_INVALID", exception.getMessage(), request); }
    @ExceptionHandler(DataAccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(DataAccessDeniedException exception, HttpServletRequest request) { return error(HttpStatus.FORBIDDEN, "ACCESS_SCOPE_FORBIDDEN", exception.getMessage(), request); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getDefaultMessage()).orElse("La requête est invalide.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
