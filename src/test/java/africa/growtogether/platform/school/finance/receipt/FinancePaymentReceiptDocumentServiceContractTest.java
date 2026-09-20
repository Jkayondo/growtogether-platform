package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentDtos;
import africa.growtogether.platform.eds.DocumentLifecycleService;
import africa.growtogether.platform.eds.DocumentSecurityDtos;
import africa.growtogether.platform.eds.DocumentSecurityService;
import africa.growtogether.platform.eds.DocumentStatus;
import africa.growtogether.platform.file.FileStorageProvider;
import africa.growtogether.platform.file.FileUploadResult;
import africa.growtogether.platform.file.FileUploadService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

class FinancePaymentReceiptDocumentServiceContractTest {

    private static final UUID TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID OTHER_TENANT =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID ACTOR =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID PAYMENT =
            UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            );

    private static final UUID STUDENT =
            UUID.fromString(
                    "55555555-5555-5555-5555-555555555555"
            );

    private static final UUID RECEIPT =
            UUID.fromString(
                    "66666666-6666-6666-6666-666666666666"
            );

    private static final UUID DOCUMENT =
            UUID.fromString(
                    "77777777-7777-7777-7777-777777777777"
            );

    private FinancePaymentReceiptJdbcRepository repository;
    private FileUploadService files;
    private DocumentLifecycleService documents;
    private EnterpriseIdentityContext identity;
    private DocumentSecurityService documentSecurity;
    private FileStorageProvider storage;
    private FinancePaymentReceiptDocumentService service;

    @BeforeEach
    void setUp() {

        repository =
                Mockito.mock(
                        FinancePaymentReceiptJdbcRepository.class
                );

        files =
                Mockito.mock(
                        FileUploadService.class
                );

        documents =
                Mockito.mock(
                        DocumentLifecycleService.class
                );

        identity =
                Mockito.mock(
                        EnterpriseIdentityContext.class
                );

        documentSecurity =
                Mockito.mock(
                        DocumentSecurityService.class
                );

        storage =
                Mockito.mock(
                        FileStorageProvider.class
                );

        service =
                new FinancePaymentReceiptDocumentService(
                        repository,
                        files,
                        documents,
                        identity,
                        documentSecurity,
                        storage
                );
    }

    @Test
    void existingDocumentLinkIsReturnedWithoutAnotherUpload() {

        when(identity.requireTenantId())
                .thenReturn(TENANT);

        ReceiptResponse linked =
                receipt(DOCUMENT);

        when(
                repository.lockReceiptForDocument(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(linked);

        ReceiptResponse result =
                service.ensureDocument(
                        TENANT,
                        RECEIPT
                );

        assertSame(linked, result);

        verifyNoInteractions(files);
        verifyNoInteractions(documents);
        verifyNoInteractions(documentSecurity);
        verifyNoInteractions(storage);

        verify(
                repository,
                never()
        ).attachDocument(
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(
                identity,
                never()
        ).requireUserId();
    }

    @Test
    void createsFileThenEdsDocumentAndLinksExistingReceipt()
            throws Exception {

        when(identity.requireTenantId())
                .thenReturn(TENANT);

        when(identity.requireUserId())
                .thenReturn(ACTOR);

        ReceiptResponse issued =
                receipt(null);

        when(
                repository.lockReceiptForDocument(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(issued);

        FileUploadResult stored =
                Mockito.mock(
                        FileUploadResult.class
                );

        when(stored.storageKey())
                .thenReturn("tenant/receipt.txt");

        when(stored.checksum())
                .thenReturn("abc123");

        when(stored.mimeType())
                .thenReturn("text/plain");

        when(stored.sizeBytes())
                .thenReturn(500L);

        when(
                files.upload(
                        any(MultipartFile.class)
                )
        ).thenReturn(stored);

        when(
                documents.create(
                        any(
                                DocumentDtos.CreateDocument.class
                        )
                )
        ).thenReturn(
                new DocumentDtos.LifecycleView(
                        DOCUMENT,
                        "GT-RCT-66666666666666666666666666666666",
                        "Payment receipt RCT-0000000001",
                        DocumentStatus.ACTIVE,
                        1,
                        null,
                        false,
                        null
                )
        );

        ReceiptResponse linked =
                receipt(DOCUMENT);

        when(
                repository.attachDocument(
                        eq(TENANT),
                        eq(RECEIPT),
                        eq(DOCUMENT),
                        eq(ACTOR),
                        any(Instant.class)
                )
        ).thenReturn(linked);

        ReceiptResponse result =
                service.ensureDocument(
                        TENANT,
                        RECEIPT
                );

        assertEquals(
                DOCUMENT,
                result.edsReceiptDocumentId()
        );

        ArgumentCaptor<MultipartFile> fileCaptor =
                ArgumentCaptor.forClass(
                        MultipartFile.class
                );

        verify(files).upload(
                fileCaptor.capture()
        );

        MultipartFile uploaded =
                fileCaptor.getValue();

        assertEquals(
                "text/plain",
                uploaded.getContentType()
        );

        String content =
                new String(
                        uploaded.getBytes(),
                        StandardCharsets.UTF_8
                );

        assertTrue(
                content.contains(
                        "RCT-0000000001"
                )
        );

        assertTrue(
                content.contains(
                        "150000.00"
                )
        );

        ArgumentCaptor<DocumentDtos.CreateDocument>
                documentCaptor =
                ArgumentCaptor.forClass(
                        DocumentDtos.CreateDocument.class
                );

        verify(documents).create(
                documentCaptor.capture()
        );

        DocumentDtos.CreateDocument command =
                documentCaptor.getValue();

        assertEquals(
                DocumentClassification.CONFIDENTIAL,
                command.classification()
        );

        assertEquals(
                "text/plain",
                command.mimeType()
        );

        assertEquals(
                "tenant/receipt.txt",
                command.storageKey()
        );

        verify(repository).attachDocument(
                eq(TENANT),
                eq(RECEIPT),
                eq(DOCUMENT),
                eq(ACTOR),
                any(Instant.class)
        );

        verifyNoInteractions(documentSecurity);
        verifyNoInteractions(storage);
    }

    @Test
    void documentGenerationRejectsCrossTenantRequestBeforeSideEffects() {

        when(identity.requireTenantId())
                .thenReturn(OTHER_TENANT);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.ensureDocument(
                                TENANT,
                                RECEIPT
                        )
        );

        verifyNoInteractions(repository);
        verifyNoInteractions(files);
        verifyNoInteractions(documents);
        verifyNoInteractions(documentSecurity);
        verifyNoInteractions(storage);
    }

    @Test
    void rendererPreservesReceiptFactsAndSettlementDisclaimer() {

        String rendered =
                FinancePaymentReceiptDocumentService.render(
                        receipt(null),
                        TENANT
                );

        assertTrue(
                rendered.contains(
                        "Payment Reference: PAY-001"
                )
        );

        assertTrue(
                rendered.contains(
                        "Currency: UGX"
                )
        );

        assertTrue(
                rendered.contains(
                        "Amount: 150000.00"
                )
        );

        assertTrue(
                rendered.contains(
                        "does not by itself prove bank settlement"
                )
        );

        assertTrue(
                rendered.contains(
                        "Allocation and any later allocation correction"
                )
        );
    }

    @Test
    void retrievalUsesExistingEdsDocumentAndStorageOnly() {

        when(identity.requireTenantId())
                .thenReturn(TENANT);

        ReceiptResponse linked =
                receipt(DOCUMENT);

        when(
                repository.findById(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                Optional.of(linked)
        );

        when(
                documentSecurity.preview(
                        DOCUMENT
                )
        ).thenReturn(
                new DocumentSecurityDtos.Preview(
                        DOCUMENT,
                        1,
                        "text/plain",
                        24L,
                        "tenant/receipt.txt",
                        true,
                        "inline"
                )
        );

        Resource resource =
                new ByteArrayResource(
                        "receipt".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        when(
                storage.load(
                        "tenant/receipt.txt"
                )
        ).thenReturn(resource);

        var result =
                service.retrieve(
                        TENANT,
                        RECEIPT
                );

        assertSame(
                resource,
                result.resource()
        );

        assertEquals(
                "text/plain",
                result.mimeType()
        );

        assertEquals(
                24L,
                result.sizeBytes()
        );

        assertTrue(
                result.inlineSupported()
        );

        assertEquals(
                "gt-receipt-"
                + RECEIPT
                + ".txt",
                result.filename()
        );

        verify(
                repository
        ).findById(
                TENANT,
                RECEIPT
        );

        verify(
                documentSecurity
        ).preview(
                DOCUMENT
        );

        verify(
                storage
        ).load(
                "tenant/receipt.txt"
        );

        verifyNoInteractions(files);
        verifyNoInteractions(documents);

        verify(
                repository,
                never()
        ).lockReceiptForDocument(
                any(),
                any()
        );

        verify(
                repository,
                never()
        ).attachDocument(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void retrievalWithoutGeneratedDocumentIsRejectedWithoutUploadOrStorage() {

        when(identity.requireTenantId())
                .thenReturn(TENANT);

        when(
                repository.findById(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                Optional.of(
                        receipt(null)
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.retrieve(
                                TENANT,
                                RECEIPT
                        )
        );

        verifyNoInteractions(files);
        verifyNoInteractions(documents);
        verifyNoInteractions(documentSecurity);
        verifyNoInteractions(storage);
    }

    @Test
    void retrievalRejectsCrossTenantRequestBeforeRepository() {

        when(identity.requireTenantId())
                .thenReturn(OTHER_TENANT);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.retrieve(
                                TENANT,
                                RECEIPT
                        )
        );

        verifyNoInteractions(repository);
        verifyNoInteractions(files);
        verifyNoInteractions(documents);
        verifyNoInteractions(documentSecurity);
        verifyNoInteractions(storage);
    }

    @Test
    void retrievalRejectsMismatchedEdsDocumentIdentityBeforeStorageLoad() {

        when(identity.requireTenantId())
                .thenReturn(TENANT);

        when(
                repository.findById(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                Optional.of(
                        receipt(DOCUMENT)
                )
        );

        UUID wrong =
                UUID.fromString(
                        "88888888-8888-8888-8888-888888888888"
                );

        when(
                documentSecurity.preview(
                        DOCUMENT
                )
        ).thenReturn(
                new DocumentSecurityDtos.Preview(
                        wrong,
                        1,
                        "text/plain",
                        24L,
                        "tenant/wrong.txt",
                        true,
                        "inline"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.retrieve(
                                TENANT,
                                RECEIPT
                        )
        );

        verifyNoInteractions(storage);
        verifyNoInteractions(files);
        verifyNoInteractions(documents);
    }

    private static ReceiptResponse receipt(
            UUID documentId
    ) {

        return new ReceiptResponse(
                RECEIPT,
                PAYMENT,
                STUDENT,
                "RCT-0000000001",
                Instant.parse(
                        "2026-09-19T10:15:30Z"
                ),
                "UGX",
                new BigDecimal(
                        "150000.00"
                ),
                Instant.parse(
                        "2026-09-19T10:16:00Z"
                ),
                ACTOR,
                documentId,
                null,
                null,
                "ISSUED",
                "ACTIVE",
                "PAY-001",
                "CASH",
                "GT-TEST-PROVIDER",
                "EXT-001",
                null
        );
    }
}
