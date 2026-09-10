package com.hopital.organization.patrimony;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalPatrimonyController {
    private final PatrimonyService service;
    public InternalPatrimonyController(PatrimonyService service) { this.service=service; }
    @GetMapping("/internal/organizations/patrimony/assets/{id}/accounting-reference")
    public Map<String,Object> reference(@PathVariable("id") UUID id) { return service.accountingReference(id); }
}
