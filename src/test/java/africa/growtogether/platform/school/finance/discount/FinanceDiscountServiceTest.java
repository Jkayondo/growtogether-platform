package africa.growtogether.platform.school.finance.discount;

import static africa.growtogether.platform.school.finance.discount.FinanceDiscountDtos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FinanceDiscountServiceTest {

    @Mock
    private FinanceDiscountJdbcRepository repository;

    @Mock
    private FinanceFoundationJdbcRepository foundationRepository;

    @Test
    void normalizesAndDefaultsDiscountSchemeCreate() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.existsFeeDiscountSchemeCode(
                        tenantId,
                        "SCHOLARSHIP-2026"
                )
        ).thenReturn(
                false
        );

        when(
                repository.createFeeDiscountScheme(
                        eq(tenantId),
                        any(
                                CreateFeeDiscountSchemeRequest.class
                        ),
                        eq("actor-1")
                )
        ).thenReturn(
                view(
                        tenantId,
                        "SCHOLARSHIP-2026"
                )
        );

        service.createFeeDiscountScheme(
                tenantId,
                new CreateFeeDiscountSchemeRequest(
                        " SCHOLARSHIP-2026 ",
                        " Merit Scholarship ",
                        " High performers ",
                        " scholarship ",
                        new BigDecimal(
                                "25.00"
                        ),
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        null,
                        null
                ),
                "actor-1"
        );

        ArgumentCaptor<CreateFeeDiscountSchemeRequest> request =
                ArgumentCaptor.forClass(
                        CreateFeeDiscountSchemeRequest.class
                );

        verify(repository).createFeeDiscountScheme(
                eq(tenantId),
                request.capture(),
                eq("actor-1")
        );

        assertEquals(
                "SCHOLARSHIP-2026",
                request.getValue()
                        .schemeCode()
        );

        assertEquals(
                "Merit Scholarship",
                request.getValue()
                        .schemeName()
        );

        assertEquals(
                "High performers",
                request.getValue()
                        .description()
        );

        assertEquals(
                "SCHOLARSHIP",
                request.getValue()
                        .discountType()
        );

        assertEquals(
                Map.of(),
                request.getValue()
                        .eligibilityRules()
        );

        assertTrue(
                request.getValue()
                        .approvalRequired()
        );
    }

    @Test
    void rejectsDuplicateSchemeCodeWithinTenant() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.existsFeeDiscountSchemeCode(
                        tenantId,
                        "DUPLICATE"
                )
        ).thenReturn(
                true
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createFeeDiscountScheme(
                                        tenantId,
                                        request(
                                                "DUPLICATE",
                                                "FIXED_AMOUNT",
                                                new BigDecimal(
                                                        "10000"
                                                )
                                        ),
                                        "actor-1"
                                )
                );

        assertEquals(
                "Fee discount scheme code already exists in this tenant.",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).createFeeDiscountScheme(
                any(),
                any(),
                anyString()
        );
    }

    @Test
    void rejectsPercentageGreaterThanOneHundred() {

        FinanceDiscountService service =
                service();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeDiscountScheme(
                                UUID.randomUUID(),
                                request(
                                        "PERCENT",
                                        "PERCENTAGE",
                                        new BigDecimal(
                                                "100.01"
                                        )
                                ),
                                "actor-1"
                        )
        );

        verifyNoInteractions(
                repository,
                foundationRepository
        );
    }

    @Test
    void rejectsNegativeDiscountValue() {

        FinanceDiscountService service =
                service();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeDiscountScheme(
                                UUID.randomUUID(),
                                request(
                                        "NEGATIVE",
                                        "FIXED_AMOUNT",
                                        new BigDecimal(
                                                "-1"
                                        )
                                ),
                                "actor-1"
                        )
        );

        verifyNoInteractions(
                repository,
                foundationRepository
        );
    }

    @Test
    void rejectsInvalidEffectiveDateRange() {

        FinanceDiscountService service =
                service();

        CreateFeeDiscountSchemeRequest request =
                new CreateFeeDiscountSchemeRequest(
                        "DATES",
                        "Dates",
                        null,
                        "WAIVER",
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        Map.of(),
                        LocalDate.of(
                                2026,
                                2,
                                1
                        ),
                        LocalDate.of(
                                2026,
                                1,
                                31
                        ),
                        true
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createFeeDiscountScheme(
                                UUID.randomUUID(),
                                request,
                                "actor-1"
                        )
        );

        verifyNoInteractions(
                repository,
                foundationRepository
        );
    }

    @Test
    void rejectsFeeCategoryOutsideTenant() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        UUID categoryId =
                UUID.randomUUID();

        when(
                repository.existsFeeDiscountSchemeCode(
                        tenantId,
                        "CATEGORY"
                )
        ).thenReturn(
                false
        );

        when(
                foundationRepository.findFeeCategory(
                        tenantId,
                        categoryId
                )
        ).thenReturn(
                Optional.empty()
        );

        CreateFeeDiscountSchemeRequest request =
                new CreateFeeDiscountSchemeRequest(
                        "CATEGORY",
                        "Category",
                        null,
                        "FIXED_AMOUNT",
                        BigDecimal.TEN,
                        categoryId,
                        null,
                        null,
                        Map.of(),
                        LocalDate.now(),
                        null,
                        true
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createFeeDiscountScheme(
                                        tenantId,
                                        request,
                                        "actor-1"
                                )
                );

        assertEquals(
                "feeCategoryId is not available in this tenant.",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).createFeeDiscountScheme(
                any(),
                any(),
                anyString()
        );
    }

    @Test
    void rejectsFeeItemOutsideTenant() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        UUID itemId =
                UUID.randomUUID();

        when(
                repository.existsFeeDiscountSchemeCode(
                        tenantId,
                        "ITEM"
                )
        ).thenReturn(
                false
        );

        when(
                foundationRepository.findFeeItem(
                        tenantId,
                        itemId
                )
        ).thenReturn(
                Optional.empty()
        );

        CreateFeeDiscountSchemeRequest request =
                new CreateFeeDiscountSchemeRequest(
                        "ITEM",
                        "Item",
                        null,
                        "FIXED_AMOUNT",
                        BigDecimal.TEN,
                        null,
                        itemId,
                        null,
                        Map.of(),
                        LocalDate.now(),
                        null,
                        true
                );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.createFeeDiscountScheme(
                                        tenantId,
                                        request,
                                        "actor-1"
                                )
                );

        assertEquals(
                "feeItemId is not available in this tenant.",
                error.getMessage()
        );

        verify(
                repository,
                never()
        ).createFeeDiscountScheme(
                any(),
                any(),
                anyString()
        );
    }

    @Test
    void rejectsCrossTenantOrMissingSchemeRead() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        UUID schemeId =
                UUID.randomUUID();

        when(
                repository.getFeeDiscountScheme(
                        tenantId,
                        schemeId
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                service.getFeeDiscountScheme(
                                        tenantId,
                                        schemeId
                                )
                );

        assertEquals(
                "Fee discount scheme is not available in this tenant.",
                error.getMessage()
        );
    }

    @Test
    void delegatesTenantScopedSchemeList() {

        FinanceDiscountService service =
                service();

        UUID tenantId =
                UUID.randomUUID();

        when(
                repository.listFeeDiscountSchemes(
                        tenantId
                )
        ).thenReturn(
                List.of(
                        view(
                                tenantId,
                                "A"
                        ),
                        view(
                                tenantId,
                                "B"
                        )
                )
        );

        List<FeeDiscountSchemeView> result =
                service.listFeeDiscountSchemes(
                        tenantId
                );

        assertEquals(
                2,
                result.size()
        );

        verify(repository).listFeeDiscountSchemes(
                tenantId
        );
    }

    private FinanceDiscountService service() {

        return new FinanceDiscountService(
                repository,
                foundationRepository
        );
    }

    private static CreateFeeDiscountSchemeRequest request(
            String code,
            String type,
            BigDecimal value
    ) {

        return new CreateFeeDiscountSchemeRequest(
                code,
                "Scheme " + code,
                null,
                type,
                value,
                null,
                null,
                null,
                Map.of(),
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                null,
                true
        );
    }

    private static FeeDiscountSchemeView view(
            UUID tenantId,
            String code
    ) {

        Instant now =
                Instant.now();

        return new FeeDiscountSchemeView(
                UUID.randomUUID(),
                tenantId,
                code,
                "Scheme " + code,
                null,
                "FIXED_AMOUNT",
                BigDecimal.TEN,
                null,
                null,
                null,
                Map.of(),
                LocalDate.of(
                        2026,
                        1,
                        1
                ),
                null,
                true,
                true,
                "ACTIVE",
                now,
                "actor-1",
                now,
                "actor-1",
                0L
        );
    }
}
