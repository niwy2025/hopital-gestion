package com.hopital.auth.application.dto;

import java.util.Set;

public record AccountResponse(String id, String username, String email, String displayName, Set<RoleResponse> roles) {
}
