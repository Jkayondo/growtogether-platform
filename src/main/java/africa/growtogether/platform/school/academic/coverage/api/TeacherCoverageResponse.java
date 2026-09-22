package africa.growtogether.platform.school.academic.coverage.api;


import java.time.LocalDate;
import java.util.UUID;


public record TeacherCoverageResponse(

        UUID id,

        UUID teacherProfileId,

        UUID teachingAssignmentId,

        UUID academicYearId,

        UUID academicTermId,

        UUID curriculumVersionId,

        UUID curriculumSubjectId,

        UUID classGradeId,

        String coverageType,

        String coverageItem,

        Integer plannedWeek,

        String coverageStatus,

        LocalDate completionDate,

        String teacherRemarks

) {}
