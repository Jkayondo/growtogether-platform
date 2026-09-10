package africa.growtogether.platform.school.assessment.marking;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


@Service
@Transactional
public class CandidateScoreService {


    private final CandidateScoreRepository repository;


    public CandidateScoreService(
            CandidateScoreRepository repository
    ) {

        this.repository = repository;

    }


    public CandidateScore create(
            CandidateScore score
    ) {

        return repository.save(score);

    }


    @Transactional(readOnly = true)
    public CandidateScore get(
            UUID id
    ) {

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Candidate score not found"
                        )
                );

    }


    public CandidateScore enterScore(
            UUID id,
            BigDecimal score
    ) {

        CandidateScore candidateScore = get(id);

        candidateScore.enterScore(score);

        return candidateScore;

    }


    public CandidateScore validate(
            UUID id
    ) {

        CandidateScore candidateScore = get(id);

        candidateScore.validate();

        return candidateScore;

    }


    public CandidateScore approve(
            UUID id
    ) {

        CandidateScore candidateScore = get(id);

        candidateScore.approve();

        return candidateScore;

    }


    public CandidateScore markAbsent(
            UUID id
    ) {

        CandidateScore candidateScore = get(id);

        candidateScore.markAbsent();

        return candidateScore;

    }

}
