package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/admin")
public class NotificationDispatchController {

    private final NotificationFailoverDispatcher dispatcher;
    private final EnterpriseIdentityContext identity;
    private final ApiResponses responses;

    public NotificationDispatchController(
            NotificationFailoverDispatcher dispatcher,
            EnterpriseIdentityContext identity,
            ApiResponses responses
    ) {
        this.dispatcher = dispatcher;
        this.identity = identity;
        this.responses = responses;
    }

    @PostMapping("/{notificationId}/dispatch")
    @PreAuthorize(
            "hasAuthority('notification.dispatch.manage')"
    )
    public ApiResponse<DispatchView> dispatch(
            @PathVariable UUID notificationId
    ) {

        UUID tenantId =
                identity.requireTenantId();

        NotificationStatus status =
                dispatcher.dispatch(
                        tenantId,
                        notificationId
                );

        return responses.success(
                "GT-ENS-DISPATCH-001",
                "Notification dispatch cycle completed.",
                new DispatchView(
                        notificationId,
                        status
                )
        );
    }

    public record DispatchView(
            UUID notificationId,
            NotificationStatus status
    ) {
    }
}
