package africa.growtogether.platform.school.academic.curriculum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CurriculumLearningAreaServiceTest {


    @Mock
    private CurriculumLearningAreaRepository repository;

    @Mock
    private CurriculumVersionRepository curriculumVersionRepository;

    private CurriculumLearningAreaService service;


    @BeforeEach
    void setUp() {

        service =
                new CurriculumLearningAreaService(
                        repository,
                        curriculumVersionRepository
                );
    }


    @Test
    void createRejectsCurriculumVersionOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumVersionId = UUID.randomUUID();

        when(
                curriculumVersionRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.create(
                                tenantId,
                                curriculumVersionId,
                                "LANG",
                                "Languages",
                                "LEARNING_AREA",
                                null,
                                1
                        )
                );

        assertEquals(
                "Curriculum version not found",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(any());
    }


    @Test
    void createAcceptsCurriculumVersionFromSameTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumVersionId = UUID.randomUUID();

        CurriculumVersion version =
                org.mockito.Mockito.mock(
                        CurriculumVersion.class
                );

        when(
                curriculumVersionRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
        ).thenReturn(
                Optional.of(version)
        );

        when(
                repository.save(any(CurriculumLearningArea.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        CurriculumLearningArea result =
                service.create(
                        tenantId,
                        curriculumVersionId,
                        "LANG",
                        "Languages",
                        "LEARNING_AREA",
                        "Language learning area",
                        2
                );

        assertEquals(
                tenantId,
                result.getTenantId()
        );

        assertEquals(
                curriculumVersionId,
                result.getCurriculumVersionId()
        );

        assertEquals(
                "LANG",
                result.getLearningAreaCode()
        );

        assertEquals(
                "Languages",
                result.getLearningAreaName()
        );

        assertEquals(
                "LEARNING_AREA",
                result.getLearningAreaType()
        );

        assertEquals(
                "Language learning area",
                result.getDescription()
        );

        assertEquals(
                2,
                result.getSequenceNumber()
        );

        verify(
                curriculumVersionRepository
        ).findByTenantIdAndId(
                tenantId,
                curriculumVersionId
        );

        verify(
                repository
        ).save(any(CurriculumLearningArea.class));
    }
}
