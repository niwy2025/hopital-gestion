package com.hopital.account.application.service;

import com.hopital.account.application.dto.PermissionResponse;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.exception.RoleNotFoundException;
import com.hopital.account.infra.persistence.entity.RoleEntity;
import com.hopital.account.infra.persistence.repository.RoleRepository;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RolePermissionService {

    private final RoleRepository roleRepository;

    public RolePermissionService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Set<RoleEntity> resolveRoles(Set<String> roleCodes) {
        Set<String> normalizedCodes = roleCodes.stream()
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        Map<String, RoleEntity> rolesByCode = new HashMap<>();
        roleRepository.findAllByCodeIn(normalizedCodes)
                .forEach(role -> rolesByCode.put(role.getCode(), role));
        return normalizedCodes.stream()
                .map(code -> findRole(code, rolesByCode))
                .collect(Collectors.toUnmodifiableSet());
    }

    public RoleEntity findRole(String code) {
        return roleRepository.findByCode(code.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new RoleNotFoundException(code));
    }

    public Set<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toUnmodifiableSet());
    }

    public RoleResponse toResponse(RoleEntity role) {
        return new RoleResponse(
                role.getCode(),
                role.getLabel(),
                role.getPermissions().stream()
                        .map(permission -> new PermissionResponse(permission.getCode(), permission.getDescription()))
                        .collect(Collectors.toUnmodifiableSet()));
    }

    private RoleEntity findRole(String code, Map<String, RoleEntity> rolesByCode) {
        RoleEntity role = rolesByCode.get(code);
        if (role == null) {
            throw new RoleNotFoundException(code);
        }
        return role;
    }
}
