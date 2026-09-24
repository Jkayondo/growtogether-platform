package africa.growtogether.platform.school.attendance;

import africa.growtogether.platform.school.profile.SchoolProfileService;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttendanceDailySummaryServiceTest {

    @Test
    void mapsAuthoritativeDailyRegisterAggregateWithoutInventingRate() {

        StudentAttendanceRepository repository =
                mock(StudentAttendanceRepository.class);

        SchoolProfileService schools =
                mock(SchoolProfileService.class);

        AttendanceDailyAggregate aggregate =
                mock(AttendanceDailyAggregate.class);

        AttendanceDailySummaryService service =
                new AttendanceDailySummaryService(
                        repository,
                        schools
                );

        UUID tenantId =
                UUID.randomUUID();

        LocalDate attendanceDate =
                LocalDate.of(
                        2026,
                        9,
                        23
                );

        when(
                repository
                        .aggregateDailyRegisterByTenantAndDate(
                                tenantId,
                                attendanceDate
                        )
        ).thenReturn(
                aggregate
        );

        when(aggregate.getSessionCount())
                .thenReturn(12L);

        when(aggregate.getExpectedStudentCount())
                .thenReturn(320L);

        when(aggregate.getRecordedAttendanceCount())
                .thenReturn(310L);

        when(aggregate.getPresentCount())
                .thenReturn(270L);

        when(aggregate.getAbsentCount())
                .thenReturn(20L);

        when(aggregate.getLateCount())
                .thenReturn(10L);

        when(aggregate.getExcusedAbsenceCount())
                .thenReturn(4L);

        when(aggregate.getUnexcusedAbsenceCount())
                .thenReturn(2L);

        when(aggregate.getMedicalAbsenceCount())
                .thenReturn(1L);

        when(aggregate.getSchoolActivityCount())
                .thenReturn(1L);

        when(aggregate.getRemoteLearningCount())
                .thenReturn(1L);

        when(aggregate.getEarlyDepartureCount())
                .thenReturn(1L);

        when(aggregate.getSuspendedCount())
                .thenReturn(0L);

        when(aggregate.getNotRequiredCount())
                .thenReturn(0L);

        when(aggregate.getUnknownCount())
                .thenReturn(0L);

        AttendanceDailySummary result =
                service.loadForDate(
                        tenantId,
                        attendanceDate
                );

        assertEquals(
                tenantId,
                result.tenantId()
        );

        assertEquals(
                attendanceDate,
                result.attendanceDate()
        );

        assertEquals(
                "DAILY_REGISTER",
                result.sessionType()
        );

        assertEquals(
                12,
                result.sessionCount()
        );

        assertEquals(
                320,
                result.expectedStudentCount()
        );

        assertEquals(
                310,
                result.recordedAttendanceCount()
        );

        assertEquals(
                10,
                result.unrecordedCount()
        );

        assertEquals(
                270,
                result.presentCount()
        );

        assertEquals(
                20,
                result.absentCount()
        );

        assertEquals(
                10,
                result.lateCount()
        );

        assertTrue(
                result.registerStarted()
        );

        assertFalse(
                result.fullyRecorded()
        );

        verify(repository)
                .aggregateDailyRegisterByTenantAndDate(
                        tenantId,
                        attendanceDate
                );
    }

    @Test
    void emptyAggregateProducesZeroSafeSummary() {

        StudentAttendanceRepository repository =
                mock(StudentAttendanceRepository.class);

        SchoolProfileService schools =
                mock(SchoolProfileService.class);

        AttendanceDailySummaryService service =
                new AttendanceDailySummaryService(
                        repository,
                        schools
                );

        UUID tenantId =
                UUID.randomUUID();

        LocalDate attendanceDate =
                LocalDate.of(
                        2026,
                        9,
                        23
                );

        when(
                repository
                        .aggregateDailyRegisterByTenantAndDate(
                                tenantId,
                                attendanceDate
                        )
        ).thenReturn(
                null
        );

        AttendanceDailySummary result =
                service.loadForDate(
                        tenantId,
                        attendanceDate
                );

        assertEquals(
                0,
                result.sessionCount()
        );

        assertEquals(
                0,
                result.expectedStudentCount()
        );

        assertEquals(
                0,
                result.recordedAttendanceCount()
        );

        assertEquals(
                0,
                result.unrecordedCount()
        );

        assertFalse(
                result.registerStarted()
        );

        assertFalse(
                result.fullyRecorded()
        );
    }

    @Test
    void unrecordedCountNeverBecomesNegative() {

        StudentAttendanceRepository repository =
                mock(StudentAttendanceRepository.class);

        SchoolProfileService schools =
                mock(SchoolProfileService.class);

        AttendanceDailyAggregate aggregate =
                mock(AttendanceDailyAggregate.class);

        AttendanceDailySummaryService service =
                new AttendanceDailySummaryService(
                        repository,
                        schools
                );

        UUID tenantId =
                UUID.randomUUID();

        LocalDate attendanceDate =
                LocalDate.of(
                        2026,
                        9,
                        23
                );

        when(
                repository
                        .aggregateDailyRegisterByTenantAndDate(
                                tenantId,
                                attendanceDate
                        )
        ).thenReturn(
                aggregate
        );

        when(aggregate.getSessionCount())
                .thenReturn(1L);

        when(aggregate.getExpectedStudentCount())
                .thenReturn(20L);

        when(aggregate.getRecordedAttendanceCount())
                .thenReturn(21L);

        AttendanceDailySummary result =
                service.loadForDate(
                        tenantId,
                        attendanceDate
                );

        assertEquals(
                0,
                result.unrecordedCount()
        );

        assertTrue(
                result.fullyRecorded()
        );
    }
}
