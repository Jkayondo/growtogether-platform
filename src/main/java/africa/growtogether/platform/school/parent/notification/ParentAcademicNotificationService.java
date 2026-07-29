package africa.growtogether.platform.school.parent.notification;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentAcademicNotificationService {


    private final ParentAcademicNotificationRepository repository;


    public ParentAcademicNotificationService(
            ParentAcademicNotificationRepository repository
    ) {

        this.repository = repository;
    }


    public ParentAcademicNotification create(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentAcademicNotificationType type,
            String message
    ) {


        ParentAcademicNotification notification =
                new ParentAcademicNotification(
                        tenantId,
                        parentId,
                        learnerId,
                        type,
                        message
                );


        return repository.save(notification);
    }
}
