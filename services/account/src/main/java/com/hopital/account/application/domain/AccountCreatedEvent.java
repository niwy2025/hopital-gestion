package com.hopital.account.application.domain;

import com.hopital.account.application.dto.AccountResponse;

public record AccountCreatedEvent(AccountResponse account) {
}
