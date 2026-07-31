package africa.growtogether.platform.school.parent.integration;


import africa.growtogether.platform.school.parent.notification.dto.ParentNotificationResponse;
import africa.growtogether.platform.school.parent.notification.provider.NotificationChannel;
import africa.growtogether.platform.school.parent.notification.ParentAcademicNotificationType;
import africa.growtogether.platform.school.parent.notification.rules.ParentNotificationRuleType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/parent/engagement")
public class ParentEngagementIntegrationController {


    private final ParentEngagementIntegrationService service;


    public ParentEngagementIntegrationController(
            ParentEngagementIntegrationService service
    ) {

        this.service = service;
    }


    @PostMapping("/notification")
    public ResponseEntity<ParentNotificationResponse> sendNotification(
            @RequestParam UUID tenantId,
            @RequestParam UUID parentId,
            @RequestParam UUID learnerId,
            @RequestParam ParentNotificationRuleType ruleType,
            @RequestParam ParentAcademicNotificationType notificationType,
            @RequestParam String destination,
            @RequestParam String message,
            @RequestParam NotificationChannel channel
    ) {


        return ResponseEntity.ok(
                service.sendAcademicNotification(
                        tenantId,
                        parentId,
                        learnerId,
                        ruleType,
                        notificationType,
                        destination,
                        message,
                        channel
                )
        );
    }
}
