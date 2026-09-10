package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class ResultClassificationService {


    private final GradeResultProcessingService gradeProcessingService;

    private final GradeAggregationEngineService aggregationEngine;

    private final DivisionClassificationEngineService divisionEngine;



    public ResultClassificationService(

            GradeResultProcessingService gradeProcessingService,

            GradeAggregationEngineService aggregationEngine,

            DivisionClassificationEngineService divisionEngine

    ) {

        this.gradeProcessingService = gradeProcessingService;
        this.aggregationEngine = aggregationEngine;
        this.divisionEngine = divisionEngine;

    }



    @Transactional(readOnly = true)
    public FinalResultClassification classify(

            UUID tenantId,

            UUID gradingSchemeId,

            List<BigDecimal> subjectScores,

            String aggregationRuleCode

    ) {


        List<GradeCalculationResult> subjectResults =
                new ArrayList<>();


        List<BigDecimal> gradePoints =
                new ArrayList<>();



        for (BigDecimal score : subjectScores) {


            GradeCalculationResult result =
                    gradeProcessingService.process(

                            tenantId,

                            gradingSchemeId,

                            score

                    );


            subjectResults.add(result);

            gradePoints.add(
                    result.getGradePoint()
            );

        }



        GradeAggregationResult aggregationResult =
                aggregationEngine.aggregate(

                        tenantId,

                        gradingSchemeId,

                        aggregationRuleCode,

                        gradePoints

                );



        DivisionClassificationResult divisionResult =
                divisionEngine.classify(

                        tenantId,

                        gradingSchemeId,

                        aggregationResult.getTotalPoints()

                );



        return new FinalResultClassification(

                subjectResults,

                aggregationResult,

                divisionResult

        );

    }

}
