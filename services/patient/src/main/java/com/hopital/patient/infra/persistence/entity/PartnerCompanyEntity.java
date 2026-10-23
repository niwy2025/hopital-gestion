package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partner_companies")
public class PartnerCompanyEntity {

    @Id
    private UUID id;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String address;

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

    protected PartnerCompanyEntity() {
    }

    public PartnerCompanyEntity(
            UUID id,
            UUID hospitalId,
            String code,
            String name,
            String phoneNumber,
            String email,
            String address,
            AuditActor actor,
            Instant createdAt) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.code = code;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.active = true;
        this.createdAt = createdAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
        this.updatedAt = createdAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public void updateProfile(
            String name,
            String phoneNumber,
            String email,
            String address,
            AuditActor actor,
            Instant updatedAt) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
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

    public UUID getHospitalId() {
        return hospitalId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
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
