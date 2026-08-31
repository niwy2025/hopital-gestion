package com.hopital.patient.application.domain;

/** Identité issue du jeton authentifié, jamais du contenu d'une requête métier. */
public record AuditActor(String userId, String username) {

    public AuditActor {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("L'identifiant de l'opérateur est obligatoire.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'opérateur est obligatoire.");
        }
        userId = userId.trim();
        username = username.trim();
    }
}
