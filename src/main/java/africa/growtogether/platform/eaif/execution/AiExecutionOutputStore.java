package africa.growtogether.platform.eaif.execution;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.*;
import africa.growtogether.platform.file.FileUploadService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AiExecutionOutputStore {
    private final FileUploadService files;
    private final DocumentLifecycleService documents;
    private final EnterpriseIdentityContext identity;
    public AiExecutionOutputStore(FileUploadService files, DocumentLifecycleService documents,
            EnterpriseIdentityContext identity) {
        this.files = files; this.documents = documents; this.identity = identity;
    }
    public String save(UUID tenantId, UUID requestId, AiTextResult result) {
        identity.requireTenant(tenantId);
        // Request ID makes identical text from separate requests distinct EDS artifacts.
        byte[] bytes = ("GT AI request: " + requestId + "\nProvider response: "
                + result.providerResponseId() + "\n\n" + result.text()).getBytes(StandardCharsets.UTF_8);
        var stored = files.upload(new OutputFile("ai-" + requestId + ".txt", bytes));
        var document = documents.create(new DocumentDtos.CreateDocument("EAIF-" + requestId,
                "AI execution output", DocumentClassification.RESTRICTED, stored.storageKey(),
                stored.checksum(), stored.mimeType(), stored.sizeBytes(), "Generated output; review required"));
        return "eds:" + document.id();
    }

    private record OutputFile(String filename, byte[] content) implements MultipartFile {
        public String getName() { return "file"; }
        public String getOriginalFilename() { return filename; }
        public String getContentType() { return "text/plain"; }
        public boolean isEmpty() { return content.length == 0; }
        public long getSize() { return content.length; }
        public byte[] getBytes() { return content.clone(); }
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), content); }
        @Override public String toString() { return "OutputFile[content redacted]"; }
    }
}
