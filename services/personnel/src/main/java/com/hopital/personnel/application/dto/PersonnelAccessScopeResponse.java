package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import java.util.UUID;

/**
 * Périmètre de données actif résolu pour un compte connecté.
 *
 * <p>Cette réponse est réservée à la communication inter-services. Elle ne remplace pas les
 * permissions fonctionnelles : elle limite uniquement l'établissement ou le laboratoire auquel
 * ces permissions peuvent s'appliquer.</p>
 */
public record PersonnelAccessScopeResponse(
        UUID accountId,
        UUID personnelId,
        PersonnelAssignmentScope scope,
        UUID hospitalId,
        String laboratoryCode) {
}
