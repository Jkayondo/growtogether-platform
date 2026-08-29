package africa.growtogether.platform.school.integration;

import africa.growtogether.platform.connect.ConnectSpace;

import africa.growtogether.platform.eiam.role.events.UserRolesChangedEvent;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SchoolConnectAdminRoleChangedListener {

    private final SchoolConnectSpaceResolver spaces;

    private final SchoolConnectAdminMembershipReconciliationService
            reconciliation;


    public SchoolConnectAdminRoleChangedListener(
            SchoolConnectSpaceResolver spaces,
            SchoolConnectAdminMembershipReconciliationService reconciliation
    ) {
        this.spaces = spaces;
        this.reconciliation = reconciliation;
    }


    @EventListener
    public void handle(
            UserRolesChangedEvent event
    ) {

        Optional<ConnectSpace> institutionSpace =
                spaces.findInstitutionSpace(
                        event.tenantId()
                );

        /*
         * EIAM is enterprise-wide.
         *
         * A tenant may legitimately exist before GT School has been
         * onboarded. In that case this School-specific projection has
         * nothing to reconcile and the EIAM role lifecycle continues.
         */
        if (institutionSpace.isEmpty()) {
            return;
        }

        reconciliation.reconcile(
                event.tenantId(),
                event.userId(),
                institutionSpace.get()
        );
    }
}
