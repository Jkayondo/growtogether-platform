package africa.growtogether.platform.school.finance.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;
import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.StudentFeeAssignmentView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FinanceFeeAssignmentLifecycleServiceTest {

    @Mock
    private FinanceFeeAssignmentJdbcRepository repository;

    @Mock
    private FinanceFoundationService foundationService;

    @Mock
    private FinanceFoundationJdbcRepository foundationRepository;

    @Test
    void delegatesAllGovernedLifecycleTransitions() {

        FinanceFeeAssignmentService service =
                new FinanceFeeAssignmentService(
                        repository,
                        foundationService,
                        foundationRepository
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        String actor =
                UUID.randomUUID()
                        .toString();


        StudentFeeAssignmentView view =
                mock(
                        StudentFeeAssignmentView.class
                );

        when(
                repository.suspendStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        ).thenReturn(
                Optional.of(
                        view
                )
        );

        when(
                repository.activateStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        ).thenReturn(
                Optional.of(
                        view
                )
        );

        when(
                repository.completeStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        ).thenReturn(
                Optional.of(
                        view
                )
        );

        when(
                repository.cancelStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        ).thenReturn(
                Optional.of(
                        view
                )
        );

        when(
                repository.archiveStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        ).thenReturn(
                Optional.of(
                        view
                )
        );

        assertSame(
                view,
                service.suspendStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        );

        assertSame(
                view,
                service.activateStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        );

        assertSame(
                view,
                service.completeStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        );

        assertSame(
                view,
                service.cancelStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        );

        assertSame(
                view,
                service.archiveStudentFeeAssignment(
                        tenantId,
                        assignmentId,
                        actor
                )
        );

        verify(repository).suspendStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        );

        verify(repository).activateStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        );

        verify(repository).completeStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        );

        verify(repository).cancelStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        );

        verify(repository).archiveStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        );
    }

    @Test
    void rejectsLifecycleTransitionWithoutActor() {

        FinanceFeeAssignmentService service =
                new FinanceFeeAssignmentService(
                        repository,
                        foundationService,
                        foundationRepository
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.cancelStudentFeeAssignment(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                " "
                        )
        );

        verifyNoInteractions(
                repository
        );
    }
}
