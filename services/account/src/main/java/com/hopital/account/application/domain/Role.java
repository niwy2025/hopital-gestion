package com.hopital.account.application.domain;

import java.util.Set;

public record Role(String code, String label, Set<Permission> permissions) {
}
