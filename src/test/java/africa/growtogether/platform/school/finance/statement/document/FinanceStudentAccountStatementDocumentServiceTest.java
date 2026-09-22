package africa.growtogether.platform.school.finance.statement.document;

import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.AccountStatement;
import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.AccountSummary;
import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.StatementEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.DocumentDtos;
import africa.growtogether.platform.eds.DocumentLifecycleService;
import africa.growtogether.platform.eds.DocumentReference;
import africa.growtogether.platform.eds.DocumentReferenceService;
import africa.growtogether.platform.eds.DocumentSecurityDtos;
import africa.growtogether.platform.eds.DocumentSecurityService;
import africa.growtogether.platform.file.FileStorageProvider;
import africa.growtogether.platform.file.FileUploadResult;
import africa.growtogether.platform.file.FileUploadService;
import africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

class FinanceStudentAccountStatementDocumentServiceTest {

    @Test
    void generationUsesAuthoritativeS5StatementAndCreatesEdsReference() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID accountId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        AccountStatement statement =
                statement(
                        studentId,
                        accountId,
                        new BigDecimal("250000")
                );

        when(
                fixture.statements.accountStatement(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 9, 21),
                        LocalDate.of(2026, 9, 21)
                )
        ).thenReturn(
                statement
        );

        when(
                fixture.references.findByReference(
                        eq("FINANCE_STATEMENT"),
                        any(UUID.class)
                )
        ).thenReturn(
                List.of()
        );

        FileUploadResult stored =
                mock(
                        FileUploadResult.class
                );

        when(stored.storageKey())
                .thenReturn(
                        "test/statement.txt"
                );

        when(stored.checksum())
                .thenReturn(
                        "checksum"
                );

        when(stored.mimeType())
                .thenReturn(
                        "text/plain"
                );

        when(stored.sizeBytes())
                .thenReturn(
                        123L
                );

        when(
                fixture.files.upload(
                        any(MultipartFile.class)
                )
        ).thenReturn(
                stored
        );

        when(
                fixture.documents.create(
                        any(DocumentDtos.CreateDocument.class)
                )
        ).thenReturn(
                new DocumentDtos.LifecycleView(
                        documentId,
                        "GT-FST-TEST",
                        "Learner financial statement",
                        null,
                        1,
                        null,
                        false,
                        null
                )
        );

        DocumentReference linked =
                mock(
                        DocumentReference.class
                );

        when(linked.getDocumentId())
                .thenReturn(
                        documentId
                );

        when(
                fixture.references.create(
                        eq(documentId),
                        eq("FINANCE_STATEMENT"),
                        any(UUID.class)
                )
        ).thenReturn(
                linked
        );

        var response =
                fixture.service.generate(
                        tenantId,
                        studentId,
                        "ugx",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 9, 21),
                        LocalDate.of(2026, 9, 21)
                );

        assertThat(response.documentId())
                .isEqualTo(
                        documentId
                );

        assertThat(response.studentId())
                .isEqualTo(
                        studentId
                );

        assertThat(response.studentFinancialAccountId())
                .isEqualTo(
                        accountId
                );

        assertThat(response.currencyCode())
                .isEqualTo(
                        "UGX"
                );

        assertThat(response.created())
                .isTrue();

        assertThat(response.contentSha256())
                .hasSize(64);

        verify(fixture.identity)
                .requireTenant(
                        tenantId
                );

        verify(fixture.statements)
                .accountStatement(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 9, 21),
                        LocalDate.of(2026, 9, 21)
                );

        verify(fixture.files)
                .upload(
                        any(MultipartFile.class)
                );

        verify(fixture.documents)
                .create(
                        any(DocumentDtos.CreateDocument.class)
                );
    }

    @Test
    void exactSnapshotRetryReturnsExistingDocumentBeforeSecondUpload() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID existingDocumentId =
                UUID.randomUUID();

        AccountStatement statement =
                statement(
                        studentId,
                        UUID.randomUUID(),
                        new BigDecimal("250000")
                );

        when(
                fixture.statements.accountStatement(
                        eq(tenantId),
                        eq(studentId),
                        eq("UGX"),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                statement
        );

        DocumentReference existing =
                mock(
                        DocumentReference.class
                );

        when(existing.getDocumentId())
                .thenReturn(
                        existingDocumentId
                );

        when(
                fixture.references.findByReference(
                        eq("FINANCE_STATEMENT"),
                        any(UUID.class)
                )
        ).thenReturn(
                List.of(
                        existing
                )
        );

        when(
                fixture.references.findByReference(
                        eq("FINANCE_STATEMENT_STUDENT"),
                        eq(studentId)
                )
        ).thenReturn(
                List.of(
                        existing
                )
        );

        var response =
                fixture.service.generate(
                        tenantId,
                        studentId,
                        "UGX",
                        null,
                        null,
                        LocalDate.of(2026, 9, 21)
                );

        assertThat(response.documentId())
                .isEqualTo(
                        existingDocumentId
                );

        assertThat(response.created())
                .isFalse();

        verifyNoInteractions(
                fixture.files,
                fixture.documents
        );

        verify(fixture.references, never())
                .create(
                        any(UUID.class),
                        anyString(),
                        any(UUID.class)
                );
    }

    @Test
    void tenantMismatchFailsBeforeFinancialStatementRead() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        doThrow(
                new SecurityException(
                        "Tenant access denied"
                )
        )
                .when(fixture.identity)
                .requireTenant(
                        tenantId
                );

        assertThatThrownBy(
                () ->
                        fixture.service.generate(
                                tenantId,
                                UUID.randomUUID(),
                                "UGX",
                                null,
                                null,
                                null
                        )
        )
                .isInstanceOf(
                        SecurityException.class
                );

        verifyNoInteractions(
                fixture.statements,
                fixture.files,
                fixture.documents,
                fixture.references
        );
    }

    @Test
    void deterministicRenderingProducesStableContentReference() {

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        AccountStatement statement =
                statement(
                        studentId,
                        UUID.randomUUID(),
                        new BigDecimal("250000")
                );

        byte[] first =
                FinanceStudentAccountStatementDocumentService
                        .render(
                                statement,
                                tenantId
                        );

        byte[] second =
                FinanceStudentAccountStatementDocumentService
                        .render(
                                statement,
                                tenantId
                        );

        assertThat(first)
                .containsExactly(
                        second
                );

        String firstHash =
                FinanceStudentAccountStatementDocumentService
                        .sha256(
                                first
                        );

        String secondHash =
                FinanceStudentAccountStatementDocumentService
                        .sha256(
                                second
                        );

        assertThat(firstHash)
                .isEqualTo(
                        secondHash
                );

        assertThat(
                FinanceStudentAccountStatementDocumentService
                        .statementReferenceId(
                                firstHash
                        )
        )
                .isEqualTo(
                        FinanceStudentAccountStatementDocumentService
                                .statementReferenceId(
                                        secondHash
                                )
                );
    }

    @Test
    void changedFinancialSnapshotProducesDifferentContentReference() {

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        AccountStatement firstStatement =
                statement(
                        studentId,
                        UUID.randomUUID(),
                        new BigDecimal("250000")
                );

        AccountStatement secondStatement =
                statement(
                        studentId,
                        firstStatement
                                .summary()
                                .studentFinancialAccountId(),
                        new BigDecimal("300000")
                );

        String firstHash =
                FinanceStudentAccountStatementDocumentService
                        .sha256(
                                FinanceStudentAccountStatementDocumentService
                                        .render(
                                                firstStatement,
                                                tenantId
                                        )
                        );

        String secondHash =
                FinanceStudentAccountStatementDocumentService
                        .sha256(
                                FinanceStudentAccountStatementDocumentService
                                        .render(
                                                secondStatement,
                                                tenantId
                                        )
                        );

        assertThat(firstHash)
                .isNotEqualTo(
                        secondHash
                );

        assertThat(
                FinanceStudentAccountStatementDocumentService
                        .statementReferenceId(
                                firstHash
                        )
        )
                .isNotEqualTo(
                        FinanceStudentAccountStatementDocumentService
                                .statementReferenceId(
                                        secondHash
                                )
                );
    }

    @Test
    void retrievalRequiresStatementAndStudentLinksToSameDocument() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        DocumentReference statementLink =
                mock(
                        DocumentReference.class
                );

        DocumentReference studentLink =
                mock(
                        DocumentReference.class
                );

        when(statementLink.getDocumentId())
                .thenReturn(
                        documentId
                );

        when(studentLink.getDocumentId())
                .thenReturn(
                        documentId
                );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT",
                        statementReferenceId
                )
        ).thenReturn(
                List.of(
                        statementLink
                )
        );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT_STUDENT",
                        studentId
                )
        ).thenReturn(
                List.of(
                        studentLink
                )
        );

        when(
                fixture.documentSecurity.preview(
                        documentId
                )
        ).thenReturn(
                new DocumentSecurityDtos.Preview(
                        documentId,
                        1,
                        "text/plain",
                        12L,
                        "statements/test.txt",
                        true,
                        "inline"
                )
        );

        ByteArrayResource resource =
                new ByteArrayResource(
                        "statement".getBytes()
                );

        when(
                fixture.storage.load(
                        "statements/test.txt"
                )
        ).thenReturn(
                resource
        );

        var download =
                fixture.service.retrieve(
                        tenantId,
                        studentId,
                        statementReferenceId
                );

        assertThat(download.resource())
                .isSameAs(
                        resource
                );

        assertThat(download.mimeType())
                .isEqualTo(
                        "text/plain"
                );

        assertThat(download.sizeBytes())
                .isEqualTo(
                        12L
                );

        assertThat(download.filename())
                .contains(
                        statementReferenceId.toString()
                );

        assertThat(download.inlineSupported())
                .isTrue();

        verify(fixture.identity)
                .requireTenant(
                        tenantId
                );

        verify(fixture.documentSecurity)
                .preview(
                        documentId
                );

        verify(fixture.storage)
                .load(
                        "statements/test.txt"
                );

        verifyNoInteractions(
                fixture.statements,
                fixture.files,
                fixture.documents
        );
    }

    @Test
    void retrievalFailsClosedWhenStatementIsNotLinkedToRequestedStudent() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        UUID documentId =
                UUID.randomUUID();

        DocumentReference statementLink =
                mock(
                        DocumentReference.class
                );

        when(statementLink.getDocumentId())
                .thenReturn(
                        documentId
                );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT",
                        statementReferenceId
                )
        ).thenReturn(
                List.of(
                        statementLink
                )
        );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT_STUDENT",
                        studentId
                )
        ).thenReturn(
                List.of()
        );

        assertThatThrownBy(
                () ->
                        fixture.service.retrieve(
                                tenantId,
                                studentId,
                                statementReferenceId
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "does not belong"
                );

        verifyNoInteractions(
                fixture.documentSecurity,
                fixture.storage,
                fixture.statements,
                fixture.files,
                fixture.documents
        );
    }

    @Test
    void retrievalRejectsAmbiguousStatementReference() {

        Fixture fixture =
                fixture();

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID statementReferenceId =
                UUID.randomUUID();

        DocumentReference first =
                mock(
                        DocumentReference.class
                );

        DocumentReference second =
                mock(
                        DocumentReference.class
                );

        when(
                fixture.references.findByReference(
                        "FINANCE_STATEMENT",
                        statementReferenceId
                )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        assertThatThrownBy(
                () ->
                        fixture.service.retrieve(
                                tenantId,
                                studentId,
                                statementReferenceId
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "Multiple EDS documents"
                );

        verifyNoInteractions(
                fixture.documentSecurity,
                fixture.storage
        );
    }

    private static AccountStatement statement(
            UUID studentId,
            UUID accountId,
            BigDecimal outstanding
    ) {

        AccountSummary summary =
                new AccountSummary(
                        studentId,
                        accountId,
                        "UGX",
                        LocalDate.of(2026, 9, 21),
                        new BigDecimal("500000"),
                        new BigDecimal("250000"),
                        outstanding,
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        2,
                        1
                );

        StatementEntry entry =
                new StatementEntry(
                        LocalDate.of(2026, 9, 1),
                        Instant.parse(
                                "2026-09-01T09:00:00Z"
                        ),
                        "PAYMENT",
                        UUID.randomUUID(),
                        "PAY-001",
                        new BigDecimal("250000"),
                        "CREDIT",
                        null,
                        UUID.randomUUID(),
                        "RECORDED"
                );

        return new AccountStatement(
                summary,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 9, 21),
                List.of(
                        entry
                )
        );
    }

    private static Fixture fixture() {

        FinanceStudentAccountStatementService statements =
                mock(
                        FinanceStudentAccountStatementService.class
                );

        FileUploadService files =
                mock(
                        FileUploadService.class
                );

        DocumentLifecycleService documents =
                mock(
                        DocumentLifecycleService.class
                );

        DocumentReferenceService references =
                mock(
                        DocumentReferenceService.class
                );

        DocumentSecurityService documentSecurity =
                mock(
                        DocumentSecurityService.class
                );

        FileStorageProvider storage =
                mock(
                        FileStorageProvider.class
                );

        EnterpriseIdentityContext identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        return new Fixture(
                statements,
                files,
                documents,
                references,
                documentSecurity,
                storage,
                identity,
                new FinanceStudentAccountStatementDocumentService(
                        statements,
                        files,
                        documents,
                        references,
                        documentSecurity,
                        storage,
                        identity
                )
        );
    }

    private record Fixture(
            FinanceStudentAccountStatementService statements,
            FileUploadService files,
            DocumentLifecycleService documents,
            DocumentReferenceService references,
            DocumentSecurityService documentSecurity,
            FileStorageProvider storage,
            EnterpriseIdentityContext identity,
            FinanceStudentAccountStatementDocumentService service
    ) {
    }
}
