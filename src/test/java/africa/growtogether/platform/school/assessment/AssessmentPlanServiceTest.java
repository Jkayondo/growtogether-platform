package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.school.academic.curriculum.Campus;
import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGrade;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.CurriculumVersion;
import africa.growtogether.platform.school.academic.curriculum.CurriculumVersionRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AssessmentPlanServiceTest {


    @Test
    void createsTenantScopedAssessmentPlanWithValidAcademicHierarchy() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository.existsByTenantIdAndPlanCode(
                        f.tenantId,
                        "AP-2026-T1"
                )
        ).thenReturn(false);

        when(
                f.repository.save(
                        any(AssessmentPlan.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );


        AssessmentPlan result =
                f.service.create(
                        f.tenantId,
                        f.command()
                );


        assertEquals(
                "AP-2026-T1",
                result.getPlanCode()
        );

        assertEquals(
                "Term One Assessment Plan",
                result.getPlanName()
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
                f.academicProgrammeId,
                result.getAcademicProgrammeId()
        );

        assertEquals(
                f.studyTrackId,
                result.getStudyTrackId()
        );

        assertEquals(
                f.curriculumVersionId,
                result.getCurriculumVersionId()
        );

        assertEquals(
                f.classGradeId,
                result.getClassGradeId()
        );

        assertEquals(
                f.streamId,
                result.getStreamId()
        );

        assertEquals(
                f.gradingSchemeId,
                result.getGradingSchemeId()
        );

        assertEquals(
                "DRAFT",
                result.getPlanStatus()
        );

        verify(
                f.references
        ).academicProgrammeExists(
                f.tenantId,
                f.academicProgrammeId
        );

        verify(
                f.references
        ).studyTrackExists(
                f.tenantId,
                f.studyTrackId
        );

        verify(
                f.references
        ).studyTrackBelongsToProgramme(
                f.tenantId,
                f.studyTrackId,
                f.academicProgrammeId
        );

        verify(
                f.references
        ).gradingSchemeExists(
                f.tenantId,
                f.gradingSchemeId
        );

        verify(
                f.repository
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsAcademicTermThatDoesNotBelongToAcademicYear() {

        Fixture f = new Fixture();

        f.stubAcademicYear();

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
                Optional.of(term)
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command()
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
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsStudyTrackThatDoesNotBelongToAcademicProgramme() {

        Fixture f = new Fixture();

        f.stubThroughClassGrade();

        when(
                f.references.academicProgrammeExists(
                        f.tenantId,
                        f.academicProgrammeId
                )
        ).thenReturn(true);

        when(
                f.references.studyTrackExists(
                        f.tenantId,
                        f.studyTrackId
                )
        ).thenReturn(true);

        when(
                f.references.studyTrackBelongsToProgramme(
                        f.tenantId,
                        f.studyTrackId,
                        f.academicProgrammeId
                )
        ).thenReturn(false);


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command()
                        )
                );


        assertEquals(
                "Study track does not belong to academic programme",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsStreamThatDoesNotBelongToCampus() {

        Fixture f = new Fixture();

        f.stubThroughCurriculumVersion();

        Stream stream =
                mock(Stream.class);

        when(
                stream.getCampusId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.streams.findByTenantIdAndId(
                        f.tenantId,
                        f.streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command()
                        )
                );


        assertEquals(
                "Stream does not belong to campus",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsStreamThatDoesNotBelongToClassGrade() {

        Fixture f = new Fixture();

        f.stubThroughCurriculumVersion();

        Stream stream =
                mock(Stream.class);

        when(
                stream.getCampusId()
        ).thenReturn(
                f.campusId
        );

        when(
                stream.getClassGradeId()
        ).thenReturn(
                UUID.randomUUID()
        );

        when(
                f.streams.findByTenantIdAndId(
                        f.tenantId,
                        f.streamId
                )
        ).thenReturn(
                Optional.of(stream)
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command()
                        )
                );


        assertEquals(
                "Stream does not belong to class grade",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsTenantScopedDuplicatePlanCode() {

        Fixture f = new Fixture();

        f.stubValidDependencies();

        when(
                f.repository.existsByTenantIdAndPlanCode(
                        f.tenantId,
                        "AP-2026-T1"
                )
        ).thenReturn(true);


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                f.command()
                        )
                );


        assertEquals(
                "Assessment plan code already exists for tenant",
                error.getMessage()
        );

        verify(
                f.repository,
                never()
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void rejectsDuplicateNullableAcademicScopeBeforePersistence() {

        Fixture f = new Fixture();

        f.stubThroughClassGrade();

        CreateAssessmentPlanCommand command =
                new CreateAssessmentPlanCommand(
                        "AP-2026-NULL-SCOPE-2",
                        "Duplicate Nullable Scope",
                        "Application-level V197 scope proof",
                        f.academicYearId,
                        null,
                        f.campusId,
                        null,
                        null,
                        null,
                        f.classGradeId,
                        null,
                        null,
                        LocalDate.of(2026, 2, 2),
                        LocalDate.of(2026, 4, 30),
                        null
                );

        when(
                f.repository.existsByTenantIdAndPlanCode(
                        f.tenantId,
                        "AP-2026-NULL-SCOPE-2"
                )
        ).thenReturn(false);

        when(
                f.repository.countByTenantAndAcademicScope(
                        f.tenantId,
                        f.academicYearId,
                        null,
                        f.campusId,
                        null,
                        null,
                        f.classGradeId,
                        null
                )
        ).thenReturn(1L);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> f.service.create(
                                f.tenantId,
                                command
                        )
                );

        assertEquals(
                "Assessment plan already exists for academic scope",
                error.getMessage()
        );

        verify(
                f.repository
        ).countByTenantAndAcademicScope(
                f.tenantId,
                f.academicYearId,
                null,
                f.campusId,
                null,
                null,
                f.classGradeId,
                null
        );

        verify(
                f.repository,
                never()
        ).save(
                any(AssessmentPlan.class)
        );
    }


    @Test
    void getIsTenantScoped() {

        Fixture f = new Fixture();

        UUID assessmentPlanId =
                UUID.randomUUID();

        AssessmentPlan expected =
                mock(AssessmentPlan.class);

        when(
                f.repository.findByTenantIdAndId(
                        f.tenantId,
                        assessmentPlanId
                )
        ).thenReturn(
                Optional.of(expected)
        );


        AssessmentPlan result =
                f.service.get(
                        f.tenantId,
                        assessmentPlanId
                );


        assertSame(
                expected,
                result
        );

        verify(
                f.repository
        ).findByTenantIdAndId(
                f.tenantId,
                assessmentPlanId
        );
    }


    private static class Fixture {

        final AssessmentPlanRepository repository =
                mock(AssessmentPlanRepository.class);

        final AcademicYearRepository academicYears =
                mock(AcademicYearRepository.class);

        final AcademicTermRepository academicTerms =
                mock(AcademicTermRepository.class);

        final CampusRepository campuses =
                mock(CampusRepository.class);

        final ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        final StreamRepository streams =
                mock(StreamRepository.class);

        final CurriculumVersionRepository curriculumVersions =
                mock(CurriculumVersionRepository.class);

        final AssessmentPlanReferenceGateway references =
                mock(AssessmentPlanReferenceGateway.class);

        final AssessmentPlanService service =
                new AssessmentPlanService(
                        repository,
                        academicYears,
                        academicTerms,
                        campuses,
                        classGrades,
                        streams,
                        curriculumVersions,
                        references
                );


        final UUID tenantId =
                UUID.randomUUID();

        final UUID academicYearId =
                UUID.randomUUID();

        final UUID academicTermId =
                UUID.randomUUID();

        final UUID campusId =
                UUID.randomUUID();

        final UUID academicProgrammeId =
                UUID.randomUUID();

        final UUID studyTrackId =
                UUID.randomUUID();

        final UUID curriculumVersionId =
                UUID.randomUUID();

        final UUID classGradeId =
                UUID.randomUUID();

        final UUID streamId =
                UUID.randomUUID();

        final UUID gradingSchemeId =
                UUID.randomUUID();


        CreateAssessmentPlanCommand command() {

            return new CreateAssessmentPlanCommand(
                    "AP-2026-T1",
                    "Term One Assessment Plan",
                    "Assessment plan service proof",
                    academicYearId,
                    academicTermId,
                    campusId,
                    academicProgrammeId,
                    studyTrackId,
                    curriculumVersionId,
                    classGradeId,
                    streamId,
                    gradingSchemeId,
                    LocalDate.of(2026, 2, 2),
                    LocalDate.of(2026, 4, 30),
                    null
            );
        }


        void stubAcademicYear() {

            AcademicYear academicYear =
                    mock(AcademicYear.class);

            when(
                    academicYear.getId()
            ).thenReturn(
                    academicYearId
            );

            when(
                    academicYear.getStartDate()
            ).thenReturn(
                    LocalDate.of(2026, 1, 1)
            );

            when(
                    academicYear.getEndDate()
            ).thenReturn(
                    LocalDate.of(2026, 12, 31)
            );

            when(
                    academicYears.findByTenantIdAndId(
                            tenantId,
                            academicYearId
                    )
            ).thenReturn(
                    Optional.of(academicYear)
            );
        }


        void stubAcademicPeriod() {

            stubAcademicYear();

            AcademicYear academicYear =
                    academicYears
                            .findByTenantIdAndId(
                                    tenantId,
                                    academicYearId
                            )
                            .orElseThrow();

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


        void stubThroughClassGrade() {

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

            when(
                    classGrades.findByTenantIdAndId(
                            tenantId,
                            classGradeId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(ClassGrade.class)
                    )
            );
        }


        void stubThroughCurriculumVersion() {

            stubThroughClassGrade();

            when(
                    references.academicProgrammeExists(
                            tenantId,
                            academicProgrammeId
                    )
            ).thenReturn(true);

            when(
                    references.studyTrackExists(
                            tenantId,
                            studyTrackId
                    )
            ).thenReturn(true);

            when(
                    references.studyTrackBelongsToProgramme(
                            tenantId,
                            studyTrackId,
                            academicProgrammeId
                    )
            ).thenReturn(true);

            when(
                    curriculumVersions.findByTenantIdAndId(
                            tenantId,
                            curriculumVersionId
                    )
            ).thenReturn(
                    Optional.of(
                            mock(CurriculumVersion.class)
                    )
            );
        }


        void stubValidDependencies() {

            stubThroughCurriculumVersion();

            Stream stream =
                    mock(Stream.class);

            when(
                    stream.getCampusId()
            ).thenReturn(
                    campusId
            );

            when(
                    stream.getClassGradeId()
            ).thenReturn(
                    classGradeId
            );

            when(
                    streams.findByTenantIdAndId(
                            tenantId,
                            streamId
                    )
            ).thenReturn(
                    Optional.of(stream)
            );

            when(
                    references.gradingSchemeExists(
                            tenantId,
                            gradingSchemeId
                    )
            ).thenReturn(true);
        }
    }
}
