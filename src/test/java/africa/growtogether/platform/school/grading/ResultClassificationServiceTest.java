package africa.growtogether.platform.school.grading;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ResultClassificationServiceTest {


    @Test
    void shouldClassifyCompleteLearnerResult() {


        GradeResultProcessingService gradeService =
                mock(GradeResultProcessingService.class);


        GradeAggregationEngineService aggregationService =
                mock(GradeAggregationEngineService.class);


        DivisionClassificationEngineService divisionService =
                mock(DivisionClassificationEngineService.class);



        UUID tenantId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();



        GradeCalculationResult subjectResult =
                new GradeCalculationResult(

                        new BigDecimal("85"),

                        "D1",

                        "Distinction One",

                        new BigDecimal("1"),

                        true,

                        true

                );



        when(
                gradeService.process(
                        any(),
                        any(),
                        any()
                )
        )
        .thenReturn(subjectResult);



        GradeAggregationResult aggregationResult =
                new GradeAggregationResult(

                        new BigDecimal("4"),

                        new BigDecimal("4"),

                        4,

                        "UG-PLE-SUBJECT-POINT-SUM"

                );



        when(
                aggregationService.aggregate(
                        any(),
                        any(),
                        any(),
                        any()
                )
        )
        .thenReturn(aggregationResult);



        DivisionClassificationResult divisionResult =
                new DivisionClassificationResult(

                        new BigDecimal("4"),

                        "DIVISION_I",

                        "Division I",

                        "Excellent performance"

                );



        when(
                divisionService.classify(
                        any(),
                        any(),
                        any()
                )
        )
        .thenReturn(divisionResult);



        ResultClassificationService service =
                new ResultClassificationService(

                        gradeService,

                        aggregationService,

                        divisionService

                );



        FinalResultClassification result =
                service.classify(

                        tenantId,

                        schemeId,

                        List.of(

                                new BigDecimal("85"),

                                new BigDecimal("80"),

                                new BigDecimal("75"),

                                new BigDecimal("70")

                        ),

                        "UG-PLE-SUBJECT-POINT-SUM"

                );



        assertEquals(

                4,

                result.getSubjectResults().size()

        );


        assertEquals(

                "DIVISION_I",

                result.getDivisionResult()
                        .getDivisionCode()

        );


        assertEquals(

                "UG-PLE-SUBJECT-POINT-SUM",

                result.getAggregationResult()
                        .getAggregationRuleCode()

        );


    }

}
