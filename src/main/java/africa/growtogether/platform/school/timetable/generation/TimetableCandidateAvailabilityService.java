package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityConflict;
import africa.growtogether.platform.school.timetable.availability.TimetableAvailabilityEvaluator;

import africa.growtogether.platform.school.timetable.bell.BellPeriod;
import africa.growtogether.platform.school.timetable.bell.BellPeriodRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TimetableCandidateAvailabilityService {

    private final BellPeriodRepository bellPeriods;

    private final TimetableAvailabilityEvaluator availability;

    public TimetableCandidateAvailabilityService(
            BellPeriodRepository bellPeriods,
            TimetableAvailabilityEvaluator availability
    ) {

        this.bellPeriods = bellPeriods;
        this.availability = availability;
    }

    /*
     * GT-AI-PRINCIPLE-001
     *
     * Intelligence may explore a candidate slot, but the authoritative
     * scheduling evidence must still come from tenant-scoped school data.
     *
     * This service deliberately performs no persistence.
     */
    @Transactional(readOnly = true)
    public Optional<TimetableAvailabilityConflict> evaluate(
            UUID tenantId,
            TimetableGenerationSnapshot snapshot,
            TimetableGenerationSnapshot.BellSlot slot,
            String dayOfWeek,
            UUID teacherProfileId,
            UUID schedulingResourceId
    ) {

        if (
                dayOfWeek == null
                || dayOfWeek.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "dayOfWeek must not be blank"
            );
        }

        BellPeriod bellPeriod =
                requireBellPeriod(
                        tenantId,
                        snapshot,
                        slot
                );

        return availability.evaluateCandidate(
                tenantId,
                bellPeriod,
                dayOfWeek,
                teacherProfileId,
                schedulingResourceId,
                snapshot.scope().effectiveFrom(),
                snapshot.scope().effectiveTo()
        );
    }

    private BellPeriod requireBellPeriod(
            UUID tenantId,
            TimetableGenerationSnapshot snapshot,
            TimetableGenerationSnapshot.BellSlot slot
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

        if (slot == null) {
            throw new IllegalArgumentException(
                    "slot must not be null"
            );
        }

        BellPeriod bellPeriod =
                bellPeriods
                        .findByTenantIdAndId(
                                tenantId,
                                slot.bellPeriodId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Bell period not found for tenant"
                                )
                        );

        if (
                snapshot.scope() == null
                || snapshot.scope().bellScheduleId() == null
        ) {
            throw new IllegalStateException(
                    "Generation snapshot Bell Schedule is required"
            );
        }

        if (
                !snapshot.scope()
                        .bellScheduleId()
                        .equals(
                                bellPeriod.getBellScheduleId()
                        )
        ) {
            throw new IllegalStateException(
                    "Candidate Bell Period does not belong to generation Bell Schedule"
            );
        }

        return bellPeriod;
    }

}
