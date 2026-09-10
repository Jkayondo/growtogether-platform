package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


@Service
@Transactional
public class GradeResultProcessingService {


    private final GradeCalculationEngineService calculationEngine;



    public GradeResultProcessingService(
            GradeCalculationEngineService calculationEngine
    ) {

        this.calculationEngine = calculationEngine;

    }



    @Transactional(readOnly = true)
    public GradeCalculationResult process(

            UUID tenantId,

            UUID gradingSchemeId,

            BigDecimal score

    ) {


        return calculationEngine.calculate(

                tenantId,

                gradingSchemeId,

                score

        );

    }


}
