package africa.growtogether.platform.system;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

    private final ApiResponses responses;
    private final Clock clock = Clock.systemUTC();

    public SystemStatusController(ApiResponses responses) {
        this.responses = responses;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SystemStatus>> status() {
        SystemStatus systemStatus =
            new SystemStatus("gt-platform", "UP", Instant.now(clock));

        return ResponseEntity.ok(
            responses.success(
                "GT-SYSTEM-STATUS-OK",
                "Platform service is operational.",
                systemStatus
            )
        );
    }
}