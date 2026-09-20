package africa.growtogether.platform.school.assessment.marking;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CandidateScoreRepository
        extends JpaRepository<CandidateScore, UUID> {


    Optional<CandidateScore> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<CandidateScore>
    findByTenantIdAndMarkSheetIdAndStudentId(
            UUID tenantId,
            UUID markSheetId,
            UUID studentId
    );


    boolean existsByTenantIdAndMarkSheetIdAndStudentId(
            UUID tenantId,
            UUID markSheetId,
            UUID studentId
    );
}
