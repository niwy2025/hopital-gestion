package com.hopital.organization.patrimony;

import com.hopital.organization.application.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Order(-10)
@RestControllerAdvice(assignableTypes=PatrimonyController.class)
public class PatrimonyExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> handle(ResponseStatusException e,HttpServletRequest r) {
        return ResponseEntity.status(e.getStatusCode()).body(new ApiError(Instant.now(),e.getStatusCode().value(),"PATRIMONY_ERROR",e.getReason(),r.getRequestURI()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(HttpServletRequest r) {
        return ResponseEntity.status(409).body(new ApiError(Instant.now(),409,"PATRIMONY_CONFLICT","Une autre opération utilise déjà cet élément. Actualisez la fiche.",r.getRequestURI()));
    }
}
