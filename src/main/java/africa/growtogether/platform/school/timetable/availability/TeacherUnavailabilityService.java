package africa.growtogether.platform.school.timetable.availability;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeacherUnavailabilityService {

    private final TeacherUnavailabilityRepository repository;
    private final TeacherProfileRepository teachers;

    public TeacherUnavailabilityService(
            TeacherUnavailabilityRepository repository,
            TeacherProfileRepository teachers
    ) {
        this.repository = repository;
        this.teachers = teachers;
    }

    @Transactional
    public TeacherUnavailability create(
            UUID tenantId,
            CreateTeacherUnavailabilityCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        teachers
                .findByTenantIdAndId(
                        tenantId,
                        command.teacherProfileId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher profile not found for tenant"
                        )
                );

        /*
         * IMPROVEMENT:
         * Do not allow contradictory overlapping active
         * unavailability periods for the same teacher.
         */
        List<TeacherUnavailability> overlaps =
                repository
                        .findByTenantIdAndTeacherProfileIdAndUnavailableFromLessThanAndUnavailableToGreaterThanAndStatus(
                                tenantId,
                                command.teacherProfileId(),
                                command.unavailableTo(),
                                command.unavailableFrom(),
                                EntityStatus.ACTIVE
                        );

        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException(
                    "Teacher already has overlapping unavailability"
            );
        }

        TeacherUnavailability unavailability =
                new TeacherUnavailability(
                        command.teacherProfileId(),
                        command.unavailableFrom(),
                        command.unavailableTo(),
                        command.reasonType(),
                        command.reason(),
                        command.recurring(),
                        command.recurrenceRule(),
                        command.leaveExtensionId()
                );

        unavailability.setTenantId(
                tenantId
        );

        return repository.save(
                unavailability
        );
    }

    @Transactional(readOnly = true)
    public TeacherUnavailability get(
            UUID tenantId,
            UUID unavailabilityId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        unavailabilityId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher unavailability not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<TeacherUnavailability> findByTeacher(
            UUID tenantId,
            UUID teacherProfileId
    ) {

        return repository
                .findByTenantIdAndTeacherProfileIdAndStatus(
                        tenantId,
                        teacherProfileId,
                        EntityStatus.ACTIVE
                );
    }

    @Transactional
    public TeacherUnavailability cancel(
            UUID tenantId,
            UUID unavailabilityId
    ) {

        TeacherUnavailability unavailability =
                get(
                        tenantId,
                        unavailabilityId
                );

        unavailability.setStatus(
                EntityStatus.INACTIVE
        );

        return repository.save(
                unavailability
        );
    }
}
