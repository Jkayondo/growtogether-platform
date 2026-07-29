package africa.growtogether.platform.school.academic;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class AcademicStructureService {


    private final AcademicLevelRepository levelRepository;
    private final AcademicGradeRepository gradeRepository;


    public AcademicStructureService(
            AcademicLevelRepository levelRepository,
            AcademicGradeRepository gradeRepository
    ) {
        this.levelRepository = levelRepository;
        this.gradeRepository = gradeRepository;
    }


    @Transactional
    public AcademicLevel createLevel(
            UUID tenantId,
            UUID curriculumConfigurationId,
            String levelName,
            Integer displayOrder
    ) {

        if (levelRepository
                .existsByCurriculumConfigurationIdAndLevelName(
                        curriculumConfigurationId,
                        levelName
                )) {

            throw new IllegalStateException(
                    "Academic level already exists."
            );
        }


        AcademicLevel level =
                new AcademicLevel(
                        tenantId,
                        curriculumConfigurationId,
                        levelName,
                        displayOrder
                );


        return levelRepository.save(level);
    }


    @Transactional
    public AcademicGrade createGrade(
            UUID tenantId,
            UUID academicLevelId,
            String gradeName,
            Integer displayOrder
    ) {

        if (gradeRepository
                .existsByAcademicLevelIdAndGradeName(
                        academicLevelId,
                        gradeName
                )) {

            throw new IllegalStateException(
                    "Academic grade already exists."
            );
        }


        AcademicGrade grade =
                new AcademicGrade(
                        tenantId,
                        academicLevelId,
                        gradeName,
                        displayOrder
                );


        return gradeRepository.save(grade);
    }


    @Transactional(readOnly = true)
    public List<AcademicLevel> getLevels(
            UUID curriculumConfigurationId
    ) {

        return levelRepository
                .findByCurriculumConfigurationIdOrderByDisplayOrderAsc(
                        curriculumConfigurationId
                );
    }


    @Transactional(readOnly = true)
    public List<AcademicGrade> getGrades(
            UUID academicLevelId
    ) {

        return gradeRepository
                .findByAcademicLevelIdOrderByDisplayOrderAsc(
                        academicLevelId
                );
    }
}
