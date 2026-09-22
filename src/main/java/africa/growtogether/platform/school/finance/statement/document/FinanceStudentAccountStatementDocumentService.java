package africa.growtogether.platform.school.finance.statement.document;

import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.AccountStatement;
import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.StatementEntry;
import static africa.growtogether.platform.school.finance.statement.document.FinanceStudentAccountStatementDocumentDtos.StatementDocumentResponse;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.DocumentClassification;
import africa.growtogether.platform.eds.DocumentDtos;
import africa.growtogether.platform.eds.DocumentLifecycleService;
import africa.growtogether.platform.eds.DocumentReference;
import africa.growtogether.platform.eds.DocumentReferenceService;
import africa.growtogether.platform.eds.DocumentSecurityService;
import africa.growtogether.platform.file.FileStorageProvider;
import africa.growtogether.platform.file.FileUploadService;
import africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FinanceStudentAccountStatementDocumentService {

    static final String REFERENCE_TYPE =
            "FINANCE_STATEMENT";

    static final String STUDENT_REFERENCE_TYPE =
            "FINANCE_STATEMENT_STUDENT";

    static final String MIME_TYPE =
            "text/plain";

    private static final String REFERENCE_NAMESPACE =
            "GT-FIN-B5-S6-STATEMENT\n";

    private final FinanceStudentAccountStatementService statements;
    private final FileUploadService files;
    private final DocumentLifecycleService documents;
    private final DocumentReferenceService references;
    private final DocumentSecurityService documentSecurity;
    private final FileStorageProvider storage;
    private final EnterpriseIdentityContext identity;

    public FinanceStudentAccountStatementDocumentService(
            FinanceStudentAccountStatementService statements,
            FileUploadService files,
            DocumentLifecycleService documents,
            DocumentReferenceService references,
            DocumentSecurityService documentSecurity,
            FileStorageProvider storage,
            EnterpriseIdentityContext identity
    ) {
        this.statements = statements;
        this.files = files;
        this.documents = documents;
        this.references = references;
        this.documentSecurity = documentSecurity;
        this.storage = storage;
        this.identity = identity;
    }

    /**
     * Generates an immutable presentation snapshot from the authoritative
     * S5 account statement.
     *
     * The statement artifact is evidence only. It is not a ledger,
     * balance engine, payment record, allocation record or invoice state.
     *
     * Idempotency is content addressed:
     *
     *   S5 authoritative snapshot
     *        -> deterministic rendering
     *        -> SHA-256
     *        -> deterministic statement reference UUID
     *        -> EDS DocumentReference lookup
     *
     * An exact retry resolves the existing EDS document before any
     * second upload or EDS document creation is attempted.
     */
    @Transactional
    public StatementDocumentResponse generate(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate asOfDate
    ) {

        requireTenant(
                tenantId
        );

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        String normalizedCurrency =
                normalizeCurrency(
                        currencyCode
                );

        AccountStatement statement =
                statements.accountStatement(
                        tenantId,
                        studentId,
                        normalizedCurrency,
                        fromDate,
                        toDate,
                        asOfDate
                );

        byte[] bytes =
                render(
                        statement,
                        tenantId
                );

        String contentSha256 =
                sha256(
                        bytes
                );

        UUID statementReferenceId =
                statementReferenceId(
                        contentSha256
                );

        List<DocumentReference> existing =
                references.findByReference(
                        REFERENCE_TYPE,
                        statementReferenceId
                );

        if (existing.size() > 1) {
            throw new IllegalStateException(
                    "Multiple EDS documents are linked "
                    + "to the same financial statement snapshot"
            );
        }

        if (existing.size() == 1) {

            UUID existingDocumentId =
                    existing.get(0)
                            .getDocumentId();

            if (existingDocumentId == null) {
                throw new IllegalStateException(
                        "Existing statement reference "
                        + "has no document identity"
                );
            }

            ensureStudentReference(
                    existingDocumentId,
                    statement.summary().studentId()
            );

            return response(
                    existingDocumentId,
                    statementReferenceId,
                    contentSha256,
                    statement,
                    false
            );
        }

        var stored =
                files.upload(
                        new StatementFile(
                                filename(
                                        studentId,
                                        statementReferenceId
                                ),
                                bytes
                        )
                );

        String documentNumber =
                "GT-FST-"
                + statementReferenceId
                        .toString()
                        .replace("-", "");

        var document =
                documents.create(
                        new DocumentDtos.CreateDocument(
                                documentNumber,
                                "Learner financial statement "
                                        + studentId,
                                DocumentClassification.CONFIDENTIAL,
                                stored.storageKey(),
                                stored.checksum(),
                                stored.mimeType(),
                                stored.sizeBytes(),
                                "GT School learner financial statement "
                                        + statementReferenceId
                        )
                );

        DocumentReference linked =
                references.create(
                        document.id(),
                        REFERENCE_TYPE,
                        statementReferenceId
                );

        if (
                linked.getDocumentId() == null
                || !document.id().equals(
                        linked.getDocumentId()
                )
        ) {
            throw new IllegalStateException(
                    "EDS document reference returned "
                    + "a different document identity"
            );
        }

        ensureStudentReference(
                document.id(),
                statement.summary().studentId()
        );

        return response(
                document.id(),
                statementReferenceId,
                contentSha256,
                statement,
                true
        );
    }

    /**
     * Retrieves an already-generated immutable statement artifact.
     *
     * No financial statement is recalculated here. The requested
     * statement reference and learner reference must resolve to the
     * same tenant-scoped EDS document.
     */
    public DocumentDownload retrieve(
            UUID tenantId,
            UUID studentId,
            UUID statementReferenceId
    ) {

        requireTenant(
                tenantId
        );

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }

        if (statementReferenceId == null) {
            throw new IllegalArgumentException(
                    "statementReferenceId must not be null"
            );
        }

        List<DocumentReference> statementLinks =
                references.findByReference(
                        REFERENCE_TYPE,
                        statementReferenceId
                );

        if (statementLinks.isEmpty()) {
            throw new IllegalArgumentException(
                    "Statement document was not found"
            );
        }

        if (statementLinks.size() > 1) {
            throw new IllegalStateException(
                    "Multiple EDS documents are linked "
                    + "to the same financial statement snapshot"
            );
        }

        UUID documentId =
                statementLinks.get(0)
                        .getDocumentId();

        if (documentId == null) {
            throw new IllegalStateException(
                    "Statement reference has no document identity"
            );
        }

        List<DocumentReference> studentLinks =
                references.findByReference(
                        STUDENT_REFERENCE_TYPE,
                        studentId
                );

        boolean belongsToStudent =
                studentLinks.stream()
                        .map(
                                DocumentReference::getDocumentId
                        )
                        .anyMatch(
                                documentId::equals
                        );

        if (!belongsToStudent) {
            throw new IllegalArgumentException(
                    "Statement document does not belong "
                    + "to the requested student"
            );
        }

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
                    "Statement document resource is unavailable"
            );
        }

        return new DocumentDownload(
                resource,
                preview.mimeType(),
                preview.sizeBytes(),
                "gt-financial-statement-"
                        + statementReferenceId
                        + ".txt",
                preview.inlineSupported()
        );
    }

    private void ensureStudentReference(
            UUID documentId,
            UUID studentId
    ) {

        List<DocumentReference> existingStudentLinks =
                references.findByReference(
                        STUDENT_REFERENCE_TYPE,
                        studentId
                );

        boolean alreadyLinked =
                existingStudentLinks.stream()
                        .map(
                                DocumentReference::getDocumentId
                        )
                        .anyMatch(
                                documentId::equals
                        );

        if (!alreadyLinked) {
            references.create(
                    documentId,
                    STUDENT_REFERENCE_TYPE,
                    studentId
            );
        }
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

    static byte[] render(
            AccountStatement statement,
            UUID tenantId
    ) {

        if (statement == null) {
            throw new IllegalArgumentException(
                    "statement must not be null"
            );
        }

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        var summary =
                statement.summary();

        StringBuilder out =
                new StringBuilder();

        out.append("GT SCHOOL\n");
        out.append("LEARNER FINANCIAL ACCOUNT STATEMENT\n\n");

        out.append("Tenant ID: ")
                .append(tenantId)
                .append('\n');

        out.append("Student ID: ")
                .append(summary.studentId())
                .append('\n');

        out.append("Financial Account ID: ")
                .append(summary.studentFinancialAccountId())
                .append('\n');

        out.append("Currency: ")
                .append(safe(summary.currencyCode()))
                .append('\n');

        out.append("Period From: ")
                .append(date(statement.fromDate()))
                .append('\n');

        out.append("Period To: ")
                .append(date(statement.toDate()))
                .append('\n');

        out.append("As Of Date: ")
                .append(date(summary.asOfDate()))
                .append('\n');

        out.append('\n');
        out.append("SUMMARY\n");

        out.append("Total Billed: ")
                .append(amount(summary.totalBilled()))
                .append('\n');

        out.append("Total Paid: ")
                .append(amount(summary.totalPaid()))
                .append('\n');

        out.append("Total Outstanding: ")
                .append(amount(summary.totalOutstanding()))
                .append('\n');

        out.append("Arrears Outstanding: ")
                .append(amount(summary.arrearsOutstanding()))
                .append('\n');

        out.append("Unallocated Payment Amount: ")
                .append(amount(summary.unallocatedPaymentAmount()))
                .append('\n');

        out.append("Invoice Count: ")
                .append(summary.invoiceCount())
                .append('\n');

        out.append("Overdue Invoice Count: ")
                .append(summary.overdueInvoiceCount())
                .append('\n');

        out.append('\n');
        out.append("STATEMENT ENTRIES: ")
                .append(statement.entries().size())
                .append('\n');

        int number = 1;

        for (StatementEntry entry : statement.entries()) {

            out.append('\n');
            out.append("ENTRY ")
                    .append(number++)
                    .append('\n');

            out.append("Effective Date: ")
                    .append(date(entry.effectiveDate()))
                    .append('\n');

            out.append("Occurred At: ")
                    .append(instant(entry.occurredAt()))
                    .append('\n');

            out.append("Type: ")
                    .append(safe(entry.entryType()))
                    .append('\n');

            out.append("Source ID: ")
                    .append(safe(entry.sourceId()))
                    .append('\n');

            out.append("Reference: ")
                    .append(safe(entry.reference()))
                    .append('\n');

            out.append("Amount: ")
                    .append(amount(entry.amount()))
                    .append('\n');

            out.append("Balance Effect: ")
                    .append(safe(entry.balanceEffect()))
                    .append('\n');

            out.append("Related Invoice ID: ")
                    .append(safe(entry.relatedInvoiceId()))
                    .append('\n');

            out.append("Related Payment ID: ")
                    .append(safe(entry.relatedPaymentId()))
                    .append('\n');

            out.append("Lifecycle Status: ")
                    .append(safe(entry.lifecycleStatus()))
                    .append('\n');
        }

        out.append('\n');
        out.append(
                "This document is an immutable presentation snapshot "
                + "of the authoritative GT School financial statement "
                + "at generation time.\n"
        );

        out.append(
                "It does not independently alter balances, payments, "
                + "allocations, receipts or invoices.\n"
        );

        return out.toString()
                .getBytes(
                        StandardCharsets.UTF_8
                );
    }

    static String sha256(
            byte[] content
    ) {

        try {

            return HexFormat.of()
                    .formatHex(
                            MessageDigest
                                    .getInstance("SHA-256")
                                    .digest(content)
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to calculate statement content hash",
                    exception
            );
        }
    }

    static UUID statementReferenceId(
            String contentSha256
    ) {

        if (
                contentSha256 == null
                || contentSha256.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "contentSha256 must not be blank"
            );
        }

        return UUID.nameUUIDFromBytes(
                (
                        REFERENCE_NAMESPACE
                        + contentSha256
                ).getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    private static StatementDocumentResponse response(
            UUID documentId,
            UUID statementReferenceId,
            String contentSha256,
            AccountStatement statement,
            boolean created
    ) {

        var summary =
                statement.summary();

        return new StatementDocumentResponse(
                documentId,
                statementReferenceId,
                contentSha256,
                summary.studentId(),
                summary.studentFinancialAccountId(),
                summary.currencyCode(),
                statement.fromDate(),
                statement.toDate(),
                summary.asOfDate(),
                created
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

        identity.requireTenant(
                tenantId
        );
    }

    private static String normalizeCurrency(
            String currencyCode
    ) {

        if (
                currencyCode == null
                || currencyCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "currencyCode must not be blank"
            );
        }

        return currencyCode
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private static String filename(
            UUID studentId,
            UUID referenceId
    ) {

        return "financial-statement-"
                + studentId
                + "-"
                + referenceId
                + ".txt";
    }

    private static String amount(
            BigDecimal value
    ) {

        return value == null
                ? "-"
                : value.toPlainString();
    }

    private static String date(
            LocalDate value
    ) {

        return value == null
                ? "-"
                : value.toString();
    }

    private static String instant(
            Instant value
    ) {

        return value == null
                ? "-"
                : value.toString();
    }

    private static String safe(
            Object value
    ) {

        if (value == null) {
            return "-";
        }

        String text =
                value.toString();

        if (text.isBlank()) {
            return "-";
        }

        return text
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }

    private record StatementFile(
            String filename,
            byte[] content
    ) implements MultipartFile {

        private StatementFile {

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
            return new ByteArrayInputStream(
                    content
            );
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
            return "StatementFile[content redacted]";
        }
    }
}
