package africa.growtogether.platform.eip;
import java.time.Instant; import java.util.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/integration/admin")
public class EipAdministrationController {
 private final EipAdministrationService service; public EipAdministrationController(EipAdministrationService s){service=s;}
 @GetMapping("/summary") @PreAuthorize("hasAuthority('integration.admin.read')") public Map<String,Object> summary(){return service.summary();}
 @GetMapping("/certifications") @PreAuthorize("hasAuthority('integration.certification.read')") public List<ConnectorCertification> list(){return service.list();}
 @PostMapping("/connectors/{connectorId}/certifications") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('integration.certification.manage')") public ConnectorCertification create(@PathVariable UUID connectorId,@RequestBody CreateCertificationRequest r){return service.createCertification(connectorId,r.environment());}
 @PostMapping("/certifications/{id}/certify") @PreAuthorize("hasAuthority('integration.certification.manage')") public ConnectorCertification certify(@PathVariable UUID id,@RequestBody CertifyRequest r){return service.certify(id,r.evidenceReference(),r.expiresAt(),r.notes());}
 public record CreateCertificationRequest(String environment){} public record CertifyRequest(String evidenceReference, Instant expiresAt,String notes){}
}
