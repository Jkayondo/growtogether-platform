package africa.growtogether.platform.school.academic.assessment;

import africa.growtogether.platform.school.academic.outcome.LearningOutcome;
import africa.growtogether.platform.school.academic.outcome.LearningOutcomeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {


    @Mock
    private AssessmentRepository repository;

    @Mock
    private LearningOutcomeRepository learningOutcomeRepository;


    private AssessmentService service;


    @BeforeEach
    void setUp() {

        service =
                new AssessmentService(
                        repository,
                        learningOutcomeRepository
                );
    }


    @Test
    void createRequiresLearningOutcomeFromSameTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        when(
                learningOutcomeRepository.findByIdAndTenantId(
                        learningOutcomeId,
                        tenantId
                )
        ).thenReturn(
                Optional.of(
                        mock(LearningOutcome.class)
                )
        );

        when(
                repository.save(
                        any(Assessment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );


        service.create(
                tenantId,
                learningOutcomeId,
                "TEST-001",
                "Test Assessment"
        );


        verify(
                learningOutcomeRepository
        ).findByIdAndTenantId(
                learningOutcomeId,
                tenantId
        );

        verify(repository).save(
                any(Assessment.class)
        );
    }


    @Test
    void missingOrWrongTenantLearningOutcomePreventsSave() {

        UUID tenantId = UUID.randomUUID();
        UUID learningOutcomeId = UUID.randomUUID();

        when(
                learningOutcomeRepository.findByIdAndTenantId(
                        learningOutcomeId,
                        tenantId
                )
        ).thenReturn(
                Optional.empty()
        );


        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        tenantId,
                        learningOutcomeId,
                        "TEST-001",
                        "Test Assessment"
                )
        );


        verify(
                repository,
                never()
        ).save(
                any(Assessment.class)
        );
    }

}
