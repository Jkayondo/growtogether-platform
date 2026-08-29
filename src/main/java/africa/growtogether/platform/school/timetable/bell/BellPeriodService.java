package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.EntityStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BellPeriodService {

    private final BellPeriodRepository repository;
    private final BellScheduleRepository schedules;

    public BellPeriodService(
            BellPeriodRepository repository,
            BellScheduleRepository schedules
    ) {
        this.repository = repository;
        this.schedules = schedules;
    }

    @Transactional
    public BellPeriod create(
            UUID tenantId,
            CreateBellPeriodCommand command
    ) {

        schedules
                .findByTenantIdAndId(
                        tenantId,
                        command.bellScheduleId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Bell schedule not found for tenant"
                        )
                );

        if (
                repository
                        .existsByTenantIdAndBellScheduleIdAndPeriodCode(
                                tenantId,
                                command.bellScheduleId(),
                                command.periodCode()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Bell period code already exists for schedule"
            );
        }

        if (
                repository
                        .existsByTenantIdAndBellScheduleIdAndSequenceNumber(
                                tenantId,
                                command.bellScheduleId(),
                                command.sequenceNumber()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Bell period sequence already exists for schedule"
            );
        }

        BellPeriod candidate =
                new BellPeriod(
                        command.bellScheduleId(),
                        command.periodCode(),
                        command.periodName(),
                        command.sequenceNumber(),
                        command.periodType(),
                        command.startTime(),
                        command.endTime(),
                        command.instructionalMinutes(),
                        command.attendanceRequired(),
                        command.schedulingAllowed()
                );

        List<BellPeriod> existingPeriods =
                repository
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                tenantId,
                                command.bellScheduleId(),
                                EntityStatus.ACTIVE
                        );

        for (BellPeriod existing : existingPeriods) {

            boolean overlaps =
                    candidate.getStartTime()
                            .isBefore(
                                    existing.getEndTime()
                            )
                    &&
                    candidate.getEndTime()
                            .isAfter(
                                    existing.getStartTime()
                            );

            if (overlaps) {
                throw new IllegalArgumentException(
                        "Bell period overlaps existing period "
                                + existing.getPeriodCode()
                );
            }
        }

        candidate.setTenantId(
                tenantId
        );

        return repository.save(
                candidate
        );
    }

    @Transactional(readOnly = true)
    public BellPeriod get(
            UUID tenantId,
            UUID periodId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        periodId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Bell period not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<BellPeriod> findBySchedule(
            UUID tenantId,
            UUID scheduleId
    ) {

        return repository
                .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                        tenantId,
                        scheduleId,
                        EntityStatus.ACTIVE
                );
    }
}
