package africa.growtogether.platform.eds.admin;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentStatus;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/admin")
public class DocumentAdministrationController {
 private final DocumentAdministrationService service;
 public DocumentAdministrationController(DocumentAdministrationService service){this.service=service;}
 @GetMapping("/summary") @PreAuthorize("hasAuthority('document.admin.read')")
 public ApiResponse<DocumentAdministrationDtos.GovernanceSummary> summary(){return ApiResponses.success(service.summary());}
 @GetMapping("/governance-queue") @PreAuthorize("hasAuthority('document.governance.read')")
 public ApiResponse<DocumentAdministrationDtos.GovernancePage> queue(@RequestParam(required=false) DocumentStatus status,
   @RequestParam(required=false) DocumentClassification classification,@RequestParam(required=false) Boolean legalHold,
   @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant retentionBefore,
   @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
  return ApiResponses.success(service.queue(status,classification,legalHold,retentionBefore,page,size));
 }
 @GetMapping("/storage/health") @PreAuthorize("hasAuthority('document.storage.read')")
 public ApiResponse<DocumentAdministrationDtos.StorageHealth> storage(){return ApiResponses.success(service.storageHealth());}
}
