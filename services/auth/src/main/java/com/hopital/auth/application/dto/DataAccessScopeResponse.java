package com.hopital.auth.application.dto;

import java.util.List;

/** Internal data perimeter consumed by protected business services. */
public record DataAccessScopeResponse(
        boolean provinceWide,
        String hospitalCode,
        List<String> hospitalLaboratoryCodes,
        String laboratoryCode) {

    public static DataAccessScopeResponse provinceWideScope() {
        return new DataAccessScopeResponse(true, null, List.of(), null);
    }
}
