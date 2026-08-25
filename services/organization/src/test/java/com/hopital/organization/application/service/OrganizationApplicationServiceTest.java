package com.hopital.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hopital.organization.application.domain.HospitalType;
import com.hopital.organization.application.domain.LaboratoryStructureType;
import com.hopital.organization.application.dto.CreateHospitalRequest;
import com.hopital.organization.application.dto.CreateLaboratoryStructureRequest;
import com.hopital.organization.application.dto.CreateProvinceRequest;
import com.hopital.organization.application.dto.CreateReferenceLaboratoryRequest;
import com.hopital.organization.application.dto.UpdateOrganizationStatusRequest;
import com.hopital.organization.application.exception.DuplicateOrganizationException;
import com.hopital.organization.infra.persistence.entity.HealthZoneEntity;
import com.hopital.organization.infra.persistence.entity.HospitalEntity;
import com.hopital.organization.infra.persistence.entity.ProvinceEntity;
import com.hopital.organization.infra.persistence.entity.ReferenceLaboratoryEntity;
import com.hopital.organization.infra.persistence.repository.HealthZoneRepository;
import com.hopital.organization.infra.persistence.repository.HospitalRepository;
import com.hopital.organization.infra.persistence.repository.LaboratoryStructureRepository;
import com.hopital.organization.infra.persistence.repository.ProvinceRepository;
import com.hopital.organization.infra.persistence.repository.ReferenceLaboratoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationApplicationServiceTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private HealthZoneRepository healthZoneRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private ReferenceLaboratoryRepository referenceLaboratoryRepository;

    @Mock
    private LaboratoryStructureRepository laboratoryStructureRepository;

    @InjectMocks
    private OrganizationApplicationService organizationApplicationService;

    @Test
    void createsAProvinceWithANormalizedCode() {
        when(provinceRepository.existsByCodeIgnoreCase("KIN")).thenReturn(false);
        when(provinceRepository.save(any(ProvinceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = organizationApplicationService.createProvince(new CreateProvinceRequest(" kin ", "Kinshasa"));

        assertThat(response.code()).isEqualTo("KIN");
        assertThat(response.name()).isEqualTo("Kinshasa");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createsAHospitalInsideItsHealthZone() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        HealthZoneEntity healthZone = new HealthZoneEntity("KINSENSO", "Kinsenso", province);
        when(hospitalRepository.existsByCodeIgnoreCase("HGR-KIN-001")).thenReturn(false);
        when(healthZoneRepository.findByCodeIgnoreCase("KINSENSO")).thenReturn(Optional.of(healthZone));
        when(hospitalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = organizationApplicationService.createHospital(new CreateHospitalRequest(
                "hgr-kin-001",
                "Hôpital général de référence de Kinsenso",
                HospitalType.GENERAL_REFERENCE,
                "kinsenso",
                "Avenue de la Santé",
                "+243810000000"));

        assertThat(response.code()).isEqualTo("HGR-KIN-001");
        assertThat(response.provinceCode()).isEqualTo("KIN");
        assertThat(response.healthZoneCode()).isEqualTo("KINSENSO");
    }

    @Test
    void rejectsADuplicateProvinceCode() {
        when(provinceRepository.existsByCodeIgnoreCase("KIN")).thenReturn(true);

        assertThatThrownBy(() -> organizationApplicationService.createProvince(new CreateProvinceRequest("kin", "Kinshasa")))
                .isInstanceOf(DuplicateOrganizationException.class)
                .hasMessageContaining("KIN");
    }

    @Test
    void deactivatesAProvince() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        when(provinceRepository.findByCodeIgnoreCase("KIN")).thenReturn(Optional.of(province));

        var response = organizationApplicationService.updateProvinceStatus("kin", new UpdateOrganizationStatusRequest(false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void deactivatesAHealthZone() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        HealthZoneEntity healthZone = new HealthZoneEntity("KINSENSO", "Kinsenso", province);
        when(healthZoneRepository.findByCodeIgnoreCase("KINSENSO")).thenReturn(Optional.of(healthZone));

        var response = organizationApplicationService.updateHealthZoneStatus(
                "kinsenso", new UpdateOrganizationStatusRequest(false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void deactivatesAHospital() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        HealthZoneEntity healthZone = new HealthZoneEntity("KINSENSO", "Kinsenso", province);
        HospitalEntity hospital = new HospitalEntity(
                UUID.randomUUID(),
                "HGR-KIN-001",
                "Hôpital général de référence de Kinsenso",
                HospitalType.GENERAL_REFERENCE,
                healthZone,
                null,
                null);
        when(hospitalRepository.findByCodeIgnoreCase("HGR-KIN-001")).thenReturn(Optional.of(hospital));

        var response = organizationApplicationService.updateHospitalStatus(
                "hgr-kin-001", new UpdateOrganizationStatusRequest(false));

        assertThat(response.active()).isFalse();
    }

    @Test
    void createsAReferenceLaboratoryInsideItsProvince() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        when(referenceLaboratoryRepository.existsByCodeIgnoreCase("LRP-KIN")).thenReturn(false);
        when(provinceRepository.findByCodeIgnoreCase("KIN")).thenReturn(Optional.of(province));
        when(referenceLaboratoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = organizationApplicationService.createReferenceLaboratory(new CreateReferenceLaboratoryRequest(
                "lrp-kin",
                "Laboratoire provincial de référence de Kinshasa",
                "kin",
                "Avenue de la Recherche",
                "+243810000001"));

        assertThat(response.code()).isEqualTo("LRP-KIN");
        assertThat(response.provinceCode()).isEqualTo("KIN");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createsALaboratoryStructureInsideItsReferenceLaboratory() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        ReferenceLaboratoryEntity referenceLaboratory = new ReferenceLaboratoryEntity(
                UUID.randomUUID(),
                "LRP-KIN",
                "Laboratoire provincial de référence de Kinshasa",
                province,
                null,
                null);
        when(laboratoryStructureRepository.existsByCodeIgnoreCase("HEMATO")).thenReturn(false);
        when(referenceLaboratoryRepository.findByCodeIgnoreCase("LRP-KIN"))
                .thenReturn(Optional.of(referenceLaboratory));
        when(laboratoryStructureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = organizationApplicationService.createLaboratoryStructure(new CreateLaboratoryStructureRequest(
                "hemato", "Hématologie", LaboratoryStructureType.DEPARTMENT, "lrp-kin"));

        assertThat(response.code()).isEqualTo("HEMATO");
        assertThat(response.referenceLaboratoryCode()).isEqualTo("LRP-KIN");
        assertThat(response.type()).isEqualTo(LaboratoryStructureType.DEPARTMENT);
    }

    @Test
    void deactivatesAReferenceLaboratory() {
        ProvinceEntity province = new ProvinceEntity("KIN", "Kinshasa");
        ReferenceLaboratoryEntity referenceLaboratory = new ReferenceLaboratoryEntity(
                UUID.randomUUID(), "LRP-KIN", "Laboratoire provincial de référence", province, null, null);
        when(referenceLaboratoryRepository.findByCodeIgnoreCase("LRP-KIN"))
                .thenReturn(Optional.of(referenceLaboratory));

        var response = organizationApplicationService.updateReferenceLaboratoryStatus(
                "lrp-kin", new UpdateOrganizationStatusRequest(false));

        assertThat(response.active()).isFalse();
    }
}
