package africa.growtogether.platform.eds;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/documents")
public class DocumentSecurityController {
 private final DocumentSecurityService service; public DocumentSecurityController(DocumentSecurityService service){this.service=service;}
 @GetMapping("/search") @PreAuthorize("hasAuthority('document.read')") public List<DocumentSecurityDtos.SearchResult> search(@RequestParam(defaultValue="") String q){return service.search(q);}
 @PutMapping("/{id}/classification") @PreAuthorize("hasAuthority('document.security.manage')") public DocumentDtos.LifecycleView classify(@PathVariable UUID id,@RequestBody DocumentSecurityDtos.ClassificationRequest r){return service.classify(id,r.classification());}
 @PostMapping("/{id}/shares") @PreAuthorize("hasAuthority('document.share')") public DocumentSecurityDtos.ShareView share(@PathVariable UUID id,@RequestBody DocumentSecurityDtos.ShareRequest r){return service.createShare(id,r);}
 @GetMapping("/{id}/shares") @PreAuthorize("hasAuthority('document.share.read')") public List<DocumentSecurityDtos.ShareView> shares(@PathVariable UUID id){return service.shares(id);}
 @DeleteMapping("/{id}/shares/{shareId}") @PreAuthorize("hasAuthority('document.share')") public ResponseEntity<Void> revoke(@PathVariable UUID id,@PathVariable UUID shareId){service.revoke(id,shareId);return ResponseEntity.noContent().build();}
 @GetMapping("/{id}/preview") @PreAuthorize("hasAuthority('document.preview')") public DocumentSecurityDtos.Preview preview(@PathVariable UUID id){return service.preview(id);}
}
