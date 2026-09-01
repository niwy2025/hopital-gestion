package com.hopital.patient.application.domain;

import java.util.UUID;

public record DataAccessScope(
        boolean provinceWide,
        boolean administrator,
        UUID personnelId,
        UUID hospitalId,
        String hospitalCode) {

    /** Compatibility constructor used by existing service tests. */
    public DataAccessScope(boolean provinceWide, UUID hospitalId, String hospitalCode) {
        this(provinceWide, provinceWide, null, hospitalId, hospitalCode);
    }

    /** Compatibility constructor for service tests and read-only scope checks. */
    public DataAccessScope(boolean provinceWide, String hospitalCode) {
        this(provinceWide, provinceWide, null, null, hospitalCode);
    }

    public boolean canAccessHospital(String candidateHospitalCode) {
        return provinceWide || (hospitalCode != null && hospitalCode.equalsIgnoreCase(candidateHospitalCode));
    }
}
