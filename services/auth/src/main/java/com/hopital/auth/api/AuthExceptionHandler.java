package com.hopital.auth.api;

import com.hopital.auth.application.exception.AuthException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    ResponseEntity<Map<String, String>> handleAuthException(AuthException exception) {
        // Ne jamais journaliser le corps de la requête : il contient le mot de passe.
        LOGGER.warn("Requête d'authentification refusée : {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "code", exception.getCode().name(),
                "error", exception.getMessage()));
    }
}
