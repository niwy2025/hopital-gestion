package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.PartnerCompanyDependentRelationship;
import com.hopital.patient.application.domain.PartnerCompanyDocumentType;
import com.hopital.patient.application.domain.PartnerCompanyMemberRole;
import com.hopital.patient.application.dto.CreatePartnerCompanyDocumentRequest;
import com.hopital.patient.application.dto.CreatePartnerCompanyMemberRequest;
import com.hopital.patient.application.dto.CreatePartnerCompanyRequest;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PartnerCompanyDocumentResponse;
import com.hopital.patient.application.dto.PartnerCompanyMemberResponse;
import com.hopital.patient.application.dto.PartnerCompanyResponse;
import com.hopital.patient.application.dto.UpdatePartnerCompanyMemberRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyMemberStatusRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.InvalidPatientCoverageException;
import com.hopital.patient.application.exception.InvalidPatientDocumentException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PartnerCompanyDocumentEntity;
import com.hopital.patient.infra.persistence.entity.PartnerCompanyEntity;
import com.hopital.patient.infra.persistence.entity.PartnerCompanyMemberEntity;
import com.hopital.patient.infra.persistence.repository.PartnerCompanyDocumentRepository;
import com.hopital.patient.infra.persistence.repository.PartnerCompanyMemberRepository;
import com.hopital.patient.infra.persistence.repository.PartnerCompanyRepository;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PartnerCompanyApplicationService {

    private static final Set<String> SUPPORTED_DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp");

    private final PartnerCompanyRepository partnerCompanyRepository;
    private final PartnerCompanyMemberRepository partnerCompanyMemberRepository;
    private final PartnerCompanyDocumentRepository partnerCompanyDocumentRepository;
    private final HospitalReferenceClient hospitalReferenceClient;

    public PartnerCompanyApplicationService(
            PartnerCompanyRepository partnerCompanyRepository,
            PartnerCompanyMemberRepository partnerCompanyMemberRepository,
            PartnerCompanyDocumentRepository partnerCompanyDocumentRepository,
            HospitalReferenceClient hospitalReferenceClient) {
        this.partnerCompanyRepository = partnerCompanyRepository;
        this.partnerCompanyMemberRepository = partnerCompanyMemberRepository;
        this.partnerCompanyDocumentRepository = partnerCompanyDocumentRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
    }

    public PageResponse<PartnerCompanyResponse> searchCompanies(
            UUID hospitalId,
            String query,
            Boolean active,
            int page,
            int size,
            DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        HospitalReferenceClient.HospitalReference hospital = resolveAccessibleHospital(hospitalId, accessScope);
        var companies = partnerCompanyRepository.search(
                hospital.hospitalId(),
                normalizeSearchFilter(query),
                active,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new PageResponse<>(
                companies.getContent().stream().map(this::toCompany).toList(),
                companies.getNumber(),
                companies.getSize(),
                companies.getTotalElements(),
                companies.getTotalPages());
    }

    public PartnerCompanyResponse getCompany(UUID companyId, DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        return toCompany(requireAccessibleCompany(companyId, accessScope));
    }

    @Transactional
    public PartnerCompanyResponse createCompany(
            CreatePartnerCompanyRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        HospitalReferenceClient.HospitalReference hospital = resolveAccessibleHospital(
                request.hospitalId(), accessScope);
        String code = normalizeCode(request.code());
        if (partnerCompanyRepository.existsByHospitalIdAndCodeIgnoreCase(hospital.hospitalId(), code)) {
            throw new InvalidPatientCoverageException(
                    "Une entreprise conventionnée avec ce code existe déjà pour cet hôpital.");
        }
        Instant now = Instant.now();
        PartnerCompanyEntity company = new PartnerCompanyEntity(
                UUID.randomUUID(),
                hospital.hospitalId(),
                code,
                request.name().trim(),
                trimToNull(request.legalName()),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                auditActor,
                now);
        return toCompany(partnerCompanyRepository.save(company));
    }

    @Transactional
    public PartnerCompanyResponse updateCompany(
            UUID companyId,
            UpdatePartnerCompanyRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        company.updateProfile(
                request.name().trim(),
                trimToNull(request.legalName()),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                auditActor,
                Instant.now());
        return toCompany(company);
    }

    @Transactional
    public PartnerCompanyResponse updateCompanyStatus(
            UUID companyId,
            UpdatePartnerCompanyStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        company.setActive(Boolean.TRUE.equals(request.active()), auditActor, Instant.now());
        return toCompany(company);
    }

    public PageResponse<PartnerCompanyMemberResponse> searchMembers(
            UUID companyId,
            String query,
            Boolean active,
            UUID sponsorMemberId,
            int page,
            int size,
            DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        var members = partnerCompanyMemberRepository.search(
                company.getId(),
                normalizeSearchFilter(query),
                active,
                sponsorMemberId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new PageResponse<>(
                members.getContent().stream().map(this::toMember).toList(),
                members.getNumber(),
                members.getSize(),
                members.getTotalElements(),
                members.getTotalPages());
    }

    public PartnerCompanyMemberResponse getMember(
            UUID companyId,
            UUID memberId,
            DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        requireAccessibleCompany(companyId, accessScope);
        return toMember(requireMember(companyId, memberId));
    }

    @Transactional
    public PartnerCompanyMemberResponse createMember(
            UUID companyId,
            CreatePartnerCompanyMemberRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        MemberLinkage linkage = validateMemberRole(
                company.getId(),
                request.memberRole(),
                request.sponsorMemberId(),
                request.dependentRelationship());
        Instant now = Instant.now();
        PartnerCompanyMemberEntity member = new PartnerCompanyMemberEntity(
                UUID.randomUUID(),
                company.getId(),
                request.fullName().trim(),
                trimToNull(request.employeeNumber()),
                request.memberRole(),
                linkage.sponsorMemberId(),
                linkage.dependentRelationship(),
                auditActor,
                now);
        return toMember(partnerCompanyMemberRepository.save(member));
    }

    @Transactional
    public PartnerCompanyMemberResponse updateMember(
            UUID companyId,
            UUID memberId,
            UpdatePartnerCompanyMemberRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        requireAccessibleCompany(companyId, accessScope);
        PartnerCompanyMemberEntity member = requireMember(companyId, memberId);
        MemberLinkage linkage = validateMemberRole(
                companyId,
                request.memberRole(),
                request.sponsorMemberId(),
                request.dependentRelationship());
        if (linkage.sponsorMemberId() != null && linkage.sponsorMemberId().equals(member.getId())) {
            throw new InvalidPatientCoverageException("Une personne liée ne peut pas être son propre titulaire.");
        }
        member.updateProfile(
                request.fullName().trim(),
                trimToNull(request.employeeNumber()),
                request.memberRole(),
                linkage.sponsorMemberId(),
                linkage.dependentRelationship(),
                auditActor,
                Instant.now());
        return toMember(member);
    }

    public PageResponse<PartnerCompanyDocumentResponse> searchDocuments(
            UUID companyId,
            String query,
            PartnerCompanyDocumentType documentType,
            int page,
            int size,
            DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        var documents = partnerCompanyDocumentRepository.search(
                company.getId(),
                normalizeSearchFilter(query),
                documentType,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new PageResponse<>(
                documents.getContent().stream().map(this::toDocument).toList(),
                documents.getNumber(),
                documents.getSize(),
                documents.getTotalElements(),
                documents.getTotalPages());
    }

    @Transactional
    public PartnerCompanyDocumentResponse addDocument(
            UUID companyId,
            CreatePartnerCompanyDocumentRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        PartnerCompanyEntity company = requireAccessibleCompany(companyId, accessScope);
        DocumentContent content = validateDocument(request);
        Instant now = Instant.now();
        PartnerCompanyDocumentEntity document = new PartnerCompanyDocumentEntity(
                UUID.randomUUID(),
                company.getId(),
                request.documentType(),
                request.fileName().trim(),
                content.contentType(),
                content.sizeBytes(),
                content.contentBase64(),
                now,
                auditActor.userId(),
                auditActor.username());
        return toDocument(partnerCompanyDocumentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(
            UUID companyId,
            UUID documentId,
            DataAccessScope accessScope) {
        requirePartnerCompanyManager(accessScope);
        requireAccessibleCompany(companyId, accessScope);
        PartnerCompanyDocumentEntity document = partnerCompanyDocumentRepository
                .findByIdAndPartnerCompanyId(documentId, companyId)
                .orElseThrow(() -> new PatientNotFoundException(documentId.toString()));
        partnerCompanyDocumentRepository.delete(document);
    }

    @Transactional
    public PartnerCompanyMemberResponse updateMemberStatus(
            UUID companyId,
            UUID memberId,
            UpdatePartnerCompanyMemberStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        requirePartnerCompanyManager(accessScope);
        requireAccessibleCompany(companyId, accessScope);
        PartnerCompanyMemberEntity member = requireMember(companyId, memberId);
        member.setActive(Boolean.TRUE.equals(request.active()), auditActor, Instant.now());
        return toMember(member);
    }

    public PartnerCompanyEntity requireAccessibleCompany(UUID companyId, DataAccessScope accessScope) {
        PartnerCompanyEntity company = partnerCompanyRepository.findById(companyId)
                .orElseThrow(() -> new PatientNotFoundException(companyId.toString()));
        resolveAccessibleHospital(company.getHospitalId(), accessScope);
        return company;
    }

    public PartnerCompanyMemberEntity requireActiveMemberForCoverage(UUID companyId, UUID memberId) {
        PartnerCompanyMemberEntity member = requireMember(companyId, memberId);
        if (!member.isActive()) {
            throw new InvalidPatientCoverageException("La personne liée sélectionnée n’est plus active.");
        }
        return member;
    }

    public PartnerCompanyEntity requireActiveCompanyForHospital(UUID companyId, UUID hospitalId) {
        PartnerCompanyEntity company = partnerCompanyRepository.findByIdAndHospitalId(companyId, hospitalId)
                .orElseThrow(() -> new InvalidPatientCoverageException(
                        "L’entreprise conventionnée n’appartient pas à l’hôpital d’enregistrement."));
        if (!company.isActive()) {
            throw new InvalidPatientCoverageException("L’entreprise conventionnée sélectionnée n’est plus active.");
        }
        return company;
    }

    private PartnerCompanyMemberEntity requireMember(UUID companyId, UUID memberId) {
        return partnerCompanyMemberRepository.findByIdAndPartnerCompanyId(memberId, companyId)
                .orElseThrow(() -> new PatientNotFoundException(memberId.toString()));
    }

    private MemberLinkage validateMemberRole(
            UUID companyId,
            PartnerCompanyMemberRole memberRole,
            UUID sponsorMemberId,
            PartnerCompanyDependentRelationship dependentRelationship) {
        if (memberRole == PartnerCompanyMemberRole.EMPLOYEE) {
            if (sponsorMemberId != null || dependentRelationship != null) {
                throw new InvalidPatientCoverageException(
                        "Un titulaire (employé) ne doit pas référencer un autre titulaire ni un lien de parenté.");
            }
            return new MemberLinkage(null, null);
        }
        if (sponsorMemberId == null) {
            throw new InvalidPatientCoverageException(
                    "Un ayant droit doit être rattaché à un titulaire de la même entreprise.");
        }
        if (dependentRelationship == null) {
            throw new InvalidPatientCoverageException(
                    "Précisez le lien de parenté de la personne à charge (conjoint, enfant, parent ou autre).");
        }
        PartnerCompanyMemberEntity sponsor = requireMember(companyId, sponsorMemberId);
        if (!sponsor.isActive() || sponsor.getMemberRole() != PartnerCompanyMemberRole.EMPLOYEE) {
            throw new InvalidPatientCoverageException(
                    "Le titulaire sélectionné doit être un employé actif de l’entreprise.");
        }
        return new MemberLinkage(sponsor.getId(), dependentRelationship);
    }

    private DocumentContent validateDocument(CreatePartnerCompanyDocumentRequest request) {
        String contentType = request.contentType() == null ? "" : request.contentType().trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_DOCUMENT_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidPatientDocumentException(
                    "Formats acceptés : PDF, Word, JPEG, PNG ou WebP · 2 Mo maximum.");
        }
        String contentBase64 = request.contentBase64().replaceAll("\\s", "");
        if (contentBase64.startsWith("data:")) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(contentBase64);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        if (decoded.length == 0 || decoded.length > 2_000_000) {
            throw new InvalidPatientDocumentException("Chaque fichier ne peut pas dépasser 2 Mo.");
        }
        return new DocumentContent(contentType, decoded.length, contentBase64);
    }

    private void requirePartnerCompanyManager(DataAccessScope accessScope) {
        if (!accessScope.canManagePartnerCompanies()) {
            throw new DataAccessDeniedException();
        }
    }

    private HospitalReferenceClient.HospitalReference resolveAccessibleHospital(
            UUID hospitalId,
            DataAccessScope accessScope) {
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActiveHospital(hospitalId);
        String hospitalCode = hospital.hospitalCode() == null
                ? ""
                : hospital.hospitalCode().trim().toUpperCase(Locale.ROOT);
        if (!accessScope.canAccessHospital(hospitalCode)) {
            throw new DataAccessDeniedException();
        }
        return hospital;
    }

    private PartnerCompanyResponse toCompany(PartnerCompanyEntity company) {
        return new PartnerCompanyResponse(
                company.getId(),
                company.getHospitalId(),
                company.getCode(),
                company.getName(),
                company.getLegalName(),
                company.getPhoneNumber(),
                company.getEmail(),
                company.getAddress(),
                company.isActive(),
                company.getCreatedAt(),
                company.getCreatedByUsername(),
                company.getUpdatedAt(),
                company.getUpdatedByUsername());
    }

    private PartnerCompanyMemberResponse toMember(PartnerCompanyMemberEntity member) {
        return new PartnerCompanyMemberResponse(
                member.getId(),
                member.getPartnerCompanyId(),
                member.getFullName(),
                member.getEmployeeNumber(),
                member.getMemberRole(),
                member.getPatientId(),
                member.getSponsorMemberId(),
                member.getDependentRelationship(),
                member.isActive(),
                member.getCreatedAt(),
                member.getCreatedByUsername(),
                member.getUpdatedAt(),
                member.getUpdatedByUsername());
    }

    private PartnerCompanyDocumentResponse toDocument(PartnerCompanyDocumentEntity document) {
        return new PartnerCompanyDocumentResponse(
                document.getId(),
                document.getPartnerCompanyId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt(),
                document.getCreatedByUsername(),
                document.getContentBase64());
    }

    private record MemberLinkage(
            UUID sponsorMemberId,
            PartnerCompanyDependentRelationship dependentRelationship) {
    }

    private record DocumentContent(String contentType, int sizeBytes, String contentBase64) {
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchFilter(String query) {
        return query == null ? "" : query.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
