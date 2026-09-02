package com.hopital.laboratory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A reference laboratory may reject a specimen only with a traceable reason. */
public record RejectReferenceSpecimenRequest(@NotBlank @Size(max = 1000) String rejectionReason) {
}
