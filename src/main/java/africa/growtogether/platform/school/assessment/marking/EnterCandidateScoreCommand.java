package africa.growtogether.platform.school.assessment.marking;


import java.math.BigDecimal;


public record EnterCandidateScoreCommand(
        BigDecimal score
) {
}
