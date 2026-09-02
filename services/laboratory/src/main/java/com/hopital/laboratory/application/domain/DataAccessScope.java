package com.hopital.laboratory.application.domain;

import java.util.Set;
import java.util.UUID;

/**
 * Data perimeter returned by Auth. It is intentionally independent from a
 * functional permission: a permission says what an account may do, this scope
 * says where the operation may apply.
 */
public record DataAccessScope(
        boolean provinceWide,
        boolean administrator,
        UUID hospitalId,
        String hospitalCode,
        Set<String> laboratoryCodes) {

    public boolean canAccessLaboratory(String laboratoryCode) {
        return provinceWide || laboratoryCodes.stream().anyMatch(code -> code.equalsIgnoreCase(laboratoryCode));
    }

    public static DataAccessScope provinceWideScope() {
        return new DataAccessScope(true, true, null, null, Set.of());
    }

    public boolean canAccessOriginHospital(UUID candidateHospitalId) {
        return provinceWide || (hospitalId != null && hospitalId.equals(candidateHospitalId));
    }
}
