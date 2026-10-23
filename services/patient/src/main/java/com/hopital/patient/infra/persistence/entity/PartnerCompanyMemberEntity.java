package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PartnerCompanyMemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partner_company_members")
public class PartnerCompanyMemberEntity {

    @Id
    private UUID id;

    @Column(name = "partner_company_id", nullable = false)
    private UUID partnerCompanyId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "employee_number", length = 60)
    private String employeeNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private PartnerCompanyMemberRole memberRole;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "sponsor_member_id")
    private UUID sponsorMemberId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_user_id", nullable = false, length = 100)
    private String updatedByUserId;

    @Column(name = "updated_by_username", nullable = false, length = 150)
    private String updatedByUsername;

    protected PartnerCompanyMemberEntity() {
    }

    public PartnerCompanyMemberEntity(
            UUID id,
            UUID partnerCompanyId,
            String fullName,
            String employeeNumber,
            PartnerCompanyMemberRole memberRole,
            UUID sponsorMemberId,
            AuditActor actor,
            Instant createdAt) {
        this.id = id;
        this.partnerCompanyId = partnerCompanyId;
        this.fullName = fullName;
        this.employeeNumber = employeeNumber;
        this.memberRole = memberRole;
        this.sponsorMemberId = sponsorMemberId;
        this.active = true;
        this.createdAt = createdAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
        this.updatedAt = createdAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public void updateProfile(
            String fullName,
            String employeeNumber,
            PartnerCompanyMemberRole memberRole,
            UUID sponsorMemberId,
            AuditActor actor,
            Instant updatedAt) {
        this.fullName = fullName;
        this.employeeNumber = employeeNumber;
        this.memberRole = memberRole;
        this.sponsorMemberId = sponsorMemberId;
        touch(actor, updatedAt);
    }

    public void linkPatient(UUID patientId, AuditActor actor, Instant updatedAt) {
        this.patientId = patientId;
        touch(actor, updatedAt);
    }

    public void setActive(boolean active, AuditActor actor, Instant updatedAt) {
        this.active = active;
        touch(actor, updatedAt);
    }

    private void touch(AuditActor actor, Instant updatedAt) {
        this.updatedAt = updatedAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPartnerCompanyId() {
        return partnerCompanyId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public PartnerCompanyMemberRole getMemberRole() {
        return memberRole;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getSponsorMemberId() {
        return sponsorMemberId;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }
}
