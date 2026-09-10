package africa.growtogether.platform.school.grading;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GradeResultProcessingServiceTest {


    @Test
    void shouldProcessGradeCalculationResult() {


        GradeCalculationEngineService engine =
                mock(GradeCalculationEngineService.class);


        GradeCalculationResult expected =
                new GradeCalculationResult(
                        new BigDecimal("85"),
                        "D1",
                        "Distinction One",
                        new BigDecimal("1"),
                        true,
                        true
                );


        UUID tenantId = UUID.randomUUID();

        UUID schemeId = UUID.randomUUID();


        when(
                engine.calculate(
                        tenantId,
                        schemeId,
                        new BigDecimal("85")
                )
        )
        .thenReturn(expected);



        GradeResultProcessingService service =
                new GradeResultProcessingService(engine);



        GradeCalculationResult result =
                service.process(
                        tenantId,
                        schemeId,
                        new BigDecimal("85")
                );


        assertEquals(
                "D1",
                result.getGradeCode()
        );


        assertEquals(
                new BigDecimal("1"),
                result.getGradePoint()
        );


        verify(engine)
                .calculate(
                        tenantId,
                        schemeId,
                        new BigDecimal("85")
                );

    }

}
