package com.hopital.organization.application.dto;

/**
 * Minimal internal projection used to validate an agent assignment without
 * exposing the complete public laboratory record to other services.
 */
public record ReferenceLaboratoryAssignmentReference(String code, boolean active) {
}
