package africa.growtogether.platform.school.assessment.examination.registration;

import java.util.UUID;


public record CreateCandidatePaperRegistrationCommand(
        UUID examinationCandidateId,
        UUID assessmentPaperId,
        UUID examinationScheduleId,
        String registrationType
) {
}
