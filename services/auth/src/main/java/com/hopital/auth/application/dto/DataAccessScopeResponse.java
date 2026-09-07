package com.hopital.auth.application.dto;

import java.util.List;

/** Internal data perimeter consumed by protected business services. */
public record DataAccessScopeResponse(
        boolean provinceWide,
        boolean administrator,
        List<String> roleCodes,
        String personnelId,
        String hospitalId,
        String hospitalCode,
        List<String> hospitalLaboratoryCodes,
        String laboratoryCode) {

    public static DataAccessScopeResponse provinceWideAdministratorScope(List<String> roleCodes) {
        return new DataAccessScopeResponse(true, true, roleCodes, null, null, null, List.of(), null);
    }

    public static DataAccessScopeResponse provinceWidePersonnelScope(String personnelId, List<String> roleCodes) {
        return new DataAccessScopeResponse(true, false, roleCodes, personnelId, null, null, List.of(), null);
    }
}
