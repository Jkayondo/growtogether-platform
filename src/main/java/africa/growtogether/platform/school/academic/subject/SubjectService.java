package africa.growtogether.platform.school.academic.subject;

import africa.growtogether.platform.common.persistence.EntityStatus;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class SubjectService {


    private final SubjectRepository repository;


    public SubjectService(
            SubjectRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public Subject create(
            UUID tenantId,
            String subjectCode,
            String subjectName,
            String shortName,
            String subjectType,
            String description
    ) {


        Subject subject =
                new Subject(
                        subjectCode,
                        subjectName,
                        shortName,
                        subjectType,
                        description
                );


        subject.setTenantId(
                tenantId
        );


        return repository.save(
                subject
        );
    }


    @Transactional(readOnly = true)
    public Subject get(
            UUID tenantId,
            UUID id
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject not found for tenant"
                        )
                );
    }


    @Transactional(readOnly = true)
    public Subject findByCode(
            UUID tenantId,
            String subjectCode
    ) {

        return repository
                .findByTenantIdAndSubjectCode(
                        tenantId,
                        subjectCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<Subject> findActiveSubjects(
            UUID tenantId
    ) {

        return repository
                .findByTenantIdAndStatus(
                        tenantId,
                        EntityStatus.ACTIVE
                );
    }


    @Transactional
    public Subject activate(
            UUID tenantId,
            Subject subject
    ) {

        validateTenant(
                tenantId,
                subject
        );


        subject.activate();


        return repository.save(
                subject
        );
    }


    @Transactional
    public Subject deactivate(
            UUID tenantId,
            Subject subject
    ) {

        validateTenant(
                tenantId,
                subject
        );


        subject.deactivate();


        return repository.save(
                subject
        );
    }


    private void validateTenant(
            UUID tenantId,
            Subject subject
    ) {

        if (!subject.getTenantId()
                .equals(tenantId)) {

            throw new IllegalArgumentException(
                    "Subject does not belong to tenant"
            );
        }

    }

}
