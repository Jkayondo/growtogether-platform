package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.eds.Document;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentRepository;
import africa.growtogether.platform.eds.DocumentVersion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdsDocumentAttachmentGatewayTest {

    @Mock
    private DocumentRepository documents;

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<DocumentVersion> versionQuery;

    private EdsDocumentAttachmentGateway gateway;

    private UUID tenantId;
    private UUID documentId;

    @BeforeEach
    void setUp() {

        gateway =
                new EdsDocumentAttachmentGateway(
                        documents,
                        identity,
                        entityManager
                );

        tenantId = UUID.randomUUID();
        documentId = UUID.randomUUID();

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );
    }

    @Test
    void historicalReadReturnsExactRequestedVersionNotCurrentVersion() {

        Document document =
                document(
                        DocumentClassification.INTERNAL,
                        3
                );

        stubDocument(
                document
        );

        when(
                identity.hasPermission(
                        "document.read"
                )
        ).thenReturn(
                true
        );

        DocumentVersion historical =
                new DocumentVersion(
                        tenantId,
                        documentId,
                        1,
                        "eds/internal/object-v1",
                        "checksum-v1",
                        "application/pdf",
                        12000,
                        "Initial version"
                );

        stubVersionQuery(
                historical
        );

        var result =
                gateway.requireReadableVersion(
                        documentId,
                        1
                );

        assertEquals(
                documentId,
                result.documentId()
        );

        assertEquals(
                1,
                result.versionNumber()
        );

        assertEquals(
                "application/pdf",
                result.mimeType()
        );

        assertEquals(
                12000,
                result.sizeBytes()
        );

        /*
         * The document itself is already on version 3.
         * History must still resolve version 1.
         */
        assertEquals(
                3,
                document.currentVersion()
        );
    }

    @Test
    void restrictedHistoricalReadDoesNotRequireSharePermissionAgain() {

        Document document =
                document(
                        DocumentClassification.RESTRICTED,
                        1
                );

        stubDocument(
                document
        );

        when(
                identity.hasPermission(
                        "document.read"
                )
        ).thenReturn(
                true
        );

        when(
                identity.hasPermission(
                        "document.restricted.read"
                )
        ).thenReturn(
                true
        );

        DocumentVersion version =
                new DocumentVersion(
                        tenantId,
                        documentId,
                        1,
                        "eds/restricted/object",
                        "restricted-checksum",
                        "image/jpeg",
                        6400,
                        null
                );

        stubVersionQuery(
                version
        );

        var result =
                gateway.requireReadableVersion(
                        documentId,
                        1
                );

        assertEquals(
                "image/jpeg",
                result.mimeType()
        );

        /*
         * Reading something already shared must not invoke
         * restricted sharing authorization again.
         */
        verify(
                identity,
                never()
        ).hasPermission(
                "document.restricted.share"
        );
    }

    @Test
    void missingDocumentReadPermissionRejectsHistoricalRead() {

        Document document =
                document(
                        DocumentClassification.INTERNAL,
                        1
                );

        stubDocument(
                document
        );

        when(
                identity.hasPermission(
                        "document.read"
                )
        ).thenReturn(
                false
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        gateway.requireReadableVersion(
                                documentId,
                                1
                        )
        );

        verifyNoInteractions(
                entityManager
        );
    }

    @Test
    void restrictedDocumentStillRequiresRestrictedReadPermission() {

        Document document =
                document(
                        DocumentClassification.RESTRICTED,
                        1
                );

        stubDocument(
                document
        );

        when(
                identity.hasPermission(
                        "document.read"
                )
        ).thenReturn(
                true
        );

        when(
                identity.hasPermission(
                        "document.restricted.read"
                )
        ).thenReturn(
                false
        );

        when(
                identity.hasRole(
                        "DOCUMENT_SECURITY_ADMIN"
                )
        ).thenReturn(
                false
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        gateway.requireReadableVersion(
                                documentId,
                                1
                        )
        );

        verifyNoInteractions(
                entityManager
        );
    }

    @Test
    void missingHistoricalVersionIsRejected() {

        Document document =
                document(
                        DocumentClassification.INTERNAL,
                        2
                );

        stubDocument(
                document
        );

        when(
                identity.hasPermission(
                        "document.read"
                )
        ).thenReturn(
                true
        );

        stubEmptyVersionQuery();

        assertThrows(
                java.util.NoSuchElementException.class,
                () ->
                        gateway.requireReadableVersion(
                                documentId,
                                1
                        )
        );
    }

    private Document document(
            DocumentClassification classification,
            int currentVersion
    ) {

        Document document =
                new Document(
                        tenantId,
                        "GT-EDS-TEST-" + UUID.randomUUID(),
                        "Test document",
                        classification
                );

        document.activateFirstVersion();

        for (
                int version = 1;
                version < currentVersion;
                version++
        ) {
            document.nextVersion();
        }

        return document;
    }

    private void stubDocument(
            Document document
    ) {

        when(
                documents.findByIdAndTenantId(
                        documentId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        document
                )
        );
    }

    private void stubVersionQuery(
            DocumentVersion version
    ) {

        stubQueryParameters();

        when(
                versionQuery.getResultList()
        ).thenReturn(
                List.of(
                        version
                )
        );
    }

    private void stubEmptyVersionQuery() {

        stubQueryParameters();

        when(
                versionQuery.getResultList()
        ).thenReturn(
                List.of()
        );
    }

    private void stubQueryParameters() {

        when(
                entityManager.createQuery(
                        anyString(),
                        eq(DocumentVersion.class)
                )
        ).thenReturn(
                versionQuery
        );

        when(
                versionQuery.setParameter(
                        "tenantId",
                        tenantId
                )
        ).thenReturn(
                versionQuery
        );

        when(
                versionQuery.setParameter(
                        "documentId",
                        documentId
                )
        ).thenReturn(
                versionQuery
        );

        when(
                versionQuery.setParameter(
                        "versionNumber",
                        1
                )
        ).thenReturn(
                versionQuery
        );
    }
}
