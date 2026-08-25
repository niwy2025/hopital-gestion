package com.hopital.organization.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "health_areas")
public class HealthAreaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Nationalized
    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "health_zone_id", nullable = false)
    private HealthZoneEntity healthZone;

    @Column(nullable = false)
    private boolean active;

    protected HealthAreaEntity() {
    }

    public HealthAreaEntity(String code, String name, HealthZoneEntity healthZone) {
        this.code = code;
        this.name = name;
        this.healthZone = healthZone;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public HealthZoneEntity getHealthZone() {
        return healthZone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
