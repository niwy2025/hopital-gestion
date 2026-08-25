package com.hopital.organization.infra.persistence.entity;

import com.hopital.organization.application.domain.HospitalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "hospitals")
public class HospitalEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Nationalized
    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HospitalType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "health_zone_id", nullable = false)
    private HealthZoneEntity healthZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_area_id")
    private HealthAreaEntity healthArea;

    @Nationalized
    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active;

    protected HospitalEntity() {
    }

    public HospitalEntity(
            UUID id,
            String code,
            String name,
            HospitalType type,
            HealthZoneEntity healthZone,
            HealthAreaEntity healthArea,
            String address,
            String phoneNumber) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.healthZone = healthZone;
        this.healthArea = healthArea;
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

    public HospitalType getType() {
        return type;
    }

    public HealthZoneEntity getHealthZone() {
        return healthZone;
    }

    public HealthAreaEntity getHealthArea() {
        return healthArea;
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
