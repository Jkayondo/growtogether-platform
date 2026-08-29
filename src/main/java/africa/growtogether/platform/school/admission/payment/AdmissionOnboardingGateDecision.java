package africa.growtogether.platform.school.admission.payment;

import java.util.List;
import java.util.UUID;

public record AdmissionOnboardingGateDecision(

        UUID admissionApplicationId,

        boolean unlocked,

        int requiredObligationCount,

        int satisfiedObligationCount,

        List<UUID> blockingObligationIds

) {

    public AdmissionOnboardingGateDecision {

        blockingObligationIds =
                blockingObligationIds == null
                        ? List.of()
                        : List.copyOf(
                                blockingObligationIds
                        );
    }
}
