package com.hopital.organization.patrimony;

import com.hopital.organization.application.dto.PageResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/patrimony")
public class PatrimonyController {
    private final PatrimonyService service; private final PatrimonyClients clients;
    public PatrimonyController(PatrimonyService service, PatrimonyClients clients) { this.service=service; this.clients=clients; }
    @GetMapping("/{resource}/search")
    public PageResponse<Map<String,Object>> search(@PathVariable("resource") String resource,
            @RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,
            @RequestParam(name="query",defaultValue="") String query,@RequestParam(name="hospitalId",required=false) UUID hospital,
            @RequestParam(name="status",defaultValue="") String status,@RequestParam(name="category",defaultValue="") String category,
            @RequestParam(name="locationId",required=false) UUID location,@RequestParam(name="assetId",required=false) UUID asset,
            @AuthenticationPrincipal Jwt jwt) { return service.search(resource,page,size,query,hospital,status,category,location,asset,clients.scope(jwt)); }
    @GetMapping("/categories") public List<Map<String,Object>> categories(@AuthenticationPrincipal Jwt jwt) { return service.categories(clients.scope(jwt)); }
    @PostMapping("/locations") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> location(@Valid @RequestBody PatrimonyRequests.Location r,@AuthenticationPrincipal Jwt jwt) { return service.createLocation(r,clients.scope(jwt)); }
    @PatchMapping("/locations/{id}/status") @ResponseStatus(HttpStatus.NO_CONTENT) public void locationStatus(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.LocationStatus r,@AuthenticationPrincipal Jwt jwt) { service.locationStatus(id,r.active(),clients.scope(jwt)); }
    @PostMapping("/assets") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> create(@Valid @RequestBody PatrimonyRequests.Asset r,@AuthenticationPrincipal Jwt jwt) { return service.saveAsset(null,r,clients.scope(jwt)); }
    @PutMapping("/assets/{id}") public Map<String,Object> update(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Asset r,@AuthenticationPrincipal Jwt jwt) { return service.saveAsset(id,r,clients.scope(jwt)); }
    @GetMapping("/assets/{id}") public Map<String,Object> get(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt) { return service.asset(id,clients.scope(jwt)); }
    @PostMapping("/assets/{id}/assignment") @ResponseStatus(HttpStatus.NO_CONTENT) public void assign(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Assignment r,@AuthenticationPrincipal Jwt jwt) { service.assign(id,r,clients.scope(jwt)); }
    @PostMapping("/assets/{id}/movements") @ResponseStatus(HttpStatus.NO_CONTENT) public void move(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Movement r,@AuthenticationPrincipal Jwt jwt) { service.move(id,r,clients.scope(jwt)); }
    @PostMapping("/assets/{id}/operations") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> operation(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Operation r,@AuthenticationPrincipal Jwt jwt) { return service.createOperation(id,r,clients.scope(jwt)); }
    @PostMapping("/operations/{id}/decision") @ResponseStatus(HttpStatus.NO_CONTENT) public void complete(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.CompleteOperation r,@AuthenticationPrincipal Jwt jwt) { service.completeOperation(id,r,clients.scope(jwt)); }
    @PostMapping("/assets/{id}/documents") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> document(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Document r,@AuthenticationPrincipal Jwt jwt) { return service.addDocument(id,r,clients.scope(jwt)); }
    @GetMapping("/documents/{id}") public Map<String,Object> download(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt) { return service.document(id,clients.scope(jwt)); }
    @PostMapping("/beds/{id}/occupancy") public Map<String,Object> occupy(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Bed r,@AuthenticationPrincipal Jwt jwt) { return service.occupy(id,r,clients.scope(jwt)); }
    @GetMapping("/beds/{id}") public Map<String,Object> bed(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt) { return service.bed(id,clients.scope(jwt)); }
    @PostMapping("/beds/{id}/release") @ResponseStatus(HttpStatus.NO_CONTENT) public void release(@PathVariable("id") UUID id,@Valid @RequestBody PatrimonyRequests.Note r,@AuthenticationPrincipal Jwt jwt) { service.release(id,r.note(),clients.scope(jwt)); }
    @GetMapping("/assets/{id}/label") public Map<String,String> label(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt) throws com.google.zxing.WriterException {
        var a=service.asset(id,clients.scope(jwt)); String code=a.get("code").toString();
        var matrix=new QRCodeWriter().encode(code,BarcodeFormat.QR_CODE,180,180);
        var svg=new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 180 180\"><rect width=\"180\" height=\"180\" fill=\"white\"/><path fill=\"black\" d=\"");
        for(int y=0;y<180;y++)for(int x=0;x<180;x++)if(matrix.get(x,y))svg.append("M").append(x).append(' ').append(y).append("h1v1h-1z");
        svg.append("\"/></svg>"); return Map.of("code",code,"base64",Base64.getEncoder().encodeToString(svg.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
