package com.hopital.patient.application.domain;

import java.util.Set;
import java.util.UUID;

public record DataAccessScope(
        boolean provinceWide,
        boolean administrator,
        Set<String> roleCodes,
        UUID personnelId,
        UUID hospitalId,
        String hospitalCode) {

    public DataAccessScope {
        roleCodes = roleCodes == null ? Set.of() : Set.copyOf(roleCodes);
    }

    /** Compatibility constructor used by clients that do not need role checks. */
    public DataAccessScope(
            boolean provinceWide,
            boolean administrator,
            UUID personnelId,
            UUID hospitalId,
            String hospitalCode) {
        this(provinceWide, administrator, Set.of(), personnelId, hospitalId, hospitalCode);
    }

    /** Compatibility constructor used by existing service tests. */
    public DataAccessScope(boolean provinceWide, UUID hospitalId, String hospitalCode) {
        this(provinceWide, provinceWide, Set.of(), null, hospitalId, hospitalCode);
    }

    /** Compatibility constructor for service tests and read-only scope checks. */
    public DataAccessScope(boolean provinceWide, String hospitalCode) {
        this(provinceWide, provinceWide, Set.of(), null, null, hospitalCode);
    }

    public boolean canAccessHospital(String candidateHospitalCode) {
        return provinceWide || (hospitalCode != null && hospitalCode.equalsIgnoreCase(candidateHospitalCode));
    }

    public boolean canReadTriage() {
        return administrator || hasAnyRole("HOSPITAL_ADMIN", "DOCTOR", "NURSE");
    }

    /**
     * Triage remains primarily a nursing activity. A doctor may only add a
     * reassessment when they are the personnel explicitly responsible for the
     * passage; merely holding the doctor role is not sufficient.
     */
    public boolean canWriteTriage(UUID responsiblePersonnelId) {
        return administrator
                || hasAnyRole("HOSPITAL_ADMIN", "NURSE")
                || (hasAnyRole("DOCTOR")
                && personnelId != null
                && personnelId.equals(responsiblePersonnelId));
    }

    private boolean hasAnyRole(String... expectedRoles) {
        for (String expectedRole : expectedRoles) {
            if (roleCodes.contains(expectedRole)) {
                return true;
            }
        }
        return false;
    }
}
