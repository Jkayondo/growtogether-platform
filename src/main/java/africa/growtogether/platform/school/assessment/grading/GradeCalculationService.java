package africa.growtogether.platform.school.assessment.grading;


import africa.growtogether.platform.school.assessment.marking.CandidateScore;
import africa.growtogether.platform.school.assessment.marking.CandidateScoreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;


@Service
@Transactional
public class GradeCalculationService {


    private final CandidateScoreRepository scoreRepository;


    public GradeCalculationService(
            CandidateScoreRepository scoreRepository
    ) {

        this.scoreRepository = scoreRepository;

    }


    public BigDecimal calculateAverage(
            UUID tenantId,
            UUID markSheetId
    ) {

        List<CandidateScore> scores =
                scoreRepository.findAll();


        BigDecimal total = BigDecimal.ZERO;
        int count = 0;


        for (CandidateScore score : scores) {

            if (
                    score.getFinalScore() != null
            ) {

                total = total.add(score.getFinalScore());
                count++;

            }

        }


        if (count == 0) {

            return BigDecimal.ZERO;

        }


        return total
                .divide(
                        BigDecimal.valueOf(count),
                        2,
                        java.math.RoundingMode.HALF_UP
                );

    }


    public String determineGrade(
            double percentage
    ) {


        if (percentage >= 80) {

            return "A";

        }

        if (percentage >= 70) {

            return "B";

        }

        if (percentage >= 60) {

            return "C";

        }

        if (percentage >= 50) {

            return "D";

        }


        return "F";

    }

}
