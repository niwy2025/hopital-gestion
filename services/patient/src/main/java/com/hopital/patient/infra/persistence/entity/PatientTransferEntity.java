package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.application.dto.CreatePatientTransferRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "patient_transfers")
public class PatientTransferEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_passage_id", nullable = false)
    private PatientPassageEntity sourcePassage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_passage_id")
    private PatientPassageEntity destinationPassage;

    @Column(name = "destination_hospital_id")
    private UUID destinationHospitalId;

    @Column(name = "destination_hospital_code", length = 30)
    private String destinationHospitalCode;

    @Column(name = "source_hospital_name", length = 200)
    private String sourceHospitalName;

    @Column(name = "destination_hospital_name", length = 200)
    private String destinationHospitalName;

    @Column(name = "external_facility_name", length = 200)
    private String externalFacilityName;

    @Column(name = "external_facility_address", length = 500)
    private String externalFacilityAddress;

    @Column(name = "destination_contact", length = 100)
    private String destinationContact;

    @Column(name = "patient_name", nullable = false, length = 310)
    private String patientName;

    @Column(name = "patient_date_of_birth", nullable = false)
    private LocalDate patientDateOfBirth;

    @Column(name = "patient_gender", nullable = false, length = 20)
    private String patientGender;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "clinical_summary", nullable = false, length = 8000)
    private String clinicalSummary;

    @Column(name = "treatment_and_instructions", length = 4000)
    private String treatmentAndInstructions;

    @Column(name = "transport_details", length = 1000)
    private String transportDetails;

    @Column(nullable = false)
    private boolean urgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PatientTransferStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "requested_by_user_id", nullable = false, length = 100)
    private String requestedByUserId;

    @Column(name = "requested_by_username", nullable = false, length = 150)
    private String requestedByUsername;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "dispatched_by_user_id", length = 100)
    private String dispatchedByUserId;

    @Column(name = "dispatched_by_username", length = 150)
    private String dispatchedByUsername;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by_user_id", length = 100)
    private String resolvedByUserId;

    @Column(name = "resolved_by_username", length = 150)
    private String resolvedByUsername;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    protected PatientTransferEntity() {
    }

    public PatientTransferEntity(PatientPassageEntity source, CreatePatientTransferRequest request,
            UUID destinationHospitalId, String destinationHospitalCode, AuditActor actor) {
        this.id = UUID.randomUUID();
        this.code = "TRF-" + id.toString().replace("-", "").substring(0, 20).toUpperCase(java.util.Locale.ROOT);
        this.sourcePassage = source;
        this.destinationHospitalId = destinationHospitalId;
        this.destinationHospitalCode = destinationHospitalCode;
        this.externalFacilityName = clean(request.externalFacilityName());
        this.externalFacilityAddress = clean(request.externalFacilityAddress());
        this.destinationContact = clean(request.destinationContact());
        this.patientName = Stream.of(source.getPatient().getLastName(), source.getPatient().getMiddleName(),
                source.getPatient().getFirstName()).filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
        this.patientDateOfBirth = source.getPatient().getDateOfBirth();
        this.patientGender = source.getPatient().getGender().name();
        this.reason = request.reason().trim();
        this.clinicalSummary = request.clinicalSummary().trim();
        this.treatmentAndInstructions = clean(request.treatmentAndInstructions());
        this.transportDetails = clean(request.transportDetails());
        this.urgent = request.urgent();
        this.status = PatientTransferStatus.REQUESTED;
        this.requestedAt = Instant.now();
        this.requestedByUserId = actor.userId();
        this.requestedByUsername = actor.username();
    }

    public void recordHospitalNames(String sourceName, String destinationName) {
        sourceHospitalName = clean(sourceName);
        destinationHospitalName = clean(destinationName);
    }

    public void dispatch(AuditActor actor, Instant now) {
        status = destinationHospitalId == null ? PatientTransferStatus.EXTERNAL_COMPLETED : PatientTransferStatus.IN_TRANSIT;
        dispatchedAt = now;
        dispatchedByUserId = actor.userId();
        dispatchedByUsername = actor.username();
        if (destinationHospitalId == null) resolve(actor, now, null);
    }

    public void receive(PatientPassageEntity passage, AuditActor actor, String note) {
        destinationPassage = passage;
        status = PatientTransferStatus.RECEIVED;
        resolve(actor, Instant.now(), note);
    }

    public void cancel(AuditActor actor, String reason) {
        status = PatientTransferStatus.CANCELLED;
        resolve(actor, Instant.now(), reason);
    }

    private void resolve(AuditActor actor, Instant now, String note) {
        resolvedAt = now;
        resolvedByUserId = actor.userId();
        resolvedByUsername = actor.username();
        resolutionNote = clean(note);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public PatientPassageEntity getSourcePassage() { return sourcePassage; }
    public PatientPassageEntity getDestinationPassage() { return destinationPassage; }
    public UUID getDestinationHospitalId() { return destinationHospitalId; }
    public String getDestinationHospitalCode() { return destinationHospitalCode; }
    public String getSourceHospitalName() { return sourceHospitalName; }
    public String getDestinationHospitalName() { return destinationHospitalName; }
    public String getExternalFacilityName() { return externalFacilityName; }
    public String getExternalFacilityAddress() { return externalFacilityAddress; }
    public String getDestinationContact() { return destinationContact; }
    public String getPatientName() { return patientName; }
    public LocalDate getPatientDateOfBirth() { return patientDateOfBirth; }
    public String getPatientGender() { return patientGender; }
    public String getReason() { return reason; }
    public String getClinicalSummary() { return clinicalSummary; }
    public String getTreatmentAndInstructions() { return treatmentAndInstructions; }
    public String getTransportDetails() { return transportDetails; }
    public boolean isUrgent() { return urgent; }
    public PatientTransferStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public String getRequestedByUserId() { return requestedByUserId; }
    public String getRequestedByUsername() { return requestedByUsername; }
    public Instant getDispatchedAt() { return dispatchedAt; }
    public String getDispatchedByUserId() { return dispatchedByUserId; }
    public String getDispatchedByUsername() { return dispatchedByUsername; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolvedByUserId() { return resolvedByUserId; }
    public String getResolvedByUsername() { return resolvedByUsername; }
    public String getResolutionNote() { return resolutionNote; }
}
