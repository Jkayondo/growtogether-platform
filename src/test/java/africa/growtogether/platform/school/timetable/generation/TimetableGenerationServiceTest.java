package africa.growtogether.platform.school.timetable.generation;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.AiFoundationService;
import africa.growtogether.platform.eaif.AiRequest;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;

import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;

import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import africa.growtogether.platform.school.timetable.bell.BellSchedule;
import africa.growtogether.platform.school.timetable.bell.BellScheduleRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimetableGenerationServiceTest {

    @Test
    void createsRuleEngineRequestWithoutCallingEaif() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        TimetableGenerationRequest result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                "RULE_ENGINE",
                                null
                        )
                );

        assertEquals(
                f.tenantId,
                result.getTenantId()
        );

        assertEquals(
                "GEN-2026-T1",
                result.getGenerationCode()
        );

        assertEquals(
                "RULE_ENGINE",
                result.getGenerationMode()
        );

        assertEquals(
                "READY",
                result.getGenerationStatus()
        );

        assertNull(
                result.getEaifRequestId()
        );

        assertNotNull(
                result.getRequestedAt()
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );

        verify(
                f.repository,
                times(2)
        ).save(
                any(TimetableGenerationRequest.class)
        );
    }

    @Test
    void createsAiAssistedRequestThroughGovernedEaifAndLinksRequestId() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        UUID aiRequestId =
                UUID.randomUUID();

        AiRequest aiRequest =
                mock(AiRequest.class);

        when(
                aiRequest.getId()
        ).thenReturn(
                aiRequestId
        );

        when(
                f.aiFoundation.submit(
                        eq(f.tenantId),
                        eq("GT_SCHOOL_TIMETABLE"),
                        eq("TIMETABLE_GENERATION"),
                        eq("GT-TIMETABLE-AI"),
                        anyString(),
                        eq(AiEnums.RiskLevel.MEDIUM),
                        eq(
                                f.generationRequestId.toString()
                        )
                )
        ).thenReturn(
                aiRequest
        );

        TimetableGenerationRequest result =
                f.service.create(
                        f.tenantId,
                        f.command(
                                "AI_ASSISTED",
                                "GT-TIMETABLE-AI"
                        )
                );

        assertEquals(
                "AI_ASSISTED",
                result.getGenerationMode()
        );

        assertEquals(
                "GT-TIMETABLE-AI",
                result.getModelCode()
        );

        assertEquals(
                "READY",
                result.getGenerationStatus()
        );

        assertEquals(
                aiRequestId,
                result.getEaifRequestId()
        );

        ArgumentCaptor<String> inputHash =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(
                f.aiFoundation
        ).submit(
                eq(f.tenantId),
                eq("GT_SCHOOL_TIMETABLE"),
                eq("TIMETABLE_GENERATION"),
                eq("GT-TIMETABLE-AI"),
                inputHash.capture(),
                eq(AiEnums.RiskLevel.MEDIUM),
                eq(
                        f.generationRequestId.toString()
                )
        );

        assertEquals(
                64,
                inputHash.getValue().length()
        );

        assertTrue(
                inputHash
                        .getValue()
                        .matches("[0-9a-f]{64}")
        );

        verify(
                f.repository,
                times(2)
        ).save(
                any(TimetableGenerationRequest.class)
        );
    }

    @Test
    void rejectsDuplicateGenerationCodeBeforeAcademicOrAiProcessing() {

        Fixture f = new Fixture();

        when(
                f.repository
                        .existsByTenantIdAndGenerationCode(
                                f.tenantId,
                                "GEN-2026-T1"
                        )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "AI_ASSISTED",
                                        "GT-TIMETABLE-AI"
                                )
                        )
                );

        assertEquals(
                "Timetable generation code already exists for tenant",
                error.getMessage()
        );

        verify(
                f.academicYears,
                never()
        ).findByTenantIdAndId(
                any(UUID.class),
                any(UUID.class)
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableGenerationRequest.class)
        );
    }

    @Test
    void rejectsAcademicTermFromDifferentYearBeforeAiSubmission() {

        Fixture f = new Fixture();

        when(
                f.repository
                        .existsByTenantIdAndGenerationCode(
                                f.tenantId,
                                "GEN-2026-T1"
                        )
        ).thenReturn(
                false
        );

        AcademicYear selectedYear =
                mock(AcademicYear.class);

        when(
                selectedYear.getId()
        ).thenReturn(
                f.academicYearId
        );

        when(
                f.academicYears.findByTenantIdAndId(
                        f.tenantId,
                        f.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        selectedYear
                )
        );

        AcademicYear wrongYear =
                mock(AcademicYear.class);

        when(
                wrongYear.getId()
        ).thenReturn(
                UUID.randomUUID()
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
                Optional.of(
                        term
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "AI_ASSISTED",
                                        "GT-TIMETABLE-AI"
                                )
                        )
                );

        assertEquals(
                "Academic term does not belong to academic year",
                error.getMessage()
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableGenerationRequest.class)
        );
    }

    @Test
    void rejectsBellScheduleFromDifferentCampusBeforeAiSubmission() {

        Fixture f = new Fixture();

        when(
                f.repository
                        .existsByTenantIdAndGenerationCode(
                                f.tenantId,
                                "GEN-2026-T1"
                        )
        ).thenReturn(
                false
        );

        AcademicYear year =
                mock(AcademicYear.class);

        when(
                year.getId()
        ).thenReturn(
                f.academicYearId
        );

        when(
                f.academicYears.findByTenantIdAndId(
                        f.tenantId,
                        f.academicYearId
                )
        ).thenReturn(
                Optional.of(
                        year
                )
        );

        AcademicTerm term =
                mock(AcademicTerm.class);

        when(
                term.getAcademicYear()
        ).thenReturn(
                year
        );

        when(
                term.getStartDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        2,
                        1
                )
        );

        when(
                term.getEndDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        4,
                        30
                )
        );

        when(
                f.academicTerms.findByTenantIdAndId(
                        f.tenantId,
                        f.academicTermId
                )
        ).thenReturn(
                Optional.of(
                        term
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

        BellSchedule wrongCampusSchedule =
                mock(BellSchedule.class);

        when(
                wrongCampusSchedule.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.bellSchedules.findByTenantIdAndId(
                        f.tenantId,
                        f.bellScheduleId
                )
        ).thenReturn(
                Optional.of(
                        wrongCampusSchedule
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command(
                                        "AI_ASSISTED",
                                        "GT-TIMETABLE-AI"
                                )
                        )
                );

        assertEquals(
                "Bell schedule does not belong to campus",
                error.getMessage()
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableGenerationRequest.class)
        );
    }

    @Test
    void rejectsYearLevelGenerationBeforeAcademicYearStart() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        AcademicYear year =
                f.academicYears
                        .findByTenantIdAndId(
                                f.tenantId,
                                f.academicYearId
                        )
                        .orElseThrow();

        when(
                year.getStartDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        1,
                        1
                )
        );

        when(
                year.getEndDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        12,
                        31
                )
        );

        CreateTimetableGenerationRequestCommand command =
                new CreateTimetableGenerationRequestCommand(
                        "gen-year-before",
                        f.academicYearId,
                        null,
                        f.campusId,
                        f.bellScheduleId,
                        "MASTER",
                        LocalDate.of(
                                2025,
                                12,
                                31
                        ),
                        LocalDate.of(
                                2026,
                                4,
                                30
                        ),
                        "AI_ASSISTED",
                        "GT-TIMETABLE-AI",
                        "Year-level generation boundary test",
                        f.requestedBy
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                command
                        )
                );

        assertEquals(
                "Generation effectiveFrom is before academic year",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableGenerationRequest.class)
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );
    }

    @Test
    void rejectsYearLevelGenerationAfterAcademicYearEnd() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        AcademicYear year =
                f.academicYears
                        .findByTenantIdAndId(
                                f.tenantId,
                                f.academicYearId
                        )
                        .orElseThrow();

        when(
                year.getStartDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        1,
                        1
                )
        );

        when(
                year.getEndDate()
        ).thenReturn(
                LocalDate.of(
                        2026,
                        12,
                        31
                )
        );

        CreateTimetableGenerationRequestCommand command =
                new CreateTimetableGenerationRequestCommand(
                        "gen-year-after",
                        f.academicYearId,
                        null,
                        f.campusId,
                        f.bellScheduleId,
                        "MASTER",
                        LocalDate.of(
                                2027,
                                1,
                                1
                        ),
                        LocalDate.of(
                                2027,
                                1,
                                31
                        ),
                        "AI_ASSISTED",
                        "GT-TIMETABLE-AI",
                        "Year-level generation boundary test",
                        f.requestedBy
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                command
                        )
                );

        assertEquals(
                "Generation effectiveFrom is after academic year",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(TimetableGenerationRequest.class)
        );

        verify(
                f.aiFoundation,
                never()
        ).submit(
                any(UUID.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(AiEnums.RiskLevel.class),
                anyString()
        );
    }


    private static class Fixture {

        final TimetableGenerationRequestRepository repository =
                mock(
                        TimetableGenerationRequestRepository.class
                );

        final AcademicYearRepository academicYears =
                mock(
                        AcademicYearRepository.class
                );

        final AcademicTermRepository academicTerms =
                mock(
                        AcademicTermRepository.class
                );

        final CampusRepository campuses =
                mock(
                        CampusRepository.class
                );

        final BellScheduleRepository bellSchedules =
                mock(
                        BellScheduleRepository.class
                );

        final AiFoundationService aiFoundation =
                mock(
                        AiFoundationService.class
                );

        final TimetableGenerationService service =
                new TimetableGenerationService(
                        repository,
                        academicYears,
                        academicTerms,
                        campuses,
                        bellSchedules,
                        aiFoundation
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

        final UUID requestedBy =
                UUID.randomUUID();

        final UUID generationRequestId =
                UUID.randomUUID();

        void stubValidDependencies() {

            when(
                    repository
                            .existsByTenantIdAndGenerationCode(
                                    tenantId,
                                    "GEN-2026-T1"
                            )
            ).thenReturn(
                    false
            );

            AcademicYear year =
                    mock(
                            AcademicYear.class
                    );

            when(
                    year.getId()
            ).thenReturn(
                    academicYearId
            );

            when(
                    academicYears
                            .findByTenantIdAndId(
                                    tenantId,
                                    academicYearId
                            )
            ).thenReturn(
                    Optional.of(
                            year
                    )
            );

            AcademicTerm term =
                    mock(
                            AcademicTerm.class
                    );

            when(
                    term.getAcademicYear()
            ).thenReturn(
                    year
            );

            when(
                    term.getStartDate()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            2,
                            1
                    )
            );

            when(
                    term.getEndDate()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            4,
                            30
                    )
            );

            when(
                    academicTerms
                            .findByTenantIdAndId(
                                    tenantId,
                                    academicTermId
                            )
            ).thenReturn(
                    Optional.of(
                            term
                    )
            );

            when(
                    campuses
                            .findByTenantIdAndId(
                                    tenantId,
                                    campusId
                            )
            ).thenReturn(
                    Optional.of(
                            mock(
                                    Campus.class
                            )
                    )
            );

            BellSchedule bellSchedule =
                    mock(
                            BellSchedule.class
                    );

            when(
                    bellSchedule.getCampusId()
            ).thenReturn(
                    campusId
            );

            when(
                    bellSchedule.getEffectiveFrom()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            2,
                            1
                    )
            );

            when(
                    bellSchedule.getEffectiveTo()
            ).thenReturn(
                    LocalDate.of(
                            2026,
                            4,
                            30
                    )
            );

            when(
                    bellSchedules
                            .findByTenantIdAndId(
                                    tenantId,
                                    bellScheduleId
                            )
            ).thenReturn(
                    Optional.of(
                            bellSchedule
                    )
            );

            when(
                    repository.save(
                            any(
                                    TimetableGenerationRequest.class
                            )
                    )
            ).thenAnswer(
                    invocation -> {

                        TimetableGenerationRequest request =
                                invocation.getArgument(0);

                        if (request.getId() == null) {
                            assignEntityId(
                                    request,
                                    generationRequestId
                            );
                        }

                        return request;
                    }
            );
        }

        CreateTimetableGenerationRequestCommand command(
                String generationMode,
                String modelCode
        ) {

            return new CreateTimetableGenerationRequestCommand(
                    "gen-2026-t1",
                    academicYearId,
                    academicTermId,
                    campusId,
                    bellScheduleId,
                    "MASTER",
                    LocalDate.of(
                            2026,
                            2,
                            2
                    ),
                    LocalDate.of(
                            2026,
                            4,
                            30
                    ),
                    generationMode,
                    modelCode,
                    "Balance teacher workload; prefer core subjects in the morning",
                    requestedBy
            );
        }

        private static void assignEntityId(
                AuditedTenantEntity entity,
                UUID id
        ) {

            try {

                Field field =
                        AuditedTenantEntity.class
                                .getDeclaredField(
                                        "id"
                                );

                field.setAccessible(
                        true
                );

                field.set(
                        entity,
                        id
                );

            } catch (
                    ReflectiveOperationException ex
            ) {

                throw new IllegalStateException(
                        "Unable to emulate JPA UUID assignment",
                        ex
                );
            }
        }
    }
}
