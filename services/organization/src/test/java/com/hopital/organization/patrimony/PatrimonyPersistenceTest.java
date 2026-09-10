package com.hopital.organization.patrimony;

import static com.hopital.organization.patrimony.PatrimonyRepository.id;
import static com.hopital.organization.patrimony.PatrimonyRepository.params;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

@JdbcTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({PatrimonyService.class,PatrimonyRepository.class,PatrimonyJobs.class,HospitalizationBoardService.class})
@EnabledIfEnvironmentVariable(named="PATRIMONY_POSTGRES_TEST",matches="true")
class PatrimonyPersistenceTest {
    @Autowired PatrimonyService service;
    @Autowired PatrimonyRepository db;
    @Autowired PatrimonyJobs jobs;
    @Autowired HospitalizationBoardService board;
    @MockBean PatrimonyClients clients;
    UUID hospital=UUID.randomUUID(), other=UUID.randomUUID(), room;
    PatrimonyScope steward=scope("INTENDANT",hospital,"intendant");
    PatrimonyScope nurse=scope("NURSE",hospital,"infirmier");
    PatrimonyScope admin=scope("HOSPITAL_ADMIN",hospital,"directeur");
    static PatrimonyScope scope(String role,UUID hospital,String actor) { return new PatrimonyScope(false,hospital,Set.of(role),actor,actor); }

    @BeforeEach void fixtures() {
        UUID province=UUID.randomUUID(),zone=UUID.randomUUID();
        db.update("INSERT INTO provinces(id,code,name) VALUES(:id,:code,'Province test')",params("id",province,"code",province.toString().substring(0,8)));
        db.update("INSERT INTO health_zones(id,code,name,province_id) VALUES(:id,:code,'Zone test',:province)",params("id",zone,"code",zone.toString().substring(0,8),"province",province));
        for(UUID h:Set.of(hospital,other))db.update("INSERT INTO hospitals(id,code,name,type,health_zone_id) VALUES(:id,:code,'Hôpital test','GENERAL_HOSPITAL',:zone)",params("id",h,"code",h.toString().substring(0,8),"zone",zone));
        var building=service.createLocation(new PatrimonyRequests.Location(hospital,null,"BUILDING","Pavillon A",null),steward);
        room=id(service.createLocation(new PatrimonyRequests.Location(hospital,id(building,"id"),"ROOM","Salle 1","Médecine"),steward),"id");
    }
    PatrimonyRequests.Asset request(String category,UUID at,Integer version) {
        return new PatrimonyRequests.Asset(hospital,category,"Lit test",null,null,"SERIE",at,"PURCHASE",null,null,
                LocalDate.now(),null,new BigDecimal("100.00"),"USD",null,null,null,version);
    }
    UUID asset(String category) { return id(service.saveAsset(null,request(category,room,null),steward),"id"); }
    UUID passage() {
        UUID passage=UUID.randomUUID();
        when(clients.passage(passage)).thenReturn(new PatrimonyClients.Passage(passage,"PAS-TEST","PAT-TEST",hospital,"OPEN"));
        return passage;
    }
    @Test void createsUuidCodeAndAllSearchesRespectPaginationAndHospital() {
        UUID key=asset("BED");asset("MOBILITY");
        assertThat(key.toString()).hasSize(36);
        assertThat(service.asset(key,steward).get("code").toString()).startsWith("INV-");
        var page=service.search("assets",0,1,"lit",null,"","",null,null,steward);
        assertThat(page.items()).hasSize(1);assertThat(page.totalElements()).isEqualTo(2);
        assertThat(service.search("assets",1,1,"lit",null,"","",null,null,steward).items()).hasSize(1);
        assertThat(service.search("assets",0,20,"",null,"","BED",room,null,steward).totalElements()).isEqualTo(1);
        for(String resource:Set.of("locations","assets","beds","events","operations","documents")) {
            assertThat(service.search(resource,0,20,"",null,"","",null,null,scope("INTENDANT",other,"autre")).totalElements()).isZero();
            service.search(resource,0,20,"",null,"","",null,key,steward);
        }
        assertThatThrownBy(()->service.asset(key,scope("INTENDANT",other,"autre"))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.asset(key,nurse)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.search("assets",0,20,"",null,"","",null,null,scope("INTENDANT",null,"sans-hopital"))).isInstanceOf(ResponseStatusException.class);
    }
    @Test void preventsStaleEditAndClosingOccupiedLocationHierarchy() {
        UUID key=asset("BED");
        service.saveAsset(key,request("BED",room,0),steward);
        assertThatThrownBy(()->service.saveAsset(key,request("BED",room,0),steward)).hasMessageContaining("modifiée");
        assertThatThrownBy(()->service.locationStatus(room,false,steward)).hasMessageContaining("équipements");
        var foreign=service.createLocation(new PatrimonyRequests.Location(other,null,"BUILDING","Autre",null),scope("INTENDANT",other,"autre"));
        assertThatThrownBy(()->service.move(key,new PatrimonyRequests.Movement("MOVE",id(foreign,"id"),null,null,"Test"),steward)).hasMessageContaining("cet hôpital");
    }
    @Test void reservesConfirmsAndReleasesBedWithCleaningAndMinimalDisclosure() {
        UUID bed=asset("BED"), passage=passage();
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(passage,false,null),steward)).isInstanceOf(ResponseStatusException.class);
        service.occupy(bed,new PatrimonyRequests.Bed(passage,true,"Réservation"),nurse);
        assertThat(service.occupy(bed,new PatrimonyRequests.Bed(passage,false,null),nurse).get("status")).isEqualTo("OCCUPIED");
        var hidden=service.search("beds",0,20,"",null,"","",null,null,steward).items().get(0);
        assertThat(hidden.get("passageId")).isNull();assertThat(hidden.get("patientCode")).isNull();
        assertThat(service.bed(bed,nurse)).doesNotContainKeys("purchaseCost","createdById","notes");
        assertThatThrownBy(()->service.move(bed,new PatrimonyRequests.Movement("LOAN",null,"Association",LocalDate.now().plusDays(2),"Prêt"),steward)).hasMessageContaining("Libérez");
        service.release(bed,"Sortie de la salle",nurse);
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(true);
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(passage,false,null),nurse)).hasMessageContaining("disponible");
        service.move(bed,new PatrimonyRequests.Movement("CLEANED",null,null,null,"Désinfection terminée"),steward);
        service.occupy(bed,new PatrimonyRequests.Bed(passage,false,null),nurse);
    }
    @Test void refusesAnotherHospitalPassageAndUnavailableBed() {
        UUID bed=asset("BED"), p=UUID.randomUUID();
        when(clients.passage(p)).thenReturn(new PatrimonyClients.Passage(p,"PAS","PAT",other,"OPEN"));
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(p,false,null),nurse)).hasMessageContaining("autre hôpital");
        service.move(bed,new PatrimonyRequests.Movement("FAULT",null,null,null,"Roue cassée"),steward);
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(passage(),false,null),nurse)).hasMessageContaining("disponible");
    }
    @Test void databasePreventsTwoBedsForSamePassage() {
        UUID bed=asset("BED"), second=asset("BED"), p=passage();
        service.occupy(bed,new PatrimonyRequests.Bed(p,false,null),nurse);
        assertThatThrownBy(()->service.occupy(second,new PatrimonyRequests.Bed(p,false,null),nurse)).isInstanceOf(ResponseStatusException.class);
    }
    @Test void bedChangeKeepsPreviousBedOnConflictAndRejectsStaleScreen() {
        UUID bed=asset("BED"), target=asset("BED"), available=asset("BED"), p=passage();
        var first=service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(bed,null,false,"Installation"),nurse);
        service.occupy(target,new PatrimonyRequests.Bed(passage(),false,"Autre patient"),nurse);
        assertThatThrownBy(()->service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(target,id(first,"id"),false,"Déplacement"),nurse)).hasMessageContaining("ancien lit");
        assertThat(db.one("SELECT * FROM patrimony_bed_stays WHERE id=:id",params("id",id(first,"id"))).get("status")).isEqualTo("OCCUPIED");
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(false);
        assertThatThrownBy(()->service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(available,null,false,"Écran périmé"),nurse)).hasMessageContaining("changé");
        assertThatThrownBy(()->service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(available,id(first,"id"),true,"Réserver un autre lit"),nurse)).hasMessageContaining("simple réservation");
        var moved=service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(available,id(first,"id"),false,"Isolement"),nurse);
        assertThat(id(moved,"assetId")).isEqualTo(available);
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(true);
        var history=db.one("SELECT * FROM patrimony_bed_stays WHERE id=:id",params("id",id(first,"id")));
        assertThat(history.get("releaseReason")).isEqualTo("BED_CHANGE");
        assertThat(history.get("note")).isEqualTo("Installation");
        assertThat(history.get("releaseNote")).isEqualTo("Isolement");
        assertThat(history.get("occupiedAt")).isNotNull();assertThat(history.get("endedAt")).isNotNull();
        assertThat(history.get("endedAt")).isEqualTo(moved.get("startedAt"));
        assertThat(moved.get("occupiedAt")).isEqualTo(moved.get("startedAt"));
        assertThatThrownBy(()->service.releasePassageBed(p,new PatrimonyRequests.BedRelease(id(first,"id"),"Ancien écran"),nurse)).hasMessageContaining("changé");
    }
    @Test void cancellationOfReservationDoesNotRequireCleaningAndPreservesLocationHistory() {
        UUID bed=asset("BED"), p=passage();
        var first=service.assignPassageBed(p,new PatrimonyRequests.BedAssignment(bed,null,true,"Admission prévue"),nurse);
        service.releasePassageBed(p,new PatrimonyRequests.BedRelease(id(first,"id"),"Annulation"),nurse);
        service.releasePassageBed(p,new PatrimonyRequests.BedRelease(id(first,"id"),"Doublon"),nurse);
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(false);
        UUID building=id(db.one("SELECT parent_id FROM patrimony_locations WHERE id=:id",params("id",room)),"parentId");
        UUID newRoom=id(service.createLocation(new PatrimonyRequests.Location(hospital,building,"ROOM","Autre salle","Chirurgie"),steward),"id");
        service.move(bed,new PatrimonyRequests.Movement("MOVE",newRoom,null,null,"Déplacement matériel"),steward);
        var old=db.one("SELECT * FROM patrimony_bed_stays WHERE id=:id",params("id",id(first,"id")));
        assertThat(old.get("roomId")).isEqualTo(room.toString());
        assertThat(old.get("occupiedAt")).isNull();
        assertThatThrownBy(()->service.hospitalization(p,0,10,steward)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.hospitalization(p,0,10,scope("NURSE",other,"autre"))).isInstanceOf(ResponseStatusException.class);
        var record=service.hospitalization(p,0,1,nurse);
        assertThat(record.get("current")).isNull();
        assertThat(((com.hopital.organization.application.dto.PageResponse<?>)record.get("history")).totalElements()).isEqualTo(1);
    }
    @Test void boardPaginatesFiltersAndNeverDisclosesPatientIdentityToSteward() {
        UUID bed=asset("BED"),p=passage();asset("BED");
        when(clients.passage(p)).thenReturn(new PatrimonyClients.Passage(p,"PAS-SECRET","PAT-SECRET",hospital,"OPEN","Identité confidentielle","Médecine",null));
        service.occupy(bed,new PatrimonyRequests.Bed(p,false,"Installation"),nurse);
        var care=board.board(0,1,"",null,null,room,"","",nurse);
        var page=(com.hopital.organization.application.dto.PageResponse<?>)care.get("beds");
        assertThat(page.items()).hasSize(1);assertThat(page.totalElements()).isEqualTo(2);
        assertThat(board.board(0,12,"SECRET",null,null,null,"","",nurse).toString()).contains("Identité confidentielle");
        assertThat(board.board(0,12,"",null,null,null,"","",steward).toString()).doesNotContain("patientName","patientCode","passageId","SECRET","confidentielle");
        var hidden=(com.hopital.organization.application.dto.PageResponse<?>)board.board(0,12,"SECRET",null,null,null,"","",steward).get("beds");
        assertThat(hidden.totalElements()).isZero();
        var otherPage=(com.hopital.organization.application.dto.PageResponse<?>)board.board(0,12,"",null,null,null,"","",scope("NURSE",other,"autre")).get("beds");
        assertThat(otherPage.totalElements()).isZero();
        assertThat(((com.hopital.organization.application.dto.PageResponse<?>)board.board(0,12,"",null,null,null,"","FREE",nurse).get("beds")).totalElements()).isEqualTo(1);
        UUID building=id(db.one("SELECT parent_id FROM patrimony_locations WHERE id=:id",params("id",room)),"parentId");
        assertThat(((com.hopital.organization.application.dto.PageResponse<?>)board.board(0,12,"",null,building,room,"Médecine","",nurse).get("beds")).totalElements()).isEqualTo(2);
        assertThat(((com.hopital.organization.application.dto.PageResponse<?>)board.board(0,12,"",null,UUID.randomUUID(),null,"","",nurse).get("beds")).totalElements()).isZero();
        assertThat(board.locations("ROOM","Salle",null,building,0,30,nurse).items()).hasSize(1);
        assertThat(board.locations("ROOM","Salle",null,UUID.randomUUID(),0,30,nurse).items()).isEmpty();
        assertThatThrownBy(()->board.board(0,12,"",other,null,null,"","",nurse)).isInstanceOf(ResponseStatusException.class);
        assertThat(board.board(0,12,"",null,null,null,"","",new PatrimonyScope(true,null,Set.of("ADMIN"),"admin","admin"))).containsKeys("beds","counts");
    }
    @Test void reconciliationIsIdempotentAndDoesNotReleaseReopenedPassage() {
        UUID bed=asset("BED"),p=passage();
        service.occupy(bed,new PatrimonyRequests.Bed(p,false,"Installation"),nurse);
        service.reconcilePassageBed(p); // sortie ancienne, mais passage actuellement ouvert
        assertThat(service.bed(bed,nurse).get("passageId")).isEqualTo(p.toString());
        when(clients.passage(p)).thenReturn(new PatrimonyClients.Passage(p,"PAS","PAT",hospital,"TRANSFERRED"));
        service.reconcilePassageBed(p);service.reconcilePassageBed(p);
        assertThat(service.bed(bed,nurse).get("passageId")).isNull();
        assertThat(db.count("SELECT count(*) FROM patrimony_events WHERE asset_id=:id AND kind='BED_RELEASED'",params("id",bed))).isEqualTo(1);
    }
    @Test void concurrentChangesToSameTargetKeepOneWinnerAndOtherPatientsOldBed() throws Exception {
        UUID first=asset("BED"),second=asset("BED"),target=asset("BED"),p1=passage(),p2=passage();
        UUID s1=id(service.occupy(first,new PatrimonyRequests.Bed(p1,false,"Installation"),nurse),"id");
        UUID s2=id(service.occupy(second,new PatrimonyRequests.Bed(p2,false,"Installation"),nurse),"id");
        // Les transactions des deux utilisateurs doivent voir les mêmes fixtures.
        org.springframework.test.context.transaction.TestTransaction.flagForCommit();
        org.springframework.test.context.transaction.TestTransaction.end();
        var gate=new java.util.concurrent.CountDownLatch(1);
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var f1=executor.submit(()->{gate.await();try{service.assignPassageBed(p1,new PatrimonyRequests.BedAssignment(target,s1,false,"Changement"),nurse);return true;}catch(ResponseStatusException e){return false;}});
            var f2=executor.submit(()->{gate.await();try{service.assignPassageBed(p2,new PatrimonyRequests.BedAssignment(target,s2,false,"Changement"),nurse);return true;}catch(ResponseStatusException e){return false;}});
            gate.countDown();boolean won1=f1.get(15,java.util.concurrent.TimeUnit.SECONDS),won2=f2.get(15,java.util.concurrent.TimeUnit.SECONDS);
            assertThat(won1^won2).isTrue();
            UUID loser=won1?p2:p1,previous=won1?second:first;
            assertThat(db.one("SELECT asset_id FROM patrimony_bed_stays WHERE passage_id=:id AND status<>'RELEASED'",params("id",loser)).get("assetId")).isEqualTo(previous.toString());
            assertThat(service.asset(previous,steward).get("cleaningRequired")).isEqualTo(false);
            assertThat(db.count("SELECT count(*) FROM patrimony_bed_stays WHERE asset_id=:id AND status<>'RELEASED'",params("id",target))).isEqualTo(1);
        } finally { org.springframework.test.context.transaction.TestTransaction.start(); }
    }
    @Test void loansReturnsAndPhysicalControlsKeepHistory() {
        UUID key=asset("BED");
        service.move(key,new PatrimonyRequests.Movement("LOAN",null,"Centre partenaire",LocalDate.now().plusDays(3),"Bordereau"),steward);
        assertThatThrownBy(()->service.move(key,new PatrimonyRequests.Movement("CLEANED",null,null,null,"Test"),steward)).isInstanceOf(ResponseStatusException.class);
        assertThat(service.asset(key,steward).get("loanRecipient")).isEqualTo("Centre partenaire");
        service.move(key,new PatrimonyRequests.Movement("RETURN",null,null,null,"Retour vérifié"),steward);
        service.move(key,new PatrimonyRequests.Movement("INVENTORY_MISSING",null,null,null,"Absent au contrôle"),steward);
        assertThat(service.asset(key,steward).get("status")).isEqualTo("AVAILABLE");
        assertThat(service.search("events",0,20,"",null,"INVENTORY_MISSING","",null,key,steward).totalElements()).isEqualTo(1);
    }
    @Test void maintenanceLifecycleAndSeparateDisposalApproval() {
        UUID key=asset("BED");
        UUID op=id(service.createOperation(key,new PatrimonyRequests.Operation("MAINTENANCE","Révision",LocalDate.now()),steward),"id");
        assertThatThrownBy(()->service.completeOperation(op,new PatrimonyRequests.CompleteOperation("COMPLETE","Test",null,null,true),steward)).hasMessageContaining("Démarrez");
        service.completeOperation(op,new PatrimonyRequests.CompleteOperation("START","Atelier",null,null,false),steward);
        service.completeOperation(op,new PatrimonyRequests.CompleteOperation("COMPLETE","Échec de réparation",BigDecimal.TEN,null,false),steward);
        assertThat(service.asset(key,steward).get("status")).isEqualTo("FAULTY");
        UUID disposal=id(service.createOperation(key,new PatrimonyRequests.Operation("RETIREMENT","Irréparable",null),admin),"id");
        assertThatThrownBy(()->service.completeOperation(disposal,new PatrimonyRequests.CompleteOperation("COMPLETE","Accord",null,null,false),admin)).hasMessageContaining("autre administrateur");
        service.completeOperation(disposal,new PatrimonyRequests.CompleteOperation("COMPLETE","Accord motivé",null,null,false),scope("HOSPITAL_ADMIN",hospital,"second-directeur"));
        assertThat(service.asset(key,steward).get("status")).isEqualTo("RETIRED");
    }
    @Test void storesDocumentWithoutReturningBinaryInListAndRejectsSpoofedMime() {
        UUID key=asset("MOBILITY");
        String pdf=Base64.getEncoder().encodeToString("%PDF-1.7 test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UUID document=id(service.addDocument(key,new PatrimonyRequests.Document("MANUAL","notice.pdf","application/pdf",pdf),steward),"id");
        assertThat(service.document(document,steward).get("base64")).isEqualTo(pdf);
        assertThat(service.search("documents",0,20,"",null,"","",null,key,steward).items().get(0)).doesNotContainKeys("content","base64");
        assertThatThrownBy(()->service.addDocument(key,new PatrimonyRequests.Document("PHOTO","photo.png","image/png",pdf),steward)).hasMessageContaining("format annoncé");
    }
    @Test void retriesOutboxAndReleasesOnlyConfirmedEndedPassage() {
        UUID bed=asset("BED"), p=passage();
        UUID outbox=id(db.one("SELECT id FROM patrimony_accounting_outbox WHERE asset_id=:id",params("id",bed)),"id");
        doThrow(new ResourceAccessException("offline")).when(clients).sendAsset(bed);
        jobs.publish(outbox);
        assertThat(service.asset(bed,steward).get("accountingStatus")).isEqualTo("PENDING");
        doNothing().when(clients).sendAsset(bed); jobs.publish(outbox);jobs.publish(outbox);
        verify(clients,times(2)).sendAsset(bed);
        assertThat(service.asset(bed,steward).get("accountingStatus")).isEqualTo("DELIVERED");
        service.occupy(bed,new PatrimonyRequests.Bed(p,false,null),nurse);
        when(clients.passage(p)).thenThrow(new ResourceAccessException("offline"));jobs.reconcileBed(bed);
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(false);
        doReturn(new PatrimonyClients.Passage(p,"PAS","PAT",hospital,"TRANSFERRED")).when(clients).passage(p);jobs.reconcileBed(bed);jobs.reconcileBed(bed);
        assertThat(service.asset(bed,steward).get("cleaningRequired")).isEqualTo(true);
        assertThat(db.count("SELECT count(*) FROM patrimony_bed_stays WHERE asset_id=:id AND status='RELEASED'",params("id",bed))).isEqualTo(1);
    }
}
