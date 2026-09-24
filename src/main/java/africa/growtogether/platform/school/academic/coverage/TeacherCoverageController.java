package africa.growtogether.platform.school.academic.coverage;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageMapper;
import africa.growtogether.platform.school.academic.coverage.api.TeacherCoverageResponse;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/teacher-coverage")
@PreAuthorize("denyAll()")
public class TeacherCoverageController {


    private final TeacherCoverageService service;


    public TeacherCoverageController(
            TeacherCoverageService service
    ) {

        this.service = service;

    }



    @GetMapping("/teacher/{teacherProfileId}")
    public List<TeacherCoverageResponse> teacherCoverage(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID teacherProfileId
    ) {

        return service.getTeacherCoverage(
                tenantId,
                teacherProfileId
        )
        .stream()
        .map(TeacherCoverageMapper::toResponse)
        .toList();

    }



    @GetMapping("/assignment/{teachingAssignmentId}")
    public List<TeacherCoverageResponse> assignmentCoverage(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID teachingAssignmentId
    ) {

        return service.getAssignmentCoverage(
                tenantId,
                teachingAssignmentId
        )
        .stream()
        .map(TeacherCoverageMapper::toResponse)
        .toList();

    }



    @PatchMapping("/{coverageId}/complete")
    public TeacherCoverageResponse complete(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID coverageId,
            @RequestParam(required = false) String remarks
    ) {

        return TeacherCoverageMapper.toResponse(
                service.markCompleted(
                        tenantId,
                        coverageId,
                        remarks
                )
        );

    }



    @PatchMapping("/{coverageId}/progress")
    public TeacherCoverageResponse progress(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID coverageId
    ) {

        return TeacherCoverageMapper.toResponse(
                service.markInProgress(
                        tenantId,
                        coverageId
                )
        );

    }



    @PatchMapping("/{coverageId}/remediation")
    public TeacherCoverageResponse remediation(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID coverageId
    ) {

        return TeacherCoverageMapper.toResponse(
                service.markRequiresRemediation(
                        tenantId,
                        coverageId
                )
        );

    }



    @PatchMapping("/{coverageId}/ahead")
    public TeacherCoverageResponse ahead(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID coverageId
    ) {

        return TeacherCoverageMapper.toResponse(
                service.markAheadOfSchedule(
                        tenantId,
                        coverageId
                )
        );

    }

}
