package africa.growtogether.platform.school.assessment.examination.candidate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record CreateExaminationCandidateCommand(

        @NotBlank
        String candidateNumber,

        @NotNull
        UUID examinationSessionId,

        @NotNull
        UUID studentId,

        @NotNull
        UUID studentEnrollmentId

) {
}
