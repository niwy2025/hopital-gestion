package com.hopital.accounting.billing;

import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class BillingReferences {
    private final RestClient patient,organization;
    public BillingReferences(RestClient.Builder builder,@Value("${hospital.patient-service.base-url}") String patientUrl,
        @Value("${hospital.organization-service.base-url}") String organizationUrl){
        var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(2000);factory.setReadTimeout(5000);
        patient=builder.clone().requestFactory(factory).baseUrl(patientUrl).build();
        organization=builder.clone().requestFactory(factory).baseUrl(organizationUrl).build();
    }
    public record Passage(UUID id,String code,UUID patientId,String patientCode,String patientName,UUID hospitalId,String hospitalCode,String status,Instant arrivedAt,Instant closedAt){}
    public record Passages(List<Passage> items,int page,int size,long totalElements,int totalPages){}
    public record Hospital(UUID hospitalId,String hospitalCode,boolean active){}
    public Passage passage(UUID id){
        var r=patient.get().uri("/internal/patients/billing/passages/{id}",id).retrieve().body(Passage.class);
        if(r==null||!id.equals(r.id()))throw new IllegalStateException("Référence de passage indisponible.");return r;
    }
    public Passages passages(UUID hospital,int page,int size,String query){
        var r=patient.get().uri(b->b.path("/internal/patients/billing/passages/search").queryParam("hospitalId",hospital)
            .queryParam("page",page).queryParam("size",size).queryParam("query",query).build()).retrieve().body(Passages.class);
        if(r==null)throw new IllegalStateException("Registre des passages indisponible.");return r;
    }
    public void hospital(UUID id){
        var r=organization.get().uri("/internal/organizations/hospitals/{id}/access-reference",id).retrieve().body(Hospital.class);
        if(r==null||!id.equals(r.hospitalId())||!r.active())throw new IllegalStateException("L'hôpital n'est pas disponible.");
    }
}
