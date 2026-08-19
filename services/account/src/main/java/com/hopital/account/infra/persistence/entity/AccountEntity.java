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
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    @Nationalized
    private String username;

    @Column(nullable = false, length = 255)
    @Nationalized
    private String email;

    @Column(name = "display_name", nullable = false, length = 255)
    @Nationalized
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    @Nationalized
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    protected AccountEntity() {
    }

    public AccountEntity(UUID id, String username, String email, String displayName, String passwordHash, Set<RoleEntity> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
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

    public Set<RoleEntity> getRoles() {
        return Set.copyOf(roles);
    }
}
