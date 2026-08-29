package africa.growtogether.platform.school.academic.subject;


import africa.growtogether.platform.common.api.ApiResponse;
import africa.growtogether.platform.common.api.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/school/academic/subjects")
public class SubjectController {


    private final SubjectService service;
    private final ApiResponses responses;


    public SubjectController(
            SubjectService service,
            ApiResponses responses
    ) {
        this.service = service;
        this.responses = responses;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('school.academic.subject.create')")
    public ApiResponse<Subject> create(
            @RequestParam UUID tenantId,
            @RequestParam String subjectCode,
            @RequestParam String subjectName,
            @RequestParam(required = false) String shortName,
            @RequestParam String subjectType,
            @RequestParam(required = false) String description
    ) {


        return responses.success(
                "GT-SCHOOL-SUBJECT-001",
                "Subject created.",
                service.create(
                        tenantId,
                        subjectCode,
                        subjectName,
                        shortName,
                        subjectType,
                        description
                )
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<Subject> get(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        return responses.success(
                "GT-SCHOOL-SUBJECT-002",
                "Subject retrieved.",
                service.findByCode(
                        tenantId,
                        code
                )
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('school.academic.subject.read')")
    public ApiResponse<List<Subject>> list(
            @RequestParam UUID tenantId
    ) {


        return responses.success(
                "GT-SCHOOL-SUBJECT-003",
                "Active subjects retrieved.",
                service.findActiveSubjects(
                        tenantId
                )
        );
    }


    @PatchMapping("/{code}/activate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<Subject> activate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        Subject subject =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-SUBJECT-004",
                "Subject activated.",
                service.activate(
                        tenantId,
                        subject
                )
        );
    }


    @PatchMapping("/{code}/deactivate")
    @PreAuthorize("hasAuthority('school.academic.subject.manage')")
    public ApiResponse<Subject> deactivate(
            @PathVariable String code,
            @RequestParam UUID tenantId
    ) {


        Subject subject =
                service.findByCode(
                        tenantId,
                        code
                );


        return responses.success(
                "GT-SCHOOL-SUBJECT-005",
                "Subject deactivated.",
                service.deactivate(
                        tenantId,
                        subject
                )
        );
    }

}
