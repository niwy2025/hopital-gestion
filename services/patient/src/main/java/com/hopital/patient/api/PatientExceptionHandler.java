package com.hopital.patient.api;

import com.hopital.patient.application.dto.ApiError;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.InvalidRegistrationHospitalException;
import com.hopital.patient.application.exception.InvalidPatientDocumentException;
import com.hopital.patient.application.exception.InvalidResponsiblePersonnelException;
import com.hopital.patient.application.exception.InvalidPatientPassageStateException;
import com.hopital.patient.application.exception.InvalidPrescriptionException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.application.exception.PrescriptionDispenseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PatientExceptionHandler {

    @ExceptionHandler(DuplicatePatientException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicatePatientException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "PATIENT_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(PatientNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(PrescriptionDispenseNotFoundException.class)
    ResponseEntity<ApiError> handleDispenseNotFound(
            PrescriptionDispenseNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PRESCRIPTION_DISPENSE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidRegistrationHospitalException.class)
    ResponseEntity<ApiError> handleInvalidRegistrationHospital(
            InvalidRegistrationHospitalException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "REGISTRATION_HOSPITAL_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPatientDocumentException.class)
    ResponseEntity<ApiError> handleInvalidDocument(InvalidPatientDocumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "PATIENT_DOCUMENT_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidResponsiblePersonnelException.class)
    ResponseEntity<ApiError> handleInvalidResponsiblePersonnel(
            InvalidResponsiblePersonnelException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "RESPONSIBLE_PERSONNEL_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPatientPassageStateException.class)
    ResponseEntity<ApiError> handleInvalidPassageState(
            InvalidPatientPassageStateException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "PASSAGE_STATE_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidPrescriptionException.class)
    ResponseEntity<ApiError> handleInvalidPrescription(
            InvalidPrescriptionException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "PRESCRIPTION_INVALID", exception.getMessage(), request);
    }

    @ExceptionHandler(DataAccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(DataAccessDeniedException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_SCOPE_FORBIDDEN", exception.getMessage(), request);
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
