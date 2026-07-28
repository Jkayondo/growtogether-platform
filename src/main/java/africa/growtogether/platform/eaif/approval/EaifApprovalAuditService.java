package africa.growtogether.platform.eaif.approval;


import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class EaifApprovalAuditService {


    public void approved(
            String requestId,
            String userId,
            String reason
    ) {

        // TODO connect enterprise audit recorder

        System.out.println(
                "EAIF.REQUEST.APPROVED "
                + requestId
                + " by "
                + userId
                + " reason="
                + reason
        );
    }


    public void rejected(
            String requestId,
            String userId,
            String reason
    ) {

        // TODO connect enterprise audit recorder

        System.out.println(
                "EAIF.REQUEST.REJECTED "
                + requestId
                + " by "
                + userId
                + " reason="
                + reason
        );
    }
}
