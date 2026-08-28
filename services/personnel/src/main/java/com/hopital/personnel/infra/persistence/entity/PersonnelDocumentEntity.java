package com.hopital.personnel.infra.persistence.entity;

import com.hopital.personnel.application.domain.PersonnelDocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personnel_documents")
public class PersonnelDocumentEntity {

    @Id
    private UUID id;

    @Column(name = "personnel_id", nullable = false)
    private UUID personnelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private PersonnelDocumentType documentType;

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

    protected PersonnelDocumentEntity() {
    }

    public PersonnelDocumentEntity(UUID id, UUID personnelId, PersonnelDocumentType documentType, String fileName, String contentType, int sizeBytes, String contentBase64, Instant createdAt) {
        this.id = id;
        this.personnelId = personnelId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.contentBase64 = contentBase64;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPersonnelId() { return personnelId; }
    public PersonnelDocumentType getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public int getSizeBytes() { return sizeBytes; }
    public String getContentBase64() { return contentBase64; }
    public Instant getCreatedAt() { return createdAt; }
}
