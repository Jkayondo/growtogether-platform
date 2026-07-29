package africa.growtogether.platform.school.reporting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicGradeRecordRepository
        extends JpaRepository<AcademicGradeRecord, UUID> {


    List<AcademicGradeRecord>
    findByLearnerIdOrderByCreatedAtDesc(
            UUID learnerId
    );


    List<AcademicGradeRecord>
    findBySubjectConfigurationId(
            UUID subjectConfigurationId
    );


    List<AcademicGradeRecord>
    findByTenantId(UUID tenantId);
}
