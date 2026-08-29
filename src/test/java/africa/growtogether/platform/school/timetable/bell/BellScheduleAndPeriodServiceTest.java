package africa.growtogether.platform.school.timetable.bell;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BellScheduleAndPeriodServiceTest {

    @Test
    void createsTenantScopedBellScheduleWithDefaultWeekdays() {

        BellScheduleRepository repository =
                mock(BellScheduleRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        BellScheduleService service =
                new BellScheduleService(
                        repository,
                        campuses
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                repository.existsByTenantIdAndCampusIdAndScheduleCode(
                        tenantId,
                        campusId,
                        "REG-01"
                )
        ).thenReturn(false);

        when(
                repository.save(
                        any(BellSchedule.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        BellSchedule result =
                service.create(
                        tenantId,
                        scheduleCommand(
                                campusId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                LocalDate.of(2026, 2, 1),
                                null
                        )
                );

        assertEquals(
                "REGULAR",
                result.getScheduleType()
        );

        assertEquals(
                "DRAFT",
                result.getScheduleStatus()
        );

        assertTrue(result.isMondayEnabled());
        assertTrue(result.isTuesdayEnabled());
        assertTrue(result.isWednesdayEnabled());
        assertTrue(result.isThursdayEnabled());
        assertTrue(result.isFridayEnabled());

        assertFalse(result.isSaturdayEnabled());
        assertFalse(result.isSundayEnabled());

        verify(
                campuses
        ).findByTenantIdAndId(
                tenantId,
                campusId
        );

        verify(
                repository
        ).save(
                any(BellSchedule.class)
        );
    }

    @Test
    void rejectsBellScheduleWhenAllDaysDisabled() {

        BellScheduleRepository repository =
                mock(BellScheduleRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        BellScheduleService service =
                new BellScheduleService(
                        repository,
                        campuses
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                repository.existsByTenantIdAndCampusIdAndScheduleCode(
                        tenantId,
                        campusId,
                        "REG-01"
                )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                scheduleCommand(
                                        campusId,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        false,
                                        LocalDate.of(2026, 2, 1),
                                        null
                                )
                        )
                );

        assertEquals(
                "At least one schedule day must be enabled",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(BellSchedule.class)
        );
    }

    @Test
    void rejectsBellScheduleWithInvalidEffectiveDates() {

        BellScheduleRepository repository =
                mock(BellScheduleRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        BellScheduleService service =
                new BellScheduleService(
                        repository,
                        campuses
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                repository.existsByTenantIdAndCampusIdAndScheduleCode(
                        tenantId,
                        campusId,
                        "REG-01"
                )
        ).thenReturn(false);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                scheduleCommand(
                                        campusId,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2026, 3, 1),
                                        LocalDate.of(2026, 2, 28)
                                )
                        )
                );

        assertEquals(
                "effectiveTo must not be before effectiveFrom",
                error.getMessage()
        );
    }

    @Test
    void rejectsDuplicateBellScheduleCodeWithinCampus() {

        BellScheduleRepository repository =
                mock(BellScheduleRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        BellScheduleService service =
                new BellScheduleService(
                        repository,
                        campuses
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        when(
                campuses.findByTenantIdAndId(
                        tenantId,
                        campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        when(
                repository.existsByTenantIdAndCampusIdAndScheduleCode(
                        tenantId,
                        campusId,
                        "REG-01"
                )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                scheduleCommand(
                                        campusId,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        LocalDate.of(2026, 2, 1),
                                        null
                                )
                        )
                );

        assertEquals(
                "Bell schedule code already exists for campus",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(BellSchedule.class)
        );
    }

    @Test
    void rejectsSecondActiveBellScheduleOfSameTypeForCampus() {

        BellScheduleRepository repository =
                mock(BellScheduleRepository.class);

        CampusRepository campuses =
                mock(CampusRepository.class);

        BellScheduleService service =
                new BellScheduleService(
                        repository,
                        campuses
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID scheduleId =
                UUID.randomUUID();

        UUID existingId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        BellSchedule schedule =
                mock(BellSchedule.class);

        BellSchedule existing =
                mock(BellSchedule.class);

        when(
                schedule.getId()
        ).thenReturn(
                scheduleId
        );

        when(
                schedule.getCampusId()
        ).thenReturn(
                campusId
        );

        when(
                schedule.getScheduleType()
        ).thenReturn(
                "REGULAR"
        );

        when(
                existing.getId()
        ).thenReturn(
                existingId
        );

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(schedule)
        );

        when(
                repository
                        .findFirstByTenantIdAndCampusIdAndScheduleTypeAndScheduleStatusAndStatus(
                                tenantId,
                                campusId,
                                "REGULAR",
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                Optional.of(existing)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.activate(
                                tenantId,
                                scheduleId
                        )
                );

        assertEquals(
                "Another active bell schedule of this type already exists for campus",
                error.getMessage()
        );

        verify(
                schedule,
                never()
        ).activate();

        verify(
                repository,
                never()
        ).save(
                schedule
        );
    }

    @Test
    void rejectsOverlappingBellPeriods() {

        BellPeriodRepository repository =
                mock(BellPeriodRepository.class);

        BellScheduleRepository schedules =
                mock(BellScheduleRepository.class);

        BellPeriodService service =
                new BellPeriodService(
                        repository,
                        schedules
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID scheduleId =
                UUID.randomUUID();

        when(
                schedules.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(
                        mock(BellSchedule.class)
                )
        );

        when(
                repository
                        .existsByTenantIdAndBellScheduleIdAndPeriodCode(
                                tenantId,
                                scheduleId,
                                "P3"
                        )
        ).thenReturn(false);

        when(
                repository
                        .existsByTenantIdAndBellScheduleIdAndSequenceNumber(
                                tenantId,
                                scheduleId,
                                3
                        )
        ).thenReturn(false);

        BellPeriod existing =
                new BellPeriod(
                        scheduleId,
                        "P2",
                        "Period 2",
                        2,
                        "TEACHING",
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 40),
                        40,
                        true,
                        true
                );

        when(
                repository
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                tenantId,
                                scheduleId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(existing)
        );

        CreateBellPeriodCommand command =
                new CreateBellPeriodCommand(
                        scheduleId,
                        "P3",
                        "Period 3",
                        3,
                        "TEACHING",
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 10),
                        40,
                        true,
                        true
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                command
                        )
                );

        assertEquals(
                "Bell period overlaps existing period P2",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(BellPeriod.class)
        );
    }

    @Test
    void allowsAdjacentBellPeriodsWithoutOverlap() {

        BellPeriodRepository repository =
                mock(BellPeriodRepository.class);

        BellScheduleRepository schedules =
                mock(BellScheduleRepository.class);

        BellPeriodService service =
                new BellPeriodService(
                        repository,
                        schedules
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID scheduleId =
                UUID.randomUUID();

        when(
                schedules.findByTenantIdAndId(
                        tenantId,
                        scheduleId
                )
        ).thenReturn(
                Optional.of(
                        mock(BellSchedule.class)
                )
        );

        when(
                repository
                        .existsByTenantIdAndBellScheduleIdAndPeriodCode(
                                tenantId,
                                scheduleId,
                                "P3"
                        )
        ).thenReturn(false);

        when(
                repository
                        .existsByTenantIdAndBellScheduleIdAndSequenceNumber(
                                tenantId,
                                scheduleId,
                                3
                        )
        ).thenReturn(false);

        BellPeriod existing =
                new BellPeriod(
                        scheduleId,
                        "P2",
                        "Period 2",
                        2,
                        "TEACHING",
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 40),
                        40,
                        true,
                        true
                );

        when(
                repository
                        .findByTenantIdAndBellScheduleIdAndStatusOrderBySequenceNumber(
                                tenantId,
                                scheduleId,
                                EntityStatus.ACTIVE
                        )
        ).thenReturn(
                List.of(existing)
        );

        when(
                repository.save(
                        any(BellPeriod.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        BellPeriod result =
                service.create(
                        tenantId,
                        new CreateBellPeriodCommand(
                                scheduleId,
                                "P3",
                                "Period 3",
                                3,
                                "TEACHING",
                                LocalTime.of(9, 40),
                                LocalTime.of(10, 20),
                                40,
                                true,
                                true
                        )
                );

        assertEquals(
                LocalTime.of(9, 40),
                result.getStartTime()
        );

        assertEquals(
                LocalTime.of(10, 20),
                result.getEndTime()
        );

        verify(
                repository
        ).save(
                any(BellPeriod.class)
        );
    }

    @Test
    void rejectsInstructionalMinutesGreaterThanClockDuration() {

        UUID scheduleId =
                UUID.randomUUID();

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new BellPeriod(
                                scheduleId,
                                "P1",
                                "Period 1",
                                1,
                                "TEACHING",
                                LocalTime.of(8, 0),
                                LocalTime.of(8, 40),
                                50,
                                true,
                                true
                        )
                );

        assertEquals(
                "instructionalMinutes must not exceed period duration",
                error.getMessage()
        );
    }

    private static CreateBellScheduleCommand scheduleCommand(
            UUID campusId,
            Boolean monday,
            Boolean tuesday,
            Boolean wednesday,
            Boolean thursday,
            Boolean friday,
            Boolean saturday,
            Boolean sunday,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        return new CreateBellScheduleCommand(
                campusId,
                "REG-01",
                "Regular School Day",
                "Standard teaching schedule",
                "regular",
                effectiveFrom,
                effectiveTo,
                monday,
                tuesday,
                wednesday,
                thursday,
                friday,
                saturday,
                sunday
        );
    }
}
