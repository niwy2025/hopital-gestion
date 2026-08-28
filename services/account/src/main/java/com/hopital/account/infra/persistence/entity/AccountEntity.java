package com.hopital.account.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Reference to the hospital owned by organization-service. There is intentionally no SQL
     * foreign key here: each microservice owns its own database.
     */
    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "profile_photo_base64", columnDefinition = "TEXT")
    private String profilePhotoBase64;

    @Column(name = "profile_photo_content_type", length = 100)
    private String profilePhotoContentType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    protected AccountEntity() {
    }

    public AccountEntity(
            UUID id,
            String username,
            String email,
            String displayName,
            String passwordHash,
            UUID hospitalId,
            String profilePhotoBase64,
            String profilePhotoContentType,
            Set<RoleEntity> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.hospitalId = hospitalId;
        this.profilePhotoBase64 = profilePhotoBase64;
        this.profilePhotoContentType = profilePhotoContentType;
        this.roles = new HashSet<>(roles);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public String getProfilePhotoBase64() {
        return profilePhotoBase64;
    }

    public String getProfilePhotoContentType() {
        return profilePhotoContentType;
    }

    public Set<RoleEntity> getRoles() {
        return Set.copyOf(roles);
    }

    public void updateProfile(
            String username,
            String email,
            String displayName,
            UUID hospitalId,
            Set<RoleEntity> roles) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.hospitalId = hospitalId;
        this.roles = new HashSet<>(roles);
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeProfilePhoto(String profilePhotoBase64, String profilePhotoContentType) {
        this.profilePhotoBase64 = profilePhotoBase64;
        this.profilePhotoContentType = profilePhotoContentType;
    }

    public void removeProfilePhoto() {
        this.profilePhotoBase64 = null;
        this.profilePhotoContentType = null;
    }
}
