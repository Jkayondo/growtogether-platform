package africa.growtogether.platform.school.student.identity;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.UUID;


import org.junit.jupiter.api.Test;


class LearnerIdentityGeneratorTest {


    private final LearnerIdentityGenerator generator =
            new DefaultLearnerIdentityGenerator();



    @Test
    void permanentLearnerNumberIsGeneratedCorrectly() {

        String first =
                generator.generatePermanentLearnerNumber();


        String second =
                generator.generatePermanentLearnerNumber();


        assertEquals(
                "GT-LRN-000000001",
                first
        );


        assertEquals(
                "GT-LRN-000000002",
                second
        );
    }



    @Test
    void schoolStudentNumberIsGeneratedCorrectly() {

        String number =
                generator.generateStudentNumber(
                        UUID.randomUUID(),
                        "PIO",
                        2026
                );


        assertTrue(
                number.startsWith(
                        "PIO-2026-"
                )
        );

    }

}
