package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.eds.Document;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentRepository;
import africa.growtogether.platform.eds.DocumentStatus;
import africa.growtogether.platform.eds.DocumentVersion;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal EDS contract for platform capabilities that need to
 * reference a governed document without receiving its storage key.
 *
 * GT Connect uses this gateway when attaching an existing EDS
 * document to a message.
 *
 * File ownership, classification and access policy remain in EDS.
 */
@Service
public class EdsDocumentAttachmentGateway {

    private final DocumentRepository documents;

    private final EnterpriseIdentityContext identity;

    private final EntityManager entityManager;

    public EdsDocumentAttachmentGateway(
            DocumentRepository documents,
            EnterpriseIdentityContext identity,
            EntityManager entityManager
    ) {
        this.documents = documents;
        this.identity = identity;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public AttachmentReference requireAttachable(
            UUID documentId
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "documentId must not be null"
            );
        }

        UUID tenantId =
                identity.requireTenantId();

        Document document =
                documents
                        .findByIdAndTenantId(
                                documentId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Document not found"
                                )
                        );

        /*
         * Internal callers do not pass through the EDS REST
         * @PreAuthorize boundary, so the gateway must preserve
         * the effective EDS read authorization itself.
         */
        requireDocumentReadPermission();

        requireReadable(
                document
        );

        /*
         * Attaching a document to GT Connect makes it available
         * to other authorised conversation participants.
         * Restricted documents therefore retain the existing
         * EDS restricted-sharing rule.
         */
        requireAttachableSharing(
                document
        );

        requireAttachableLifecycle(
                document
        );

        int currentVersion =
                document.currentVersion();

        if (currentVersion <= 0) {
            throw new IllegalStateException(
                    "Document has no attachable version"
            );
        }

        List<DocumentVersion> matching =
                entityManager
                        .createQuery(
                                """
                                select v
                                from DocumentVersion v
                                where v.tenantId = :tenantId
                                  and v.documentId = :documentId
                                  and v.versionNumber = :versionNumber
                                """,
                                DocumentVersion.class
                        )
                        .setParameter(
                                "tenantId",
                                tenantId
                        )
                        .setParameter(
                                "documentId",
                                documentId
                        )
                        .setParameter(
                                "versionNumber",
                                currentVersion
                        )
                        .getResultList();

        DocumentVersion version =
                matching
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Current EDS document version was not found"
                                )
                        );

        return new AttachmentReference(
                documentId,
                version.versionNumber(),
                version.mimeType(),
                version.sizeBytes()
        );
    }

    @Transactional(readOnly = true)
    public AttachmentReference requireReadableVersion(
            UUID documentId,
            int versionNumber
    ) {

        if (documentId == null) {
            throw new IllegalArgumentException(
                    "documentId must not be null"
            );
        }

        if (versionNumber <= 0) {
            throw new IllegalArgumentException(
                    "versionNumber must be greater than zero"
            );
        }

        UUID tenantId =
                identity.requireTenantId();

        Document document =
                documents
                        .findByIdAndTenantId(
                                documentId,
                                tenantId
                        )
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Document not found"
                                )
                        );

        /*
         * Historical attachment retrieval is read-only.
         *
         * It deliberately preserves EDS read/classification rules
         * but does not repeat the send-time restricted-sharing rule.
         * The document was already shared when the Connect message
         * was originally created.
         */
        requireDocumentReadPermission();

        requireReadable(
                document
        );

        List<DocumentVersion> matching =
                entityManager
                        .createQuery(
                                """
                                select v
                                from DocumentVersion v
                                where v.tenantId = :tenantId
                                  and v.documentId = :documentId
                                  and v.versionNumber = :versionNumber
                                """,
                                DocumentVersion.class
                        )
                        .setParameter(
                                "tenantId",
                                tenantId
                        )
                        .setParameter(
                                "documentId",
                                documentId
                        )
                        .setParameter(
                                "versionNumber",
                                versionNumber
                        )
                        .getResultList();

        DocumentVersion version =
                matching
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "EDS document version was not found"
                                )
                        );

        return new AttachmentReference(
                documentId,
                version.versionNumber(),
                version.mimeType(),
                version.sizeBytes()
        );
    }


    private void requireDocumentReadPermission() {

        if (
                identity.hasPermission(
                        "document.read"
                )
        ) {
            return;
        }

        throw new AccessDeniedException(
                "Document read permission is required"
        );
    }

    private void requireReadable(
            Document document
    ) {

        if (
                document.classification()
                        != DocumentClassification.RESTRICTED
        ) {
            return;
        }

        if (
                identity.hasRole(
                        "DOCUMENT_SECURITY_ADMIN"
                )
                        || identity.hasPermission(
                        "document.restricted.read"
                )
        ) {
            return;
        }

        throw new AccessDeniedException(
                "Document access denied"
        );
    }

    private void requireAttachableSharing(
            Document document
    ) {

        if (
                document.classification()
                        != DocumentClassification.RESTRICTED
        ) {
            return;
        }

        if (
                identity.hasPermission(
                        "document.restricted.share"
                )
        ) {
            return;
        }

        throw new AccessDeniedException(
                "Restricted documents require elevated sharing permission"
        );
    }

    private static void requireAttachableLifecycle(
            Document document
    ) {

        DocumentStatus status =
                document.documentStatus();

        if (
                status == DocumentStatus.DELETED
                        || status == DocumentStatus.DISPOSED
        ) {
            throw new IllegalStateException(
                    "Deleted or disposed documents cannot be attached"
            );
        }
    }

    public record AttachmentReference(
            UUID documentId,
            int versionNumber,
            String mimeType,
            long sizeBytes
    ) {
    }
}
