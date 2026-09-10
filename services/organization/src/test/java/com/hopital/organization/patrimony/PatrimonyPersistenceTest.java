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
@Import({PatrimonyService.class,PatrimonyRepository.class,PatrimonyJobs.class})
@EnabledIfEnvironmentVariable(named="PATRIMONY_POSTGRES_TEST",matches="true")
class PatrimonyPersistenceTest {
    @Autowired PatrimonyService service;
    @Autowired PatrimonyRepository db;
    @Autowired PatrimonyJobs jobs;
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
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(p,false,null),nurse)).hasMessageContaining("cet hôpital");
        service.move(bed,new PatrimonyRequests.Movement("FAULT",null,null,null,"Roue cassée"),steward);
        assertThatThrownBy(()->service.occupy(bed,new PatrimonyRequests.Bed(passage(),false,null),nurse)).hasMessageContaining("disponible");
    }
    @Test void databasePreventsTwoBedsForSamePassage() {
        UUID bed=asset("BED"), second=asset("BED"), p=passage();
        service.occupy(bed,new PatrimonyRequests.Bed(p,false,null),nurse);
        assertThatThrownBy(()->service.occupy(second,new PatrimonyRequests.Bed(p,false,null),nurse)).isInstanceOf(DataIntegrityViolationException.class);
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
