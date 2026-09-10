package africa.growtogether.platform.school.results.audit;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class AcademicResultAuditService {


    private final AcademicResultAuditEventRepository repository;


    public AcademicResultAuditService(
            AcademicResultAuditEventRepository repository
    ){

        this.repository = repository;

    }



    public AcademicResultAuditEvent record(

            UUID tenantId,

            UUID learnerId,

            UUID studentSubjectResultId,

            String eventType,

            UUID performedBy

    ){

        AcademicResultAuditEvent event =
                new AcademicResultAuditEvent(
                        learnerId,
                        studentSubjectResultId,
                        eventType,
                        performedBy
                );


        event.setTenantId(tenantId);


        return repository.save(event);

    }



    @Transactional(readOnly = true)
    public List<AcademicResultAuditEvent> history(
            UUID tenantId,
            UUID studentSubjectResultId
    ){

        return repository
                .findByTenantIdAndStudentSubjectResultIdOrderByPerformedAtAsc(
                        tenantId,
                        studentSubjectResultId
                );

    }


}
