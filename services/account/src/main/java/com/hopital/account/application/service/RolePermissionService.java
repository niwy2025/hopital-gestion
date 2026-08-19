package com.hopital.account.application.service;

import com.hopital.account.application.domain.Permission;
import com.hopital.account.application.domain.Role;
import com.hopital.account.application.exception.RoleNotFoundException;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionService {

    private final Map<String, Role> roles = Map.of(
            "ADMIN", new Role("ADMIN", "Administrateur", Set.of(
                    new Permission("ACCOUNT_READ", "Consulter les comptes"),
                    new Permission("ACCOUNT_WRITE", "Créer et modifier les comptes"),
                    new Permission("ROLE_ASSIGN", "Attribuer les rôles"))),
            "DOCTOR", new Role("DOCTOR", "Médecin", Set.of(
                    new Permission("PATIENT_READ", "Consulter les dossiers patients"),
                    new Permission("PRESCRIPTION_WRITE", "Créer des prescriptions"))),
            "NURSE", new Role("NURSE", "Infirmier", Set.of(
                    new Permission("PATIENT_READ", "Consulter les dossiers patients"),
                    new Permission("CARE_WRITE", "Saisir les actes de soin"))),
            "RECEPTIONIST", new Role("RECEPTIONIST", "Accueil", Set.of(
                    new Permission("APPOINTMENT_WRITE", "Gérer les rendez-vous"),
                    new Permission("PATIENT_REGISTER", "Enregistrer les patients"))),
            "PATIENT", new Role("PATIENT", "Patient", Set.of(
                    new Permission("PROFILE_READ", "Consulter son profil"),
                    new Permission("APPOINTMENT_READ", "Consulter ses rendez-vous"))));

    public Set<Role> resolveRoles(Set<String> roleCodes) {
        return roleCodes.stream().map(this::findRole).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Role findRole(String code) {
        Role role = roles.get(code.toUpperCase());
        if (role == null) {
            throw new RoleNotFoundException(code);
        }
        return role;
    }

    public Set<Role> listRoles() {
        return Set.copyOf(roles.values());
    }
}
