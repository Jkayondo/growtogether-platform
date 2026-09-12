package africa.growtogether.platform.school.finance.assignment;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;
import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FinanceFeeAssignmentServiceTest {

    @Mock
    private FinanceFeeAssignmentJdbcRepository repository;

    @Mock
    private FinanceFoundationService foundationService;

    @Mock
    private FinanceFoundationJdbcRepository foundationRepository;


    @Test
    void assignsActiveFeeStructureUsingStudentCurrencyAccount() {

        FinanceFeeAssignmentService service =
                service();


        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();

        UUID structureId =
                UUID.randomUUID();

        UUID accountId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();


        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );


        when(
                foundationRepository.enrollmentBelongsToStudent(
                        tenantId,
                        enrollmentId,
                        studentId
                )
        ).thenReturn(
                true
        );


        when(
                foundationService.listFeeStructures(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        structure(
                                structureId,
                                tenantId,
                                "ACTIVE"
                        )
                )
        );


        when(
                foundationService.findStudentAccount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        account(
                                accountId,
                                tenantId,
                                studentId,
                                enrollmentId
                        )
                )
        );


        when(
                repository.existsAssignmentReference(
                        tenantId,
                        "FIN_B2_001"
                )
        ).thenReturn(
                false
        );


        when(
                repository.existsAssignmentScope(
                        eq(tenantId),
                        eq(studentId),
                        eq(structureId),
                        any(LocalDate.class)
                )
        ).thenReturn(
                false
        );


        ArgumentCaptor<AssignStudentFeeRequest> normalized =
                ArgumentCaptor.forClass(
                        AssignStudentFeeRequest.class
                );


        service.assignStudentFee(
                tenantId,
                new AssignStudentFeeRequest(
                        " fin b2 001 ",
                        studentId,
                        enrollmentId,
                        structureId,
                        LocalDate.of(
                                2026,
                                2,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                4,
                                30
                        ),
                        null
                ),
                actorId,
                actorId.toString()
        );


        verify(repository).createStudentFeeAssignment(
                eq(tenantId),
                normalized.capture(),
                eq(accountId),
                eq(actorId),
                eq(actorId.toString())
        );


        assertEquals(
                "FIN_B2_001",
                normalized.getValue()
                        .assignmentReference()
        );
    }


    @Test
    void rejectsFeeStructureThatIsNotActive() {

        FinanceFeeAssignmentService service =
                service();


        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID structureId =
                UUID.randomUUID();


        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );


        when(
                foundationService.listFeeStructures(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        structure(
                                structureId,
                                tenantId,
                                "APPROVED"
                        )
                )
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        service.assignStudentFee(
                                tenantId,
                                request(
                                        studentId,
                                        null,
                                        structureId
                                ),
                                UUID.randomUUID(),
                                "actor-1"
                        )
        );


        verifyNoInteractions(
                repository
        );
    }


    @Test
    void rejectsEnrollmentThatDoesNotBelongToStudent() {

        FinanceFeeAssignmentService service =
                service();


        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();


        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );


        when(
                foundationRepository.enrollmentBelongsToStudent(
                        tenantId,
                        enrollmentId,
                        studentId
                )
        ).thenReturn(
                false
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.assignStudentFee(
                                tenantId,
                                request(
                                        studentId,
                                        enrollmentId,
                                        UUID.randomUUID()
                                ),
                                UUID.randomUUID(),
                                "actor-1"
                        )
        );


        verifyNoInteractions(
                foundationService
        );

        verifyNoInteractions(
                repository
        );
    }


    @Test
    void rejectsDuplicateStudentStructureEffectiveDateScope() {

        FinanceFeeAssignmentService service =
                service();


        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID structureId =
                UUID.randomUUID();

        UUID accountId =
                UUID.randomUUID();


        when(
                foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(
                true
        );


        when(
                foundationService.listFeeStructures(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        structure(
                                structureId,
                                tenantId,
                                "ACTIVE"
                        )
                )
        );


        when(
                foundationService.findStudentAccount(
                        tenantId,
                        studentId,
                        "UGX"
                )
        ).thenReturn(
                Optional.of(
                        account(
                                accountId,
                                tenantId,
                                studentId,
                                null
                        )
                )
        );


        when(
                repository.existsAssignmentReference(
                        tenantId,
                        "FIN_B2_001"
                )
        ).thenReturn(
                false
        );


        when(
                repository.existsAssignmentScope(
                        eq(tenantId),
                        eq(studentId),
                        eq(structureId),
                        any(LocalDate.class)
                )
        ).thenReturn(
                true
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        service.assignStudentFee(
                                tenantId,
                                request(
                                        studentId,
                                        null,
                                        structureId
                                ),
                                UUID.randomUUID(),
                                "actor-1"
                        )
        );


        verify(
                repository,
                never()
        ).createStudentFeeAssignment(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }


    private FinanceFeeAssignmentService service() {

        return new FinanceFeeAssignmentService(
                repository,
                foundationService,
                foundationRepository
        );
    }


    private AssignStudentFeeRequest request(
            UUID studentId,
            UUID enrollmentId,
            UUID structureId
    ) {

        return new AssignStudentFeeRequest(
                "FIN_B2_001",
                studentId,
                enrollmentId,
                structureId,
                LocalDate.of(
                        2026,
                        2,
                        1
                ),
                LocalDate.of(
                        2026,
                        4,
                        30
                ),
                null
        );
    }


    private FeeStructureView structure(
            UUID id,
            UUID tenantId,
            String structureStatus
    ) {

        return new FeeStructureView(
                id,
                tenantId,
                "TUITION_2026",
                "Tuition 2026",
                null,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                "UGX",
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                LocalDate.of(
                        2026,
                        12,
                        31
                ),
                null,
                UUID.randomUUID(),
                structureStatus,
                "ACTIVE"
        );
    }


    private StudentFinancialAccountView account(
            UUID id,
            UUID tenantId,
            UUID studentId,
            UUID enrollmentId
    ) {

        return new StudentFinancialAccountView(
                id,
                tenantId,
                "ACC-001",
                studentId,
                enrollmentId,
                "UGX",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "ACTIVE",
                "ACTIVE"
        );
    }
}
