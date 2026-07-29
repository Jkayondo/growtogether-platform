package africa.growtogether.platform.school.parent.audit;


import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class ParentAcademicAuditService {


    private final ParentAcademicAuditEventRepository repository;


    public ParentAcademicAuditService(
            ParentAcademicAuditEventRepository repository
    ) {
        this.repository = repository;
    }


    public ParentAcademicAuditEvent record(
            UUID tenantId,
            UUID parentId,
            UUID learnerId,
            ParentAcademicAuditEventType eventType
    ) {

        ParentAcademicAuditEvent event =
                new ParentAcademicAuditEvent(
                        tenantId,
                        parentId,
                        learnerId,
                        eventType
                );


        return repository.save(event);
    }
}
