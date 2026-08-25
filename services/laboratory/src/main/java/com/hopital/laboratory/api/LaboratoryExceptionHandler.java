package com.hopital.laboratory.api;

import com.hopital.laboratory.application.dto.ApiError;
import com.hopital.laboratory.application.exception.DuplicateLaboratoryResourceException;
import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import com.hopital.laboratory.application.exception.LaboratoryResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LaboratoryExceptionHandler {

    @ExceptionHandler(DuplicateLaboratoryResourceException.class)
    ResponseEntity<ApiError> handleDuplicate(
            DuplicateLaboratoryResourceException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "LABORATORY_RESOURCE_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(LaboratoryResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(
            LaboratoryResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "LABORATORY_RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidLaboratoryWorkflowException.class)
    ResponseEntity<ApiError> handleWorkflow(
            InvalidLaboratoryWorkflowException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "LABORATORY_WORKFLOW_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("La requête est invalide.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Le corps de la requête contient une valeur invalide.",
                request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
