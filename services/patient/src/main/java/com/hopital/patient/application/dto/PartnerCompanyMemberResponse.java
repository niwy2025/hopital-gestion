package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PartnerCompanyMemberRole;
import java.time.Instant;
import java.util.UUID;

public record PartnerCompanyMemberResponse(
        UUID id,
        UUID partnerCompanyId,
        String fullName,
        String employeeNumber,
        PartnerCompanyMemberRole memberRole,
        UUID patientId,
        UUID sponsorMemberId,
        boolean active,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername) {
}
