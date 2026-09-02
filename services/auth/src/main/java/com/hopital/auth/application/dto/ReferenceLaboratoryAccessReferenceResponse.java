package com.hopital.auth.application.dto;

/** Internal organization projection used when resolving a reference-lab scope. */
public record ReferenceLaboratoryAccessReferenceResponse(String code, boolean active) {
}
