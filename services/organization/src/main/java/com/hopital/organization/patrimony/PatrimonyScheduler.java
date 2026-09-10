package com.hopital.organization.patrimony;

import static com.hopital.organization.patrimony.PatrimonyRepository.id;
import static com.hopital.organization.patrimony.PatrimonyRepository.params;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class PatrimonyScheduler {
    private final PatrimonyRepository db; private final PatrimonyJobs jobs;
    public PatrimonyScheduler(PatrimonyRepository db,PatrimonyJobs jobs) { this.db=db; this.jobs=jobs; }
    @Scheduled(fixedDelayString="${hospital.patrimony.accounting-delay-ms:30000}",initialDelay=30000)
    public void publish() {
        for(var row:db.list("SELECT id FROM patrimony_accounting_outbox WHERE status='PENDING' AND retry_at<=now() ORDER BY retry_at LIMIT 10",params())) jobs.publish(id(row,"id"));
    }
    @Scheduled(fixedDelayString="${hospital.patrimony.beds-delay-ms:60000}",initialDelay=60000)
    public void reconcile() {
        for(var row:db.list("SELECT asset_id FROM patrimony_bed_stays WHERE status<>'RELEASED' ORDER BY checked_at NULLS FIRST LIMIT 30",params())) jobs.reconcileBed(id(row,"assetId"));
    }
}
