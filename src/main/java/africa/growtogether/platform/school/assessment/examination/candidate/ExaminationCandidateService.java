package africa.growtogether.platform.school.assessment.examination.candidate;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class ExaminationCandidateService {


    private final ExaminationCandidateRepository repository;


    public ExaminationCandidateService(
            ExaminationCandidateRepository repository
    ) {

        this.repository = repository;

    }


    public ExaminationCandidate register(
            UUID tenantId,
            String candidateNumber,
            UUID examinationSessionId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {


        if (repository.existsByTenantIdAndCandidateNumber(
                tenantId,
                candidateNumber
        )) {

            throw new IllegalArgumentException(
                    "Candidate number already exists"
            );

        }


        ExaminationCandidate candidate =
                new ExaminationCandidate(
                        candidateNumber,
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                );


        return repository.save(candidate);

    }


    @Transactional(readOnly = true)
    public ExaminationCandidate get(
            UUID tenantId,
            String candidateNumber
    ) {

        return repository
                .findByTenantIdAndCandidateNumber(
                        tenantId,
                        candidateNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Examination candidate not found"
                        )
                );

    }


    public ExaminationCandidate verify(
            UUID tenantId,
            String candidateNumber,
            UUID verifiedBy
    ) {

        ExaminationCandidate candidate =
                get(
                        tenantId,
                        candidateNumber
                );


        candidate.verify(
                verifiedBy
        );


        return candidate;

    }

}
