package africa.growtogether.platform.school.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicLevelRepository
        extends JpaRepository<AcademicLevel, UUID> {


    List<AcademicLevel> findByCurriculumConfigurationIdOrderByDisplayOrderAsc(
            UUID curriculumConfigurationId
    );


    boolean existsByCurriculumConfigurationIdAndLevelName(
            UUID curriculumConfigurationId,
            String levelName
    );
}
