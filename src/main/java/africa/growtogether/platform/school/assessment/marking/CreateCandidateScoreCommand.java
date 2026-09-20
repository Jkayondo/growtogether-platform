package africa.growtogether.platform.school.assessment.marking;


import java.util.UUID;


public record CreateCandidateScoreCommand(

        UUID markSheetId,

        UUID examinationCandidateId,

        UUID candidatePaperRegistrationId,

        UUID studentId,

        UUID studentEnrollmentId

) {
}
