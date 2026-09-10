package com.hopital.organization.patrimony;

import static com.hopital.organization.patrimony.PatrimonyRepository.params;
import com.hopital.organization.application.dto.PageResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class HospitalizationBoardService {
    private final PatrimonyRepository db;
    public HospitalizationBoardService(PatrimonyRepository db) { this.db=db; }
    private static final String JOINS="""
        FROM patrimony_assets a JOIN hospitals h ON h.id=a.hospital_id
        LEFT JOIN patrimony_locations r ON r.id=a.location_id
        LEFT JOIN patrimony_locations p ON p.id=r.parent_id
        LEFT JOIN patrimony_locations g ON g.id=p.parent_id
        LEFT JOIN patrimony_bed_stays s ON s.asset_id=a.id AND s.status<>'RELEASED'
        """;
    private static final String STATE="""
        CASE WHEN s.status IS NOT NULL THEN s.status
        WHEN a.status<>'AVAILABLE' THEN a.status WHEN a.cleaning_required THEN 'CLEANING'
        WHEN r.id IS NULL OR r.kind<>'ROOM' OR NOT r.active THEN 'UNLOCATED' ELSE 'FREE' END
        """;
    public Map<String,Object> board(int page,int size,String query,UUID hospital,UUID building,UUID room,String service,String state,PatrimonyScope scope) {
        if(!scope.allows("read"))scope.require("beds");
        hospital=scope.administrator()?hospital:scope.hospital(hospital);page=Math.max(0,page);size=Math.min(60,Math.max(1,size));
        var p=params("limit",size,"offset",(long)page*size,"hospital",hospital,"building",building,"room",room,
            "query","%"+bounded(query)+"%","service","%"+bounded(service)+"%","state",state);
        String where=" WHERE a.category_code='BED'";
        if(hospital!=null)where+=" AND a.hospital_id=:hospital";
        if(building!=null)where+=" AND (CASE WHEN p.kind='BUILDING' THEN p.id WHEN g.kind='BUILDING' THEN g.id END)=:building";
        if(room!=null)where+=" AND r.id=:room";
        where+=" AND coalesce(r.service_name,'') ILIKE :service";
        where+=" AND (a.code ILIKE :query OR a.name ILIKE :query OR r.name ILIKE :query OR p.name ILIKE :query OR g.name ILIKE :query";
        if(scope.allows("beds"))where+=" OR s.patient_name ILIKE :query OR s.patient_code ILIKE :query OR s.passage_code ILIKE :query";
        where+=")";
        var counts=db.list("SELECT "+STATE+" state,count(*) total "+JOINS+where+" GROUP BY 1",p);
        if(state!=null&&!state.isBlank())where+=" AND ("+STATE+")=:state";
        long total=db.count("SELECT count(*) "+JOINS+where,p);
        String care=scope.allows("beds")?",s.passage_id,s.passage_code,s.patient_name,s.patient_code":"";
        var beds=db.list("SELECT a.id,a.code,a.name,a.hospital_id,h.name hospital_name,r.id room_id,r.name room_name,r.service_name,"+
            "CASE WHEN p.kind='BUILDING' THEN p.name WHEN g.kind='BUILDING' THEN g.name END building_name,"+STATE+" state"+care+" "+JOINS+where+
            " ORDER BY h.name,r.name NULLS LAST,a.code,a.id LIMIT :limit OFFSET :offset",p);
        return Map.of("beds",new PageResponse<>(beds,page,size,total,(int)Math.ceil((double)total/size)),"counts",counts);
    }
    public PageResponse<Map<String,Object>> locations(String kind,String query,UUID hospital,UUID building,int page,int size,PatrimonyScope scope) {
        if(!scope.allows("read"))scope.require("beds");hospital=scope.administrator()?hospital:scope.hospital(hospital);
        if(!java.util.Set.of("ROOM","BUILDING").contains(kind))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
        page=Math.max(0,page);size=Math.min(60,Math.max(1,size));
        var p=params("kind",kind,"hospital",hospital,"building",building,"query","%"+bounded(query)+"%","limit",size,"offset",(long)page*size);
        String from=" FROM patrimony_locations r LEFT JOIN patrimony_locations p ON p.id=r.parent_id LEFT JOIN patrimony_locations g ON g.id=p.parent_id WHERE r.active AND r.kind=:kind AND r.name ILIKE :query";
        if(hospital!=null)from+=" AND r.hospital_id=:hospital";
        if(building!=null&&"ROOM".equals(kind))from+=" AND (p.id=:building OR g.id=:building)";
        long total=db.count("SELECT count(*)"+from,p);
        return new PageResponse<>(db.list("SELECT r.id,r.name,r.service_name"+from+" ORDER BY r.name,r.id LIMIT :limit OFFSET :offset",p),page,size,total,(int)Math.ceil((double)total/size));
    }
    private static String bounded(String value) { return value==null?"":value.trim().substring(0,Math.min(200,value.trim().length())); }
}
