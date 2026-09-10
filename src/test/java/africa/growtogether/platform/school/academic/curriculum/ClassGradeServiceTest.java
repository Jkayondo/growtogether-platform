package africa.growtogether.platform.school.academic.curriculum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ClassGradeServiceTest {

    @Mock
    private ClassGradeRepository repository;

    @Mock
    private EducationLevelService educationLevels;

    @InjectMocks
    private ClassGradeService service;


    @Test
    void createRequiresEducationLevelOwnedByTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        EducationLevel educationLevel =
                org.mockito.Mockito.mock(
                        EducationLevel.class
                );

        when(
                educationLevels.get(
                        tenantId,
                        educationLevelId
                )
        ).thenReturn(
                educationLevel
        );

        when(
                repository.save(
                        any(ClassGrade.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ClassGrade created =
                service.create(
                        tenantId,
                        educationLevelId,
                        "P1",
                        "Primary One",
                        1,
                        40
                );

        verify(
                educationLevels
        ).get(
                tenantId,
                educationLevelId
        );

        ArgumentCaptor<ClassGrade> captor =
                ArgumentCaptor.forClass(
                        ClassGrade.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        ClassGrade saved =
                captor.getValue();

        assertEquals(
                tenantId,
                saved.getTenantId()
        );

        assertEquals(
                educationLevelId,
                saved.getEducationLevelId()
        );

        assertEquals(
                "P1",
                saved.getClassCode()
        );

        assertEquals(
                "Primary One",
                saved.getClassName()
        );

        assertEquals(
                1,
                saved.getSequenceNumber()
        );

        assertEquals(
                40,
                saved.getCapacity()
        );

        assertSame(
                saved,
                created
        );
    }


    @Test
    void createRejectsEducationLevelOutsideTenantAndDoesNotSave() {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        doThrow(
                new IllegalArgumentException(
                        "Education level not found for tenant"
                )
        ).when(
                educationLevels
        ).get(
                tenantId,
                educationLevelId
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.create(
                                tenantId,
                                educationLevelId,
                                "P1",
                                "Primary One",
                                1,
                                40
                        )
        );

        verify(
                repository,
                never()
        ).save(
                any(ClassGrade.class)
        );
    }


    @Test
    void findByEducationLevelUsesTenantScopedRepositoryQuery() {

        UUID tenantId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        List<ClassGrade> expected =
                List.of();

        when(
                repository
                        .findByTenantIdAndEducationLevelId(
                                tenantId,
                                educationLevelId
                        )
        ).thenReturn(
                expected
        );

        List<ClassGrade> result =
                service.findByEducationLevel(
                        tenantId,
                        educationLevelId
                );

        assertSame(
                expected,
                result
        );

        verify(
                repository
        ).findByTenantIdAndEducationLevelId(
                tenantId,
                educationLevelId
        );
    }


    @Test
    void findByCodeUsesTenantScopedRepositoryQuery() {

        UUID tenantId =
                UUID.randomUUID();

        ClassGrade classGrade =
                org.mockito.Mockito.mock(
                        ClassGrade.class
                );

        when(
                repository.findByTenantIdAndClassCode(
                        tenantId,
                        "P1"
                )
        ).thenReturn(
                java.util.Optional.of(
                        classGrade
                )
        );

        ClassGrade result =
                service.findByCode(
                        tenantId,
                        "P1"
                );

        assertSame(
                classGrade,
                result
        );

        verify(
                repository
        ).findByTenantIdAndClassCode(
                tenantId,
                "P1"
        );
    }
}
