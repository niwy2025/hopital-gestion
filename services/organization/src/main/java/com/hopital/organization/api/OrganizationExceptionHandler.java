package com.hopital.organization.api;

import com.hopital.organization.application.dto.ApiError;
import com.hopital.organization.application.exception.DuplicateOrganizationException;
import com.hopital.organization.application.exception.OrganizationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrganizationExceptionHandler {

    @ExceptionHandler(DuplicateOrganizationException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicateOrganizationException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "ORGANIZATION_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(OrganizationNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("La requête est invalide.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
