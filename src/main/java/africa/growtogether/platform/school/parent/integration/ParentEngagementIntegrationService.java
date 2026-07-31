package africa.growtogether.platform.school.parent.integration;


import africa.growtogether.platform.school.parent.notification.ParentAcademicNotification;
import africa.growtogether.platform.school.parent.notification.ParentAcademicNotificationService;
import africa.growtogether.platform.school.parent.notification.ParentAcademicNotificationType;
import africa.growtogether.platform.school.parent.notification.ParentNotificationWorkflowService;
import africa.growtogether.platform.school.parent.notification.dto.ParentNotificationResponse;
import africa.growtogether.platform.school.parent.notification.dto.SendParentNotificationRequest;
import africa.growtogether.platform.school.parent.notification.provider.NotificationChannel;
import africa.growtogether.platform.school.parent.notification.rules.ParentNotificationRuleEngineService;
import africa.growtogether.platform.school.parent.notification.rules.ParentNotificationRuleType;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentEngagementIntegrationService {


    private final ParentNotificationRuleEngineService ruleEngine;

    private final ParentAcademicNotificationService academicNotificationService;

    private final ParentNotificationWorkflowService workflowService;


    public ParentEngagementIntegrationService(
            ParentNotificationRuleEngineService ruleEngine,
            ParentAcademicNotificationService academicNotificationService,
            ParentNotificationWorkflowService workflowService
    ) {

        this.ruleEngine = ruleEngine;
        this.academicNotificationService = academicNotificationService;
        this.workflowService = workflowService;
    }


    public ParentNotificationResponse sendAcademicNotification(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentNotificationRuleType ruleType,
            ParentAcademicNotificationType notificationType,
            String destination,
            String message,
            NotificationChannel channel
    ) {


        if (!ruleEngine.shouldNotify(
                tenantId,
                ruleType
        )) {

            throw new IllegalStateException(
                    "Parent notification rule is disabled."
            );
        }


        ParentAcademicNotification notification =
                academicNotificationService.create(
                        tenantId,
                        parentId,
                        learnerId,
                        notificationType,
                        message
                );


        return workflowService.send(
                new SendParentNotificationRequest(
                        parentId,
                        destination,
                        message,
                        channel
                )
        );
    }
}
