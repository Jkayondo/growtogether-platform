package africa.growtogether.platform.school.academic.coverage.api;


import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;


public final class TeacherCoverageMapper {


    private TeacherCoverageMapper() {

    }


    public static TeacherCoverageResponse toResponse(
            TeacherCoverage coverage
    ) {

        return new TeacherCoverageResponse(

                coverage.getId(),

                coverage.getTeacherProfileId(),

                coverage.getTeachingAssignmentId(),

                coverage.getAcademicYearId(),

                coverage.getAcademicTermId(),

                coverage.getCurriculumVersionId(),

                coverage.getCurriculumSubjectId(),

                coverage.getClassGradeId(),

                coverage.getCoverageType(),

                coverage.getCoverageItem(),

                coverage.getPlannedWeek(),

                coverage.getCoverageStatus(),

                coverage.getCompletionDate(),

                coverage.getTeacherRemarks()

        );

    }

}
