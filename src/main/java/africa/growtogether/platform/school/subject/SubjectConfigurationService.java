package africa.growtogether.platform.school.subject;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class SubjectConfigurationService {


    private final SubjectConfigurationRepository repository;


    public SubjectConfigurationService(
            SubjectConfigurationRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public SubjectConfiguration create(
            UUID tenantId,
            UUID academicGradeId,
            String subjectName,
            String subjectCode,
            boolean mandatory
    ) {


        if (repository
                .existsByAcademicGradeIdAndSubjectName(
                        academicGradeId,
                        subjectName
                )) {

            throw new IllegalStateException(
                    "Subject already exists for this grade."
            );
        }


        SubjectConfiguration subject =
                new SubjectConfiguration(
                        tenantId,
                        academicGradeId,
                        subjectName,
                        subjectCode,
                        mandatory
                );


        return repository.save(subject);
    }


    @Transactional(readOnly = true)
    public List<SubjectConfiguration> getByGrade(
            UUID academicGradeId
    ) {

        return repository
                .findByAcademicGradeIdOrderBySubjectNameAsc(
                        academicGradeId
                );
    }


    @Transactional(readOnly = true)
    public List<SubjectConfiguration> getByTenant(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }
}
