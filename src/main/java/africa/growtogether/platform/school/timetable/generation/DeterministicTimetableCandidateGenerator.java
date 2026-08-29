package africa.growtogether.platform.school.timetable.generation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class DeterministicTimetableCandidateGenerator {

    private final TimetableCandidateAvailabilityService candidateAvailability;

    public DeterministicTimetableCandidateGenerator(
            TimetableCandidateAvailabilityService candidateAvailability
    ) {

        this.candidateAvailability =
                candidateAvailability;
    }

    public TimetableGenerationCandidate generate(
            UUID tenantId,
            TimetableGenerationSnapshot snapshot
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "snapshot must not be null"
            );
        }

        if (snapshot.generationRequestId() == null) {
            throw new IllegalArgumentException(
                    "generationRequestId must not be null"
            );
        }

        List<String> days =
                snapshot.enabledDays() == null
                        ? List.of()
                        : snapshot.enabledDays();

        List<TimetableGenerationSnapshot.BellSlot> periods =
                snapshot.bellSlots() == null
                        ? List.of()
                        : snapshot.bellSlots()
                                .stream()
                                .sorted(
                                        Comparator.comparing(
                                                TimetableGenerationSnapshot.BellSlot
                                                        ::sequenceNumber
                                        )
                                )
                                .toList();

        List<TimetableGenerationSnapshot.SubjectDemand> demand =
                snapshot.subjects() == null
                        ? List.of()
                        : snapshot.subjects()
                                .stream()
                                .filter(
                                        item ->
                                                item.weeklyPeriods() != null
                                                && item.weeklyPeriods() > 0
                                )
                                /*
                                 * Higher-demand subjects are placed first.
                                 * Code is the deterministic tie-breaker.
                                 */
                                .sorted(
                                        Comparator
                                                .comparing(
                                                        TimetableGenerationSnapshot.SubjectDemand
                                                                ::weeklyPeriods,
                                                        Comparator.reverseOrder()
                                                )
                                                .thenComparing(
                                                        TimetableGenerationSnapshot.SubjectDemand
                                                                ::subjectOfferingCode
                                                )
                                )
                                .toList();

        int requiredPeriods =
                demand
                        .stream()
                        .mapToInt(
                                TimetableGenerationSnapshot.SubjectDemand
                                        ::weeklyPeriods
                        )
                        .sum();

        List<TimetableGenerationCandidate.Placement> placements =
                new ArrayList<>();

        List<TimetableGenerationCandidate.UnplacedDemand> unplaced =
                new ArrayList<>();

        if (
                requiredPeriods > 0
                && (
                        days.isEmpty()
                        || periods.isEmpty()
                )
        ) {

            for (
                    TimetableGenerationSnapshot.SubjectDemand subject
                    : demand
            ) {

                unplaced.add(
                        unplaced(
                                subject,
                                subject.weeklyPeriods(),
                                "NO_SCHEDULING_SLOTS",
                                "No enabled teaching day and Bell Period combination is available"
                        )
                );
            }

            return candidate(
                    snapshot,
                    requiredPeriods,
                    placements,
                    unplaced
            );
        }

        Map<UUID, TimetableGenerationSnapshot.ClassDemand>
                classesById =
                new HashMap<>();

        if (snapshot.classes() != null) {

            for (
                    TimetableGenerationSnapshot.ClassDemand item
                    : snapshot.classes()
            ) {

                classesById.put(
                        item.classOfferingId(),
                        item
                );
            }
        }

        List<TimetableGenerationSnapshot.TeachingSupply> assignments =
                snapshot.teachingAssignments() == null
                        ? List.of()
                        : snapshot.teachingAssignments()
                                .stream()
                                .sorted(
                                        Comparator.comparing(
                                                TimetableGenerationSnapshot.TeachingSupply
                                                        ::assignmentReference
                                        )
                                )
                                .toList();

        Map<UUID, Integer> assignmentUsage =
                new HashMap<>();

        for (
                TimetableGenerationSnapshot.SubjectDemand subject
                : demand
        ) {

            TimetableGenerationSnapshot.ClassDemand classDemand =
                    classesById.get(
                            subject.classOfferingId()
                    );

            if (classDemand == null) {

                unplaced.add(
                        unplaced(
                                subject,
                                subject.weeklyPeriods(),
                                "CLASS_SCOPE_NOT_FOUND",
                                "Subject demand does not have a matching class offering in the generation snapshot"
                        )
                );

                continue;
            }

            List<TimetableGenerationSnapshot.TeachingSupply>
                    matchingAssignments =
                    assignments
                            .stream()
                            .filter(
                                    assignment ->
                                            matches(
                                                    subject,
                                                    classDemand,
                                                    assignment
                                            )
                            )
                            .toList();

            if (matchingAssignments.isEmpty()) {

                unplaced.add(
                        unplaced(
                                subject,
                                subject.weeklyPeriods(),
                                "NO_MATCHING_TEACHING_ASSIGNMENT",
                                "No active teaching assignment matches the subject, class grade and stream"
                        )
                );

                continue;
            }

            int placedForSubject =
                    0;

            for (
                    int occurrence = 0;
                    occurrence < subject.weeklyPeriods();
                    occurrence++
            ) {

                PlacementAttempt attempt =
                        findPlacement(
                                tenantId,
                                snapshot,
                                subject,
                                classDemand,
                                matchingAssignments,
                                days,
                                periods,
                                occurrence,
                                placements,
                                assignmentUsage
                        );

                if (attempt == null) {
                    break;
                }

                placements.add(
                        attempt.placement()
                );

                assignmentUsage.merge(
                        attempt.assignmentId(),
                        1,
                        Integer::sum
                );

                placedForSubject++;
            }

            int remaining =
                    subject.weeklyPeriods()
                            - placedForSubject;

            if (remaining > 0) {

                unplaced.add(
                        unplaced(
                                subject,
                                remaining,
                                "NO_CONFLICT_FREE_SLOT",
                                "No remaining conflict-free slot exists within teacher assignment capacity"
                        )
                );
            }
        }

        placements.sort(
                Comparator
                        .comparingInt(
                                (
                                        TimetableGenerationCandidate.Placement placement
                                ) ->
                                        days.indexOf(
                                                placement.dayOfWeek()
                                        )
                        )
                        .thenComparingInt(
                                (
                                        TimetableGenerationCandidate.Placement placement
                                ) ->
                                        periodSequence(
                                                periods,
                                                placement.bellPeriodId()
                                        )
                        )
                        .thenComparing(
                                (
                                        TimetableGenerationCandidate.Placement placement
                                ) ->
                                        placement.classOfferingId()
                                                .toString()
                        )
        );

        return candidate(
                snapshot,
                requiredPeriods,
                placements,
                unplaced
        );
    }

    private PlacementAttempt findPlacement(
            UUID tenantId,
            TimetableGenerationSnapshot snapshot,
            TimetableGenerationSnapshot.SubjectDemand subject,
            TimetableGenerationSnapshot.ClassDemand classDemand,
            List<TimetableGenerationSnapshot.TeachingSupply> assignments,
            List<String> days,
            List<TimetableGenerationSnapshot.BellSlot> periods,
            int occurrence,
            List<TimetableGenerationCandidate.Placement> existing,
            Map<UUID, Integer> assignmentUsage
    ) {

        /*
         * IMPROVEMENT:
         * Rotate the preferred day for each weekly occurrence.
         *
         * Five required periods across a Monday-Friday calendar will
         * therefore naturally prefer one lesson on each day instead of
         * filling every Monday period first.
         */
        int preferredDayIndex =
                occurrence % days.size();

        for (
                int dayOffset = 0;
                dayOffset < days.size();
                dayOffset++
        ) {

            String day =
                    days.get(
                            (
                                    preferredDayIndex
                                    + dayOffset
                            )
                            % days.size()
                    );

            for (
                    TimetableGenerationSnapshot.BellSlot period
                    : periods
            ) {

                if (
                        !classSlotAvailable(
                                existing,
                                subject.classOfferingId(),
                                subject.streamId(),
                                day,
                                period.bellPeriodId()
                        )
                ) {
                    continue;
                }

                for (
                        TimetableGenerationSnapshot.TeachingSupply assignment
                        : assignments
                ) {

                    int used =
                            assignmentUsage.getOrDefault(
                                    assignment.teachingAssignmentId(),
                                    0
                            );

                    if (
                            used >= assignment.weeklyPeriods()
                    ) {
                        continue;
                    }

                    if (
                            !teacherSlotAvailable(
                                    existing,
                                    assignment.teacherProfileId(),
                                    day,
                                    period.bellPeriodId()
                            )
                    ) {
                        continue;
                    }

                    /*
                     * GT-AI-PRINCIPLE-001
                     *
                     * A structurally empty slot is not necessarily an
                     * operationally available slot.
                     *
                     * The authoritative tenant-scoped availability
                     * evaluator checks every weekly occurrence using
                     * the school's configured timezone.
                     *
                     * No conflict is persisted here because this is
                     * still hypothetical candidate exploration.
                     */
                    if (
                            candidateAvailability
                                    .evaluate(
                                            tenantId,
                                            snapshot,
                                            period,
                                            day,
                                            assignment.teacherProfileId(),
                                            null
                                    )
                                    .isPresent()
                    ) {
                        continue;
                    }

                    /*
                     * A9.3.2 — governed resource selection.
                     *
                     * A scheduling resource is optional in the
                     * authoritative timetable model. When suitable
                     * resources exist, however, GT selects one using
                     * subject specialization, capacity, candidate-slot
                     * conflicts and operational availability.
                     */
                    UUID selectedResourceId =
                            selectResource(
                                    tenantId,
                                    snapshot,
                                    subject,
                                    classDemand,
                                    day,
                                    period,
                                    existing
                            );

                    String placementReason =
                            selectedResourceId == null
                                    ? "Placed by deterministic GT timetable engine from verified subject demand and teaching assignment; no mandatory scheduling resource assigned"
                                    : "Placed by deterministic GT timetable engine from verified subject demand, teaching assignment and governed scheduling resource selection";

                    TimetableGenerationCandidate.Placement placement =
                            new TimetableGenerationCandidate.Placement(

                                    day,

                                    period.bellPeriodId(),

                                    period.periodCode(),

                                    subject.classOfferingId(),

                                    subject.subjectOfferingId(),

                                    classDemand.classGradeId(),

                                    subject.streamId(),

                                    subject.subjectId(),

                                    assignment.teachingAssignmentId(),

                                    assignment.teacherProfileId(),

                                    selectedResourceId,

                                    placementReason
                            );

                    return new PlacementAttempt(
                            placement,
                            assignment.teachingAssignmentId()
                    );
                }
            }
        }

        return null;
    }

    private UUID selectResource(
            UUID tenantId,
            TimetableGenerationSnapshot snapshot,
            TimetableGenerationSnapshot.SubjectDemand subject,
            TimetableGenerationSnapshot.ClassDemand classDemand,
            String day,
            TimetableGenerationSnapshot.BellSlot period,
            List<TimetableGenerationCandidate.Placement> existing
    ) {

        if (
                snapshot.resources() == null
                || snapshot.resources().isEmpty()
        ) {
            return null;
        }

        Integer requiredCapacity =
                requiredCapacity(
                        snapshot,
                        subject,
                        classDemand
                );

        List<TimetableGenerationSnapshot.ResourceSupply> candidates =
                snapshot.resources()
                        .stream()
                        .filter(
                                TimetableGenerationSnapshot.ResourceSupply
                                        ::bookable
                        )
                        .filter(
                                resource ->
                                        resource.resourceStatus() != null
                                        && "ACTIVE".equalsIgnoreCase(
                                                resource.resourceStatus()
                                        )
                        )
                        /*
                         * A resource specialized for another subject
                         * must never be silently reused.
                         */
                        .filter(
                                resource ->
                                        resource.specializedForSubjectId() == null
                                        || Objects.equals(
                                                resource.specializedForSubjectId(),
                                                subject.subjectId()
                                        )
                        )
                        /*
                         * When both capacities are known, enforce them.
                         * Unknown capacity is not treated as evidence of
                         * insufficient capacity.
                         */
                        .filter(
                                resource ->
                                        requiredCapacity == null
                                        || resource.capacity() == null
                                        || resource.capacity()
                                                >= requiredCapacity
                        )
                        /*
                         * Prefer subject-specialized resources over
                         * generic resources. Resource code is the
                         * deterministic tie-breaker.
                         */
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                (
                                                        TimetableGenerationSnapshot.ResourceSupply resource
                                                ) ->
                                                        Objects.equals(
                                                                resource.specializedForSubjectId(),
                                                                subject.subjectId()
                                                        )
                                                                ? 0
                                                                : 1
                                        )
                                        .thenComparing(
                                                (
                                                        TimetableGenerationSnapshot.ResourceSupply resource
                                                ) ->
                                                        resource.resourceCode() == null
                                                                ? ""
                                                                : resource.resourceCode()
                                        )
                        )
                        .toList();

        for (
                TimetableGenerationSnapshot.ResourceSupply resource
                : candidates
        ) {

            if (
                    !resourceSlotAvailable(
                            existing,
                            resource.schedulingResourceId(),
                            day,
                            period.bellPeriodId()
                    )
            ) {
                continue;
            }

            /*
             * Resource availability is evaluated independently from
             * teacher availability because the teacher has already
             * passed the authoritative check immediately above.
             */
            if (
                    candidateAvailability
                            .evaluate(
                                    tenantId,
                                    snapshot,
                                    period,
                                    day,
                                    null,
                                    resource.schedulingResourceId()
                            )
                            .isPresent()
            ) {
                continue;
            }

            return resource.schedulingResourceId();
        }

        /*
         * Resource assignment remains optional in the authoritative
         * Release 1 timetable domain. Failure to find a valid resource
         * therefore does not invent a new lesson-blocking rule.
         */
        return null;
    }

    private Integer requiredCapacity(
            TimetableGenerationSnapshot snapshot,
            TimetableGenerationSnapshot.SubjectDemand subject,
            TimetableGenerationSnapshot.ClassDemand classDemand
    ) {

        if (
                subject.streamId() != null
                && snapshot.streams() != null
        ) {

            Integer streamCapacity =
                    snapshot.streams()
                            .stream()
                            .filter(
                                    stream ->
                                            Objects.equals(
                                                    stream.streamId(),
                                                    subject.streamId()
                                            )
                            )
                            .map(
                                    TimetableGenerationSnapshot.StreamScope
                                            ::capacity
                            )
                            .filter(
                                    Objects::nonNull
                            )
                            .findFirst()
                            .orElse(null);

            if (streamCapacity != null) {
                return streamCapacity;
            }
        }

        return classDemand.plannedCapacity();
    }

    private boolean resourceSlotAvailable(
            List<TimetableGenerationCandidate.Placement> existing,
            UUID schedulingResourceId,
            String day,
            UUID bellPeriodId
    ) {

        if (schedulingResourceId == null) {
            return true;
        }

        return existing
                .stream()
                .noneMatch(
                        placement ->
                                Objects.equals(
                                        schedulingResourceId,
                                        placement.schedulingResourceId()
                                )
                                && day.equals(
                                        placement.dayOfWeek()
                                )
                                && Objects.equals(
                                        bellPeriodId,
                                        placement.bellPeriodId()
                                )
                );
    }

    private boolean matches(
            TimetableGenerationSnapshot.SubjectDemand subject,
            TimetableGenerationSnapshot.ClassDemand classDemand,
            TimetableGenerationSnapshot.TeachingSupply assignment
    ) {

        if (
                !Objects.equals(
                        subject.subjectId(),
                        assignment.subjectId()
                )
        ) {
            return false;
        }

        if (
                !Objects.equals(
                        classDemand.classGradeId(),
                        assignment.classGradeId()
                )
        ) {
            return false;
        }

        /*
         * Be conservative at this stage:
         *
         * - a stream-specific subject requires the same stream assignment;
         * - a class-wide subject requires a class-wide assignment.
         *
         * We do not silently convert a stream assignment into a
         * class-wide teaching authority.
         */
        return Objects.equals(
                subject.streamId(),
                assignment.streamId()
        );
    }

    private boolean classSlotAvailable(
            List<TimetableGenerationCandidate.Placement> existing,
            UUID classOfferingId,
            UUID streamId,
            String day,
            UUID bellPeriodId
    ) {

        for (
                TimetableGenerationCandidate.Placement placement
                : existing
        ) {

            if (
                    !Objects.equals(
                            classOfferingId,
                            placement.classOfferingId()
                    )
            ) {
                continue;
            }

            if (
                    !day.equals(
                            placement.dayOfWeek()
                    )
                    || !Objects.equals(
                            bellPeriodId,
                            placement.bellPeriodId()
                    )
            ) {
                continue;
            }

            /*
             * Class-wide lessons conflict with every stream.
             * Two different streams may use the same period.
             */
            if (
                    streamId == null
                    || placement.streamId() == null
                    || Objects.equals(
                            streamId,
                            placement.streamId()
                    )
            ) {
                return false;
            }
        }

        return true;
    }

    private boolean teacherSlotAvailable(
            List<TimetableGenerationCandidate.Placement> existing,
            UUID teacherProfileId,
            String day,
            UUID bellPeriodId
    ) {

        return existing
                .stream()
                .noneMatch(
                        placement ->
                                Objects.equals(
                                        teacherProfileId,
                                        placement.teacherProfileId()
                                )
                                && day.equals(
                                        placement.dayOfWeek()
                                )
                                && Objects.equals(
                                        bellPeriodId,
                                        placement.bellPeriodId()
                                )
                );
    }

    private int periodSequence(
            List<TimetableGenerationSnapshot.BellSlot> periods,
            UUID bellPeriodId
    ) {

        return periods
                .stream()
                .filter(
                        item ->
                                Objects.equals(
                                        item.bellPeriodId(),
                                        bellPeriodId
                                )
                )
                .map(
                        TimetableGenerationSnapshot.BellSlot
                                ::sequenceNumber
                )
                .findFirst()
                .orElse(
                        Integer.MAX_VALUE
                );
    }

    private TimetableGenerationCandidate.UnplacedDemand unplaced(
            TimetableGenerationSnapshot.SubjectDemand subject,
            int remaining,
            String reasonCode,
            String explanation
    ) {

        return new TimetableGenerationCandidate.UnplacedDemand(

                subject.subjectOfferingId(),

                subject.subjectOfferingCode(),

                subject.classOfferingId(),

                subject.streamId(),

                remaining,

                reasonCode,

                explanation
        );
    }

    private TimetableGenerationCandidate candidate(
            TimetableGenerationSnapshot snapshot,
            int required,
            List<TimetableGenerationCandidate.Placement> placements,
            List<TimetableGenerationCandidate.UnplacedDemand> unplaced
    ) {

        int placed =
                placements.size();

        String status =
                placed == required
                        && unplaced.isEmpty()
                        ? "COMPLETE"
                        : "INCOMPLETE";

        return new TimetableGenerationCandidate(

                snapshot.generationRequestId(),

                status,

                required,

                placed,

                List.copyOf(
                        placements
                ),

                List.copyOf(
                        unplaced
                )
        );
    }

    private record PlacementAttempt(

            TimetableGenerationCandidate.Placement placement,

            UUID assignmentId

    ) {
    }
}
