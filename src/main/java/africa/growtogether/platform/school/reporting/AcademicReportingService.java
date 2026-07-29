package africa.growtogether.platform.school.reporting;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AcademicReportingService {


    private final AcademicGradeRecordRepository repository;


    public AcademicReportingService(
            AcademicGradeRecordRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public AcademicGradeRecord recordResult(
            UUID tenantId,
            UUID learnerId,
            UUID subjectConfigurationId,
            GradeScale gradeScale,
            Integer score,
            String gradeValue
    ) {

        AcademicGradeRecord record =
                new AcademicGradeRecord(
                        tenantId,
                        learnerId,
                        subjectConfigurationId,
                        gradeScale,
                        score,
                        gradeValue
                );


        return repository.save(record);
    }


    @Transactional(readOnly = true)
    public List<AcademicGradeRecord> getLearnerResults(
            UUID learnerId
    ) {

        return repository
                .findByLearnerIdOrderByCreatedAtDesc(
                        learnerId
                );
    }


    @Transactional(readOnly = true)
    public List<AcademicGradeRecord> getSubjectResults(
            UUID subjectConfigurationId
    ) {

        return repository
                .findBySubjectConfigurationId(
                        subjectConfigurationId
                );
    }


    @Transactional(readOnly = true)
    public List<AcademicGradeRecord> getTenantResults(
            UUID tenantId
    ) {

        return repository.findByTenantId(tenantId);
    }
}
