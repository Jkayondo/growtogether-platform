package africa.growtogether.platform.eds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class DocumentReferenceServiceTest {

    @Test
    void canonicalCreateUsesAuthenticatedTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Fixture fixture =
                fixture();

        when(fixture.identity.requireTenantId())
                .thenReturn(tenantId);

        Document document =
                mock(Document.class);

        when(
                fixture.documents.findByIdAndTenantId(
                        documentId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(document)
        );

        when(
                fixture.references
                        .existsByTenantIdAndDocumentIdAndReferenceTypeAndReferenceId(
                                tenantId,
                                documentId,
                                "FINANCE_STATEMENT",
                                referenceId
                        )
        ).thenReturn(false);

        when(
                fixture.references.save(
                        any(DocumentReference.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        DocumentReference result =
                fixture.service.create(
                        documentId,
                        "finance_statement",
                        referenceId
                );

        assertThat(result.getDocumentId())
                .isEqualTo(documentId);

        assertThat(result.getReferenceType())
                .isEqualTo("FINANCE_STATEMENT");

        assertThat(result.getReferenceId())
                .isEqualTo(referenceId);

        verify(fixture.identity)
                .requireTenantId();

        verify(fixture.documents)
                .findByIdAndTenantId(
                        documentId,
                        tenantId
                );
    }

    @Test
    void compatibilityCreateRequiresAuthenticatedTenantMatch() {

        UUID tenantId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Fixture fixture =
                fixture();

        Document document =
                mock(Document.class);

        when(
                fixture.documents.findByIdAndTenantId(
                        documentId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(document)
        );

        when(
                fixture.references
                        .existsByTenantIdAndDocumentIdAndReferenceTypeAndReferenceId(
                                tenantId,
                                documentId,
                                "LEARNER",
                                referenceId
                        )
        ).thenReturn(false);

        when(
                fixture.references.save(
                        any(DocumentReference.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        fixture.service.create(
                tenantId,
                documentId,
                "LEARNER",
                referenceId
        );

        verify(fixture.identity)
                .requireTenant(
                        tenantId
                );
    }

    @Test
    void compatibilityCreateFailsClosedForTenantMismatch() {

        UUID requestedTenantId =
                UUID.randomUUID();

        Fixture fixture =
                fixture();

        doThrow(
                new SecurityException(
                        "Tenant access denied"
                )
        )
                .when(fixture.identity)
                .requireTenant(
                        requestedTenantId
                );

        assertThatThrownBy(
                () ->
                        fixture.service.create(
                                requestedTenantId,
                                UUID.randomUUID(),
                                "LEARNER",
                                UUID.randomUUID()
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                );

        verify(fixture.identity)
                .requireTenant(
                        requestedTenantId
                );
    }

    @Test
    void canonicalLookupUsesAuthenticatedTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Fixture fixture =
                fixture();

        when(fixture.identity.requireTenantId())
                .thenReturn(tenantId);

        when(
                fixture.references
                        .findByTenantIdAndReferenceTypeAndReferenceId(
                                tenantId,
                                "FINANCE_STATEMENT",
                                referenceId
                        )
        ).thenReturn(
                List.of()
        );

        List<DocumentReference> result =
                fixture.service.findByReference(
                        "finance_statement",
                        referenceId
                );

        assertThat(result)
                .isEmpty();

        verify(fixture.identity)
                .requireTenantId();

        verify(fixture.references)
                .findByTenantIdAndReferenceTypeAndReferenceId(
                        tenantId,
                        "FINANCE_STATEMENT",
                        referenceId
                );
    }

    @Test
    void compatibilityLookupRequiresAuthenticatedTenantMatch() {

        UUID tenantId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();

        Fixture fixture =
                fixture();

        when(
                fixture.references
                        .findByTenantIdAndReferenceTypeAndReferenceId(
                                tenantId,
                                "LEARNER",
                                referenceId
                        )
        ).thenReturn(
                List.of()
        );

        fixture.service.findByReference(
                tenantId,
                "LEARNER",
                referenceId
        );

        verify(fixture.identity)
                .requireTenant(
                        tenantId
                );
    }

    @Test
    void compatibilityLookupFailsClosedForTenantMismatch() {

        UUID requestedTenantId =
                UUID.randomUUID();

        Fixture fixture =
                fixture();

        doThrow(
                new SecurityException(
                        "Tenant access denied"
                )
        )
                .when(fixture.identity)
                .requireTenant(
                        requestedTenantId
                );

        assertThatThrownBy(
                () ->
                        fixture.service.findByReference(
                                requestedTenantId,
                                "LEARNER",
                                UUID.randomUUID()
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                );
    }

    private static Fixture fixture() {

        DocumentReferenceRepository references =
                mock(
                        DocumentReferenceRepository.class
                );

        DocumentRepository documents =
                mock(
                        DocumentRepository.class
                );

        EnterpriseIdentityContext identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        DocumentReferenceService service =
                new DocumentReferenceService(
                        references,
                        documents,
                        identity
                );

        return new Fixture(
                references,
                documents,
                identity,
                service
        );
    }

    private record Fixture(
            DocumentReferenceRepository references,
            DocumentRepository documents,
            EnterpriseIdentityContext identity,
            DocumentReferenceService service
    ) {
    }
}
