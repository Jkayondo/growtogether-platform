package africa.growtogether.platform.eds;
import java.util.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/documents")
public class DocumentLifecycleController {
 private final DocumentLifecycleService service; public DocumentLifecycleController(DocumentLifecycleService s){service=s;}
 @PostMapping @PreAuthorize("hasAuthority('document.create')") public DocumentDtos.LifecycleView create(@RequestBody DocumentDtos.CreateDocument c){return service.create(c);}
 @PostMapping("/{id}/versions") @PreAuthorize("hasAuthority('document.version.manage')") public DocumentDtos.LifecycleView version(@PathVariable UUID id,@RequestBody DocumentDtos.AddVersion c){return service.addVersion(id,c);}
 @GetMapping("/{id}/versions") @PreAuthorize("hasAuthority('document.read')") public List<DocumentVersion> versions(@PathVariable UUID id){return service.versions(id);}
 @PostMapping("/{id}/checkout") @PreAuthorize("hasAuthority('document.update')") public DocumentDtos.LifecycleView checkout(@PathVariable UUID id){return service.checkOut(id);}
 @PostMapping("/{id}/checkin") @PreAuthorize("hasAuthority('document.update')") public DocumentDtos.LifecycleView checkin(@PathVariable UUID id){return service.checkIn(id);}
 @PutMapping("/{id}/retention") @PreAuthorize("hasAuthority('document.retention.manage')") public DocumentDtos.LifecycleView retention(@PathVariable UUID id,@RequestBody DocumentDtos.RetentionRequest r){return service.retention(id,r.retentionUntil());}
 @PostMapping("/{id}/legal-hold/{enabled}") @PreAuthorize("hasAuthority('document.legalhold.manage')") public DocumentDtos.LifecycleView hold(@PathVariable UUID id,@PathVariable boolean enabled){return service.legalHold(id,enabled);}
 @PostMapping("/{id}/archive") @PreAuthorize("hasAuthority('document.archive')") public DocumentDtos.LifecycleView archive(@PathVariable UUID id){return service.archive(id);}
 @PostMapping("/{id}/restore") @PreAuthorize("hasAuthority('document.restore')") public DocumentDtos.LifecycleView restore(@PathVariable UUID id){return service.restore(id);}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('document.delete')") public DocumentDtos.LifecycleView delete(@PathVariable UUID id){return service.delete(id);}
 @DeleteMapping("/{id}/permanent") @PreAuthorize("hasAuthority('document.dispose')") public DocumentDtos.LifecycleView dispose(@PathVariable UUID id){return service.dispose(id);}
}
