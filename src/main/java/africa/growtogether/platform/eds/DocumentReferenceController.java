package africa.growtogether.platform.eds;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents/references")
public class DocumentReferenceController {

    private final DocumentReferenceService service;

    public DocumentReferenceController(
            DocumentReferenceService service
    ) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('document.update')")
    public DocumentReference create(
            @RequestBody CreateDocumentReferenceRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }

        return service.create(
                request.documentId(),
                request.referenceType(),
                request.referenceId()
        );
    }

    @GetMapping("/{referenceType}/{referenceId}")
    @PreAuthorize("hasAuthority('document.update')")
    public List<DocumentReference> findByReference(
            @PathVariable String referenceType,
            @PathVariable UUID referenceId
    ) {

        return service.findByReference(
                referenceType,
                referenceId
        );
    }

    public record CreateDocumentReferenceRequest(
            UUID documentId,
            String referenceType,
            UUID referenceId
    ) {
    }
}
