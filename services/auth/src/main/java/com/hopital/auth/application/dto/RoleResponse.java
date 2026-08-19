package com.hopital.auth.application.dto;

import java.util.Set;

public record RoleResponse(String code, String label, Set<PermissionResponse> permissions) {
}
