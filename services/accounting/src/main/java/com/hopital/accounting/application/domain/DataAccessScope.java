package com.hopital.accounting.application.domain;

import java.util.UUID;

/** Accounting is always isolated per hospital unless the provincial administrator is acting. */
public record DataAccessScope(boolean provinceWide, boolean administrator, UUID hospitalId, String hospitalCode) {
    public boolean canAccessHospital(UUID candidateHospitalId) {
        return provinceWide || (hospitalId != null && hospitalId.equals(candidateHospitalId));
    }
}
