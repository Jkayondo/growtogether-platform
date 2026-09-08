package africa.growtogether.platform.file;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.util.UUID;



@RestController
@RequestMapping("/api/v1/files")
public class FileRetentionController {


    private final FileRetentionService service;



    public FileRetentionController(
            FileRetentionService service
    ) {

        this.service = service;

    }



    @PutMapping("/{id}/retention")
    @PreAuthorize(
            "hasAuthority('file.retention.manage')"
    )
    public ResponseEntity<FileRecord> retention(
            @PathVariable UUID id,
            @RequestBody RetentionRequest request
    ) {


        return ResponseEntity.ok(
                service.setRetention(
                        id,
                        request.retentionUntil()
                )
        );

    }



    @PostMapping("/{id}/legal-hold/{enabled}")
    @PreAuthorize(
            "hasAuthority('file.legalhold.manage')"
    )
    public ResponseEntity<FileRecord> legalHold(
            @PathVariable UUID id,
            @PathVariable boolean enabled
    ) {


        return ResponseEntity.ok(
                service.legalHold(
                        id,
                        enabled
                )
        );

    }



    @DeleteMapping("/{id}/dispose")
    @PreAuthorize(
            "hasAuthority('file.dispose')"
    )
    public ResponseEntity<Void> dispose(
            @PathVariable UUID id
    ) {


        service.dispose(
                id
        );


        return ResponseEntity.noContent()
                .build();

    }



    public record RetentionRequest(
            Instant retentionUntil
    ) {}

}
