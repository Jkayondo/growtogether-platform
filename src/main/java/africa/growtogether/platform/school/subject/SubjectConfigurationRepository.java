package africa.growtogether.platform.school.subject;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface SubjectConfigurationRepository
        extends JpaRepository<SubjectConfiguration, UUID> {


    List<SubjectConfiguration>
    findByAcademicGradeIdOrderBySubjectNameAsc(
            UUID academicGradeId
    );


    boolean existsByAcademicGradeIdAndSubjectName(
            UUID academicGradeId,
            String subjectName
    );


    List<SubjectConfiguration>
    findByTenantId(UUID tenantId);
}
