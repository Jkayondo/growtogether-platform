package africa.growtogether.platform.school.grading;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GradeAggregationEngineServiceTest {


    @Test
    void shouldAggregatePleSubjectPoints() {


        GradeAggregationRuleRepository repository =
                mock(GradeAggregationRuleRepository.class);


        GradeAggregationRule rule =
                mock(GradeAggregationRule.class);


        UUID tenantId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();


        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndRuleCode(
                                tenantId,
                                schemeId,
                                "UG-PLE-SUBJECT-POINT-SUM"
                        )
        )
        .thenReturn(
                Optional.of(rule)
        );


        when(rule.getAggregationType())
                .thenReturn("SUBJECT_POINT_SUM");


        when(rule.getRuleCode())
                .thenReturn("UG-PLE-SUBJECT-POINT-SUM");



        GradeAggregationEngineService service =
                new GradeAggregationEngineService(repository);



        GradeAggregationResult result =
                service.aggregate(

                        tenantId,

                        schemeId,

                        "UG-PLE-SUBJECT-POINT-SUM",

                        List.of(
                                new BigDecimal("1"),
                                new BigDecimal("2"),
                                new BigDecimal("3"),
                                new BigDecimal("4")
                        )

                );


        assertEquals(
                new BigDecimal("10"),
                result.getTotalPoints()
        );


        assertEquals(
                "UG-PLE-SUBJECT-POINT-SUM",
                result.getAggregationRuleCode()
        );

    }



    @Test
    void shouldAggregateUacePrincipalSubsidiaryPoints() {


        GradeAggregationRuleRepository repository =
                mock(GradeAggregationRuleRepository.class);


        GradeAggregationRule rule =
                mock(GradeAggregationRule.class);


        UUID tenantId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();



        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndRuleCode(
                                tenantId,
                                schemeId,
                                "UG-UACE-PRINCIPAL-SUBSIDIARY-POINTS"
                        )
        )
        .thenReturn(
                Optional.of(rule)
        );


        when(rule.getAggregationType())
                .thenReturn("PRINCIPAL_SUBSIDIARY_POINTS");


        when(rule.getRuleCode())
                .thenReturn("UG-UACE-PRINCIPAL-SUBSIDIARY-POINTS");



        GradeAggregationResult result =
                new GradeAggregationEngineService(repository)
                        .aggregate(

                                tenantId,

                                schemeId,

                                "UG-UACE-PRINCIPAL-SUBSIDIARY-POINTS",

                                List.of(
                                        new BigDecimal("6"),
                                        new BigDecimal("5"),
                                        new BigDecimal("4")
                                )

                        );


        assertEquals(
                new BigDecimal("15"),
                result.getTotalPoints()
        );

    }



    @Test
    void shouldRejectMissingAggregationRule() {


        GradeAggregationRuleRepository repository =
                mock(GradeAggregationRuleRepository.class);


        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndRuleCode(
                                any(),
                                any(),
                                any()
                        )
        )
        .thenReturn(Optional.empty());



        GradeAggregationEngineService service =
                new GradeAggregationEngineService(repository);



        assertThrows(

                IllegalArgumentException.class,

                () ->
                        service.aggregate(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "UNKNOWN",
                                List.of(
                                        new BigDecimal("5")
                                )
                        )

        );

    }



    @Test
    void shouldRejectEmptyPoints() {


        GradeAggregationRuleRepository repository =
                mock(GradeAggregationRuleRepository.class);


        GradeAggregationRule rule =
                mock(GradeAggregationRule.class);


        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndRuleCode(
                                any(),
                                any(),
                                any()
                        )
        )
        .thenReturn(Optional.of(rule));


        when(rule.getAggregationType())
                .thenReturn("SUBJECT_POINT_SUM");


        when(rule.getRuleCode())
                .thenReturn("TEST");



        GradeAggregationEngineService service =
                new GradeAggregationEngineService(repository);



        assertThrows(

                IllegalArgumentException.class,

                () ->
                        service.aggregate(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                "TEST",
                                List.of()
                        )

        );

    }

}
