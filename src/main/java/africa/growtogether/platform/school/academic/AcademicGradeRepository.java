package africa.growtogether.platform.school.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface AcademicGradeRepository
        extends JpaRepository<AcademicGrade, UUID> {


    List<AcademicGrade> findByAcademicLevelIdOrderByDisplayOrderAsc(
            UUID academicLevelId
    );


    boolean existsByAcademicLevelIdAndGradeName(
            UUID academicLevelId,
            String gradeName
    );
}
