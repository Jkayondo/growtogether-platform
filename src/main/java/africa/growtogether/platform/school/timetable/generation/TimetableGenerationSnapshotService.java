package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;

import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;
import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import africa.growtogether.platform.school.timetable.resource.SchedulingResource;
import africa.growtogether.platform.school.timetable.resource.SchedulingResourceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TimetableGenerationSnapshotService {

    private final TimetableGenerationRequestRepository generationRequests;

    private final BellScheduleRepository bellSchedules;

    private final BellPeriodRepository bellPeriods;

    private final ClassOfferingRepository classOfferings;

    private final SubjectOfferingRepository subjectOfferings;

    private final StreamRepository streams;

    private final TeachingAssignmentRepository teachingAssignments;

    private final SchedulingResourceRepository resources;

    private final SchoolProfileService schoolProfiles;

    public TimetableGenerationSnapshotService(
            TimetableGenerationRequestRepository generationRequests,
            BellScheduleRepository bellSchedules,
            BellPeriodRepository bellPeriods,
            ClassOfferingRepository classOfferings,
            SubjectOfferingRepository subjectOfferings,
            StreamRepository streams,
            TeachingAssignmentRepository teachingAssignments,
            SchedulingResourceRepository resources,
            SchoolProfileService schoolProfiles
    ) {

        this.generationRequests =
                generationRequests;

        this.bellSchedules =
                bellSchedules;

        this.bellPeriods =
                bellPeriods;

        this.classOfferings =
                classOfferings;

        this.subjectOfferings =
                subjectOfferings;

        this.streams =
                streams;

        this.teachingAssignments =
                teachingAssignments;

        this.resources =
                resources;

        this.schoolProfiles =
                schoolProfiles;
    }

    @Transactional(readOnly = true)
    public TimetableGenerationSnapshot build(
            UUID tenantId,
            UUID generationRequestId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (generationRequestId == null) {
            throw new IllegalArgumentException(
                    "generationRequestId must not be null"
            );
        }

        TimetableGenerationRequest request =
                generationRequests
                        .findByTenantIdAndId(
                                tenantId,
                                generationRequestId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Timetable generation request not found"
                                )
                        );

        if (
                !"READY".equals(
                        request.getGenerationStatus()
                )
        ) {
            throw new IllegalStateException(
                    "Generation snapshot can only be built for a READY request"
            );
        }

        BellSchedule bellSchedule =
                bellSchedules
                        .findByTenantIdAndId(
                                tenantId,
                                request.getBellScheduleId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell schedule not found for tenant"
                                )
                        );

        if (
                !request.getCampusId().equals(
                        bellSchedule.getCampusId()
                )
        ) {
            throw new IllegalStateException(
                    "Generation request bell schedule no longer belongs to campus"
            );
        }

        List<String> enabledDays =
                enabledDays(
                        bellSchedule
                );

        List<TimetableGenerationSnapshot.BellSlot> slots =
                bellPeriods
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                tenantId,
                                request.getBellScheduleId(),
                                EntityStatus.ACTIVE
                        )
                        .stream()
                        .filter(
                                BellPeriod::isSchedulingAllowed
                        )
                        .map(
                                period ->
                                        new TimetableGenerationSnapshot.BellSlot(
                                                period.getId(),
                                                period.getPeriodCode(),
                                                period.getSequenceNumber(),
                                                period.getPeriodType(),
                                                period.getStartTime(),
                                                period.getEndTime()
                                        )
                        )
                        .toList();

        List<ClassOffering> activeClasses =
                classOfferings
                        .findByTenantIdAndAcademicYearId(
                                tenantId,
                                request.getAcademicYearId()
                        )
                        .stream()
                        .filter(
                                item ->
                                        request.getCampusId()
                                                .equals(
                                                        item.getCampusId()
                                                )
                        )
                        .filter(
                                item ->
                                        isActive(
                                                item.getOfferingStatus()
                                        )
                        )
                        .sorted(
                                Comparator.comparing(
                                        ClassOffering::getOfferingCode
                                )
                        )
                        .toList();

        List<TimetableGenerationSnapshot.ClassDemand> classDemand =
                activeClasses
                        .stream()
                        .map(
                                item ->
                                        new TimetableGenerationSnapshot.ClassDemand(
                                                item.getId(),
                                                item.getOfferingCode(),
                                                item.getClassGradeId(),
                                                item.getPlannedCapacity()
                                        )
                        )
                        .toList();

        List<TimetableGenerationSnapshot.SubjectDemand> subjectDemand =
                buildSubjectDemand(
                        tenantId,
                        request,
                        activeClasses
                );

        Set<UUID> activeClassGradeIds =
                new HashSet<>();

        for (ClassOffering item : activeClasses) {

            if (item.getClassGradeId() != null) {
                activeClassGradeIds.add(
                        item.getClassGradeId()
                );
            }
        }

        List<TimetableGenerationSnapshot.StreamScope> streamScope =
                streams
                        .findByTenantIdAndCampusId(
                                tenantId,
                                request.getCampusId()
                        )
                        .stream()
                        .filter(
                                item ->
                                        activeClassGradeIds.contains(
                                                item.getClassGradeId()
                                        )
                        )
                        .sorted(
                                Comparator.comparing(
                                        item ->
                                                item.getId()
                                                        .toString()
                                )
                        )
                        .map(
                                item ->
                                        new TimetableGenerationSnapshot.StreamScope(
                                                item.getId(),
                                                item.getCampusId(),
                                                item.getClassGradeId(),
                                                item.getCapacity()
                                        )
                        )
                        .toList();

        List<TimetableGenerationSnapshot.TeachingSupply> teachingSupply =
                teachingAssignments
                        .findByTenantIdAndAcademicYearId(
                                tenantId,
                                request.getAcademicYearId()
                        )
                        .stream()
                        .filter(
                                item ->
                                        request.getCampusId()
                                                .equals(
                                                        item.getCampusId()
                                                )
                        )
                        .filter(
                                item ->
                                        isActive(
                                                item.getAssignmentStatus()
                                        )
                        )
                        .filter(
                                item ->
                                        termApplies(
                                                item.getAcademicTermId(),
                                                request.getAcademicTermId()
                                        )
                        )
                        .filter(
                                item ->
                                        coversGenerationScope(
                                                item.getEffectiveFrom(),
                                                item.getEffectiveTo(),
                                                request.getEffectiveFrom(),
                                                request.getEffectiveTo()
                                        )
                        )
                        .sorted(
                                Comparator.comparing(
                                        TeachingAssignment::getAssignmentReference
                                )
                        )
                        .map(
                                item ->
                                        new TimetableGenerationSnapshot.TeachingSupply(
                                                item.getId(),
                                                item.getAssignmentReference(),
                                                item.getTeacherProfileId(),
                                                item.getAcademicYearId(),
                                                item.getAcademicTermId(),
                                                item.getCampusId(),
                                                item.getClassGradeId(),
                                                item.getStreamId(),
                                                item.getSubjectId(),
                                                item.getWeeklyPeriods(),
                                                item.getWorkloadPercentage(),
                                                item.getEffectiveFrom(),
                                                item.getEffectiveTo()
                                        )
                        )
                        .toList();

        List<TimetableGenerationSnapshot.ResourceSupply> resourceSupply =
                resources
                        .findByTenantIdAndCampusId(
                                tenantId,
                                request.getCampusId()
                        )
                        .stream()
                        .filter(
                                item ->
                                        isActive(
                                                item.getResourceStatus()
                                        )
                        )
                        .filter(
                                SchedulingResource::isBookable
                        )
                        .sorted(
                                Comparator.comparing(
                                        SchedulingResource::getResourceCode
                                )
                        )
                        .map(
                                item ->
                                        new TimetableGenerationSnapshot.ResourceSupply(
                                                item.getId(),
                                                item.getResourceCode(),
                                                item.getResourceName(),
                                                item.getResourceType(),
                                                item.getCapacity(),
                                                item.getSpecializedForSubjectId(),
                                                item.isBookable(),
                                                item.getResourceStatus()
                                        )
                        )
                        .toList();

        return new TimetableGenerationSnapshot(

                request.getId(),

                request.getGenerationCode(),

                new TimetableGenerationSnapshot.Scope(

                        request.getAcademicYearId(),

                        request.getAcademicTermId(),

                        request.getCampusId(),

                        request.getBellScheduleId(),

                        request.getTimetableType(),

                        request.getEffectiveFrom(),

                        request.getEffectiveTo(),

                        schoolProfiles
                                .requireTimezone(
                                        tenantId
                                )
                                .getId(),

                        request.getGenerationMode(),

                        request.getModelCode(),

                        request.getObjectives()
                ),

                List.copyOf(
                        enabledDays
                ),

                List.copyOf(
                        slots
                ),

                List.copyOf(
                        classDemand
                ),

                List.copyOf(
                        subjectDemand
                ),

                List.copyOf(
                        streamScope
                ),

                List.copyOf(
                        teachingSupply
                ),

                List.copyOf(
                        resourceSupply
                )
        );
    }

    private List<TimetableGenerationSnapshot.SubjectDemand>
    buildSubjectDemand(
            UUID tenantId,
            TimetableGenerationRequest request,
            List<ClassOffering> classes
    ) {

        List<TimetableGenerationSnapshot.SubjectDemand> result =
                new ArrayList<>();

        for (ClassOffering classOffering : classes) {

            List<SubjectOffering> offerings =
                    subjectOfferings
                            .findByTenantIdAndClassOfferingId(
                                    tenantId,
                                    classOffering.getId()
                            );

            for (SubjectOffering subject : offerings) {

                if (
                        !isActive(
                                subject.getOfferingStatus()
                        )
                ) {
                    continue;
                }

                if (
                        !termApplies(
                                subject.getAcademicTermId(),
                                request.getAcademicTermId()
                        )
                ) {
                    continue;
                }

                result.add(
                        new TimetableGenerationSnapshot.SubjectDemand(
                                subject.getId(),
                                subject.getSubjectOfferingCode(),
                                subject.getClassOfferingId(),
                                subject.getAcademicTermId(),
                                subject.getStreamId(),
                                subject.getSubjectId(),
                                subject.getWeeklyPeriods()
                        )
                );
            }
        }

        result.sort(
                Comparator.comparing(
                        TimetableGenerationSnapshot.SubjectDemand
                                ::subjectOfferingCode
                )
        );

        return result;
    }

    private List<String> enabledDays(
            BellSchedule schedule
    ) {

        List<String> days =
                new ArrayList<>();

        if (schedule.isMondayEnabled()) {
            days.add("MONDAY");
        }

        if (schedule.isTuesdayEnabled()) {
            days.add("TUESDAY");
        }

        if (schedule.isWednesdayEnabled()) {
            days.add("WEDNESDAY");
        }

        if (schedule.isThursdayEnabled()) {
            days.add("THURSDAY");
        }

        if (schedule.isFridayEnabled()) {
            days.add("FRIDAY");
        }

        if (schedule.isSaturdayEnabled()) {
            days.add("SATURDAY");
        }

        if (schedule.isSundayEnabled()) {
            days.add("SUNDAY");
        }

        return days;
    }

    private boolean termApplies(
            UUID itemTermId,
            UUID requestedTermId
    ) {

        if (requestedTermId == null) {
            return true;
        }

        return itemTermId == null
                || requestedTermId.equals(
                        itemTermId
                );
    }

    private boolean coversGenerationScope(
            LocalDate itemFrom,
            LocalDate itemTo,
            LocalDate scopeFrom,
            LocalDate scopeTo
    ) {

        if (
                itemFrom != null
                && scopeFrom != null
                && itemFrom.isAfter(
                        scopeFrom
                )
        ) {
            return false;
        }

        if (
                itemTo != null
                && scopeTo != null
                && itemTo.isBefore(
                        scopeTo
                )
        ) {
            return false;
        }

        return true;
    }

    private boolean isActive(
            String value
    ) {

        return value != null
                && "ACTIVE".equalsIgnoreCase(
                        value.trim()
                );
    }
}
