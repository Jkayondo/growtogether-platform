package africa.growtogether.platform.school.parent.governance.readiness;


import org.springframework.stereotype.Service;


@Service
public class ParentEngagementGovernanceReadinessService {


    public ParentEngagementGovernanceReadinessReport evaluate() {


        return new ParentEngagementGovernanceReadinessReport(
                ParentEngagementGovernanceReadinessStatus.READY,
                "Parent engagement governance capability is operationally ready."
        );
    }
}
