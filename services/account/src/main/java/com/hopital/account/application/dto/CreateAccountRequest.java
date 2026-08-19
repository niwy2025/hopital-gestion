package com.hopital.account.application.dto;

import java.util.Set;

public record CreateAccountRequest(String username, String email, String displayName, String password, Set<String> roles) {
}
