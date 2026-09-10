package africa.growtogether.platform.school.academic.progression;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class LearnerProgressionHistoryService {


    private final LearnerProgressionHistoryRepository repository;


    public LearnerProgressionHistoryService(
            LearnerProgressionHistoryRepository repository
    ) {

        this.repository = repository;

    }


    public LearnerProgressionHistory recordProgression(
            UUID tenantId,
            UUID learnerId,
            UUID promotionDecisionId,
            String progressionType,
            LocalDate effectiveDate
    ) {


        LearnerProgressionHistory history =
                new LearnerProgressionHistory(
                        tenantId,
                        learnerId,
                        promotionDecisionId,
                        progressionType,
                        effectiveDate
                );


        return repository.save(history);

    }


    @Transactional(readOnly = true)
    public List<LearnerProgressionHistory> findLearnerHistory(
            UUID tenantId,
            UUID learnerId
    ) {


        return repository
                .findByTenantIdAndLearnerIdOrderByEffectiveDateDesc(
                        tenantId,
                        learnerId
                );

    }


    @Transactional(readOnly = true)
    public List<LearnerProgressionHistory> findAcademicYearProgressions(
            UUID tenantId,
            UUID academicYearId
    ) {


        return repository
                .findByTenantIdAndAcademicYearId(
                        tenantId,
                        academicYearId
                );

    }


    @Transactional(readOnly = true)
    public List<LearnerProgressionHistory> findByPromotionDecision(
            UUID tenantId,
            UUID promotionDecisionId
    ) {


        return repository
                .findByTenantIdAndPromotionDecisionId(
                        tenantId,
                        promotionDecisionId
                );

    }


    public LearnerProgressionHistory archiveProgression(
            LearnerProgressionHistory history
    ) {


        history.archive();


        return repository.save(history);

    }

}
