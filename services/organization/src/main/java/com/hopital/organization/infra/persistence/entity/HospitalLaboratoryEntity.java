package com.hopital.organization.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "hospital_laboratories")
public class HospitalLaboratoryEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @Column(length = 255)
    private String location;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active;

    protected HospitalLaboratoryEntity() {
    }

    public HospitalLaboratoryEntity(
            UUID id,
            String code,
            String name,
            HospitalEntity hospital,
            String location,
            String phoneNumber) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.hospital = hospital;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public HospitalEntity getHospital() {
        return hospital;
    }

    public String getLocation() {
        return location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
