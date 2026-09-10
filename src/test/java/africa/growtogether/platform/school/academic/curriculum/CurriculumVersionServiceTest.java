package africa.growtogether.platform.school.academic.curriculum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CurriculumVersionServiceTest {

    private CurriculumVersionRepository repository;
    private CurriculumVersionService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(CurriculumVersionRepository.class);

        service =
                new CurriculumVersionService(
                        repository
                );
    }


    @Test
    void createRejectsCurriculumFromAnotherTenant() {

        UUID requestedTenant =
                UUID.randomUUID();

        UUID curriculumTenant =
                UUID.randomUUID();

        Curriculum curriculum =
                mock(Curriculum.class);

        when(
                curriculum.getTenantId()
        ).thenReturn(
                curriculumTenant
        );


        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                requestedTenant,
                                curriculum,
                                "2026",
                                "2026 Curriculum",
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                )
                        )
                );


        assertEquals(
                "Curriculum does not belong to tenant",
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
    void createAllowsCurriculumFromSameTenant() {

        UUID tenantId =
                UUID.randomUUID();

        Curriculum curriculum =
                mock(Curriculum.class);

        when(
                curriculum.getTenantId()
        ).thenReturn(
                tenantId
        );

        when(
                repository.save(
                        any(CurriculumVersion.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        CurriculumVersion result =
                service.create(
                        tenantId,
                        curriculum,
                        "2026",
                        "2026 Curriculum",
                        LocalDate.of(
                                2026,
                                1,
                                1
                        )
                );


        assertEquals(
                tenantId,
                result.getTenantId()
        );

        ArgumentCaptor<CurriculumVersion> captor =
                ArgumentCaptor.forClass(
                        CurriculumVersion.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        assertEquals(
                tenantId,
                captor.getValue().getTenantId()
        );

        assertTrue(
                result == captor.getValue()
        );
    }
}
