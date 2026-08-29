package africa.growtogether.platform.eip.integration;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static africa.growtogether.platform.eip.integration.EipRuntimeConfigurationDtos.*;

@RestController
@RequestMapping(
        "/api/v1/integration/runtime/external-delivery"
)
public class EipRuntimeConfigurationController {

    private final EipRuntimeConfigurationService service;

    public EipRuntimeConfigurationController(
            EipRuntimeConfigurationService service
    ) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('integration.runtime.manage')"
    )
    public ApiResponse<ExternalDeliveryView> get() {
        return ApiResponses.success(
                service.externalDelivery()
        );
    }

    @PutMapping
    @PreAuthorize(
            "hasAuthority('integration.runtime.manage')"
    )
    public ApiResponse<ExternalDeliveryView> set(
            @Valid
            @RequestBody
            SetExternalDeliveryCommand command
    ) {
        return ApiResponses.success(
                service.setExternalDelivery(
                        command
                )
        );
    }
}
