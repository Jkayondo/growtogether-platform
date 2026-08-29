package africa.growtogether.platform.school.academic.curriculum;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
public class SubjectOfferingService {


    private final SubjectOfferingRepository repository;


    public SubjectOfferingService(
            SubjectOfferingRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SubjectOffering create(
            UUID tenantId,
            String subjectOfferingCode,
            UUID classOfferingId,
            UUID academicTermId,
            UUID streamId,
            UUID subjectId,
            UUID academicDepartmentId,
            UUID gradingSchemeId,
            Integer weeklyPeriods,
            BigDecimal creditValue,
            Integer minimumEnrollment,
            Integer maximumEnrollment
    ) {


        SubjectOffering offering =
                new SubjectOffering(
                        subjectOfferingCode,
                        classOfferingId,
                        academicTermId,
                        streamId,
                        subjectId,
                        academicDepartmentId,
                        gradingSchemeId,
                        weeklyPeriods,
                        creditValue,
                        minimumEnrollment,
                        maximumEnrollment
                );


        offering.setTenantId(
                tenantId
        );


        return repository.save(
                offering
        );
    }


    @Transactional(readOnly = true)
    public List<SubjectOffering> findByClassOffering(
            UUID tenantId,
            UUID classOfferingId
    ) {

        return repository
                .findByTenantIdAndClassOfferingId(
                        tenantId,
                        classOfferingId
                );
    }


    @Transactional(readOnly = true)
    public List<SubjectOffering> findBySubject(
            UUID tenantId,
            UUID subjectId
    ) {

        return repository
                .findByTenantIdAndSubjectId(
                        tenantId,
                        subjectId
                );
    }


    @Transactional(readOnly = true)
    public SubjectOffering findByCode(
            UUID tenantId,
            String subjectOfferingCode
    ) {

        return repository
                .findByTenantIdAndSubjectOfferingCode(
                        tenantId,
                        subjectOfferingCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Subject offering not found"
                        )
                );
    }


    @Transactional
    public SubjectOffering activate(
            SubjectOffering offering
    ) {

        offering.activate();

        return repository.save(
                offering
        );
    }


    @Transactional
    public SubjectOffering deactivate(
            SubjectOffering offering
    ) {

        offering.deactivate();

        return repository.save(
                offering
        );
    }

}
