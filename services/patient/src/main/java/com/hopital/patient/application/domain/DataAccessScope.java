package com.hopital.patient.application.domain;

public record DataAccessScope(boolean provinceWide, String hospitalCode) {

    public boolean canAccessHospital(String candidateHospitalCode) {
        return provinceWide || (hospitalCode != null && hospitalCode.equalsIgnoreCase(candidateHospitalCode));
    }
}
