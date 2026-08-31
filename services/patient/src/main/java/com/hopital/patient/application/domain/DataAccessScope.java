package com.hopital.patient.application.domain;

import java.util.UUID;

public record DataAccessScope(boolean provinceWide, UUID hospitalId, String hospitalCode) {

    /** Compatibility constructor for service tests and read-only scope checks. */
    public DataAccessScope(boolean provinceWide, String hospitalCode) {
        this(provinceWide, null, hospitalCode);
    }

    public boolean canAccessHospital(String candidateHospitalCode) {
        return provinceWide || (hospitalCode != null && hospitalCode.equalsIgnoreCase(candidateHospitalCode));
    }
}
