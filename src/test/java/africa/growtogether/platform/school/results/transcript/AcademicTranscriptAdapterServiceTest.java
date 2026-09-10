package africa.growtogether.platform.school.results.transcript;


import africa.growtogether.platform.school.results.AcademicResultRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class AcademicTranscriptAdapterServiceTest {


    @Test
    void shouldMapAcademicResultRecordToTranscriptEntry() {


        UUID studentId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();


        AcademicResultRecord record =
                new AcademicResultRecord(

                        studentId,

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        UUID.randomUUID(),

                        schemeId,

                        UUID.randomUUID(),

                        "Mathematics",

                        new BigDecimal("85"),

                        "D1",

                        "Distinction One",

                        new BigDecimal("1"),

                        new BigDecimal("10"),

                        "I",

                        "Division One"

                );


        AcademicTranscriptAdapterService service =
                new AcademicTranscriptAdapterService();


        TranscriptEntry entry =
                service.map(record);


        assertEquals(
                studentId,
                entry.studentId()
        );


        assertEquals(
                "Mathematics",
                entry.subjectName()
        );


        assertEquals(
                "D1",
                entry.gradeCode()
        );


        assertEquals(
                "Distinction One",
                entry.gradeName()
        );


        assertEquals(
                new BigDecimal("1"),
                entry.gradePoint()
        );


        assertEquals(
                schemeId,
                entry.gradingSchemeId()
        );

    }



    @Test
    void shouldMapMultipleAcademicResults() {


        AcademicTranscriptAdapterService service =
                new AcademicTranscriptAdapterService();


        AcademicResultRecord first =
                new AcademicResultRecord(

                        UUID.randomUUID(),

                        null,

                        null,

                        null,

                        UUID.randomUUID(),

                        null,

                        "English",

                        new BigDecimal("75"),

                        "D2",

                        "Distinction Two",

                        new BigDecimal("2"),

                        null,

                        null,

                        null

                );


        AcademicResultRecord second =
                new AcademicResultRecord(

                        UUID.randomUUID(),

                        null,

                        null,

                        null,

                        UUID.randomUUID(),

                        null,

                        "Physics",

                        new BigDecimal("65"),

                        "C4",

                        "Credit Four",

                        new BigDecimal("4"),

                        null,

                        null,

                        null

                );


        List<TranscriptEntry> entries =
                service.mapAll(
                        List.of(first, second)
                );


        assertEquals(
                2,
                entries.size()
        );


        assertEquals(
                "English",
                entries.get(0).subjectName()
        );


        assertEquals(
                "Physics",
                entries.get(1).subjectName()
        );

    }

}
