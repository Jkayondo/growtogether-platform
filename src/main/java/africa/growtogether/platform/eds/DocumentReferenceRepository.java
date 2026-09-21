package africa.growtogether.platform.eds;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface DocumentReferenceRepository
        extends JpaRepository<DocumentReference, UUID> {


    List<DocumentReference> findByTenantIdAndReferenceTypeAndReferenceId(
            UUID tenantId,
            String referenceType,
            UUID referenceId
    );


    boolean existsByTenantIdAndDocumentIdAndReferenceTypeAndReferenceId(
            UUID tenantId,
            UUID documentId,
            String referenceType,
            UUID referenceId
    );

}
