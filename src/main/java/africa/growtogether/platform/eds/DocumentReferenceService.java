package africa.growtogether.platform.eds;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentReferenceService {

    private final DocumentReferenceRepository references;
    private final DocumentRepository documents;
    private final EnterpriseIdentityContext identity;

    public DocumentReferenceService(
            DocumentReferenceRepository references,
            DocumentRepository documents,
            EnterpriseIdentityContext identity
    ) {
        this.references = references;
        this.documents = documents;
        this.identity = identity;
    }

    /*
     * Canonical API.
     *
     * Tenant identity is derived from the authenticated
     * EnterpriseIdentityContext and is never supplied by the caller.
     */
    @Transactional
    public DocumentReference create(
            UUID documentId,
            String referenceType,
            UUID referenceId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        return createForTenant(
                tenantId,
                documentId,
                referenceType,
                referenceId
        );
    }

    /*
     * Compatibility API for existing internal domain consumers.
     *
     * The supplied tenant is NOT trusted. It must equal the
     * authenticated Enterprise tenant before any repository access.
     */
    @Transactional
    public DocumentReference create(
            UUID tenantId,
            UUID documentId,
            String referenceType,
            UUID referenceId
    ) {

        identity.requireTenant(
                tenantId
        );

        return createForTenant(
                tenantId,
                documentId,
                referenceType,
                referenceId
        );
    }

    private DocumentReference createForTenant(
            UUID tenantId,
            UUID documentId,
            String referenceType,
            UUID referenceId
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "documentId must not be null"
            );
        }

        if (
                referenceType == null
                || referenceType.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "referenceType must not be blank"
            );
        }

        if (referenceId == null) {
            throw new IllegalArgumentException(
                    "referenceId must not be null"
            );
        }

        String normalizedReferenceType =
                normalizeReferenceType(
                        referenceType
                );

        Document document =
                documents
                        .findByIdAndTenantId(
                                documentId,
                                tenantId
                        )
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Document not found for tenant"
                                        )
                        );

        if (
                document.documentStatus()
                        == DocumentStatus.DELETED
                || document.documentStatus()
                        == DocumentStatus.DISPOSED
        ) {
            throw new IllegalStateException(
                    "Document cannot be referenced"
            );
        }

        if (
                references
                        .existsByTenantIdAndDocumentIdAndReferenceTypeAndReferenceId(
                                tenantId,
                                documentId,
                                normalizedReferenceType,
                                referenceId
                        )
        ) {
            throw new IllegalStateException(
                    "Document reference already exists"
            );
        }

        return references.save(
                new DocumentReference(
                        tenantId,
                        documentId,
                        normalizedReferenceType,
                        referenceId
                )
        );
    }

    /*
     * Canonical lookup API.
     */
    @Transactional(readOnly = true)
    public List<DocumentReference> findByReference(
            String referenceType,
            UUID referenceId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        return findByReferenceForTenant(
                tenantId,
                referenceType,
                referenceId
        );
    }

    /*
     * Compatibility lookup API for existing internal consumers.
     *
     * The requested tenant must match the authenticated tenant.
     */
    @Transactional(readOnly = true)
    public List<DocumentReference> findByReference(
            UUID tenantId,
            String referenceType,
            UUID referenceId
    ) {

        identity.requireTenant(
                tenantId
        );

        return findByReferenceForTenant(
                tenantId,
                referenceType,
                referenceId
        );
    }

    private List<DocumentReference> findByReferenceForTenant(
            UUID tenantId,
            String referenceType,
            UUID referenceId
    ) {

        if (
                referenceType == null
                || referenceType.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "referenceType must not be blank"
            );
        }

        if (referenceId == null) {
            throw new IllegalArgumentException(
                    "referenceId must not be null"
            );
        }

        return references
                .findByTenantIdAndReferenceTypeAndReferenceId(
                        tenantId,
                        normalizeReferenceType(
                                referenceType
                        ),
                        referenceId
                );
    }

    private static String normalizeReferenceType(
            String referenceType
    ) {

        return referenceType
                .trim()
                .toUpperCase();
    }
}
