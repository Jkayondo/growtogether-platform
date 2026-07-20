package africa.growtogether.platform.eds.integration;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eds.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EdsIntegrationService {
 private final WorkflowDocumentLinkRepository links; private final DocumentEventOutboxRepository outbox; private final DocumentAiRequestRepository ai;
 private final DocumentRepository documents; private final EnterpriseIdentityContext identity; private final EdsAuditRecorder audit; private final ObjectMapper mapper;
 public EdsIntegrationService(WorkflowDocumentLinkRepository l,DocumentEventOutboxRepository o,DocumentAiRequestRepository a,DocumentRepository d,EnterpriseIdentityContext i,EdsAuditRecorder r,ObjectMapper m){links=l;outbox=o;ai=a;documents=d;identity=i;audit=r;mapper=m;}
 @Transactional public LinkView attach(UUID documentId,UUID workflowInstanceId,String relationshipType){requireDocument(documentId);var link=links.save(new WorkflowDocumentLink(identity.tenantId(),workflowInstanceId,documentId,relationshipType));publish(documentId,"DOCUMENT_LINKED_TO_WORKFLOW",Map.of("workflowInstanceId",workflowInstanceId.toString(),"relationshipType",link.relationshipType()));audit.success("EDS.WORKFLOW.LINKED",documentId,"Document linked to workflow",Map.of("workflowInstanceId",workflowInstanceId.toString()));return LinkView.from(link);}
 @Transactional(readOnly=true) public List<LinkView> workflowLinks(UUID workflowInstanceId){return links.findByWorkflowInstanceIdAndTenantIdOrderByCreatedAtAsc(workflowInstanceId,identity.tenantId()).stream().map(LinkView::from).toList();}
 @Transactional public AiRequestView requestAi(UUID documentId,String operation){requireDocument(documentId);var request=ai.save(new DocumentAiRequest(identity.tenantId(),documentId,operation));publish(documentId,"DOCUMENT_AI_REQUESTED",Map.of("operation",request.operation(),"requestId",request.id().toString()));audit.success("EDS.AI.REQUESTED",documentId,"Document AI operation requested",Map.of("operation",request.operation()));return AiRequestView.from(request);}
 @Transactional public UUID publish(UUID documentId,String eventType,Map<String,Object> payload){requireDocument(documentId);try{return outbox.save(new DocumentEventOutbox(identity.tenantId(),documentId,eventType,mapper.writeValueAsString(payload==null?Map.of():payload))).id();}catch(Exception e){throw new IllegalArgumentException("Event payload must be JSON serializable",e);}}
 private void requireDocument(UUID id){documents.findByIdAndTenantId(id,identity.tenantId()).orElseThrow(()->new NoSuchElementException("Document not found"));}
 public record LinkView(UUID id,UUID workflowInstanceId,UUID documentId,String relationshipType){static LinkView from(WorkflowDocumentLink x){return new LinkView(x.id(),x.workflowInstanceId(),x.documentId(),x.relationshipType());}}
 public record AiRequestView(UUID id,UUID documentId,String operation,String status){static AiRequestView from(DocumentAiRequest x){return new AiRequestView(x.id(),x.documentId(),x.operation(),x.requestStatus());}}
}
