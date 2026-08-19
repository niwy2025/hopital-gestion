package com.hopital.account.application.domain;

import java.util.Set;

public record AccountUser(String id, String username, String email, String displayName, String password, Set<Role> roles) {

    public boolean matchesIdentifier(String identifier) {
        return username.equalsIgnoreCase(identifier) || email.equalsIgnoreCase(identifier);
    }
}
