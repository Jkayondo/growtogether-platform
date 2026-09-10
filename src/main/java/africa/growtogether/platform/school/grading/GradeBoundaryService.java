package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;


@Service
@Transactional
public class GradeBoundaryService {


    private final GradeBoundaryRepository repository;


    public GradeBoundaryService(
            GradeBoundaryRepository repository
    ) {

        this.repository = repository;

    }


    @Transactional(readOnly = true)
    public GradeBoundary findGradeForScore(
            UUID tenantId,
            UUID gradingSchemeId,
            BigDecimal score
    ) {


        List<GradeBoundary> boundaries =
                repository
                        .findByTenantIdAndGradingSchemeIdOrderByMinimumScoreDesc(
                                tenantId,
                                gradingSchemeId
                        );


        return boundaries.stream()
                .filter(boundary -> boundary.matches(score))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No grade boundary matches score: " + score
                        )
                );

    }


    @Transactional(readOnly = true)
    public List<GradeBoundary> getSchemeBoundaries(
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return repository
                .findByTenantIdAndGradingSchemeIdOrderByMinimumScoreDesc(
                        tenantId,
                        gradingSchemeId
                );

    }

}
