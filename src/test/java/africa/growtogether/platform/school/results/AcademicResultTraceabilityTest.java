package africa.growtogether.platform.school.results;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class AcademicResultTraceabilityTest {



    @Test
    void shouldPreserveAcademicTraceabilityReferences() {


        UUID studentId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID termId =
                UUID.randomUUID();

        UUID classId =
                UUID.randomUUID();

        UUID assessmentId =
                UUID.randomUUID();

        UUID subjectId =
                UUID.randomUUID();

        UUID gradingSchemeId =
                UUID.randomUUID();

        UUID aggregationRuleId =
                UUID.randomUUID();

        UUID divisionRuleId =
                UUID.randomUUID();



        AcademicResultRecord record =

                new AcademicResultRecord(

                        studentId,

                        academicYearId,

                        termId,

                        UUID.randomUUID(),

                        gradingSchemeId,

                        subjectId,

                        "Mathematics",

                        new BigDecimal("85"),

                        "D1",

                        "Distinction One",

                        new BigDecimal("1"),

                        new BigDecimal("10"),

                        "I",

                        "Division One"

                );



        assertEquals(
                studentId,
                record.getStudentId()
        );


        assertEquals(
                academicYearId,
                record.getAcademicYearId()
        );


        assertEquals(
                termId,
                record.getTermId()
        );


        assertEquals(
                subjectId,
                record.getSubjectId()
        );


        assertEquals(
                gradingSchemeId,
                record.getGradingSchemeId()
        );

    }



    @Test
    void shouldPreserveGradeOutcomeEvidence() {


        AcademicResultRecord record =

                new AcademicResultRecord(

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        "Physics",

                        new BigDecimal("72"),

                        "C3",

                        "Credit Three",

                        new BigDecimal("3"),

                        new BigDecimal("15"),

                        "II",

                        "Division Two"

                );



        assertEquals(
                "Physics",
                record.getSubjectName()
        );


        assertEquals(
                "C3",
                record.getGradeCode()
        );


        assertEquals(
                "Credit Three",
                record.getGradeName()
        );


        assertEquals(
                new BigDecimal("3"),
                record.getGradePoint()
        );


        assertEquals(
                new BigDecimal("15"),
                record.getAggregateValue()
        );


        assertEquals(
                "II",
                record.getDivisionCode()
        );


        assertEquals(
                "Division Two",
                record.getDivisionName()
        );

    }


}
