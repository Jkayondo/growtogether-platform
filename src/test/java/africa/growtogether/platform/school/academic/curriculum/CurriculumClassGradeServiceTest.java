package africa.growtogether.platform.school.academic.curriculum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CurriculumClassGradeServiceTest {

    private CurriculumClassGradeRepository repository;
    private ClassGradeRepository classGradeRepository;
    private CurriculumClassGradeService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(CurriculumClassGradeRepository.class);

        classGradeRepository =
                mock(ClassGradeRepository.class);

        service =
                new CurriculumClassGradeService(
                        repository,
                        classGradeRepository
                );
    }


    @Test
    void createRejectsCurriculumVersionFromAnotherTenant() {

        UUID requestedTenant =
                UUID.randomUUID();

        UUID versionTenant =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(
                version.getTenantId()
        ).thenReturn(
                versionTenant
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                requestedTenant,
                                version,
                                classGradeId,
                                1
                        )
                );


        assertEquals(
                "Curriculum version does not belong to tenant",
                error.getMessage()
        );

        verify(
                classGradeRepository,
                never()
        ).findByTenantIdAndId(
                any(),
                any()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void createRejectsClassGradeFromAnotherTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        when(
                version.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                classGradeRepository.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.empty()
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                version,
                                classGradeId,
                                1
                        )
                );


        assertEquals(
                "Class grade not found",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).save(
                any()
        );
    }


    @Test
    void createAllowsSameTenantParents() {

        UUID tenantId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        CurriculumVersion version =
                mock(CurriculumVersion.class);

        ClassGrade classGrade =
                mock(ClassGrade.class);

        when(
                version.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                classGradeRepository.findByTenantIdAndId(
                        tenantId,
                        classGradeId
                )
        ).thenReturn(
                Optional.of(classGrade)
        );

        when(
                repository.save(
                        any(CurriculumClassGrade.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        CurriculumClassGrade result =
                service.create(
                        tenantId,
                        version,
                        classGradeId,
                        1
                );


        assertEquals(
                tenantId,
                result.getTenantId()
        );

        verify(
                classGradeRepository
        ).findByTenantIdAndId(
                tenantId,
                classGradeId
        );

        verify(
                repository
        ).save(
                result
        );
    }
}
