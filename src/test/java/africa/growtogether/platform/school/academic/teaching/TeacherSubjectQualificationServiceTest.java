package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeacherSubjectQualificationServiceTest {

    @Test
    void createsValidTeacherSubjectQualification() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(Subject.class)
                )
        );

        when(
                repository.findByTenantIdAndTeacherProfileIdAndSubjectId(
                        tenantId,
                        teacherProfileId,
                        subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.save(
                        any(TeacherSubjectQualification.class)
                )
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        TeacherSubjectQualification result =
                service.create(
                        tenantId,
                        teacherProfileId,
                        subjectId,
                        "QUALIFIED",
                        false,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null,
                        null
                );

        assertNotNull(result);
        assertEquals(
                teacherProfileId,
                result.getTeacherProfileId()
        );
        assertEquals(
                subjectId,
                result.getSubjectId()
        );
        assertEquals(
                "QUALIFIED",
                result.getCompetencyLevel()
        );
        assertEquals(
                "PENDING",
                result.getVerificationStatus()
        );

        verify(
                repository
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void rejectsTeacherProfileNotFoundForTenant() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                teacherProfileId,
                                UUID.randomUUID(),
                                "QUALIFIED",
                                false,
                                null,
                                null,
                                LocalDate.of(2026, 1, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Teacher profile not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void rejectsSubjectNotFoundForTenant() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                teacherProfileId,
                                subjectId,
                                "QUALIFIED",
                                false,
                                null,
                                null,
                                LocalDate.of(2026, 1, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Subject not found for tenant",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void rejectsDuplicateTeacherSubjectQualification() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(Subject.class)
                )
        );

        when(
                repository.findByTenantIdAndTeacherProfileIdAndSubjectId(
                        tenantId,
                        teacherProfileId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherSubjectQualification.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                teacherProfileId,
                                subjectId,
                                "QUALIFIED",
                                false,
                                null,
                                null,
                                LocalDate.of(2026, 1, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Teacher already has a qualification for this subject",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void rejectsSecondActivePrimarySubject() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(Subject.class)
                )
        );

        when(
                repository.findByTenantIdAndTeacherProfileIdAndSubjectId(
                        tenantId,
                        teacherProfileId,
                        subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                repository.findByTenantIdAndTeacherProfileIdAndPrimarySubjectTrueAndStatus(
                        tenantId,
                        teacherProfileId,
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherSubjectQualification.class)
                )
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                teacherProfileId,
                                subjectId,
                                "QUALIFIED",
                                true,
                                null,
                                null,
                                LocalDate.of(2026, 1, 1),
                                null,
                                null
                        )
                );

        assertEquals(
                "Teacher already has an active primary subject",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void rejectsEffectiveToBeforeEffectiveFrom() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID teacherProfileId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        when(
                teacherProfiles.findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                Optional.of(
                        mock(TeacherProfile.class)
                )
        );

        when(
                subjects.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(
                        mock(Subject.class)
                )
        );

        when(
                repository.findByTenantIdAndTeacherProfileIdAndSubjectId(
                        tenantId,
                        teacherProfileId,
                        subjectId
                )
        ).thenReturn(
                Optional.empty()
        );

        LocalDate effectiveFrom =
                LocalDate.of(2026, 2, 1);

        LocalDate effectiveTo =
                LocalDate.of(2026, 1, 31);

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                teacherProfileId,
                                subjectId,
                                "QUALIFIED",
                                false,
                                null,
                                null,
                                effectiveFrom,
                                effectiveTo,
                                null
                        )
                );

        assertEquals(
                "Qualification effective-to date cannot be before effective-from date",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any(TeacherSubjectQualification.class)
        );
    }

    @Test
    void verifyRecordsVerificationStatusUserAndTimestamp() {

        TeacherSubjectQualificationRepository repository =
                mock(TeacherSubjectQualificationRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        SubjectRepository subjects =
                mock(SubjectRepository.class);

        ClassGradeRepository classGrades =
                mock(ClassGradeRepository.class);

        TeacherSubjectQualificationService service =
                new TeacherSubjectQualificationService(
                        repository,
                        teacherProfiles,
                        subjects,
                        classGrades
                );

        UUID tenantId = UUID.randomUUID();
        UUID qualificationId = UUID.randomUUID();
        UUID verifiedBy = UUID.randomUUID();

        TeacherSubjectQualification qualification =
                new TeacherSubjectQualification(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "QUALIFIED",
                        false,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null,
                        null
                );

        qualification.setTenantId(
                tenantId
        );

        when(
                repository.findByTenantIdAndId(
                        tenantId,
                        qualificationId
                )
        ).thenReturn(
                Optional.of(qualification)
        );

        when(
                repository.save(
                        qualification
                )
        ).thenReturn(
                qualification
        );

        TeacherSubjectQualification result =
                service.verify(
                        tenantId,
                        qualificationId,
                        verifiedBy
                );

        assertEquals(
                "VERIFIED",
                result.getVerificationStatus()
        );

        assertEquals(
                verifiedBy,
                result.getVerifiedBy()
        );

        assertNotNull(
                result.getVerifiedAt()
        );

        verify(
                repository
        ).findByTenantIdAndId(
                tenantId,
                qualificationId
        );

        verify(
                repository
        ).save(
                qualification
        );
    }
}
