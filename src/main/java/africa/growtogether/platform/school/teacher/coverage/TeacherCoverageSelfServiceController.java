package africa.growtogether.platform.school.teacher.coverage;

import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Authenticated teacher self-service curriculum coverage API.
 *
 * Tenant and teacher identity are resolved server-side.
 */
@RestController
@RequestMapping("/api/v1/school/teacher/coverage")
public class TeacherCoverageSelfServiceController {

    private final TeacherCoverageSelfServiceService service;

    public TeacherCoverageSelfServiceController(
            TeacherCoverageSelfServiceService service
    ) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.read')"
    )
    public TeacherCoverageView coverage() {

        return service.currentCoverage();
    }

    @GetMapping("/assignments/{assignmentId}")
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.read')"
    )
    public TeacherCoverageView assignmentCoverage(
            @PathVariable UUID assignmentId
    ) {

        return service.assignmentCoverage(
                assignmentId
        );
    }

    @PatchMapping("/{coverageId}/progress")
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.update')"
    )
    public TeacherCoverageResponse progress(
            @PathVariable UUID coverageId
    ) {

        return service.markInProgress(
                coverageId
        );
    }

    @PatchMapping("/{coverageId}/complete")
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.update')"
    )
    public TeacherCoverageResponse complete(
            @PathVariable UUID coverageId,
            @RequestParam(required = false)
            String remarks
    ) {

        return service.markCompleted(
                coverageId,
                remarks
        );
    }

    @PatchMapping("/{coverageId}/remediation")
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.update')"
    )
    public TeacherCoverageResponse remediation(
            @PathVariable UUID coverageId
    ) {

        return service.markRequiresRemediation(
                coverageId
        );
    }

    @PatchMapping("/{coverageId}/ahead")
    @PreAuthorize(
            "hasAuthority('school.teacher.coverage.update')"
    )
    public TeacherCoverageResponse ahead(
            @PathVariable UUID coverageId
    ) {

        return service.markAheadOfSchedule(
                coverageId
        );
    }
}
