package africa.growtogether.platform.eaif.integration;

import africa.growtogether.platform.eds.integration.EdsIntegrationService;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** EDS contract for managed AI source and output artifacts. */
@Component
public class EaifArtifactGateway {
    private final EdsIntegrationService documents;
    public EaifArtifactGateway(EdsIntegrationService documents) { this.documents = documents; }

    public UUID requestDocumentOperation(UUID documentId, String operation) {
        return documents.requestAi(documentId, operation).id();
    }
}
