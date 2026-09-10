package com.hopital.organization.patrimony;

import static com.hopital.organization.patrimony.PatrimonyRepository.id;
import static com.hopital.organization.patrimony.PatrimonyRepository.params;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatrimonyJobs {
    private final PatrimonyRepository db; private final PatrimonyClients clients; private final PatrimonyService service;
    public PatrimonyJobs(PatrimonyRepository db,PatrimonyClients clients,PatrimonyService service) { this.db=db; this.clients=clients; this.service=service; }
    @Transactional
    public void publish(UUID key) {
        var rows=db.list("SELECT * FROM patrimony_accounting_outbox WHERE id=:id AND status='PENDING' FOR UPDATE SKIP LOCKED",params("id",key));
        if(rows.isEmpty())return;
        try {
            clients.sendAsset(id(rows.get(0),"assetId"));
            db.update("UPDATE patrimony_accounting_outbox SET status='DELIVERED',delivered_at=now(),attempts=attempts+1 WHERE id=:id",params("id",key));
        } catch(org.springframework.web.client.RestClientException e) {
            db.update("UPDATE patrimony_accounting_outbox SET attempts=attempts+1,retry_at=now()+interval '2 minutes' WHERE id=:id",params("id",key));
        }
    }
    @Transactional
    public void reconcileBed(UUID key) {
        var a=db.one("SELECT * FROM patrimony_assets WHERE id=:id FOR UPDATE",params("id",key));
        var rows=db.list("SELECT * FROM patrimony_bed_stays WHERE asset_id=:id AND status<>'RELEASED'",params("id",key));
        if(rows.isEmpty())return;
        var stay=rows.get(0);
        db.update("UPDATE patrimony_bed_stays SET checked_at=now() WHERE id=:id",params("id",id(stay,"id")));
        try {
            var passage=clients.passage(id(stay,"passageId"));
            if(Set.of("CLOSED","TRANSFERRED","CANCELLED").contains(passage.status()))
                service.releaseLocked(a,"Fin du passage",new PatrimonyScope(true,null,Set.of("ADMIN"),"system","Système"));
        } catch(org.springframework.web.client.RestClientException e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Vérification différée de l’occupation d’un lit : service patient indisponible.");
        }
    }
}
