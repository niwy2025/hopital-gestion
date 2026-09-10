package com.hopital.organization.patrimony;

import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record PatrimonyScope(boolean administrator, UUID hospitalId, Set<String> roleCodes, String userId, String username) {
    public boolean allows(String action) {
        if (administrator) return true;
        if (hospitalId == null) return false;
        Set<String> roles = switch(action) {
            case "read" -> Set.of("HOSPITAL_ADMIN", "INTENDANT", "MAINTENANCE_TECHNICIAN", "HOSPITAL_ACCOUNTANT", "FINANCE_MANAGER", "FINANCE_AUDITOR");
            case "write" -> Set.of("HOSPITAL_ADMIN", "INTENDANT");
            case "maintain" -> Set.of("HOSPITAL_ADMIN", "INTENDANT", "MAINTENANCE_TECHNICIAN");
            case "validate" -> Set.of("HOSPITAL_ADMIN");
            case "beds" -> Set.of("HOSPITAL_ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST");
            case "finance" -> Set.of("HOSPITAL_ACCOUNTANT", "FINANCE_MANAGER");
            default -> Set.of();
        };
        return roleCodes != null && roleCodes.stream().anyMatch(roles::contains);
    }
    public void require(String action) {
        if (!allows(action)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre rôle ne permet pas cette opération.");
    }
    public UUID hospital(UUID requested) {
        UUID selected = administrator ? requested : hospitalId;
        if (selected == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sélectionnez un hôpital.");
        if (!administrator && requested != null && !requested.equals(hospitalId)) deny();
        return selected;
    }
    public void checkHospital(UUID hospital) {
        if (!administrator && (hospitalId == null || !hospitalId.equals(hospital))) deny();
    }
    private void deny() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cet élément appartient à un autre hôpital."); }
}
