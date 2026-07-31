package africa.growtogether.platform.school.academic.integration;


import africa.growtogether.platform.school.academic.AcademicStructureService;
import africa.growtogether.platform.school.academic.audit.AcademicConfigurationAuditEventType;
import africa.growtogether.platform.school.academic.audit.AcademicConfigurationAuditRecorder;
import africa.growtogether.platform.school.academic.curriculum.Curriculum;
import africa.growtogether.platform.school.academic.curriculum.CurriculumService;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
public class AcademicConfigurationIntegrationService {


    private final CurriculumService curriculumService;

    private final AcademicStructureService academicStructureService;

    private final AcademicConfigurationAuditRecorder auditRecorder;


    public AcademicConfigurationIntegrationService(
            CurriculumService curriculumService,
            AcademicStructureService academicStructureService,
            AcademicConfigurationAuditRecorder auditRecorder
    ) {

        this.curriculumService = curriculumService;
        this.academicStructureService = academicStructureService;
        this.auditRecorder = auditRecorder;
    }


    public Curriculum configureCurriculum(
            UUID tenantId,
            String curriculumCode,
            String curriculumName,
            String curriculumType
    ) {

        Curriculum curriculum =
                curriculumService.create(
                        tenantId,
                        curriculumCode,
                        curriculumName,
                        curriculumType
                );


        auditRecorder.success(
                AcademicConfigurationAuditEventType.CURRICULUM_CREATED.name(),
                curriculum.getId().toString(),
                "Curriculum created",
                Map.of(
                        "curriculumCode",
                        curriculumCode
                )
        );


        return curriculum;
    }


    public void configureAcademicLevel(
            UUID tenantId,
            UUID curriculumConfigurationId,
            String levelName,
            Integer displayOrder
    ) {

        academicStructureService.createLevel(
                tenantId,
                curriculumConfigurationId,
                levelName,
                displayOrder
        );


        auditRecorder.success(
                AcademicConfigurationAuditEventType.ACADEMIC_LEVEL_CREATED.name(),
                curriculumConfigurationId.toString(),
                "Academic level created",
                Map.of(
                        "levelName",
                        levelName
                )
        );
    }


    public void configureAcademicGrade(
            UUID tenantId,
            UUID academicLevelId,
            String gradeName,
            Integer displayOrder
    ) {

        academicStructureService.createGrade(
                tenantId,
                academicLevelId,
                gradeName,
                displayOrder
        );


        auditRecorder.success(
                AcademicConfigurationAuditEventType.ACADEMIC_GRADE_CREATED.name(),
                academicLevelId.toString(),
                "Academic grade created",
                Map.of(
                        "gradeName",
                        gradeName
                )
        );
    }
}
