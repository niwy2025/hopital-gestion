package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.SupportingDocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Justificatif attached to an invoice, payment, entry, or financial-statement annex. */
@Entity
@Table(name = "accounting_supporting_documents")
public class AccountingSupportingDocumentEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(name = "related_type", nullable = false, length = 30) private String relatedType;
    @Column(name = "related_id", nullable = false) private UUID relatedId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private SupportingDocumentType type;
    @Column(name = "file_name", nullable = false, length = 255) private String fileName;
    @Column(name = "content_type", nullable = false, length = 120) private String contentType;
    @Column(name = "content_base64", nullable = false, columnDefinition = "TEXT") private String contentBase64;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(name = "uploaded_at", nullable = false) private Instant uploadedAt;
    @Column(name = "uploaded_by_user_id", nullable = false, length = 100) private String uploadedByUserId;
    @Column(name = "uploaded_by_username", nullable = false, length = 150) private String uploadedByUsername;

    protected AccountingSupportingDocumentEntity() { }
    public AccountingSupportingDocumentEntity(UUID id, UUID hospitalId, String hospitalCode, String relatedType, UUID relatedId,
            SupportingDocumentType type, String fileName, String contentType, String contentBase64, long sizeBytes,
            String userId, String username, Instant uploadedAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.relatedType = relatedType;
        this.relatedId = relatedId; this.type = type; this.fileName = fileName; this.contentType = contentType;
        this.contentBase64 = contentBase64; this.sizeBytes = sizeBytes; this.uploadedByUserId = userId;
        this.uploadedByUsername = username; this.uploadedAt = uploadedAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getRelatedType() { return relatedType; } public UUID getRelatedId() { return relatedId; } public SupportingDocumentType getType() { return type; }
    public String getFileName() { return fileName; } public String getContentType() { return contentType; } public String getContentBase64() { return contentBase64; }
    public long getSizeBytes() { return sizeBytes; } public Instant getUploadedAt() { return uploadedAt; } public String getUploadedByUsername() { return uploadedByUsername; }
}
