package com.hopital.pharmacy.application.domain;

import java.util.UUID;

public record DataAccessScope(
        boolean provinceWide,
        boolean administrator,
        UUID hospitalId,
        String hospitalCode) {

    public boolean canAccessHospital(String candidateHospitalCode) {
        return provinceWide || (hospitalCode != null && hospitalCode.equalsIgnoreCase(candidateHospitalCode));
    }
}
