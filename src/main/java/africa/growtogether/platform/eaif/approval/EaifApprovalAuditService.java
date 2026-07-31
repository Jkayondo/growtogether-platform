package africa.growtogether.platform.eaif.approval;


import africa.growtogether.platform.eaif.integration.EaifAuditRecorder;

import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class EaifApprovalAuditService {


    private final EaifAuditRecorder audit;


    public EaifApprovalAuditService(
            EaifAuditRecorder audit
    ) {
        this.audit = audit;
    }



    public void approved(
            String requestId,
            String userId,
            String reason
    ) {

        audit.success(
                "EAIF.REQUEST.APPROVED",
                requestId,
                "AI request approved",
                Map.of(
                        "approvedBy",
                        userId,
                        "reason",
                        reason
                )
        );
    }




    public void rejected(
            String requestId,
            String userId,
            String reason
    ) {

        audit.failure(
                "EAIF.REQUEST.REJECTED",
                requestId,
                "AI request rejected",
                Map.of(
                        "rejectedBy",
                        userId,
                        "reason",
                        reason
                )
        );
    }
}