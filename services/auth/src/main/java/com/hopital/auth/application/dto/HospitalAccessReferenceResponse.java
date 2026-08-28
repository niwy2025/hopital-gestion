package com.hopital.auth.application.dto;

import java.util.List;

public record HospitalAccessReferenceResponse(
        String hospitalId,
        String hospitalCode,
        List<String> hospitalLaboratoryCodes) {
}
