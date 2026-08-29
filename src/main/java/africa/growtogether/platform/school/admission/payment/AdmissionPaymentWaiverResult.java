package africa.growtogether.platform.school.admission.payment;

import java.math.BigDecimal;

public record AdmissionPaymentWaiverResult(

        AdmissionPaymentObligation obligation,

        BigDecimal requiredAmount,

        BigDecimal allocatedAmount,

        BigDecimal waivedAmount,

        BigDecimal outstandingAmount,

        AdmissionPaymentGateStatus gateStatus

) {
}
