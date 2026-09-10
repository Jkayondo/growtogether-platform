package africa.growtogether.platform.school.assessment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;


public record CreateAssessmentPlanCommand(

        @NotBlank
        String planCode,

        @NotBlank
        String planName,

        String description,

        @NotNull
        UUID academicYearId,

        UUID academicTermId,

        @NotNull
        UUID campusId,

        UUID academicProgrammeId,

        UUID studyTrackId,

        UUID curriculumVersionId,

        @NotNull
        UUID classGradeId,

        UUID streamId,

        UUID gradingSchemeId,

        @NotNull
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        UUID workflowInstanceId

) {
}
