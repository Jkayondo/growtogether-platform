package africa.growtogether.platform.school.assessment.examination;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class ExaminationSessionService {


    private final ExaminationSessionRepository repository;


    public ExaminationSessionService(
            ExaminationSessionRepository repository
    ) {

        this.repository = repository;

    }


    public ExaminationSession create(
            ExaminationSession session
    ) {

        return repository.save(session);

    }


    @Transactional(readOnly = true)
    public ExaminationSession get(
            UUID id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Examination session not found"
                        )
                );

    }


    @Transactional(readOnly = true)
    public ExaminationSession getByCode(
            UUID tenantId,
            String sessionCode
    ) {

        return repository
                .findByTenantIdAndSessionCode(
                        tenantId,
                        sessionCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Examination session not found"
                        )
                );

    }


    public ExaminationSession approve(
            UUID id,
            UUID approvedBy
    ) {

        ExaminationSession session = get(id);

        session.approve(
                approvedBy
        );

        return session;

    }


    public ExaminationSession openRegistration(
            UUID id
    ) {

        ExaminationSession session = get(id);

        session.openRegistration();

        return session;

    }


    public ExaminationSession activate(
            UUID id
    ) {

        ExaminationSession session = get(id);

        session.activate();

        return session;

    }


    public ExaminationSession complete(
            UUID id
    ) {

        ExaminationSession session = get(id);

        session.complete();

        return session;

    }

}
