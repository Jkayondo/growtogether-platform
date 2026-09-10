package africa.growtogether.platform.school.grading;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GradeCalculationEngineServiceTest {


    @Mock
    private GradeBoundaryRepository repository;


    @InjectMocks
    private GradeCalculationEngineService service;



    @Test
    void shouldCalculateGradeFromScore() {

        UUID tenantId = UUID.randomUUID();
        UUID schemeId = UUID.randomUUID();


        GradeBoundary boundary =
                mock(GradeBoundary.class);


        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatusOrderByMinimumScoreDesc(
                                tenantId,
                                schemeId,
                                "ACTIVE"
                        )
        )
        .thenReturn(
                List.of(boundary)
        );


        when(boundary.matches(
                new BigDecimal("85")
        ))
        .thenReturn(true);


        when(boundary.getGradeCode())
                .thenReturn("D1");


        when(boundary.getGradeName())
                .thenReturn("Distinction One");


        when(boundary.getGradePoint())
                .thenReturn(new BigDecimal("1"));


        when(boundary.isPassGrade())
                .thenReturn(true);


        when(boundary.isDistinctionGrade())
                .thenReturn(true);



        GradeCalculationResult result =
                service.calculate(
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


        assertTrue(
                result.isPassGrade()
        );

    }



    @Test
    void shouldRejectScoreWithoutMatchingBoundary() {

        UUID tenantId = UUID.randomUUID();
        UUID schemeId = UUID.randomUUID();


        when(
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatusOrderByMinimumScoreDesc(
                                tenantId,
                                schemeId,
                                "ACTIVE"
                        )
        )
        .thenReturn(
                List.of()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.calculate(
                                tenantId,
                                schemeId,
                                new BigDecimal("150")
                        )
        );

    }

}
