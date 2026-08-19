package com.hopital.auth.application.domain;

import java.util.Set;

public record HospitalUser(String id, String username, Set<String> roles) {
}
