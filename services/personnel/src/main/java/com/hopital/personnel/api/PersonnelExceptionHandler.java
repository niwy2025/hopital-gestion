package com.hopital.personnel.api;

import com.hopital.personnel.application.dto.ApiError;
import com.hopital.personnel.application.exception.DuplicatePersonnelException;
import com.hopital.personnel.application.exception.InvalidPersonnelDocumentException;
import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import com.hopital.personnel.application.exception.PersonnelNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PersonnelExceptionHandler {

    @ExceptionHandler(DuplicatePersonnelException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicatePersonnelException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "PERSONNEL_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(PersonnelNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(PersonnelNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PERSONNEL_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPersonnelReferenceException.class)
    ResponseEntity<ApiError> handleInvalidReference(InvalidPersonnelReferenceException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPersonnelDocumentException.class)
    ResponseEntity<ApiError> handleInvalidDocument(InvalidPersonnelDocumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
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
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Le corps de la requête contient une valeur invalide.", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
