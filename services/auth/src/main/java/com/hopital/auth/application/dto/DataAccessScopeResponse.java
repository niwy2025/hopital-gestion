package com.hopital.auth.application.dto;

import java.util.List;

/** Internal data perimeter consumed by protected business services. */
public record DataAccessScopeResponse(
        boolean provinceWide,
        boolean administrator,
        String personnelId,
        String hospitalId,
        String hospitalCode,
        List<String> hospitalLaboratoryCodes,
        String laboratoryCode) {

    public static DataAccessScopeResponse provinceWideAdministratorScope() {
        return new DataAccessScopeResponse(true, true, null, null, null, List.of(), null);
    }

    public static DataAccessScopeResponse provinceWidePersonnelScope(String personnelId) {
        return new DataAccessScopeResponse(true, false, personnelId, null, null, List.of(), null);
    }
}
