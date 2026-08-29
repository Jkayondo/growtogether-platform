package africa.growtogether.platform.school.timetable.core;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;

import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;
import africa.growtogether.platform.school.timetable.reliability.TimetableChangeHistoryService;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimetableServiceTest {

    @Test
    void createsTenantScopedTimetableWithValidAcademicHierarchy() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository.existsByTenantIdAndTimetableCode(
                        f.tenantId,
                        "TT-2026-T1"
                )
        ).thenReturn(false);

        when(
                f.repository
                        .existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndVersionNumber(
                                f.tenantId,
                                f.academicYearId,
                                f.academicTermId,
                                f.campusId,
                                "MASTER",
                                1
                        )
        ).thenReturn(false);

        when(
                f.repository.save(
                        any(Timetable.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        Timetable result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                f.academicTermId,
                                LocalDate.of(2026, 2, 2),
                                LocalDate.of(2026, 4, 30)
                        )
                );

        assertEquals(
                "TT-2026-T1",
                result.getTimetableCode()
        );

        assertEquals(
                f.academicYearId,
                result.getAcademicYearId()
        );

        assertEquals(
                f.academicTermId,
                result.getAcademicTermId()
        );

        assertEquals(
                f.campusId,
                result.getCampusId()
        );

        assertEquals(
                f.bellScheduleId,
                result.getBellScheduleId()
        );

        assertEquals(
                "MASTER",
                result.getTimetableType()
        );

        assertEquals(
                1,
                result.getVersionNumber()
        );

        assertEquals(
                "MANUAL",
                result.getGeneratedBy()
        );

        assertEquals(
                "DRAFT",
                result.getTimetableStatus()
        );

        verify(
                f.repository
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void rejectsAcademicTermThatDoesNotBelongToAcademicYear() {

        Fixture f = new Fixture();

        AcademicYear wrongYear =
                mock(AcademicYear.class);

        when(
                wrongYear.getId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.academicYears.findByTenantIdAndId(
                        f.tenantId,
                        f.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        AcademicTerm term =
                mock(AcademicTerm.class);

        when(
                term.getAcademicYear()
        ).thenReturn(
                wrongYear
        );

        when(
                f.academicTerms.findByTenantIdAndId(
                        f.tenantId,
                        f.academicTermId
                )
        ).thenReturn(
                Optional.of(term)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        f.academicTermId,
                                        LocalDate.of(2026, 2, 2),
                                        LocalDate.of(2026, 4, 30)
                                )
                        )
                );

        assertEquals(
                "Academic term does not belong to academic year",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void rejectsBellScheduleThatDoesNotBelongToCampus() {

        Fixture f = new Fixture();

        f.stubAcademicPeriod();

        when(
                f.campuses.findByTenantIdAndId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        BellSchedule bellSchedule =
                mock(BellSchedule.class);

        when(
                bellSchedule.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.bellSchedules.findByTenantIdAndId(
                        f.tenantId,
                        f.bellScheduleId
                )
        ).thenReturn(
                Optional.of(bellSchedule)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        f.academicTermId,
                                        LocalDate.of(2026, 2, 2),
                                        LocalDate.of(2026, 4, 30)
                                )
                        )
                );

        assertEquals(
                "Bell schedule does not belong to campus",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void rejectsTimetableOutsideAcademicTermDates() {

        Fixture f = new Fixture();

        when(
                f.academicYears.findByTenantIdAndId(
                        f.tenantId,
                        f.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        AcademicYear year =
                mock(AcademicYear.class);

        when(
                year.getId()
        ).thenReturn(
                f.academicYearId
        );

        AcademicTerm term =
                mock(AcademicTerm.class);

        when(
                term.getAcademicYear()
        ).thenReturn(year);

        when(
                term.getStartDate()
        ).thenReturn(
                LocalDate.of(2026, 2, 1)
        );

        when(
                term.getEndDate()
        ).thenReturn(
                LocalDate.of(2026, 4, 30)
        );

        when(
                f.academicTerms.findByTenantIdAndId(
                        f.tenantId,
                        f.academicTermId
                )
        ).thenReturn(
                Optional.of(term)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        f.academicTermId,
                                        LocalDate.of(2026, 1, 20),
                                        LocalDate.of(2026, 4, 30)
                                )
                        )
                );

        assertEquals(
                "Timetable effectiveFrom is before academic term",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void rejectsTimetableOutsideBellScheduleDates() {

        Fixture f = new Fixture();

        f.stubAcademicPeriod();

        when(
                f.campuses.findByTenantIdAndId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        BellSchedule bellSchedule =
                mock(BellSchedule.class);

        when(
                bellSchedule.getCampusId()
        ).thenReturn(
                f.campusId
        );

        when(
                bellSchedule.getEffectiveFrom()
        ).thenReturn(
                LocalDate.of(2026, 2, 15)
        );

        when(
                bellSchedule.getEffectiveTo()
        ).thenReturn(
                LocalDate.of(2026, 4, 30)
        );

        when(
                f.bellSchedules.findByTenantIdAndId(
                        f.tenantId,
                        f.bellScheduleId
                )
        ).thenReturn(
                Optional.of(bellSchedule)
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        f.academicTermId,
                                        LocalDate.of(2026, 2, 2),
                                        LocalDate.of(2026, 4, 30)
                                )
                        )
                );

        assertEquals(
                "Timetable effectiveFrom is before bell schedule",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void rejectsDuplicateYearWideTimetableVersionWithNullTerm() {

        Fixture f = new Fixture();

        when(
                f.academicYears.findByTenantIdAndId(
                        f.tenantId,
                        f.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        mock(AcademicYear.class)
                )
        );

        when(
                f.campuses.findByTenantIdAndId(
                        f.tenantId,
                        f.campusId
                )
        ).thenReturn(
                Optional.of(
                        mock(Campus.class)
                )
        );

        BellSchedule bellSchedule =
                mock(BellSchedule.class);

        when(
                bellSchedule.getCampusId()
        ).thenReturn(
                f.campusId
        );

        when(
                bellSchedule.getEffectiveFrom()
        ).thenReturn(
                LocalDate.of(2026, 1, 1)
        );

        when(
                bellSchedule.getEffectiveTo()
        ).thenReturn(
                LocalDate.of(2026, 12, 31)
        );

        when(
                f.bellSchedules.findByTenantIdAndId(
                        f.tenantId,
                        f.bellScheduleId
                )
        ).thenReturn(
                Optional.of(bellSchedule)
        );

        when(
                f.repository.existsByTenantIdAndTimetableCode(
                        f.tenantId,
                        "TT-2026-T1"
                )
        ).thenReturn(false);

        when(
                f.repository
                        .existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndCampusIdAndTimetableTypeAndVersionNumber(
                                f.tenantId,
                                f.academicYearId,
                                null,
                                f.campusId,
                                "MASTER",
                                1
                        )
        ).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        null,
                                        LocalDate.of(2026, 2, 2),
                                        LocalDate.of(2026, 4, 30)
                                )
                        )
                );

        assertEquals(
                "Timetable version already exists for academic scope",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(Timetable.class)
        );
    }

    @Test
    void followsControlledApprovalPublicationActivationLifecycle() {

        UUID academicYearId =
                UUID.randomUUID();

        UUID academicTermId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID bellScheduleId =
                UUID.randomUUID();

        Timetable timetable =
                new Timetable(
                        "TT-001",
                        "Term One Master Timetable",
                        null,
                        academicYearId,
                        academicTermId,
                        campusId,
                        bellScheduleId,
                        "MASTER",
                        1,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30),
                        "MANUAL",
                        null,
                        null
                );

        assertEquals(
                "DRAFT",
                timetable.getTimetableStatus()
        );

        timetable.submitForReview();

        assertEquals(
                "UNDER_REVIEW",
                timetable.getTimetableStatus()
        );

        UUID approvedBy =
                UUID.randomUUID();

        timetable.approve(
                approvedBy
        );

        assertEquals(
                "APPROVED",
                timetable.getTimetableStatus()
        );

        assertEquals(
                approvedBy,
                timetable.getApprovedBy()
        );

        assertNotNull(
                timetable.getApprovedAt()
        );

        UUID publishedBy =
                UUID.randomUUID();

        timetable.publish(
                publishedBy
        );

        assertEquals(
                "PUBLISHED",
                timetable.getTimetableStatus()
        );

        assertEquals(
                publishedBy,
                timetable.getPublishedBy()
        );

        assertNotNull(
                timetable.getPublishedAt()
        );

        timetable.activate();

        assertEquals(
                "ACTIVE",
                timetable.getTimetableStatus()
        );
    }

    @Test
    void rejectsPublishingBeforeApproval() {

        Timetable timetable =
                new Timetable(
                        "TT-001",
                        "Term One Master Timetable",
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "MASTER",
                        1,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30),
                        "MANUAL",
                        null,
                        null
                );

        timetable.submitForReview();

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> timetable.publish(
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "Only APPROVED timetables can be published",
                error.getMessage()
        );

        assertEquals(
                "UNDER_REVIEW",
                timetable.getTimetableStatus()
        );
    }

    private static class Fixture {

        final TimetableRepository repository =
                mock(TimetableRepository.class);

        final AcademicYearRepository academicYears =
                mock(AcademicYearRepository.class);

        final AcademicTermRepository academicTerms =
                mock(AcademicTermRepository.class);

        final CampusRepository campuses =
                mock(CampusRepository.class);

        final BellScheduleRepository bellSchedules =
                mock(BellScheduleRepository.class);

        final TimetableChangeHistoryService history =
                mock(TimetableChangeHistoryService.class);

        final TimetableService service =
                new TimetableService(
                        repository,
                        academicYears,
                        academicTerms,
                        campuses,
                        bellSchedules,
                        history
                );

        final UUID tenantId =
                UUID.randomUUID();

        final UUID academicYearId =
                UUID.randomUUID();

        final UUID academicTermId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        final UUID bellScheduleId =
                UUID.randomUUID();

        void stubAcademicPeriod() {

            AcademicYear academicYear =
                    mock(AcademicYear.class);

            when(
                    academicYear.getId()
            ).thenReturn(
                    academicYearId
            );

            when(
                    academicYears.findByTenantIdAndId(
                            tenantId,
                            academicYearId
                    )
            ).thenReturn(
                    Optional.of(academicYear)
            );

            AcademicTerm term =
                    mock(AcademicTerm.class);

            when(
                    term.getAcademicYear()
            ).thenReturn(
                    academicYear
            );

            when(
                    term.getStartDate()
            ).thenReturn(
                    LocalDate.of(2026, 2, 1)
            );

            when(
                    term.getEndDate()
            ).thenReturn(
                    LocalDate.of(2026, 4, 30)
            );

            when(
                    academicTerms.findByTenantIdAndId(
                            tenantId,
                            academicTermId
                    )
            ).thenReturn(
                    Optional.of(term)
            );
        }

        void stubValidDependencies() {

            stubAcademicPeriod();

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

            BellSchedule bellSchedule =
                    mock(BellSchedule.class);

            when(
                    bellSchedule.getCampusId()
            ).thenReturn(
                    campusId
            );

            when(
                    bellSchedule.getEffectiveFrom()
            ).thenReturn(
                    LocalDate.of(2026, 2, 1)
            );

            when(
                    bellSchedule.getEffectiveTo()
            ).thenReturn(
                    LocalDate.of(2026, 4, 30)
            );

            when(
                    bellSchedules.findByTenantIdAndId(
                            tenantId,
                            bellScheduleId
                    )
            ).thenReturn(
                    Optional.of(bellSchedule)
            );
        }

        CreateTimetableCommand command(
                UUID termId,
                LocalDate effectiveFrom,
                LocalDate effectiveTo
        ) {

            return new CreateTimetableCommand(
                    "TT-2026-T1",
                    "2026 Term One Master Timetable",
                    "Main campus timetable",
                    academicYearId,
                    termId,
                    campusId,
                    bellScheduleId,
                    "MASTER",
                    1,
                    effectiveFrom,
                    effectiveTo,
                    null,
                    null,
                    null
            );
        }
    }
}
