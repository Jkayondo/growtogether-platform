package africa.growtogether.platform.school.timetable.generation;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;
import java.util.UUID;

public record TimetableGenerationSnapshot(

        UUID generationRequestId,

        String generationCode,

        Scope scope,

        List<String> enabledDays,

        List<BellSlot> bellSlots,

        List<ClassDemand> classes,

        List<SubjectDemand> subjects,

        List<StreamScope> streams,

        List<TeachingSupply> teachingAssignments,

        List<ResourceSupply> resources

) {

    public record Scope(

            UUID academicYearId,

            UUID academicTermId,

            UUID campusId,

            UUID bellScheduleId,

            String timetableType,

            LocalDate effectiveFrom,

            LocalDate effectiveTo,

            String schoolTimezone,

            String generationMode,

            String modelCode,

            String objectives

    ) {
    }

    public record BellSlot(

            UUID bellPeriodId,

            String periodCode,

            Integer sequenceNumber,

            String periodType,

            LocalTime startTime,

            LocalTime endTime

    ) {
    }

    public record ClassDemand(

            UUID classOfferingId,

            String offeringCode,

            UUID classGradeId,

            Integer plannedCapacity

    ) {
    }

    public record SubjectDemand(

            UUID subjectOfferingId,

            String subjectOfferingCode,

            UUID classOfferingId,

            UUID academicTermId,

            UUID streamId,

            UUID subjectId,

            Integer weeklyPeriods

    ) {
    }

    public record StreamScope(

            UUID streamId,

            UUID campusId,

            UUID classGradeId,

            Integer capacity

    ) {
    }

    public record TeachingSupply(

            UUID teachingAssignmentId,

            String assignmentReference,

            UUID teacherProfileId,

            UUID academicYearId,

            UUID academicTermId,

            UUID campusId,

            UUID classGradeId,

            UUID streamId,

            UUID subjectId,

            int weeklyPeriods,

            BigDecimal workloadPercentage,

            LocalDate effectiveFrom,

            LocalDate effectiveTo

    ) {
    }

    public record ResourceSupply(

            UUID schedulingResourceId,

            String resourceCode,

            String resourceName,

            String resourceType,

            Integer capacity,

            UUID specializedForSubjectId,

            boolean bookable,

            String resourceStatus

    ) {
    }
}
