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

    /** Entreprises conventionnées : administrateur central ou administrateur d’hôpital. */
    public boolean canManagePartnerCompanies() {
        return administrator || hasAnyRole("HOSPITAL_ADMIN");
    }

    /**
     * Triage is a nursing (or hospital-admin) activity. Doctors consult the
     * recorded assessments; they do not fill the triage form.
     */
    public boolean canWriteTriage(UUID responsiblePersonnelId) {
        return administrator || hasAnyRole("HOSPITAL_ADMIN", "NURSE");
    }

    /** Catalogue hospitalier des mesures et signes d’alerte de triage. */
    public boolean canManageTriageCatalog() {
        return administrator || hasAnyRole("HOSPITAL_ADMIN");
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
