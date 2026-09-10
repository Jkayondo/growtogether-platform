package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GradeCalculationEngineService {


    private final GradeBoundaryRepository repository;


    public GradeCalculationEngineService(
            GradeBoundaryRepository repository
    ) {

        this.repository = repository;

    }



    @Transactional(readOnly = true)
    public GradeCalculationResult calculate(
            UUID tenantId,
            UUID gradingSchemeId,
            BigDecimal score
    ) {


        List<GradeBoundary> boundaries =
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatusOrderByMinimumScoreDesc(
                                tenantId,
                                gradingSchemeId,
                                "ACTIVE"
                        );


        GradeBoundary matchedBoundary =
                boundaries.stream()
                        .filter(boundary -> boundary.matches(score))
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No grade boundary matches score: " + score
                                )
                        );


        return new GradeCalculationResult(

                score,

                matchedBoundary.getGradeCode(),

                matchedBoundary.getGradeName(),

                matchedBoundary.getGradePoint(),

                matchedBoundary.isPassGrade(),

                matchedBoundary.isDistinctionGrade()

        );

    }

}
