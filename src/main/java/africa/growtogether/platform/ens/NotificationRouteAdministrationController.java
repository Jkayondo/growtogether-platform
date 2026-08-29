package africa.growtogether.platform.ens;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static africa.growtogether.platform.ens.NotificationRouteAdministrationDtos.*;

@RestController
@RequestMapping("/api/v1/notifications/admin/routes")
public class NotificationRouteAdministrationController {

    private final NotificationRouteAdministrationService service;

    public NotificationRouteAdministrationController(
            NotificationRouteAdministrationService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('notification.route.manage')")
    public ApiResponse<RouteView> create(
            @Valid @RequestBody CreateRouteCommand command
    ) {
        return ApiResponses.success(
                service.create(command)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('notification.route.read')")
    public ApiResponse<List<RouteView>> list() {
        return ApiResponses.success(
                service.list()
        );
    }
}
