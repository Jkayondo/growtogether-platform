package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BellScheduleService {

    private final BellScheduleRepository repository;
    private final CampusRepository campuses;

    public BellScheduleService(
            BellScheduleRepository repository,
            CampusRepository campuses
    ) {
        this.repository = repository;
        this.campuses = campuses;
    }

    @Transactional
    public BellSchedule create(
            UUID tenantId,
            CreateBellScheduleCommand command
    ) {

        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );

        if (
                repository
                        .existsByTenantIdAndCampusIdAndScheduleCode(
                                tenantId,
                                command.campusId(),
                                command.scheduleCode()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Bell schedule code already exists for campus"
            );
        }

        BellSchedule schedule =
                new BellSchedule(
                        command.campusId(),
                        command.scheduleCode(),
                        command.scheduleName(),
                        command.description(),
                        command.scheduleType(),
                        command.effectiveFrom(),
                        command.effectiveTo(),
                        command.mondayEnabled(),
                        command.tuesdayEnabled(),
                        command.wednesdayEnabled(),
                        command.thursdayEnabled(),
                        command.fridayEnabled(),
                        command.saturdayEnabled(),
                        command.sundayEnabled()
                );

        schedule.setTenantId(
                tenantId
        );

        return repository.save(
                schedule
        );
    }

    @Transactional(readOnly = true)
    public BellSchedule get(
            UUID tenantId,
            UUID scheduleId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Bell schedule not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<BellSchedule> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }

    @Transactional
    public BellSchedule approve(
            UUID tenantId,
            UUID scheduleId
    ) {

        BellSchedule schedule =
                get(
                        tenantId,
                        scheduleId
                );

        schedule.approve();

        return repository.save(
                schedule
        );
    }

    @Transactional
    public BellSchedule activate(
            UUID tenantId,
            UUID scheduleId
    ) {

        BellSchedule schedule =
                get(
                        tenantId,
                        scheduleId
                );

        repository
                .findFirstByTenantIdAndCampusIdAndScheduleTypeAndScheduleStatusAndStatus(
                        tenantId,
                        schedule.getCampusId(),
                        schedule.getScheduleType(),
                        "ACTIVE",
                        EntityStatus.ACTIVE
                )
                .filter(
                        existing ->
                                !existing.getId().equals(
                                        schedule.getId()
                                )
                )
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException(
                                    "Another active bell schedule of this type already exists for campus"
                            );
                        }
                );

        schedule.activate();

        return repository.save(
                schedule
        );
    }

    @Transactional
    public BellSchedule suspend(
            UUID tenantId,
            UUID scheduleId
    ) {

        BellSchedule schedule =
                get(
                        tenantId,
                        scheduleId
                );

        schedule.suspend();

        return repository.save(
                schedule
        );
    }
}
