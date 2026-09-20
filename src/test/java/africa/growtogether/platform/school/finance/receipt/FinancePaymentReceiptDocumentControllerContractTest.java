package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

class FinancePaymentReceiptDocumentControllerContractTest {

    private static final UUID TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID RECEIPT =
            UUID.fromString(
                    "66666666-6666-6666-6666-666666666666"
            );

    private FinancePaymentReceiptService receipts;
    private FinancePaymentReceiptDocumentService documents;
    private FinancePaymentReceiptDeliveryService deliveries;
    private FinancePaymentReceiptController controller;

    @BeforeEach
    void setUp() {

        receipts =
                Mockito.mock(
                        FinancePaymentReceiptService.class
                );

        documents =
                Mockito.mock(
                        FinancePaymentReceiptDocumentService.class
                );

        deliveries =
                Mockito.mock(
                        FinancePaymentReceiptDeliveryService.class
                );

        controller =
                new FinancePaymentReceiptController(
                        receipts,
                        documents,
                        deliveries
                );
    }

    @Test
    void generationEndpointRetainsManageAuthority()
            throws Exception {

        Method method =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "ensureDocument",
                                UUID.class,
                                UUID.class
                        );

        assertAuthority(
                method,
                "school.finance.manage"
        );

        PostMapping mapping =
                method.getAnnotation(
                        PostMapping.class
                );

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/receipts/{receiptId}/document"
                },
                mapping.value()
        );
    }

    @Test
    void inlineViewUsesFinanceReadAuthority()
            throws Exception {

        Method method =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "viewDocument",
                                UUID.class,
                                UUID.class
                        );

        assertAuthority(
                method,
                "school.finance.read"
        );

        GetMapping mapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/receipts/{receiptId}/document"
                },
                mapping.value()
        );
    }

    @Test
    void attachmentDownloadUsesFinanceReadAuthority()
            throws Exception {

        Method method =
                FinancePaymentReceiptController.class
                        .getMethod(
                                "downloadDocument",
                                UUID.class,
                                UUID.class
                        );

        assertAuthority(
                method,
                "school.finance.read"
        );

        GetMapping mapping =
                method.getAnnotation(
                        GetMapping.class
                );

        assertNotNull(mapping);

        assertArrayEquals(
                new String[]{
                        "/receipts/{receiptId}/document/download"
                },
                mapping.value()
        );
    }

    @Test
    void inlineViewReturnsExistingResourceWithoutGeneration() {

        var resource =
                new ByteArrayResource(
                        "receipt".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        when(
                documents.retrieve(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                new FinancePaymentReceiptDocumentService.DocumentDownload(
                        resource,
                        "text/plain",
                        7L,
                        "gt-receipt.txt",
                        true
                )
        );

        var response =
                controller.viewDocument(
                        TENANT,
                        RECEIPT
                );

        assertEquals(
                resource,
                response.getBody()
        );

        assertEquals(
                "text/plain",
                response.getHeaders()
                        .getContentType()
                        .toString()
        );

        assertEquals(
                7L,
                response.getHeaders()
                        .getContentLength()
        );

        assertEquals(
                "inline; filename=\"gt-receipt.txt\"",
                response.getHeaders()
                        .getFirst(
                                HttpHeaders.CONTENT_DISPOSITION
                        )
        );

        assertEquals(
                "private, no-store",
                response.getHeaders()
                        .getFirst(
                                HttpHeaders.CACHE_CONTROL
                        )
        );

        verify(documents).retrieve(
                TENANT,
                RECEIPT
        );
    }

    @Test
    void downloadAlwaysUsesAttachmentDisposition() {

        var resource =
                new ByteArrayResource(
                        "receipt".getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        when(
                documents.retrieve(
                        TENANT,
                        RECEIPT
                )
        ).thenReturn(
                new FinancePaymentReceiptDocumentService.DocumentDownload(
                        resource,
                        "text/plain",
                        7L,
                        "gt-receipt.txt",
                        true
                )
        );

        var response =
                controller.downloadDocument(
                        TENANT,
                        RECEIPT
                );

        assertEquals(
                resource,
                response.getBody()
        );

        assertEquals(
                "attachment; filename=\"gt-receipt.txt\"",
                response.getHeaders()
                        .getFirst(
                                HttpHeaders.CONTENT_DISPOSITION
                        )
        );

        assertEquals(
                "nosniff",
                response.getHeaders()
                        .getFirst(
                                "X-Content-Type-Options"
                        )
        );

        verify(documents).retrieve(
                TENANT,
                RECEIPT
        );
    }

    private static void assertAuthority(
            Method method,
            String expected
    ) {

        PreAuthorize authorization =
                method.getAnnotation(
                        PreAuthorize.class
                );

        assertNotNull(authorization);

        assertEquals(
                "hasAuthority('"
                + expected
                + "')",
                authorization.value()
        );
    }
}
