package africa.growtogether.platform.ens.integration;
import africa.growtogether.platform.ens.*; import org.springframework.stereotype.Component;
/** Stable EWE-to-ENS contract. Workflow code publishes notification intent here, never to providers. */
@Component public class WorkflowNotificationGateway { private final NotificationService notifications; public WorkflowNotificationGateway(NotificationService n){notifications=n;}
 public NotificationDtos.View taskAssigned(String recipient,String workflowReference,String message){return notifications.send(new NotificationDtos.SendCommand("WORKFLOW_TASK_ASSIGNED",recipient,NotificationChannel.IN_APP,NotificationPriority.HIGH,"Workflow task assigned",message,"EWE",workflowReference));}
 public NotificationDtos.View taskEscalated(String recipient,String workflowReference,String message){return notifications.send(new NotificationDtos.SendCommand("WORKFLOW_TASK_ESCALATED",recipient,NotificationChannel.IN_APP,NotificationPriority.CRITICAL,"Workflow task escalated",message,"EWE",workflowReference));}
}
