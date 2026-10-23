package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PartnerCompanyDependentRelationship;
import com.hopital.patient.application.domain.PartnerCompanyMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdatePartnerCompanyMemberRequest(
        @NotBlank @Size(max = 200) String fullName,
        @Size(max = 60) String employeeNumber,
        @NotNull PartnerCompanyMemberRole memberRole,
        UUID sponsorMemberId,
        PartnerCompanyDependentRelationship dependentRelationship) {
}
