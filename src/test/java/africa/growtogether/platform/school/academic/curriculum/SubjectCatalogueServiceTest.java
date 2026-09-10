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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SubjectCatalogueServiceTest {


    @Mock
    private SubjectCatalogueRepository repository;

    @Mock
    private CurriculumVersionRepository curriculumVersionRepository;

    @Mock
    private CurriculumLearningAreaRepository learningAreaRepository;

    private SubjectCatalogueService service;


    @BeforeEach
    void setUp() {

        service =
                new SubjectCatalogueService(
                        repository,
                        curriculumVersionRepository,
                        learningAreaRepository
                );
    }


    @Test
    void createRejectsCurriculumVersionOutsideTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumVersionId = UUID.randomUUID();
        UUID learningAreaId = UUID.randomUUID();

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
                                learningAreaId,
                                "MATH",
                                "Mathematics",
                                "CORE",
                                null,
                                1
                        )
                );

        assertEquals(
                "Curriculum version not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                learningAreaRepository
        );

        verify(
                repository,
                never()
        ).save(any());
    }


    @Test
    void createRejectsLearningAreaOutsideExactTenantAndVersion() {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumVersionId = UUID.randomUUID();
        UUID learningAreaId = UUID.randomUUID();

        CurriculumVersion curriculumVersion =
                org.mockito.Mockito.mock(
                        CurriculumVersion.class
                );

        when(
                curriculumVersionRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
        ).thenReturn(
                Optional.of(curriculumVersion)
        );

        when(
                learningAreaRepository
                        .findByTenantIdAndCurriculumVersionIdAndId(
                                tenantId,
                                curriculumVersionId,
                                learningAreaId
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
                                learningAreaId,
                                "MATH",
                                "Mathematics",
                                "CORE",
                                null,
                                1
                        )
                );

        assertEquals(
                "Learning area not found",
                exception.getMessage()
        );

        verify(
                repository,
                never()
        ).save(any());
    }


    @Test
    void createAcceptsOnlyExactSameTenantAndVersionParents() {

        UUID tenantId = UUID.randomUUID();
        UUID curriculumVersionId = UUID.randomUUID();
        UUID learningAreaId = UUID.randomUUID();

        CurriculumVersion curriculumVersion =
                org.mockito.Mockito.mock(
                        CurriculumVersion.class
                );

        CurriculumLearningArea learningArea =
                org.mockito.Mockito.mock(
                        CurriculumLearningArea.class
                );

        when(
                curriculumVersionRepository.findByTenantIdAndId(
                        tenantId,
                        curriculumVersionId
                )
        ).thenReturn(
                Optional.of(curriculumVersion)
        );

        when(
                learningAreaRepository
                        .findByTenantIdAndCurriculumVersionIdAndId(
                                tenantId,
                                curriculumVersionId,
                                learningAreaId
                        )
        ).thenReturn(
                Optional.of(learningArea)
        );

        when(
                repository.save(any(SubjectCatalogue.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        SubjectCatalogue result =
                service.create(
                        tenantId,
                        curriculumVersionId,
                        learningAreaId,
                        "MATH",
                        "Mathematics",
                        "CORE",
                        "Mathematics catalogue subject",
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
                learningAreaId,
                result.getLearningAreaId()
        );

        assertEquals(
                "MATH",
                result.getSubjectCode()
        );

        assertEquals(
                "Mathematics",
                result.getSubjectName()
        );

        assertEquals(
                "CORE",
                result.getSubjectType()
        );

        assertEquals(
                "Mathematics catalogue subject",
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
                learningAreaRepository
        ).findByTenantIdAndCurriculumVersionIdAndId(
                tenantId,
                curriculumVersionId,
                learningAreaId
        );

        verify(
                repository
        ).save(any(SubjectCatalogue.class));
    }
}
