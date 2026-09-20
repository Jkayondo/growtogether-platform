package africa.growtogether.platform.school.finance.receipt;

import static africa.growtogether.platform.school.finance.receipt.FinancePaymentReceiptDtos.ReceiptResponse;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentDtos;
import africa.growtogether.platform.eds.DocumentLifecycleService;
import africa.growtogether.platform.file.FileUploadService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FinancePaymentReceiptDocumentService {

    private static final String MIME_TYPE =
            "text/plain";

    private final FinancePaymentReceiptJdbcRepository repository;
    private final FileUploadService files;
    private final DocumentLifecycleService documents;
    private final EnterpriseIdentityContext identity;
    private final africa.growtogether.platform.eds.DocumentSecurityService documentSecurity;
    private final africa.growtogether.platform.file.FileStorageProvider storage;

    public FinancePaymentReceiptDocumentService(
            FinancePaymentReceiptJdbcRepository repository,
            FileUploadService files,
            DocumentLifecycleService documents,
            EnterpriseIdentityContext identity,
            africa.growtogether.platform.eds.DocumentSecurityService documentSecurity,
            africa.growtogether.platform.file.FileStorageProvider storage
    ) {
        this.repository = repository;
        this.files = files;
        this.documents = documents;
        this.identity = identity;
        this.documentSecurity = documentSecurity;
        this.storage = storage;
    }

    /**
     * Creates the EDS presentation artifact for an already-issued
     * receipt. Receipt issuance and document generation deliberately
     * remain separate lifecycle operations.
     *
     * A retry never creates another receipt identity. If the receipt
     * already has an EDS document link, the existing receipt is
     * returned unchanged.
     */
    @Transactional
    public ReceiptResponse ensureDocument(
            UUID tenantId,
            UUID receiptId
    ) {

        requireTenant(tenantId);

        if (receiptId == null) {
            throw new IllegalArgumentException(
                    "receiptId must not be null"
            );
        }

        ReceiptResponse receipt =
                repository.lockReceiptForDocument(
                        tenantId,
                        receiptId
                );

        if (receipt.edsReceiptDocumentId() != null) {
            return receipt;
        }

        UUID actorId =
                identity.requireUserId();

        byte[] bytes =
                render(receipt, tenantId)
                        .getBytes(StandardCharsets.UTF_8);

        /*
         * Reuse the existing enterprise File capability.
         * No Finance-specific binary store is introduced.
         */
        var stored =
                files.upload(
                        new ReceiptFile(
                                "receipt-"
                                + receipt.receiptId()
                                + ".txt",
                                bytes
                        )
                );

        /*
         * The EDS document number is derived from the immutable
         * receipt UUID, rather than receipt_number, because EDS
         * allows 80 characters while Finance receipt_number
         * allows 100.
         */
        String documentNumber =
                "GT-RCT-"
                + receipt.receiptId()
                        .toString()
                        .replace("-", "");

        var document =
                documents.create(
                        new DocumentDtos.CreateDocument(
                                documentNumber,
                                "Payment receipt "
                                + receipt.receiptNumber(),
                                DocumentClassification.CONFIDENTIAL,
                                stored.storageKey(),
                                stored.checksum(),
                                stored.mimeType(),
                                stored.sizeBytes(),
                                "GT School payment receipt "
                                + receipt.receiptNumber()
                        )
                );

        return repository.attachDocument(
                tenantId,
                receiptId,
                document.id(),
                actorId,
                Instant.now()
        );
    }

    /**
     * Retrieves the already-generated receipt document.
     *
     * This operation never issues a receipt, generates a receipt
     * number, uploads a new file, creates a document, or creates a
     * document version.
     *
     * EDS performs tenant/classification read authorization and
     * resolves the current immutable document version. The existing
     * FileStorageProvider then loads the storage key authorised by
     * EDS.
     */
    public DocumentDownload retrieve(
            UUID tenantId,
            UUID receiptId
    ) {

        requireTenant(tenantId);

        if (receiptId == null) {
            throw new IllegalArgumentException(
                    "receiptId must not be null"
            );
        }

        ReceiptResponse receipt =
                repository.findById(
                        tenantId,
                        receiptId
                ).orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Receipt was not found"
                                )
                );

        UUID documentId =
                receipt.edsReceiptDocumentId();

        if (documentId == null) {
            throw new IllegalStateException(
                    "Receipt document has not been generated"
            );
        }

        /*
         * preview(...) is the existing EDS read/classification
         * authorization contract and returns the storage key,
         * MIME type and size for the current document version.
         *
         * EDS may record its normal PREVIEW_REQUESTED audit event.
         * It does not create or modify a receipt or document version.
         */
        var preview =
                documentSecurity.preview(
                        documentId
                );

        if (
                preview.documentId() == null
                || !documentId.equals(
                        preview.documentId()
                )
        ) {
            throw new IllegalStateException(
                    "EDS returned a different document identity"
            );
        }

        var resource =
                storage.load(
                        preview.storageKey()
                );

        if (resource == null) {
            throw new IllegalStateException(
                    "Receipt document resource is unavailable"
            );
        }

        return new DocumentDownload(
                resource,
                preview.mimeType(),
                preview.sizeBytes(),
                "gt-receipt-"
                + receipt.receiptId()
                + ".txt",
                preview.inlineSupported()
        );
    }

    public record DocumentDownload(
            org.springframework.core.io.Resource resource,
            String mimeType,
            long sizeBytes,
            String filename,
            boolean inlineSupported
    ) {

        public DocumentDownload {

            if (resource == null) {
                throw new IllegalArgumentException(
                        "resource must not be null"
                );
            }

            if (
                    mimeType == null
                    || mimeType.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "mimeType must not be blank"
                );
            }

            if (sizeBytes < 0) {
                throw new IllegalArgumentException(
                        "sizeBytes must not be negative"
                );
            }

            if (
                    filename == null
                    || filename.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "filename must not be blank"
                );
            }
        }
    }

    static String render(
            ReceiptResponse receipt,
            UUID tenantId
    ) {

        return """
                GT SCHOOL
                PAYMENT RECEIPT / ACKNOWLEDGEMENT

                Receipt Number: %s
                Receipt ID: %s
                Tenant ID: %s

                Payment Reference: %s
                Payment ID: %s
                Student ID: %s
                Payment Date: %s

                Currency: %s
                Amount: %s
                Payment Method: %s
                Provider: %s
                External Transaction Reference: %s

                Issued At: %s

                This receipt acknowledges the payment recorded by GT School.
                It does not by itself prove bank settlement, provider
                reconciliation, general-ledger posting, or allocation status.
                Allocation and any later allocation correction remain
                separately auditable.
                """
                .formatted(
                        safe(receipt.receiptNumber()),
                        receipt.receiptId(),
                        tenantId,
                        safe(receipt.paymentReference()),
                        receipt.paymentId(),
                        receipt.studentId(),
                        receipt.receiptDate(),
                        safe(receipt.currencyCode()),
                        amount(receipt.receiptAmount()),
                        safe(receipt.paymentMethod()),
                        safe(receipt.providerName()),
                        safe(
                                receipt.externalTransactionReference()
                        ),
                        receipt.issuedAt()
                );
    }

    private void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "X-Tenant-ID is required"
            );
        }

        UUID authenticatedTenant =
                identity.requireTenantId();

        if (
                authenticatedTenant == null
                || !tenantId.equals(authenticatedTenant)
        ) {
            throw new IllegalArgumentException(
                    "X-Tenant-ID does not match "
                    + "the authenticated tenant"
            );
        }
    }

    private static String amount(
            BigDecimal value
    ) {

        return value == null
                ? "-"
                : value.toPlainString();
    }

    private static String safe(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }

    private record ReceiptFile(
            String filename,
            byte[] content
    ) implements MultipartFile {

        private ReceiptFile {
            if (
                    filename == null
                    || filename.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "filename must not be blank"
                );
            }

            content =
                    content == null
                            ? new byte[0]
                            : content.clone();
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return MIME_TYPE;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(
                File dest
        ) throws IOException {

            Files.write(
                    dest.toPath(),
                    content
            );
        }

        @Override
        public String toString() {
            return "ReceiptFile[content redacted]";
        }
    }
}
