package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.school.reportcard.result.ReportCardGenerationIntegrationService;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class GradingReportCardMapperServiceTest {


    @Test
    void shouldMapSingleGradeResultToReportCardData() {


        GradeCalculationResult result =
                new GradeCalculationResult(

                        new BigDecimal("85"),

                        "D1",

                        "Distinction One",

                        new BigDecimal("1"),

                        true,

                        true

                );


        GradingReportCardMapperService mapper =
                new GradingReportCardMapperService();



        List<ReportCardGenerationIntegrationService.StudentSubjectResultData> mapped =
                mapper.map(
                        List.of(result)
                );



        assertEquals(
                1,
                mapped.size()
        );


        assertEquals(
                85.0,
                mapped.get(0).finalScore()
        );


        assertEquals(
                "D1",
                mapped.get(0).gradeCode()
        );


        assertEquals(
                "Distinction One",
                mapped.get(0).gradeName()
        );


        assertEquals(
                1.0,
                mapped.get(0).gradePoint()
        );

    }



    @Test
    void shouldMapMultipleSubjects() {


        GradingReportCardMapperService mapper =
                new GradingReportCardMapperService();



        List<GradeCalculationResult> results =
                List.of(

                        new GradeCalculationResult(

                                new BigDecimal("85"),

                                "D1",

                                "Distinction One",

                                new BigDecimal("1"),

                                true,

                                true

                        ),

                        new GradeCalculationResult(

                                new BigDecimal("65"),

                                "C3",

                                "Credit Three",

                                new BigDecimal("3"),

                                true,

                                false

                        )

                );



        List<ReportCardGenerationIntegrationService.StudentSubjectResultData> mapped =
                mapper.map(results);



        assertEquals(
                2,
                mapped.size()
        );


        assertEquals(
                "D1",
                mapped.get(0).gradeCode()
        );


        assertEquals(
                "C3",
                mapped.get(1).gradeCode()
        );

    }

}
