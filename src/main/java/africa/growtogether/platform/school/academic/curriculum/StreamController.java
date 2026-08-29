package africa.growtogether.platform.school.academic.curriculum;

import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school/academic/streams")
public class StreamController {

    private final StreamService service;
    private final ApiResponses responses;

    public StreamController(
            StreamService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('school.academic.stream.create')"
    )
    public ApiResponse<Stream> create(
            @RequestParam UUID tenantId,
            @RequestParam UUID campusId,
            @RequestParam UUID classGradeId,
            @RequestParam String streamCode,
            @RequestParam String streamName,
            @RequestParam(required = false) Integer capacity
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-001",
                "Stream created.",
                service.create(
                        tenantId,
                        campusId,
                        classGradeId,
                        streamCode,
                        streamName,
                        capacity
                )
        );
    }

    @GetMapping("/{streamId}")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.read')"
    )
    public ApiResponse<Stream> getById(
            @PathVariable UUID streamId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-002",
                "Stream retrieved.",
                service.findById(
                        tenantId,
                        streamId
                )
        );
    }

    @GetMapping("/code/{streamCode}")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.read')"
    )
    public ApiResponse<Stream> getByCode(
            @PathVariable String streamCode,
            @RequestParam UUID tenantId,
            @RequestParam UUID campusId,
            @RequestParam UUID classGradeId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-003",
                "Stream retrieved by code.",
                service.findByCode(
                        tenantId,
                        campusId,
                        classGradeId,
                        streamCode
                )
        );
    }

    @GetMapping("/campus/{campusId}")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.read')"
    )
    public ApiResponse<List<Stream>> findByCampus(
            @PathVariable UUID campusId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-004",
                "Streams retrieved by campus.",
                service.findByCampus(
                        tenantId,
                        campusId
                )
        );
    }

    @GetMapping("/class-grade/{classGradeId}")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.read')"
    )
    public ApiResponse<List<Stream>> findByClassGrade(
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-005",
                "Streams retrieved by class grade.",
                service.findByClassGrade(
                        tenantId,
                        classGradeId
                )
        );
    }

    @GetMapping("/campus/{campusId}/class-grade/{classGradeId}")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.read')"
    )
    public ApiResponse<List<Stream>> findByCampusAndClassGrade(
            @PathVariable UUID campusId,
            @PathVariable UUID classGradeId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-006",
                "Streams retrieved by campus and class grade.",
                service.findByCampusAndClassGrade(
                        tenantId,
                        campusId,
                        classGradeId
                )
        );
    }

    @PatchMapping("/{streamId}/activate")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.manage')"
    )
    public ApiResponse<Stream> activate(
            @PathVariable UUID streamId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-007",
                "Stream activated.",
                service.activate(
                        tenantId,
                        streamId
                )
        );
    }

    @PatchMapping("/{streamId}/deactivate")
    @PreAuthorize(
            "hasAuthority('school.academic.stream.manage')"
    )
    public ApiResponse<Stream> deactivate(
            @PathVariable UUID streamId,
            @RequestParam UUID tenantId
    ) {

        return responses.success(
                "GT-SCHOOL-STREAM-008",
                "Stream deactivated.",
                service.deactivate(
                        tenantId,
                        streamId
                )
        );
    }
}
