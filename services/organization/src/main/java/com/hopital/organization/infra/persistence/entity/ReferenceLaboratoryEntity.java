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
@Table(name = "reference_laboratories")
public class ReferenceLaboratoryEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province_id", nullable = false)
    private ProvinceEntity province;

    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active;

    protected ReferenceLaboratoryEntity() {
    }

    public ReferenceLaboratoryEntity(
            UUID id,
            String code,
            String name,
            ProvinceEntity province,
            String address,
            String phoneNumber) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.province = province;
        this.address = address;
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

    public ProvinceEntity getProvince() {
        return province;
    }

    public String getAddress() {
        return address;
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
