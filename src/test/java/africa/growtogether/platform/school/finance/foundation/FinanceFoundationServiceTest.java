package africa.growtogether.platform.school.finance.foundation;

import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class FinanceFoundationServiceTest {

    @Mock
    private FinanceFoundationJdbcRepository repository;


    @Test
    void normalizesFeeCategoryFoundation() {

        FinanceFoundationService service =
                new FinanceFoundationService(
                        repository
                );

        UUID tenantId =
                UUID.randomUUID();


        when(
                repository.existsFeeCategoryCode(
                        eq(tenantId),
                        anyString()
                )
        ).thenReturn(false);


        service.createFeeCategory(
                tenantId,
                new CreateFeeCategoryRequest(
                        " tuition fees ",
                        " Tuition ",
                        " Core tuition ",
                        "tuition",
                        null,
                        null,
                        null,
                        null
                ),
                "actor-1"
        );


        ArgumentCaptor<CreateFeeCategoryRequest> request =
                ArgumentCaptor.forClass(
                        CreateFeeCategoryRequest.class
                );


        verify(repository).createFeeCategory(
                eq(tenantId),
                request.capture(),
                eq("actor-1")
        );


        assertEquals(
                "TUITION_FEES",
                request.getValue().categoryCode()
        );

        assertEquals(
                "TUITION",
                request.getValue().categoryType()
        );

        assertFalse(
                request.getValue().refundable()
        );

        assertTrue(
                request.getValue().mandatoryByDefault()
        );

        assertTrue(
                request.getValue().recurring()
        );
    }


    @Test
    void rejectsFeeItemCategoryOutsideTenant() {

        FinanceFoundationService service =
                new FinanceFoundationService(
                        repository
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID categoryId =
                UUID.randomUUID();


        when(
                repository.findFeeCategory(
                        tenantId,
                        categoryId
                )
        ).thenReturn(
                Optional.empty()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeItem(
                                tenantId,
                                new CreateFeeItemRequest(
                                        categoryId,
                                        "TUITION",
                                        "Tuition",
                                        null,
                                        "UGX",
                                        BigDecimal.valueOf(
                                                100000
                                        ),
                                        "TERM",
                                        false,
                                        true,
                                        false,
                                        null
                                ),
                                "actor-1"
                        )
        );
    }


    @Test
    void rejectsStructureItemCurrencyMismatch() {

        FinanceFoundationService service =
                new FinanceFoundationService(
                        repository
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID structureId =
                UUID.randomUUID();

        UUID itemId =
                UUID.randomUUID();


        when(
                repository.findFeeStructure(
                        tenantId,
                        structureId
                )
        ).thenReturn(
                Optional.of(
                        structure(
                                structureId,
                                tenantId,
                                "UGX",
                                "DRAFT"
                        )
                )
        );


        when(
                repository.findFeeItem(
                        tenantId,
                        itemId
                )
        ).thenReturn(
                Optional.of(
                        item(
                                itemId,
                                tenantId,
                                "USD"
                        )
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.addFeeStructureItem(
                                tenantId,
                                structureId,
                                new AddFeeStructureItemRequest(
                                        itemId,
                                        BigDecimal.TEN,
                                        BigDecimal.ONE,
                                        true,
                                        false,
                                        null,
                                        1
                                ),
                                "actor-1"
                        )
        );
    }


    @Test
    void rejectsEnrollmentThatDoesNotBelongToStudent() {

        FinanceFoundationService service =
                new FinanceFoundationService(
                        repository
                );

        UUID tenantId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        UUID enrollmentId =
                UUID.randomUUID();


        when(
                repository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ).thenReturn(true);


        when(
                repository.enrollmentBelongsToStudent(
                        tenantId,
                        enrollmentId,
                        studentId
                )
        ).thenReturn(false);


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.openStudentAccount(
                                tenantId,
                                new OpenStudentFinancialAccountRequest(
                                        "ACC-001",
                                        studentId,
                                        enrollmentId,
                                        "UGX",
                                        BigDecimal.ZERO,
                                        null
                                ),
                                "actor-1"
                        )
        );
    }


    private static FeeStructureView structure(
            UUID id,
            UUID tenantId,
            String currency,
            String lifecycle
    ) {

        return new FeeStructureView(
                id,
                tenantId,
                "FS-1",
                "Structure",
                null,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                currency,
                LocalDate.now(),
                null,
                null,
                null,
                lifecycle,
                "ACTIVE"
        );
    }


    private static FeeItemView item(
            UUID id,
            UUID tenantId,
            String currency
    ) {

        return new FeeItemView(
                id,
                tenantId,
                UUID.randomUUID(),
                "ITEM-1",
                "Item",
                null,
                currency,
                BigDecimal.ONE,
                "TERM",
                false,
                true,
                false,
                null,
                true,
                "ACTIVE"
        );
    }
}
