package com.hopital.auth.application.dto;

import java.util.List;

public record AccountWorkspaceResponse(AccountResponse account, List<KnownDeviceResponse> devices) {
}
