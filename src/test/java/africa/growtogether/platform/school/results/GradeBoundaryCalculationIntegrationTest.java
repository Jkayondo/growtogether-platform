package africa.growtogether.platform.school.results;


import africa.growtogether.platform.school.grading.GradeBoundary;

import org.junit.jupiter.api.Test;


import java.util.UUID;
import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;


class GradeBoundaryCalculationIntegrationTest {


    @Test
    void scoreMatchesConfiguredGradeBoundary(){


        GradeBoundary boundary =
                new GradeBoundary(
                        UUID.randomUUID(),
                        "A",
                        "Excellent",
                        BigDecimal.valueOf(80.0),
                        BigDecimal.valueOf(100.0),
                        BigDecimal.valueOf(4.0),
                        1
                );


        assertTrue(
                boundary.matches(BigDecimal.valueOf(90.0))
        );


        assertFalse(
                boundary.matches(BigDecimal.valueOf(70.0))
        );


    }


}
