package africa.growtogether.platform.school.admission.payment;

import java.math.BigDecimal;

public record AdmissionPaymentAllocationResult(

        AdmissionPaymentAllocation allocation,

        BigDecimal requiredAmount,

        BigDecimal waivedAmount,

        BigDecimal totalAllocatedAmount,

        BigDecimal outstandingAmount,

        AdmissionPaymentGateStatus gateStatus

) {
}
