package africa.growtogether.platform.school.integration.teacher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin HTTP adapter for the existing governed teacher AI workflow.
 *
 * <p>All tenant, identity, ownership, request-scope and EAIF governance
 * enforcement remains in {@link TeacherAIWorkflowService} and its existing
 * collaborators. This controller does not call an AI provider directly.
 */
@RestController
@RequestMapping("/api/v1/school/teacher/ai")
public class TeacherAIWorkflowController {

    private final TeacherAIWorkflowService service;

    public TeacherAIWorkflowController(TeacherAIWorkflowService service) {
        this.service = service;
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAuthority('ai.request.create')")
    public TeacherAIWorkflowService.Submission submit(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @Valid @RequestBody SubmitRequest request) {

        return service.submit(
                tenantId,
                request.teacherProfileId(),
                request.assignmentId(),
                request.modelCode(),
                request.input());
    }

    @PostMapping("/requests/{requestId}/execute")
    @PreAuthorize("hasAuthority('ai.runtime.execute')")
    public TeacherAIWorkflowService.RequestStatus execute(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID requestId,
            @Valid @RequestBody ExecuteRequest request) {

        return service.execute(
                tenantId,
                request.teacherProfileId(),
                request.assignmentId(),
                requestId,
                request.input());
    }

    @GetMapping("/requests/{requestId}")
    @PreAuthorize("hasAuthority('ai.request.read')")
    public TeacherAIWorkflowService.RequestStatus status(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID requestId,
            @RequestParam UUID teacherProfileId,
            @RequestParam UUID assignmentId) {

        return service.status(
                tenantId,
                teacherProfileId,
                assignmentId,
                requestId);
    }

    public record SubmitRequest(
            @NotNull UUID teacherProfileId,
            @NotNull UUID assignmentId,
            @NotBlank @Size(max = 100) String modelCode,
            @NotBlank @Size(max = 100000) String input) {
    }

    public record ExecuteRequest(
            @NotNull UUID teacherProfileId,
            @NotNull UUID assignmentId,
            @NotBlank @Size(max = 100000) String input) {
    }
}
