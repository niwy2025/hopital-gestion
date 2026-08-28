package com.hopital.laboratory.application.domain;

import java.util.Set;

public record DataAccessScope(boolean provinceWide, Set<String> laboratoryCodes) {

    public boolean canAccessLaboratory(String laboratoryCode) {
        return provinceWide || laboratoryCodes.stream().anyMatch(code -> code.equalsIgnoreCase(laboratoryCode));
    }

    public static DataAccessScope provinceWideScope() {
        return new DataAccessScope(true, Set.of());
    }
}
