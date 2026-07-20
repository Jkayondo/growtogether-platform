package africa.growtogether.platform.eds.integration;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

interface WorkflowDocumentLinkRepository extends JpaRepository<WorkflowDocumentLink,UUID> {
 List<WorkflowDocumentLink> findByWorkflowInstanceIdAndTenantIdOrderByCreatedAtAsc(UUID workflowInstanceId,UUID tenantId);
}
interface DocumentEventOutboxRepository extends JpaRepository<DocumentEventOutbox,UUID> {}
interface DocumentAiRequestRepository extends JpaRepository<DocumentAiRequest,UUID> {}
