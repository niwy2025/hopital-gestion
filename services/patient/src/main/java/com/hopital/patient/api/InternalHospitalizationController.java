package com.hopital.patient.api;

import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Identité minimale destinée uniquement au réseau privé des services. */
@RestController
@RequestMapping("/internal/patients/passages")
public class InternalHospitalizationController {
    private final PatientPassageRepository passages;
    public InternalHospitalizationController(PatientPassageRepository passages) { this.passages=passages; }
    @GetMapping("/{id}/hospitalization-reference")
    @Transactional(readOnly=true)
    public Map<String,Object> reference(@PathVariable("id") UUID id) {
        var p=passages.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        var patient=p.getPatient(); var result=new LinkedHashMap<String,Object>();
        result.put("passageId",p.getId());result.put("passageCode",p.getCode());result.put("patientCode",patient.getCode());
        result.put("patientName",java.util.stream.Stream.of(patient.getLastName(),patient.getMiddleName(),patient.getFirstName())
                .filter(v->v!=null&&!v.isBlank()).collect(java.util.stream.Collectors.joining(" ")));
        result.put("hospitalId",p.getHospitalId());result.put("status",p.getStatus());
        result.put("serviceName",p.getServiceName());result.put("closedAt",p.getClosedAt());
        return result;
    }
}
