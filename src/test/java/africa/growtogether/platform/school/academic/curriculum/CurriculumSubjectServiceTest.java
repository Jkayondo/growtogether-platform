package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.school.academic.subject.Subject;
import africa.growtogether.platform.school.academic.subject.SubjectRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CurriculumSubjectServiceTest {


    @Mock
    private CurriculumSubjectRepository repository;

    @Mock
    private ClassGradeRepository classGradeRepository;

    @Mock
    private SubjectRepository subjectRepository;


    private CurriculumSubjectService service;


    @BeforeEach
    void setUp() {
        service = new CurriculumSubjectService(
                repository,
                classGradeRepository,
                subjectRepository
        );
    }


    @Test
    void createRejectsCurriculumVersionFromAnotherTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(version.getTenantId())
                .thenReturn(otherTenantId);


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                version,
                                classGradeId,
                                subjectId
                        )
                );


        assertEquals(
                "Curriculum version does not belong to tenant",
                error.getMessage()
        );

        verifyNoInteractions(
                classGradeRepository,
                subjectRepository,
                repository
        );
    }


    @Test
    void createRejectsClassGradeOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(version.getTenantId())
                .thenReturn(tenantId);

        when(
                classGradeRepository.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(Optional.empty());


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                version,
                                classGradeId,
                                subjectId
                        )
                );


        assertEquals(
                "Class grade not found",
                error.getMessage()
        );

        verify(
                classGradeRepository
        ).findByTenantIdAndId(
                tenantId,
                classGradeId
        );

        verifyNoInteractions(
                subjectRepository,
                repository
        );
    }


    @Test
    void createRejectsSubjectOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        ClassGrade classGrade =
                mock(ClassGrade.class);

        when(version.getTenantId())
                .thenReturn(tenantId);

        when(
                classGradeRepository.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                subjectRepository.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(Optional.empty());


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                version,
                                classGradeId,
                                subjectId
                        )
                );


        assertEquals(
                "Subject not found",
                error.getMessage()
        );

        verify(
                subjectRepository
        ).findByTenantIdAndId(
                tenantId,
                subjectId
        );

        verifyNoInteractions(repository);
    }


    @Test
    void createAcceptsOnlySameTenantParents() {

        UUID tenantId = UUID.randomUUID();
        UUID classGradeId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        ClassGrade classGrade =
                mock(ClassGrade.class);

        Subject subject =
                mock(Subject.class);

        when(version.getTenantId())
                .thenReturn(tenantId);

        when(
                classGradeRepository.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                subjectRepository.findByTenantIdAndId(
                        tenantId,
                        subjectId
                )
        ).thenReturn(
                Optional.of(subject)
        );

        when(repository.save(any(CurriculumSubject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        CurriculumSubject result =
                service.create(
                        tenantId,
                        version,
                        classGradeId,
                        subjectId
                );


        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        assertEquals(version, result.getCurriculumVersion());
        assertEquals(classGradeId, result.getClassGradeId());
        assertEquals(subjectId, result.getSubjectId());
        assertEquals("CORE", result.getSubjectRequirement());

        verify(repository)
                .save(any(CurriculumSubject.class));
    }


    @Test
    void changeRequirementRejectsUnsupportedValue() {

        CurriculumSubject subject =
                mock(CurriculumSubject.class);


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.changeRequirement(
                                subject,
                                "UNRECOGNISED"
                        )
                );


        assertEquals(
                "Unsupported subject requirement",
                error.getMessage()
        );

        verify(
                subject,
                never()
        ).changeRequirement(anyString());

        verifyNoInteractions(repository);
    }


    @Test
    void changeRequirementNormalizesAndSavesSupportedValue() {

        CurriculumSubject subject =
                mock(CurriculumSubject.class);

        when(
                repository.save(subject)
        ).thenReturn(subject);


        CurriculumSubject result =
                service.changeRequirement(
                        subject,
                        " elective "
                );


        assertSame(
                subject,
                result
        );

        verify(subject)
                .changeRequirement(
                        "ELECTIVE"
                );

        verify(repository)
                .save(subject);
    }

}
