package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.PatientDocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_documents")
public class PatientDocumentEntity {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private PatientDocumentType documentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "content_base64", nullable = false, columnDefinition = "TEXT")
    private String contentBase64;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    protected PatientDocumentEntity() {
    }

    public PatientDocumentEntity(
            UUID id,
            UUID patientId,
            PatientDocumentType documentType,
            String fileName,
            String contentType,
            int sizeBytes,
            String contentBase64,
            Instant createdAt,
            String createdByUserId,
            String createdByUsername) {
        this.id = id;
        this.patientId = patientId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.contentBase64 = contentBase64;
        this.createdAt = createdAt;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
    }

    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public PatientDocumentType getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public int getSizeBytes() { return sizeBytes; }
    public String getContentBase64() { return contentBase64; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
}
