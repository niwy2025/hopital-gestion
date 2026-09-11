package com.hopital.patient.api;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** Références administratives privées : ni observations ni résultats médicaux. */
@RestController
@RequestMapping("/internal/patients/billing/passages")
@Transactional(readOnly=true)
public class InternalBillingController {
    private final PatientPassageRepository passages;
    public InternalBillingController(PatientPassageRepository passages){this.passages=passages;}
    public record Reference(UUID id,String code,UUID patientId,String patientCode,String patientName,UUID hospitalId,String hospitalCode,String status,Instant arrivedAt,Instant closedAt){}
    @GetMapping("/{id}")
    public Reference get(@PathVariable("id") UUID id){return reference(passages.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)));}
    @GetMapping("/search")
    public PageResponse<Reference> search(@RequestParam("hospitalId") UUID hospital,
        @RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,
        @RequestParam(name="query",defaultValue="") String query){
        var result=passages.searchBilling(hospital,query.trim().substring(0,Math.min(200,query.trim().length())),
            PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by(Sort.Order.desc("arrivedAt"),Sort.Order.desc("id"))));
        return new PageResponse<>(result.getContent().stream().map(this::reference).toList(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages());
    }
    private Reference reference(PatientPassageEntity p){
        var a=p.getPatient();
        String name=Stream.of(a.getLastName(),a.getMiddleName(),a.getFirstName()).filter(v->v!=null&&!v.isBlank()).collect(Collectors.joining(" "));
        return new Reference(p.getId(),p.getCode(),a.getId(),a.getCode(),name,p.getHospitalId(),p.getHospitalCode(),p.getStatus().name(),p.getArrivedAt(),p.getClosedAt());
    }
}
