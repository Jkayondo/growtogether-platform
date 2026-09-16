package africa.growtogether.platform.eiam.bootstrap;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eiam/bootstrap")
public class FirstAdminBootstrapController {

    public static final String BOOTSTRAP_TOKEN_HEADER =
            "X-GT-Bootstrap-Token";

    private final FirstAdminBootstrapService service;
    private final ApiResponses responses;

    public FirstAdminBootstrapController(
            FirstAdminBootstrapService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping("/first-admin")
    public ResponseEntity<ApiResponse<FirstAdminBootstrapView>>
            firstAdministrator(
                    @RequestHeader(
                            name = BOOTSTRAP_TOKEN_HEADER,
                            required = false
                    )
                    String bootstrapToken,
                    @Valid
                    @RequestBody
                    FirstAdminBootstrapCommand command
            ) {

        FirstAdminBootstrapView view =
                service.bootstrap(
                        bootstrapToken,
                        command
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        responses.success(
                                "GT-EIAM-BOOTSTRAP-001",
                                "Initial tenant administrator created.",
                                view
                        )
                );
    }
}
