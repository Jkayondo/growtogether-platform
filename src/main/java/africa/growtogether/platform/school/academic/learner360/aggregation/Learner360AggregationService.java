package africa.growtogether.platform.school.academic.learner360.aggregation;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Service
public class Learner360AggregationService {


    private final Learner360SummaryRepository repository;


    public Learner360AggregationService(
            Learner360SummaryRepository repository
    ) {

        this.repository = repository;
    }



    @Transactional
    public Learner360Summary createSummary(
            UUID tenantId,
            UUID learnerId,
            UUID learner360ProfileId,
            Double overallScore,
            Integer assessmentCount
    ) {


        Learner360Summary summary =
                new Learner360Summary(
                        learnerId,
                        learner360ProfileId
                );


        summary.setTenantId(
                tenantId
        );


        summary.updatePerformance(
                overallScore,
                assessmentCount
        );


        summary.calculateRisk();


        summary.setCalculatedAt(
                Instant.now()
        );


        return repository.save(
                summary
        );
    }



    @Transactional(readOnly = true)
    public Learner360Summary findByLearner(
            UUID tenantId,
            UUID learnerId
    ) {


        return repository
                .findByTenantIdAndLearnerId(
                        tenantId,
                        learnerId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Learner 360 summary not found"
                        )
                );
    }


}
