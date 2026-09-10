package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GradeAggregationEngineService {


    private final GradeAggregationRuleRepository ruleRepository;


    public GradeAggregationEngineService(
            GradeAggregationRuleRepository ruleRepository
    ) {

        this.ruleRepository = ruleRepository;

    }



    @Transactional(readOnly = true)
    public GradeAggregationResult aggregate(

            UUID tenantId,

            UUID gradingSchemeId,

            String aggregationRuleCode,

            List<BigDecimal> gradePoints

    ) {


        GradeAggregationRule rule =
                ruleRepository
                        .findByTenantIdAndGradingSchemeIdAndRuleCode(
                                tenantId,
                                gradingSchemeId,
                                aggregationRuleCode
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Aggregation rule not found: "
                                                        + aggregationRuleCode
                                        )
                        );



        if (gradePoints == null || gradePoints.isEmpty()) {

            throw new IllegalArgumentException(
                    "Grade points cannot be empty"
            );

        }



        BigDecimal totalPoints;


        switch (rule.getAggregationType()) {


            case "SUBJECT_POINT_SUM",
                 "PRINCIPAL_SUBSIDIARY_POINTS" ->

                    totalPoints =
                            gradePoints.stream()
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );


            default ->

                    throw new IllegalArgumentException(
                            "Unsupported aggregation type: "
                                    + rule.getAggregationType()
                    );

        }



        return new GradeAggregationResult(

                totalPoints,

                totalPoints,

                gradePoints.size(),

                rule.getRuleCode()

        );

    }

}
