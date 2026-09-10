package africa.growtogether.platform.school.academic.progression;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class PromotionDecisionService {


    private final PromotionDecisionRepository repository;


    public PromotionDecisionService(
            PromotionDecisionRepository repository
    ) {

        this.repository = repository;

    }


    public PromotionDecision create(
            UUID tenantId,
            UUID learnerId,
            UUID promotionRuleId
    ) {


        PromotionDecision decision =
                new PromotionDecision(
                        tenantId,
                        learnerId,
                        promotionRuleId
                );


        return repository.save(decision);

    }


    @Transactional(readOnly = true)
    public List<PromotionDecision> findByLearner(
            UUID tenantId,
            UUID learnerId
    ) {


        return repository
                .findByTenantIdAndLearnerId(
                        tenantId,
                        learnerId
                );

    }


    @Transactional(readOnly = true)
    public List<PromotionDecision> findByStatus(
            UUID tenantId,
            String status
    ) {


        return repository
                .findByTenantIdAndDecisionStatus(
                        tenantId,
                        status
                );

    }


    public PromotionDecision recommend(
            PromotionDecision decision
    ) {


        PromotionDecision current =
                findById(decision.getId());

        current.recommend();

        return repository.save(current);

    }


    public PromotionDecision approve(
            PromotionDecision decision
    ) {


        PromotionDecision current =
                findById(decision.getId());

        current.approve();

        return repository.save(current);

    }


    public PromotionDecision promote(
            PromotionDecision decision
    ) {


        PromotionDecision current =
                findById(decision.getId());

        current.promote();

        return repository.save(current);

    }


    public PromotionDecision repeat(
            PromotionDecision decision
    ) {


        decision.repeat();

        return repository.save(decision);

    }


    public PromotionDecision reject(
            PromotionDecision decision
    ) {


        decision.reject();

        return repository.save(decision);

    }


    @Transactional(readOnly = true)
    public PromotionDecision findById(
            UUID id
    ) {

        return repository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Promotion decision not found: " + id
                        )
                );

    }


}
