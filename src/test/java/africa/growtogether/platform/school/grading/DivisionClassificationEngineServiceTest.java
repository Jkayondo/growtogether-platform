package africa.growtogether.platform.school.grading;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class DivisionClassificationEngineServiceTest {



    @Test
    void shouldClassifyPleDivisionOne() {


        GradeDivisionRuleRepository repository =
                mock(GradeDivisionRuleRepository.class);


        GradeDivisionRule rule =
                mock(GradeDivisionRule.class);


        UUID tenantId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();



        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatus(
                                tenantId,
                                schemeId,
                                "ACTIVE"
                        )
        )
        .thenReturn(
                List.of(rule)
        );



        when(rule.matches(10))
                .thenReturn(true);


        when(rule.getDivisionCode())
                .thenReturn("DIVISION_I");


        when(rule.getDivisionName())
                .thenReturn("Division I");


        when(rule.getDescription())
                .thenReturn("Excellent performance");



        DivisionClassificationEngineService service =
                new DivisionClassificationEngineService(repository);



        DivisionClassificationResult result =
                service.classify(

                        tenantId,

                        schemeId,

                        new BigDecimal("10")

                );



        assertEquals(
                "DIVISION_I",
                result.getDivisionCode()
        );


        assertEquals(
                "Division I",
                result.getDivisionName()
        );


        assertEquals(
                new BigDecimal("10"),
                result.getAggregateValue()
        );

    }



    @Test
    void shouldRejectUnknownAggregate() {


        GradeDivisionRuleRepository repository =
                mock(GradeDivisionRuleRepository.class);


        GradeDivisionRule rule =
                mock(GradeDivisionRule.class);



        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatus(
                                any(),
                                any(),
                                eq("ACTIVE")
                        )
        )
        .thenReturn(
                List.of(rule)
        );


        when(rule.matches(99))
                .thenReturn(false);



        DivisionClassificationEngineService service =
                new DivisionClassificationEngineService(repository);



        assertThrows(

                IllegalArgumentException.class,

                () ->
                        service.classify(

                                UUID.randomUUID(),

                                UUID.randomUUID(),

                                new BigDecimal("99")

                        )

        );

    }



}
